package me.loganfuller.tostr;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtoneService extends Service {

    private Ringtone ringtone;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        ringtone.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        this.ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();

        return START_NOT_STICKY;
    }
}
