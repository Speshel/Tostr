package me.loganfuller.tostr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    private static final String PN = "PubNub";
    PubNub pubnub;
    String channel = "tostr";

    // Alarm variables
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarm();
            }
        });

        // Alarm
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        // Initialize PubNub with Subscribe and Publish keys
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
                }
                else if(status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    // We are connected, so we can now publish successfully.
                    if(status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                        txtStatus.setText("CONNECTED");
                        pubnub.publish().channel(channel).message("Successfully connected to channel " + channel).async(new PNCallback<PNPublishResult>() {
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

                } else {
                    // Message has been received on channel stored in
                    // message.getSubscription()
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
        pubnub.subscribe().channels(Collections.singletonList(channel)).execute();
    }

    public void setAlarm() {
        final Calendar c = Calendar.getInstance();
        int alarmHour = c.get(Calendar.HOUR_OF_DAY);
        int alarmMinute = c.get(Calendar.MINUTE);

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
                        Log.i("Alarm", "Alarm set for: " + hourOfDay + ":" + minute);
                    }
                }, alarmHour, alarmMinute, false);
        timePickerDialog.show();
    }

    public void cancelAlarm() {
        try {
            alarmManager.cancel(pendingIntent);
            Intent stopIntent = new Intent(MainActivity.this, RingtoneService.class);
            MainActivity.this.stopService(stopIntent);
            Log.i("Alarm", "The alarm has been cancelled.");
        } catch(Exception e) {
            Log.i("Alarm", "There is no alarm clock to cancel.");
            Toast.makeText(MainActivity.this, "There is no alarm to cancel.", Toast.LENGTH_SHORT).show();
        }
    }

    private void publishTest() {
        pubnub.publish().message(Arrays.asList("hello", "there")).channel(channel).async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                if(!status.isError()) {
                    Log.i(PN, "Publish sent!");
                } else {
                    Log.w(PN, "Publish not sent!");
                }
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
