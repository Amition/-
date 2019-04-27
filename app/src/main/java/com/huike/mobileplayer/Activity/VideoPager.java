package com.huike.mobileplayer.Activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huike.mobileplayer.Bean.BeanPager;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.domain.MediaItem;
import com.huike.mobileplayer.system.SystemVidePlayer;
import com.huike.mobileplayer.utils.Utils;

import java.util.ArrayList;

public class VideoPager extends BeanPager {

    private ArrayList<MediaItem> mediaItems;
    private Utils utils;
    private ListView lv_videPager_item;
    private TextView tv_videPager_notView;
    private ProgressBar pb_videPager_progress;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        private myBeasAdapter myBeasAdapter;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                pb_videPager_progress.setVisibility(View.GONE);
                tv_videPager_notView.setVisibility(View.GONE);
                //初始化listView
                try {
                    myBeasAdapter = new myBeasAdapter();
                    lv_videPager_item.setAdapter(myBeasAdapter);

                    lv_videPager_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            MediaItem item = mediaItems.get(position);
                            Intent intent = new Intent(context,SystemVidePlayer.class);
                            intent.setDataAndType(Uri.parse(item.getData()),"video/*");
                            Bundle extras = new Bundle();
                            extras.putSerializable("videolist",mediaItems);
                            intent.putExtras(extras);
                            intent.putExtra("position",position);
                            context.startActivity(intent);//启动自定义播放器
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                pb_videPager_progress.setVisibility(View.GONE);
                tv_videPager_notView.setVisibility(View.VISIBLE);
            }

        }
    };

    public VideoPager(Context context) {
        super(context);
        utils = new Utils();
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager, null);
        lv_videPager_item = view.findViewById(R.id.lv_videPager_item);
        tv_videPager_notView = view.findViewById(R.id.tv_videPager_notView);
        pb_videPager_progress = view.findViewById(R.id.pb_videPager_progress);



        return view;
    }

    @Override
    public void initData() {
//        super.initData();

        try {
            getData();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getData() {

        mediaItems = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                super.run();

//                LogUtil.e();

//                SystemClock.sleep(5000);
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Video.Media.DISPLAY_NAME,//名称
                        MediaStore.Video.Media.DURATION,//长度
                        MediaStore.Video.Media.SIZE,//大小
                        MediaStore.Video.Media.DATA//地址
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

                    mediaItems.add(item);
                }

                cursor.close();

                handler.sendEmptyMessage(0);

            }
        }.start();
    }

     class myBeasAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mediaItems.size() == 0 ? 0 : mediaItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mediaItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder  viewHolder ;
            if (convertView == null) {
                convertView  = View.inflate(context, R.layout.vide_paget_data, null);
                viewHolder = new ViewHolder(convertView);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            MediaItem item = mediaItems.get(position);
            viewHolder.tv_videPager_title.setText(item.getName());
            viewHolder.tv_videPager_time.setText(utils.stringForTime(Integer.parseInt(item.getDuration() + "")));
            viewHolder.tv_videPager_data.setText(android.text.format.Formatter.formatShortFileSize(context,item.getSIZE()));

            return convertView;
        }

     class ViewHolder {
            public View rootView;
            public TextView tv_videPager_title;
            public TextView tv_videPager_time;
            public TextView tv_videPager_data;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.tv_videPager_title = (TextView) rootView.findViewById(R.id.tv_videPager_title);
                this.tv_videPager_time = (TextView) rootView.findViewById(R.id.tv_videPager_time);
                this.tv_videPager_data = (TextView) rootView.findViewById(R.id.tv_videPager_data);
            }

        }
    }


}
