package tk.imrhj.autologin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by rhj on 15/5/19.
 */
public class ProtectedService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }
}
