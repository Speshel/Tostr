package me.loganfuller.tostr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import static android.os.Build.VERSION.SDK_INT;

public class AlarmReceiver extends BroadcastReceiver{

    private static final long FREQUENCY = 1000;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Alarm", "The alarm is ringing!");
        Intent startIntent = new Intent(context, RingtoneService.class);
        context.startService(startIntent);
//        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        manager.cancel(pendingIntent);
//        if (SDK_INT < Build.VERSION_CODES.KITKAT)
//            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + FREQUENCY, pendingIntent);
//        else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M)
//            manager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + FREQUENCY, pendingIntent);
//        else if (SDK_INT >= Build.VERSION_CODES.M)
//            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + FREQUENCY, pendingIntent);
//        Log.i("Alarm", "The alarm has been reset for " + FREQUENCY + " from now.");
    }
}
