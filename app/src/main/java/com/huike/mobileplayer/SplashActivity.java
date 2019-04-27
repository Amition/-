package com.huike.mobileplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huike.mobileplayer.Activity.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private RelativeLayout rl_splash;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        initActivity();

    }


    private void initActivity() {

        boolean grantExternalRW = isGrantExternalRW(SplashActivity.this);//判断是否有SD权限
        Log.e("SD",grantExternalRW +"");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity();
                }
            }, 2000);


            rl_splash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity();
                    handler.removeCallbacksAndMessages(null);
                }
            });



    }


    private void startActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void initView() {
        rl_splash = (RelativeLayout) findViewById(R.id.rl_splash);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

}
