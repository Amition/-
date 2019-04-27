package com.huike.mobileplayer.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.huike.mobileplayer.Bean.BeanPager;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.adapter.ReplaceFragment;

import java.util.ArrayList;

public  class MainActivity extends FragmentActivity {

    private RadioGroup rg_main;
    private int position; //记录页面位置

    private ArrayList<BeanPager> beanPagers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        iniEvent();
    }

    private void iniEvent() {
        rg_main.check(R.id.rb_video);//默认第一个页面
        setFragment(0);

        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    default:
                        position = 0;
                        break;

                    case R.id.rb_Audio:
                        position = 1;
                        break;

                    case R.id.rb_netVido:
                        position = 2;
                        break;

                    case R.id.rb_netAudio:
                        position = 3;
                        break;

                }

                setFragment(position);
            }
        });
    }

    private void initData() {

        beanPagers = new ArrayList<>();
        beanPagers.add(new VideoPager(this));
        beanPagers.add(new AudioPager(this));
        beanPagers.add(new NetVidePager(this));
        beanPagers.add(new NetAudioPager(this));
    }

    private void initView() {
        rg_main = (RadioGroup) findViewById(R.id.rg_main);
    }



    private void setFragment(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

       transaction.replace(R.id.fl_main,new ReplaceFragment(beanPagers,position));

        transaction.commit();
    }


    private boolean flag = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if(keyCode == KeyEvent.KEYCODE_BACK){

            if(!flag){
                flag = true;
                Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flag = false;
                    }
                },2000);
                return true;
            }

        }



        return super.onKeyDown(keyCode, event);
    }
}

