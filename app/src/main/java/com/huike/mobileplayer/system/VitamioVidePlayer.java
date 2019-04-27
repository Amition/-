package com.huike.mobileplayer.system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huike.mobileplayer.R;
import com.huike.mobileplayer.View.VitamioideoView;
import com.huike.mobileplayer.domain.MediaItem;
import com.huike.mobileplayer.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

public class VitamioVidePlayer extends Activity implements View.OnClickListener {

    private Utils utils = new Utils();
    private static final int PROGRESS = 0;
    private VitamioideoView vv_svp_videview;
    private RelativeLayout rootView;
    private LinearLayout llVideTop;
    private TextView tvSysVideName;
    private ImageView ivSys;
    private TextView tvSysTime;
    private Button butSysVoice;
    private SeekBar sbTopVoice;
    private TextView tvSysStartTime;
    private SeekBar sbBtnMedia;
    private TextView tvSysVidetime;
    private Button btnSysBlack;
    private Button btnSysPre;
    private Button btnSysPlary;
    private Button btnSysNext;
    private Button btnSysFullScreen;
    private Button tv_sys_lock;
    private View include_media;
    private View include_load;
    private View include_Netload;

    private GestureDetector gestureDetector;

    private BetteryReciver reciver;

    private boolean flag = false;//标识屏幕隐藏显示进度栏

    private boolean isFullScreen = false;//全屏判断


    private final static int DEFALUT_SCREENT = 1;//默认播放样式
    private final static int FULL_SCREENT = 2;//默认播放样式


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    int currentPosition = (int) vv_svp_videview.getCurrentPosition();
                    sbBtnMedia.setProgress(currentPosition);

                    tvSysStartTime.setText(utils.stringForTime(currentPosition));

                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    String s = format.format(new Date());

                    tvSysTime.setText(s);

                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private Uri uri;
    private int screentWidth;
    private int screentHeight;
    private int videWidth;
    private int videHeight;
    private int currentVolum;
    private AudioManager am;
    private int currentMaxVolume;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-04-16 18:19:58 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        rootView = (RelativeLayout) findViewById(0);
        llVideTop = (LinearLayout) findViewById(R.id.ll_vide_top);
        tvSysVideName = (TextView) findViewById(R.id.tv_sys_videName);
        ivSys = (ImageView) findViewById(R.id.iv_sys_);
        tvSysTime = (TextView) findViewById(R.id.tv_sys_time);
        butSysVoice = (Button) findViewById(R.id.but_sys_voice);
        sbTopVoice = (SeekBar) findViewById(R.id.sb_top_voice);
        tvSysStartTime = (TextView) findViewById(R.id.tv_sys_startTime);
        sbBtnMedia = (SeekBar) findViewById(R.id.sb_btn_media);
        tvSysVidetime = (TextView) findViewById(R.id.tv_sys_videtime);
        btnSysBlack = (Button) findViewById(R.id.btn_sys_black);
        btnSysPre = (Button) findViewById(R.id.btn_sys_pre);
        btnSysPlary = (Button) findViewById(R.id.btn_sys_plary);
        btnSysNext = (Button) findViewById(R.id.btn_sys_next);
        btnSysFullScreen = (Button) findViewById(R.id.btn_sys_fullScreen);
        tv_sys_lock = findViewById(R.id.tv_sys_lock);
        include_media = findViewById(R.id.include_media);
        include_load = findViewById(R.id.include_load);
        include_Netload = findViewById(R.id.include_Netload);

        butSysVoice.setOnClickListener(this);
        btnSysBlack.setOnClickListener(this);
        btnSysPre.setOnClickListener(this);
        btnSysPlary.setOnClickListener(this);
        btnSysNext.setOnClickListener(this);
        btnSysFullScreen.setOnClickListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vitamio_vide_player);

        //设置横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initView();
        initData();
        initBro();


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            currentVolum--;
            setVolcaSize(currentVolum);
            return  true;
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            currentVolum++;
            setVolcaSize(currentVolum);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initBro() {

        reciver = new BetteryReciver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//当电量改变的时候发送广播
        registerReceiver(reciver, filter);
    }

    private boolean  isVoice = false;
    private void initData() {



        //得到屏幕的宽高
//        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        screentWidth = manager.getDefaultDisplay().getWidth();
//        screentHeight = manager.getDefaultDisplay().getHeight();

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screentWidth = outMetrics.widthPixels;
        screentHeight = outMetrics.heightPixels;
        Log.e("screentWidth",screentWidth +"");
        Log.e("screentWidth",screentHeight +"");



        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
        uri = getIntent().getData();
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem item = mediaItems.get(position);
            vv_svp_videview.setVideoPath(item.getData());
            tvSysVideName.setText(item.getName());

            boolean netWork = Utils.isNetWork(item.getData());
            setNetCache(netWork);

        } else if (uri != null) {
            vv_svp_videview.setVideoURI(uri);
            tvSysVideName.setText(uri.toString());

        }


        vv_svp_videview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                //视频的宽;
                videWidth = mp.getVideoWidth();

                //视频的宽;
                videHeight = mp.getVideoHeight();

                int duration = (int) mp.getDuration();//视频总长度
                sbBtnMedia.setMax(duration);

                tvSysVidetime.setText(utils.stringForTime(duration));


                handler.sendEmptyMessage(0);

                vv_svp_videview.start();//开始播放

                include_load.setVisibility(View.GONE);//开始播放 隐藏视图


//                setVideType(DEFALUT_SCREENT);

            }
        });

        vv_svp_videview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VitamioVidePlayer.this, "播放完成!", Toast.LENGTH_SHORT).show();
                setbuttonNext();

            }
        });

        vv_svp_videview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VitamioVidePlayer.this, "播放失败！自动为你播放下一个视频", Toast.LENGTH_SHORT).show();
//                setbuttonNext();
                return false;
            }
        });



//        vv_svp_videview.setMediaController(new MediaController(this));//点击播放


        //设置sbBtnMedia的监听
        sbBtnMedia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    vv_svp_videview.seekTo(progress);

                }
            }

            //seekBar 改变时调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //手子离开时回调这个方法
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });


        //音量 seekBar
        sbTopVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    setVolcaSize(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




        //初始化手势识别器
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                setPaues();
//                Toast.makeText(SystemVidePlayer.this, "屏幕长安", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//                Toast.makeText(SystemVidePlayer.this, "屏幕双击", Toast.LENGTH_SHORT).show();

//                if (!isFullScreen){
//                    setVideType(FULL_SCREENT);
//                }else{
//                    setVideType(DEFALUT_SCREENT);
//
//                }

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                Toast.makeText(SystemVidePlayer.this, "屏幕单机", Toast.LENGTH_SHORT).show();
                if (!flag) {
                    //隐藏视图
                    flag = true;
                    include_media.setVisibility(View.GONE);
                } else {
                    flag = false;
                    //显示视图
                    include_media.setVisibility(View.VISIBLE);


                    //延迟5秒关闭状态栏
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    include_media.setVisibility(View.GONE);
                                    flag = true;
                                }
                            });

                        }
                    }, 5000);

                }



                return super.onSingleTapConfirmed(e);
            }
        });

        vv_svp_videview.setKeepScreenOn(true);//保持不锁屏


    }

    private void setNetCache(boolean netWork) {
        if(netWork){
            //网络视频 添加缓存
            int buffer = vv_svp_videview.getBufferPercentage();
             int totalBuffer =  sbBtnMedia.getMax() * buffer;
             int secondaryProgress =  totalBuffer / 100;
            sbBtnMedia.setSecondaryProgress(secondaryProgress);
            Log.e("data","加载");

        }else{
            Log.e("data","没有加载");
            sbBtnMedia.setSecondaryProgress(0);
        }

        //显示加载网速
        vv_svp_videview.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START://网络延迟
                        include_Netload.setVisibility(View.VISIBLE);
                        Log.e("cache","网络延迟");
                        break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        include_Netload.setVisibility(View.GONE);
                        break;

                }

                return true;
            }
        });

    }

    private void setVolcaSize(int progress) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,1);
        sbTopVoice.setProgress(progress);
        currentVolum = progress;
        if(progress < 0){
                isVoice =true;
        }else{
            isVoice =false;

        }
    }


//    private void setVideType(int Type) {
//
//
//        switch (Type) {
//            case DEFALUT_SCREENT:
//                int mVideoWidth = videWidth;
//                int mVideoHeight = videHeight;
//
//                int width = screentWidth;
//                int height = screentHeight;
//
//                if(mVideoHeight > 0 && mVideoWidth > 0 ){
//
//                    // for compatibility, we adjust size based on aspect ratio
//                    if ( mVideoWidth * height  < width * mVideoHeight ) {
//                        //Log.i("@@@", "image too wide, correcting");
//                        width = height * mVideoWidth / mVideoHeight;
//                    } else if ( mVideoWidth * height  > width * mVideoHeight ) {
//                        //Log.i("@@@", "image too tall, correcting");
//                        height = width * mVideoHeight / mVideoWidth;
//                    }
//
//                    vv_svp_videview.setVidoSize(width, height);
//
//                }
//
//                btnSysFullScreen.setBackgroundResource(R.drawable.btn_full_screen_select);
//                isFullScreen = false;
//                break;
//
//            case FULL_SCREENT:
//                vv_svp_videview.setVidoSize(screentWidth, screentHeight);
//                btnSysFullScreen.setBackgroundResource(R.drawable.btn_full_screen_normal);
//
//                isFullScreen =true;
//                break;
//
//        }
//    }

    private void initView() {
        Vitamio.isInitialized(getApplicationContext());//初始化解码器

        findViews();
        vv_svp_videview = findViewById(R.id.vv_vvp_videview);

        include_media.setVisibility(View.GONE);//隐藏状态栏
        flag = true;


        //得到系统音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        //当前音量
        currentVolum = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        sbTopVoice.setMax(currentMaxVolume);
        sbTopVoice.setProgress(currentVolum);

    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2019-04-16 18:19:58 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == butSysVoice) {
            // Handle clicks for butSysVoice
            isVoice = !isVoice;
            if(isVoice){
                am.setStreamVolume(AudioManager.STREAM_MUSIC,0,1);
                sbTopVoice.setProgress(0);
            }else{
                am.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolum,1);
                sbTopVoice.setProgress(currentVolum);
            }

        } else if (v == btnSysBlack) {
            // Handle clicks for btnSysBlack

            finish();
        } else if (v == btnSysPre) {
            // Handle clicks for btnSysPre

            setbuttonPre();
        } else if (v == btnSysPlary) {
            // Handle clicks for btnSysPlary

            setPaues();

        } else if (v == btnSysNext) {
            // Handle clicks for btnSysNext

            setbuttonNext();

        } else if (v == btnSysFullScreen) {
            // Handle clicks for btnSysFullScreen

//            if (isFullScreen){
//                setVideType(DEFALUT_SCREENT);
//
//            }else{
//                setVideType(FULL_SCREENT);
//
//            }

            int orientation = getResources().getConfiguration().orientation;
            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                btnSysFullScreen.setBackgroundResource(R.drawable.btn_full_screen_select);

            }else{
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                btnSysFullScreen.setBackgroundResource(R.drawable.btn_default_screen_normal);

            }

        } else if (v == tvSysVideName) {

        }
    }

    private void setPaues() {
        if (vv_svp_videview.isPlaying()) {
            //暂停
            vv_svp_videview.pause();
            btnSysPlary.setBackgroundResource(R.drawable.btn_play_normal);
        } else {
            //播放
            vv_svp_videview.start();
            btnSysPlary.setBackgroundResource(R.drawable.btn_pause_normal);

        }
    }

    private void setbuttonPre() {

        include_load.setVisibility(View.VISIBLE);


        if (mediaItems != null && mediaItems.size() > 0) {
            position--;

            if (position == 0) {
                setButtonPreState();
            }

            MediaItem item = mediaItems.get(position);
            vv_svp_videview.setVideoPath(item.getData());
            tvSysVideName.setText(item.getName());

            boolean netWork = Utils.isNetWork(item.getData());
            Log.e("data",netWork +"");
            setNetCache(netWork);


        } else if (uri != null) {
            finish();
        }
    }

    private void setbuttonNext() {

        include_load.setVisibility(View.VISIBLE);


        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                MediaItem item = mediaItems.get(position);
                vv_svp_videview.setVideoPath(item.getData());
                tvSysVideName.setText(item.getName());

                boolean netWork = Utils.isNetWork(item.getData());
                setNetCache(netWork);

                if (position == mediaItems.size() - 1) {
                    setButtonState();


                }
            } else {
                finish();
            }

        } else if (uri != null) {
            finish();
        }
    }

    private void setButtonPreState() {
        if (mediaItems != null && mediaItems.size() > 0) {

            btnSysPre.setEnabled(false);
            btnSysPre.setBackgroundResource(R.drawable.btn_pre_gray);

        } else if (uri != null) {

            //只有一个播放地址
            btnSysNext.setEnabled(false);
            btnSysNext.setBackgroundResource(R.drawable.btn_next_gray);

            btnSysPre.setEnabled(false);
            btnSysPre.setBackgroundResource(R.drawable.btn_pre_gray);

        }
    }

    private void setButtonState() {

        if (mediaItems != null && mediaItems.size() > 0) {

            btnSysNext.setEnabled(false);
            btnSysNext.setBackgroundResource(R.drawable.btn_next_gray);


        } else if (uri != null) {

            //只有一个播放地址
            btnSysNext.setEnabled(false);
            btnSysNext.setBackgroundResource(R.drawable.btn_next_gray);

            btnSysPre.setEnabled(false);
            btnSysPre.setBackgroundResource(R.drawable.btn_pre_gray);

        }


    }

    class BetteryReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            Log.e("level", level + "");
            if (level <= 0) {
                ivSys.setImageResource(R.drawable.ic_battery_0);
            } else if (level >= 0 && level <= 10) {
                ivSys.setImageResource(R.drawable.ic_battery_10);

            } else if (level >= 10 && level <= 20) {
                ivSys.setImageResource(R.drawable.ic_battery_20);

            } else if (level >= 20 && level <= 40) {
                ivSys.setImageResource(R.drawable.ic_battery_40);

            } else if (level >= 40 && level <= 60) {
                ivSys.setImageResource(R.drawable.ic_battery_60);

            } else if (level >= 60 && level <= 80) {
                ivSys.setImageResource(R.drawable.ic_battery_80);

            } else if (level >= 80 && level <= 100) {
                ivSys.setImageResource(R.drawable.ic_battery_100);
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (reciver != null) {
            unregisterReceiver(reciver);
        }

        super.onDestroy();


    }

    private float startY ;
    private float touchRang;
    private int mVol;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        int orientation = getResources().getConfiguration().orientation;//得到屏幕方向

        if(orientation == Configuration.ORIENTATION_LANDSCAPE){//如果是水平方向就可以监听事件
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:

                    startY =  event.getY();
                    touchRang = Math.min(screentHeight,screentWidth);
                    mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    break;

                case MotionEvent.ACTION_MOVE:

                    float endY = event.getY();
                    float distanceY = startY  - endY;
                    float changVolume = (distanceY / touchRang) * currentMaxVolume;
                    float volume = Math.min(Math.max(mVol + changVolume, 0),currentMaxVolume);
                    if(changVolume != 0){
                        setVolcaSize((int) volume);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    break;

            }
        }


        return super.onTouchEvent(event);
    }

    //屏幕切换回调
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "V", Toast.LENGTH_SHORT).show();
        }else {
//            Toast.makeText(this, "H", Toast.LENGTH_SHORT).show();
//            setVideType(FULL_SCREENT);
        }
    }

}
