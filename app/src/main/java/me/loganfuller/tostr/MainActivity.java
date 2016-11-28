package me.loganfuller.tostr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
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

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    private static final String PN = "PubNub";
    PubNub pubnub;
    String channel = "tostr";

    // Alarm variables
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Alarm
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        findViewById(R.id.btnStopAlarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });

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

                    if(message.getMessage().toString().contains("alarm")) {
                        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (SDK_INT < Build.VERSION_CODES.KITKAT)
                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
                        else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
                        else if (SDK_INT >= Build.VERSION_CODES.M)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
                        Log.i("Alarm", "The alarm has been set!");
                    } else if(message.getMessage().toString().contains("stop")) {
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
        pubnub.subscribe().channels(Arrays.asList(channel)).execute();
    }

    public void cancelAlarm() {
        alarmManager.cancel(pendingIntent);
        Intent stopIntent = new Intent(MainActivity.this, RingtoneService.class);
        MainActivity.this.stopService(stopIntent);
        Log.i("Alarm", "The alarm has been cancelled.");
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
}
