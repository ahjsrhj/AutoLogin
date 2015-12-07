package tk.imrhj.autologin.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import tk.imrhj.autologin.util.HttpContent;
import tk.imrhj.autologin.R;
import tk.imrhj.autologin.service.WifiChangeService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button saveInfo;
    private Button login;
    private EditText userName;
    private EditText password;
    private boolean haveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChangeColor();
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

                if (user_name.length() < 1 || pass_word.length() < 1) {
                    Toast.makeText(MainActivity.this, "用户名或密码格式不正确!", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences(this.getString(R.string.stringFileName), MODE_MULTI_PROCESS).edit();
                    editor.putBoolean(this.getString(R.string.stringHaveData), true);
                    editor.putString(this.getString(R.string.stringUsername), user_name);
                    editor.putString(this.getString(R.string.stringPassword), pass_word);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "保存账号信息成功!", Toast.LENGTH_SHORT).show();
                    stopService(service);
                    startService(service);


                }
                break;
            case R.id.btn_login:            //登陆按钮
                service.putExtra("bool_login", true);
                startService(service);
                Log.d("MainActivity", "我是登陆");
                break;
        }
    }

    //改变通知栏颜色
    private void ChangeColor() {
        //系统版本大于5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));

            View view = (View) findViewById(R.id.view_status_bar);
            view.setVisibility(View.GONE);

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

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
            case R.id.action_updata:
                HttpContent.getResponse();
                break;
            case R.id.action_exit:
                Intent intent = new Intent(MainActivity.this, WifiChangeService.class);
                stopService(intent);
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }




}
