package me.loganfuller.tostr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver{

    private static final String PN = "PubNub";
    PubNub pubnub;
    String channel = "tostr";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Initialize PubNub with Subscribe and Publish keys
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-05493e44-b06e-11e6-b4d6-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-8b0161d7-b88a-44b6-99e2-bd29ad0ecda9");

        pubnub = new PubNub(pnConfiguration);

        Log.i("Alarm", "The alarm is ringing!");
        Intent startIntent = new Intent(context, RingtoneService.class);
        context.startService(startIntent);

        pubnub.publish().channel(channel).message("Alarm was received.").async(new PNCallback<PNPublishResult>() {
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
