package tk.imrhj.autologin;



import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by rhj on 15/5/23.
 */
public  class HttpContent {
    private static String webVersion;
    private static String version;
    private static final int UPDATE = 1;
    private static final int NO_UPDATE = 2;
    private static boolean showDialog = true;


    //定义handler对象处理消息
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE:
                    if (!showDialog)
                        break;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getContext());
                    String dialogMessage = "发现新的版本" + version + "\n是否升级?";
                    builder.setTitle("提示")
                            .setMessage(dialogMessage)
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showDialog = true;
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setData(Uri.parse(MyApplication.getContext().getString(R.string.update_url)));
                                    MyApplication.getContext().startActivity(intent);
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showDialog = true;
                                    Toast.makeText(MyApplication.getContext(), "你取消了升级", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog ad = builder.create();
                    ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    ad.setCanceledOnTouchOutside(false);
                    ad.show();
                    showDialog = false;
                    break;
                case NO_UPDATE:
                    Toast.makeText(MyApplication.getContext(), "暂无更新", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };



    public static void getResponse() {
        final String string = "http://2.imrhj.sinaapp.com/app/version.xml";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(string);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    webVersion = response.toString();
                    System.out.println(webVersion);
                    Log.d("HttpContent", webVersion);
                    //处理json数据
                    JSONObject jsonObject = new JSONObject(webVersion);
                    version = jsonObject.getString("version");
                    System.out.println(version);

                    //获取版本号
                    PackageManager manager = MyApplication.getContext().getPackageManager();
                    PackageInfo info = manager.getPackageInfo(MyApplication.getContext().getPackageName(), 0);
                    LogUtil.d("packinfo",info.versionName);


                    //检测是否需要升级
                    Message message = new Message();
                    Double doubleVersion = Double.parseDouble(version);
                    Double thisVersion = Double.parseDouble(info.versionName);

                    if (doubleVersion > thisVersion) {
                        message.what = UPDATE;
                    } else {
                        message.what = NO_UPDATE;
                    }

                    handler.sendMessage(message);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
