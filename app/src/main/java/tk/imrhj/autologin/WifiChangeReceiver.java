package tk.imrhj.autologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by rhj on 15/5/17.
 */
public class WifiChangeReceiver extends BroadcastReceiver {
    private Intent service;
    @Override
    public void onReceive(Context context, Intent intent) {
        service = new Intent(context, WifiChangeService.class);

        context.startService(service);
        System.out.println("启动服务");


    }


}
