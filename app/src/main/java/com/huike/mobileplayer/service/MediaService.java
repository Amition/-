package com.huike.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.huike.mobileplayer.Activity.AudioPlayer;
import com.huike.mobileplayer.IMyAidlInterface;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.domain.MediaItem;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;



public class MediaService extends Service {

    public static final String OPEN_AUDIO = "com.huike.mobileplayer.OPENMEDIAO";

     private   IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
            MediaService service =  MediaService.this;
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }

            @Override
            public void openAudio(int position) throws RemoteException {
                try {
                    service.openAudio(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void start() throws RemoteException {
                service.start();
            }

            @Override
            public void pause() throws RemoteException {
                service.pause();
            }

            @Override
            public void next() throws RemoteException {
                service.next();
            }

            @Override
            public void pre() throws RemoteException {
                service.pre();
            }

            @Override
            public int getPlaymode() throws RemoteException {
                return service.getPlaymode();
            }

            @Override
            public void setPlaymode(int playmode) throws RemoteException {
                service.setPlaymode(playmode);
            }

            @Override
            public int getCurrentPosition() throws RemoteException {
                return service.getCurrentPosition();
            }

            @Override
            public int getDuration() throws RemoteException {
                return service.getDuration();
            }

            @Override
            public String getName() throws RemoteException {
                return service.getName();
            }

            @Override
            public String getArtist() throws RemoteException {
                return service.getArtist();
            }

            @Override
            public void seekTo(int seekto) throws RemoteException {
                service.seekTo(seekto);
            }

            @Override
            public boolean isPlaying() throws RemoteException {
                return service.isPlaying();
            }

            @Override
            public void notifyChange(String action) throws RemoteException {
                service.notifyChange(action);
            }

         @Override
         public String getAudioPath() throws RemoteException {
             return service.getAudioPath();
         }

         @Override
         public int getAudioSessionId() throws RemoteException {
             return service.getAudioSessionId();
         }

     };

    private int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }


    private ArrayList<MediaItem> mediaItems;
    private int position;
    private MediaPlayer mediaPlayer;
    private MediaItem item;

    //播放模式
    public   final  static  int REPEAT_ORDER = 0;
    public   final  static  int REPEAT_SINGLE = 1;
    public   final  static  int REPEAT_ALL = 2;
    private int playmode = REPEAT_ORDER;//默认播放模式


    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getData();


    }

    private void getData() {

        mediaItems = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                super.run();

//                LogUtil.e();

//                SystemClock.sleep(5000);
                ContentResolver contentResolver = MediaService.this.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//在Sdcard显示的名称
                        MediaStore.Audio.Media.DURATION,//视频的长度
                        MediaStore.Audio.Media.SIZE,//视频文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                while (cursor.moveToNext()) {
                    MediaItem item = new MediaItem();
                    String name = cursor.getString(0);
                    item.setName(name);
                    long duration = cursor.getLong(1);
                    item.setDuration(duration);
                    long size = cursor.getLong(2);
                    item.setSIZE(size);
                    String data = cursor.getString(3);
                    item.setData(data);
                    String artist = cursor.getString(4);
                    item.setArtist(artist);

//                    Log.e("artist",artist);
                    mediaItems.add(item);
                }

                cursor.close();


            }
        }.start();
    }

    /**
     * 根据位置打开音乐
     *
     * @param position
     */
    private void openAudio(int position) throws IOException {
        this.position = position;
        if(mediaItems != null && mediaItems.size() >0){

            item = mediaItems.get(position);
            Log.e("item",item.getArtist());

            if(mediaPlayer != null){
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setDataSource(item.getData());
                mediaPlayer.prepareAsync();//异步播放
        }else{
            Toast.makeText(this, "数据还没有加载好呢！", Toast.LENGTH_SHORT).show();
        }



    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {

        }
    }

    class  MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            start();
            notifyChange(OPEN_AUDIO);

        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();//播放错 直接播放下一个 视频
            return true;
        }
    }


    /**
     * 根据不同的动作发广播
     * @param action
     */
    private void notifyChange(String action) {
//        Intent intent = new Intent();
//        intent.setAction(action);
//        sendBroadcast(intent);
        EventBus.getDefault().post(new MediaItem());//EevenBus 替换 broadcase
    }

    /**
     * 通知服务管理
     */

private NotificationManager notification;
    /**
     * 播放音乐
     */
    private void start() {
        mediaPlayer.start();

        notification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intents = new Intent(this,AudioPlayer.class);
        intents.putExtra("Notiftion",true);
        PendingIntent intent = PendingIntent.getActivity(this,0,intents,0);
        Notification.Builder builder = new Notification.Builder(MediaService.this);
        builder.setContentTitle("123播放器");
        builder.setSmallIcon(R.drawable.notification_music_playing);
        builder.setContentText("正在播放：" + getName());
        builder.setContentIntent(intent);

        notification.notify(1,builder.build());

//        Intent intents = new Intent(this,AudioPlayer.class);
//        intents.putExtra("Notiftion",true);
//        PendingIntent intent = PendingIntent.getActivity(this,0,intents,0);
//        Notification notifications = new Notification.Builder(this)
//                .setSmallIcon(R.drawable.notification_music_playing)
//                .setContentTitle("123播放器")
//                .setContentText("正在播放：" + getName())
//                .setContentIntent(intent)
//                .build();
//        notifications.flags = Notification.FLAG_ONGOING_EVENT;//点击不关闭
//        notification.notify(1,notifications);


    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mediaPlayer.pause();
        //通知消失掉
        notification.cancel(1);
    }

    /**
     * 下一首
     */
    private void next() {
        try {
            int playmode = getPlaymode();

            if (playmode == MediaService.REPEAT_ORDER) {
                if (mediaItems != null && mediaItems.size() > 0){
                    position ++;
                    if(position == mediaItems.size() -1){
                        position = mediaItems.size() -1;
                    }
                    openAudio(position);

                }
            } else if (playmode == MediaService.REPEAT_SINGLE) {
            } else if (playmode == MediaService.REPEAT_ALL) {

                if(mediaItems != null && mediaItems.size() > 0){
                    position++;
                    if(position == mediaItems.size()-1){
                        position = 0;
                    }
                    openAudio(position);
                }

            } else {

                if (mediaItems != null && mediaItems.size() > 0){
                    position ++;
                    if(position == mediaItems.size() -1){
                        position = 0;
                    }
                    openAudio(position);

                }

            }
            //保持到Service的实例中
            setPlaymode(playmode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 上一首
     */
    private void pre() {

        try {
            int playmode = getPlaymode();

            if (playmode == MediaService.REPEAT_ORDER) {
                if (mediaItems != null && mediaItems.size() > 0){
                    position --;
                    if(position == 0){
                        position = mediaItems.size() -1;
                    }
                    openAudio(position);

                }
            } else if (playmode == MediaService.REPEAT_SINGLE) {
            } else if (playmode == MediaService.REPEAT_ALL) {

                position--;
                if(position == 0){
                    position = mediaItems.size() -1;
                }
                openAudio(position);

            } else {

                if (mediaItems != null && mediaItems.size() > 0){
                    position --;
                    if(position == 0){
                        position = mediaItems.size() -1;
                    }
                    openAudio(position);

                }

            }
            //保持到Service的实例中
            setPlaymode(playmode);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlaymode() {
        return playmode;
    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlaymode(int playmode) {
        this.playmode = playmode;
    }

    /**
     * 得到当前播放进度
     *
     * @return
     */
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到当前的总时长
     *
     * @return
     */
    private int getDuration() {
        return mediaPlayer.getDuration();
    };


    /**
     * 得到歌曲的名称
     *
     * @return
     */
    private String getName() {
        Log.e("item",item.getName());
        return item.getName();
    }


    /**
     * 得到演唱者
     *
     * @return
     */
    private String getArtist() {
        Log.e("item",item.getArtist());

        return  item.getArtist();
    }

    /**
     * 音频的拖动
     *
     * @param seekto
     */
    private void seekTo(int seekto) {
        mediaPlayer.seekTo(seekto);
    }


    /**
     * 是否在播放中
     * @return
     */
    private boolean isPlaying() {
        return  mediaPlayer.isPlaying();
    }

    private String getAudioPath() {
        return item.getData();
    }

}
