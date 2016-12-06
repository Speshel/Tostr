package me.loganfuller.tostr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    /*
    PubNub Variables
     */
    private static final String PN = "PubNub";
    PubNub pubnub;
    String channel = "tostr";

    /*
    Alarm Variables
     */
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    /*
    Miscellaneous Variables
     */
    String timeStart = null;
    String timeStop = null;

    /*
    View declarations
     */
    TextView txtStatus, txtAlarmTime;
    Button btnSetAlarm, btnStopAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Setup the toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        Setup the FAB button and its associated onClickListener
         */

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarm();
            }
        });
        */

        /*
        Setup the alarm
         */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        /*
        Initialize PubNub
         */
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-05493e44-b06e-11e6-b4d6-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-8b0161d7-b88a-44b6-99e2-bd29ad0ecda9");

        pubnub = new PubNub(pnConfiguration);

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                //When radio/connectivity is lost
                if(status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    Toast.makeText(MainActivity.this, "Connection to PubNub lost. Attempting to reconnect.", Toast.LENGTH_LONG).show();
                    txtStatus.setText(R.string.pubnub_status_disconnected);
                }
                else if(status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    // We are connected, so we can now publish successfully.
                    if(status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                        txtStatus.setText(R.string.pubnub_status_connected);
                        pubnub.publish().channel(channel).message("Android has successfully connected to channel " + channel).async(new PNCallback<PNPublishResult>() {
                            @Override
                            public void onResponse(PNPublishResult result, PNStatus status) {
                                // Check whether request completed successfully
                                if(!status.isError()) {
                                    // Message successfully published
                                    Log.i(PN, "Publish request successfully completed.");
                                }
                                else {
                                    // Message publish failed
                                    Log.w(PN, "Publish request failed to complete.");
                                }
                            }
                        });
                    }
                }
                else if(status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                    // When radio/connectivity is list, then regained.
                    Log.i(PN, "Reconnected");
                }
                else if(status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                    // Handles message decryption error.
                    Log.w(PN, "Error with decryption.");
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                if(message.getChannel() != null) {
                    // Message has been received on channel group stored in
                    // message.getChannel()
                    Log.i(PN, "Information from " + message.getChannel() + " received.");
                    Log.i(PN, "Message: " + message.getMessage());

                    if(message.getMessage().toString().contains("stop")) {
                        cancelAlarm();
                    }

                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
        pubnub.subscribe().channels(Collections.singletonList(channel)).execute();

        /*
        Assign views to their variables
         */
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtAlarmTime = (TextView) findViewById(R.id.txtAlarmTime);

        btnStopAlarm = (Button) findViewById(R.id.btnStopAlarm);
        btnStopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
            }
        });

        btnSetAlarm = (Button) findViewById(R.id.btnSetAlarm);
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarm();
            }
        });

    }

    public void setAlarm() {
        final Calendar c = Calendar.getInstance();
        int alarmHour = c.get(Calendar.HOUR_OF_DAY);
        int alarmMinute = c.get(Calendar.MINUTE);

        Date curDate = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.CANADA);
        timeStart = format.format(curDate);
        Log.i("TEST", timeStart);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Create a calendar object to set the alarm time.
                        Calendar calNow = Calendar.getInstance();
                        Calendar calSet = (Calendar) calNow.clone();

                        calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calSet.set(Calendar.MINUTE, minute);
                        // Make sure to set the seconds, otherwise the alarm will ring anytime between hourOfDay:minute:0 and hourOfDay:minute:59, could cause a significant delay.
                        calSet.set(Calendar.SECOND, 0);

                        if(calSet.compareTo(calNow) <= 0) {
                            // Set the time for tomorrow since the time has already been passed today.
                            calSet.add(Calendar.DATE, 1);
                        }

                        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (SDK_INT < Build.VERSION_CODES.KITKAT)
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
                        else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
                        else if (SDK_INT >= Build.VERSION_CODES.M) {
                            // Displays alarm clock icon in status bar.
                            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calSet.getTimeInMillis(), pendingIntent);
                            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

                            // Does not display alarm clock icon in status bar
                            //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
                        }
                        String alarmTime = hourOfDay + ":" + minute + ":0";
                        timeStop = alarmTime;
                        Log.i("Alarm", "Alarm set for: " + alarmTime);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.CANADA);
                            Date dateObj = sdf.parse(alarmTime);
                            txtAlarmTime.setText(new SimpleDateFormat("hh:mm a", Locale.CANADA).format(dateObj));
                            Toast.makeText(MainActivity.this, "The alarm has been set for: " + sdf.format(dateObj), Toast.LENGTH_SHORT).show();
                            timeStop = alarmTime;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, alarmHour, alarmMinute, false);
        timePickerDialog.show();
    }

    public void cancelAlarm() {
        if(alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Intent stopIntent = new Intent(MainActivity.this, RingtoneService.class);
            MainActivity.this.stopService(stopIntent);
            Log.i("Alarm", "The alarm has been cancelled.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "The alarm has been cancelled.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.i("Alarm", "There is no alarm clock to cancel.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "There is no alarm to cancel.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtAlarmTime.setText(R.string.alarm_no_alarm);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_emergency_stop) {
            cancelAlarm();
        }

        return super.onOptionsItemSelected(item);
    }
}
