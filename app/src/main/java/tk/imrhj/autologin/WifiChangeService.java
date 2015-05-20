package tk.imrhj.autologin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rhj on 15/5/17.
 */
public class WifiChangeService extends Service {

    private final int CONTENT_SUCCESS = 1;
    private final int CONTENT_FAILD = 0;
    private boolean haveConnect = false;
    private boolean haveData = false;
    private String username;
    private String password;
    private String userLength;
    private String userPost;
    private String netPost =   "username=net&password=d0083043c6576dd2&drop=0&type=1&n=110";
    //                          username=2013014053&password=83aa400af464C76D&drop=0&type=1&n=110
    private String netLength = "58";


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case CONTENT_SUCCESS:
                    System.out.println("登陆成功");
                    showToast("登陆成功");
                    break;
                case CONTENT_FAILD:
                    System.out.println("登陆失败");
                    showToast("登陆失败\n" + message.getData().getString("string"));
                    System.out.println(message.getData().getString("string"));
                    break;
            }
        }
    };

    public void showToast(String string) {
        Toast toast;
        toast = Toast.makeText(MyApplication.getContext(), string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
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
                String tmpPass = preferences.getString(this.getString(R.string.stringPassword), "");
                password = MD5.getMD5Str(tmpPass);
                String string = "username=" + username + "&password=" + password + "&drop=0&type=1&n=110";
                userPost = string.toLowerCase();
                userLength = "" + userPost.length();
            }
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null) {
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                intent.putExtra("haveConnect", false);
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                intent.putExtra("haveConnect", true);
            }
        }

        if (haveConnect) {
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String SSID = wifiInfo.getSSID();
            System.out.println(SSID);

            if (SSID.equals("\"WLZX\"") || SSID.equals("\"rhj-miwifi_5G\"")) {
                System.out.println("开始登陆" + SSID);
                doLogin(netPost, netLength);
            } else if (SSID.equals("\"WXXY\"")) {
                System.out.println("开始登陆" + SSID);
                doLogin(userPost, userLength);
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
                    URL url = new URL("http://211.70.160.3/cgi-bin/do_login");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);

                    //设置连接属性
                    connection.setRequestProperty("Host", "211.70.160.3");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", content_length);
                    connection.setRequestProperty("Charset", "UTF-8");

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(post);
                    out.close();

                    int reponseCode = connection.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == reponseCode) {
                        StringBuffer buffer = new StringBuffer();
                        String line;
                        BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = responseReader.readLine()) != null) {
                            buffer.append(line);
                        }
                        responseReader.close();
                        System.out.println(buffer.toString());
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("string", buffer.toString());
                        message.setData(bundle);
                        if (buffer.toString().matches("\\d+")) {
                            System.out.println("TRUE");
                            message.what = CONTENT_SUCCESS;
                        } else {
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
