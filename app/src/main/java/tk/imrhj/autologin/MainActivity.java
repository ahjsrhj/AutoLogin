package tk.imrhj.autologin;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Button saveInfo;
    private Button login;
    private EditText userName;
    private EditText password;
    private boolean haveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeColor();
        setContentView(R.layout.activity_main);
        startService(new Intent(this, WifiChangeService.class));



        login = (Button) findViewById(R.id.btn_login);
        saveInfo = (Button) findViewById(R.id.btn_start);
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        LoadData();
        login.setOnClickListener(this);
        saveInfo.setOnClickListener(this);




    }

    @Override
    public void onClick(View v) {
        Intent service = new Intent(this, WifiChangeService.class);
        switch (v.getId()) {
            case R.id.btn_start:        //保存按钮
                String user_name = userName.getText().toString();
                String pass_word = password.getText().toString();

                if (user_name == null || user_name == "" || pass_word == null || pass_word == "") {
                    Toast.makeText(MainActivity.this, "用户名或密码格式不正确!", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences(this.getString(R.string.stringFileName), MODE_MULTI_PROCESS).edit();
                    editor.putBoolean(this.getString(R.string.stringHaveData), true);
                    editor.putString(this.getString(R.string.stringUsername), user_name);
                    editor.putString(this.getString(R.string.stringPassword), pass_word);
                    editor.commit();
                    Toast.makeText(MainActivity.this, "保存账号信息成功!", Toast.LENGTH_SHORT).show();
                    stopService(service);
                    startService(service);

                }
                break;
            case R.id.btn_login:            //登陆按钮
                service.putExtra("bool_login", true);
                startService(service);
                break;
        }
    }

    //改变通知栏颜色
    private void ChangeColor() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 创建状态栏的管理实例
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // 激活状态栏设置
            tintManager.setStatusBarTintEnabled(true);
            // 激活导航栏设置
            tintManager.setNavigationBarTintEnabled(true);
            // 设置一个颜色给系统栏
            tintManager.setTintColor(Color.parseColor(this.getString(R.string.bk_color)));
            tintManager.setNavigationBarTintColor(Color.parseColor(this.getString(R.string.bk_color)));
        }
    }

    //打开应用时读取数据
    private void LoadData() {
        SharedPreferences preferences = getSharedPreferences(this.getString(R.string.stringFileName), MODE_MULTI_PROCESS);
        haveData = preferences.getBoolean(this.getString(R.string.stringHaveData), false);
        if (haveData) {
            userName.setText(preferences.getString(this.getString(R.string.stringUsername),""));
            password.setText(preferences.getString(this.getString(R.string.stringPassword),""));
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this,"暂未添加",Toast.LENGTH_SHORT).show();

                break;
            case R.id.action_help:
                break;
            case R.id.action_about:
                break;
            case R.id.action_updata:
                checkUpdata();
                break;
            case R.id.action_exit:
                Intent intent = new Intent(MainActivity.this, WifiChangeService.class);
                stopService(intent);
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUpdata() {
        String stringVersion = HttpContent.getVersionFromJson();
        Double doubleVersion = Double.parseDouble(stringVersion);
        Double version = Double.parseDouble(this.getString(R.string.version));

        if (doubleVersion > version) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            String message = "发现新的版本" + stringVersion + "\n是否升级?";
            dialog.setTitle("提示")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("http://pan.baidu.com/s/1dD4ejRF"));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "你取消了升级", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        } else {
            Toast.makeText(this, "暂无更新", Toast.LENGTH_SHORT).show();
        }

    }




}
