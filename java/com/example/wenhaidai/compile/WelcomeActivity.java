package com.example.wenhaidai.compile;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //去掉标题栏,必须要放在setcontentView方法的前边
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_welcomectivity);
        Toast.makeText(getApplicationContext(), "欢迎使用正经编译器", Toast.LENGTH_LONG).show();
        // overridePendingTransition(R.anim.hyperspace_in, R.anim.hyperspace_out);//第一个参数为第一个activity进入时的动画，第二个人参数为第二个activity退出时的动画
        //通过handler的postDelayed实现1000ms延迟过度
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 2000);
    }
}
