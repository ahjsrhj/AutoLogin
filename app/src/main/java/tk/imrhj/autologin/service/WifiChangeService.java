package tk.imrhj.autologin.service;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tk.imrhj.autologin.util.LogUtil;
import tk.imrhj.autologin.util.MD5;
import tk.imrhj.autologin.util.MyApplication;
import tk.imrhj.autologin.R;

/**
 * Created by rhj on 15/5/17.
 * Created for :
 */
public class WifiChangeService extends Service {

    private final int CONTENT_SUCCESS = 1;
    private final int CONTENT_FAILD = 0;
    private boolean wifiConnect = false;
    private boolean haveData = false;
    private boolean haveConnect = false;
    private boolean showDialog = true;
    private String username;
    private String password;
    private String userLength;
    private String userPost;
    //    private String netPost =   "username=net&password=d0083043c6576dd2&drop=0&type=1&n=110";
    private String netPost =
            "action=login&username=net&password=net" +
                    "&ac_id=4&user_ip=&nas_ip=&user_mac=&save_me=0&ajax=1";
    private String netLength = "90";



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case CONTENT_SUCCESS:
                    haveConnect = true;
                    System.out.println("登陆成功");
                    showToast("登陆成功");
                    break;
                case CONTENT_FAILD:
                    System.out.println("登陆失败");
                    haveConnect = false;
                    showToast("登陆失败\n" + message.getData().getString("string"));
                    System.out.println(message.getData().getString("string"));
                    break;
            }
        }
    };

    public void showToast(String string) {
        Toast toast;
        toast = Toast.makeText(MyApplication.getContext(), string, Toast.LENGTH_SHORT);
    //    toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public void onCreate() {
            super.onCreate();
            SharedPreferences preferences = getSharedPreferences(this.getString(R.string.stringFileName), MODE_MULTI_PROCESS);

            haveData = preferences.getBoolean(this.getString(R.string.stringHaveData), false);
            if (haveData) {
                username = preferences.getString(this.getString(R.string.stringUsername), "");
                password = preferences.getString(this.getString(R.string.stringPassword), "");
                String string = "action=login&username=" + username + "&password=" + password + "&ac_id=4&user_ip=&nas_ip=&user_mac=&save_me=0&ajax=1";
                userPost = string.toLowerCase();
                userLength = "" + userPost.length();
            }
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("我是start");
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.inKeyguardRestrictedInputMode()) {
            LogUtil.d("WifiChangeService", "屏幕状态开启");
            System.out.println("屏幕开启");
        } else {
            LogUtil.d("WifiChangeServicde", "屏幕关闭或未解锁");
            System.out.println("屏幕关闭状态或未解锁");
        }

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (intent.getBooleanExtra("bool_login", false)) {
            haveConnect = false;
            wifiConnect = true;
        } else {
            int ipAddress = info == null ? 0 : info.getIpAddress();
            if (wifiManager.isWifiEnabled() && ipAddress != 0) {
                wifiConnect = true;
            } else {
                haveConnect = false;
                wifiConnect = false;
                System.out.println("断开连接");
            }
        }

        if (wifiConnect && !haveConnect) {

            String SSID = info.getSSID();
            System.out.println(SSID);

            if (SSID.equals("\"WLZX\"") || SSID.equals("\"rhj-miwifi_5G\"")
                    || SSID.equals("WLZX") || SSID.equals("rhj-miwifi_5G")
                    || SSID.equals("\"WXXY\"") || SSID.equals("WXXY")) {
                doLogin(netPost, netLength);
                haveConnect = true;
            } else if (showDialog && ((haveData && SSID.equals("\"WXXY\"")) || (haveData && SSID.equals("WXXY")))) {


                if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                    LogUtil.d("WifiChangeService", "屏幕状态开启");
                    haveConnect = true;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("提示")
                            .setMessage("探子来报!WXXY已连接!\n是否登陆?")
                            .setPositiveButton("登陆", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doLogin(userPost, userLength);
                                    haveConnect = true;
                                    showDialog = true;
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    haveConnect = false;
                                    showDialog = true;
                                }
                            });
                    AlertDialog ad = dialog.create();
                    ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    ad.setCanceledOnTouchOutside(false);
                    ad.show();
                    showDialog = false;
                } else {
                    LogUtil.d("WifiChangeService", "屏幕状态关闭或者未解锁");
                }
            }
        }


    flags = START_STICKY;
    return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void doLogout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://211.70.160.3/cgi-bin/do_logout?action=logout");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        }).start();
    }



    public void doLogin(final String post, final String content_length) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://211.70.160.3/include/auth_action.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);

                    //设置连接属性
                    connection.setRequestProperty("Host", "211.70.160.3");
                    connection.setRequestProperty("Origin", "http://211.70.160.3");
                    connection.setRequestProperty("Referer", "http://211.70.160.3/srun_portal_pc.php?url=&ac_id=4");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", content_length);
                    connection.setRequestProperty("Charset", "UTF-8");

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(post);
                    out.close();

                    int reponseCode = connection.getResponseCode();
                    Log.e(this.toString(), "run " + reponseCode);
                    if (HttpURLConnection.HTTP_OK == reponseCode) {
                        StringBuffer buffer = new StringBuffer();
                        String line;
                        BufferedReader responseReader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream())
                        );
                        while ((line = responseReader.readLine()) != null) {
                            buffer.append(line);
                        }
                        responseReader.close();
                        System.out.println("服务器返回消息" + buffer.toString());
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("string", buffer.toString());
                        message.setData(bundle);
                        if (buffer.toString().matches("login_ok,[\\w\\d,%]+")) {
                            System.out.println("TRUE");
                            message.what = CONTENT_SUCCESS;
                        } else {
                            Log.e(this.toString(), "run " + buffer.toString());
                            System.out.println("FALSE");
                            message.what = CONTENT_FAILD;
                        }
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

}
