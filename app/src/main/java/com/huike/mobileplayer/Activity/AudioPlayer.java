package com.huike.mobileplayer.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huike.mobileplayer.IMyAidlInterface;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.View.BaseVisualizerView;
import com.huike.mobileplayer.View.LyricView;
import com.huike.mobileplayer.domain.Lyric;
import com.huike.mobileplayer.domain.MediaItem;
import com.huike.mobileplayer.service.MediaService;
import com.huike.mobileplayer.utils.LyricUtils;
import com.huike.mobileplayer.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

public class AudioPlayer extends AppCompatActivity implements View.OnClickListener {

    private LyricView lyric_View;
    private BaseVisualizerView iv_animation;
    private TextView tv_audio_author;
    private TextView tv_audio_MusicName;
    private TextView tv_audio_nolyirc;
    private TextView tv_audio_timer;
    private Button btn_audio_mode;
    private Button btn_audio_pre;
    private Button btn_audio_play;
    private Button btn_audio_next;
    private Button btn_audio_world;
    private SeekBar sb_seek;
    private int position;
    private MyBroadcastReceiver receiver;
    private final static int PROGRESS = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == PROGRESS){

                try {
                    int currentPosition = iMyAidlInterface.getCurrentPosition();
                    int duration = iMyAidlInterface.getDuration();

                    sb_seek.setProgress(currentPosition);

                    tv_audio_timer.setText(utils.stringForTime(currentPosition)+"/"+utils.stringForTime(duration));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                handler.removeMessages(PROGRESS);
                handler.sendEmptyMessageDelayed(PROGRESS,1000);
            }else if(msg.what == SHOW_LYIRICS){

                try {
                    int currentPosition = iMyAidlInterface.getCurrentPosition();
                    lyric_View.setShowNextLyric(currentPosition);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                handler.removeMessages(SHOW_LYIRICS);
                handler.sendEmptyMessage(SHOW_LYIRICS);
            }
        }
    };
    private Visualizer mVisualizer;
    private void setupVisualizerFxAndUi()
    {

        int audioSessionid = 0;
        try {
            audioSessionid = iMyAidlInterface.getAudioSessionId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("audioSessionid=="+audioSessionid);
        mVisualizer = new Visualizer(audioSessionid);
        // 参数内必须是2的位数
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 设置允许波形表示，并且捕获它
        iv_animation.setVisualizer(mVisualizer);
        mVisualizer.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVisualizer.release();

    }

    private IMyAidlInterface iMyAidlInterface;
    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);

            try {
                if(!notify){
                    iMyAidlInterface.openAudio(position);

                }else{
                        iMyAidlInterface.notifyChange(MediaService.OPEN_AUDIO);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                iMyAidlInterface = null;
        }
    };
//    private AnimationDrawable ad;
    private Utils utils;
    private boolean notify;
    private int SHOW_LYIRICS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        initView();
        initEventbus();
        initBro();
        initData();
//        iniAnimations();
        bindAndStartService();
    }

    public void initEventbus() {
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void  onEventbus(MediaItem item){

        getAuhtor();
        getName();
        handler.sendEmptyMessage(PROGRESS);

        setShowLyric();

        try {
            sb_seek.setMax(iMyAidlInterface.getDuration());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        isPlayerState();

        setupVisualizerFxAndUi();

    }

    private void isPlayerState() {
        if(iMyAidlInterface != null){
            try {
                if(iMyAidlInterface.isPlaying()){
                    btn_audio_play.setBackgroundResource(R.drawable.btn_now_playing_pause_select);
                }else{
                    btn_audio_play.setBackgroundResource(R.drawable.btn_now_playing_real_play_pressed_select);

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private void setShowLyric() {
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String path = iMyAidlInterface.getAudioPath();

            path = path.substring(0,path.indexOf("."));
            File file = new File(path + ".lrc");

            if(!file.exists()){
                 file = new File(path + ".txt");
            }

            lyricUtils.readLyricFile(file);
            ArrayList<Lyric> lyrics = lyricUtils.getLyrics();

            lyric_View.setLyrice(lyricUtils.getLyrics());


        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(lyricUtils.isExistLyric()){
            handler.sendEmptyMessage(SHOW_LYIRICS);//发送歌词上升消息
        }
    }


    private void initBro() {
        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.OPEN_AUDIO);
        registerReceiver(receiver,filter);
    }

    class  MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
                        getAuhtor();
                        getName();
            handler.sendEmptyMessage(PROGRESS);

            try {
                sb_seek.setMax(iMyAidlInterface.getDuration());
            } catch (RemoteException e) {
                e.printStackTrace();
            }



        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(con != null)
            unbindService(con);
            con = null;

            if(receiver != null){
                unregisterReceiver(receiver);
                receiver = null;
            }
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MediaService.class);
        intent.setAction("com.huike.mobileplayer.OPENMEDIAO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//避免Service被重新创建

    }



//    private void iniAnimations() {
//        iv_animation.setBackgroundResource(R.drawable.now_playing_matrix_animation);
//        ad = (AnimationDrawable) iv_animation.getBackground();
//        ad.start();
//
//
//    }

    private void initData() {
        utils = new Utils();
        getData();


    }

    @SuppressLint("SetTextI18n")

    private void getName() {
        try {
            tv_audio_MusicName.setText(iMyAidlInterface.getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getAuhtor() {
        try {
            tv_audio_author.setText(iMyAidlInterface.getArtist());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getData() {
        notify = getIntent().getBooleanExtra("Notiftion", false);

            if(!notify){
                position = getIntent().getIntExtra("position", 0);

            }



    }


    private void initView() {
        iv_animation = (BaseVisualizerView) findViewById(R.id.iv_animation);
        tv_audio_author = (TextView) findViewById(R.id.tv_audio_author);
        tv_audio_MusicName = (TextView) findViewById(R.id.tv_audio_MusicName);
        tv_audio_nolyirc = (TextView) findViewById(R.id.tv_audio_nolyirc);
        tv_audio_timer = (TextView) findViewById(R.id.tv_audio_timer);
        btn_audio_mode = (Button) findViewById(R.id.btn_audio_mode);
        btn_audio_pre = (Button) findViewById(R.id.btn_audio_pre);
        btn_audio_play = (Button) findViewById(R.id.btn_audio_play);
        btn_audio_next = (Button) findViewById(R.id.btn_audio_next);
        btn_audio_world = (Button) findViewById(R.id.btn_audio_world);
        sb_seek = findViewById(R.id.sb_seek);
        lyric_View = findViewById(R.id.lyric_View);

        btn_audio_mode.setOnClickListener(this);
        btn_audio_pre.setOnClickListener(this);
        btn_audio_play.setOnClickListener(this);
        btn_audio_next.setOnClickListener(this);
        btn_audio_world.setOnClickListener(this);

        sb_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try {
                        iMyAidlInterface.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio_mode:
                changePlaymode();
                break;
            case R.id.btn_audio_pre:
                try {
                    iMyAidlInterface.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn_audio_play:

                try {
                    if(!iMyAidlInterface.isPlaying()){
                        btn_audio_play.setBackgroundResource(R.drawable.btn_now_playing_pause_select);
                        try {
                            iMyAidlInterface.start();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
//                        ad.start();
                    }else{
                        btn_audio_play.setBackgroundResource(R.drawable.btn_now_playing_real_play_pressed_select);
                        //pause
                        try {
                            iMyAidlInterface.pause();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
//                        ad.stop();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn_audio_next:
                try {
                    iMyAidlInterface.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_audio_world:

                break;
        }
    }

    private void changePlaymode() {
        try {
            int playmode = iMyAidlInterface.getPlaymode();

            if (playmode == MediaService.REPEAT_ORDER) {
                playmode = MediaService.REPEAT_SINGLE;
            } else if (playmode == MediaService.REPEAT_SINGLE) {
                playmode = MediaService.REPEAT_ALL;
            } else if (playmode == MediaService.REPEAT_ALL) {
                playmode = MediaService.REPEAT_ORDER;
            } else {
                playmode = MediaService.REPEAT_ORDER;
            }
            //保持到Service的实例中
            iMyAidlInterface.setPlaymode(playmode);

            showPlaymode();


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showPlaymode() {
        try {
            int playmode = iMyAidlInterface.getPlaymode();//从服务里面

            if (playmode == MediaService.REPEAT_ORDER) {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btn_audio_mode.setBackgroundResource(R.drawable.btn_audio_player_order);
            } else if (playmode == MediaService.REPEAT_SINGLE) {
                Toast.makeText(AudioPlayer.this, "单曲播放", Toast.LENGTH_SHORT).show();
                btn_audio_mode.setBackgroundResource(R.drawable.btn_audio_player_single);
            } else if (playmode == MediaService.REPEAT_ALL) {
                Toast.makeText(AudioPlayer.this, "全部播放", Toast.LENGTH_SHORT).show();
                btn_audio_mode.setBackgroundResource(R.drawable.btn_audio_player_all);
            } else {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btn_audio_mode.setBackgroundResource(R.drawable.btn_audio_player_order);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
