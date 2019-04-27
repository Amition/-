package com.huike.startallvideplaver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button tv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        tv_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                intent.setDataAndType(Uri.parse("http://120.79.171.62:8080/vide.mp4"),"video/*");
                intent.setDataAndType(Uri.parse("http://192.168.0.104:8080/ogg.Ogg"),"video/*");
                startActivity(intent);
            }
        });

        //http://192.168.0.104:8080/vide.mp4
    }

    private void initView() {
        tv_main = findViewById(R.id.tv_main);
    }
}
