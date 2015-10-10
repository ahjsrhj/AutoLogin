package tk.imrhj.autologin.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by rhj on 15/5/18.
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
