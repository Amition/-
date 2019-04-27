package com.huike.mobileplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.huike.mobileplayer.Bean.BeanPager;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.domain.MediaItem;
import com.huike.mobileplayer.system.SystemVidePlayer;
import com.huike.mobileplayer.utils.Url;
import com.scwang.smartrefresh.header.DeliveryHeader;
import com.scwang.smartrefresh.header.DropBoxHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

public class NetVidePager extends BeanPager {


    private ListView lv_videPager_item;
    private TextView tv_videPager_notView;
    private ProgressBar pb_videPager_progress;
    private SmartRefreshLayout refreshLayout;

    private ArrayList<MediaItem> mediaItems;
    private MyAdapter adapter;

    public NetVidePager(Context context) {
        super(context);
    }

    @Override
    public View initView() {

        View view = View.inflate(context, R.layout.net_video_pager, null);
        lv_videPager_item = view.findViewById(R.id.lv_videPager_item);
        tv_videPager_notView = view.findViewById(R.id.tv_videPager_notView);
        pb_videPager_progress = view.findViewById(R.id.pb_videPager_progress);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        return view;

        //        //api.m.mtime.cn/pageSubArea/TrailerList.api
    }

    @Override
    public void initData() {
        super.initData();

        initSmartRefreshLayout();
        getDataNetVido();



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

    }

    private void initSmartRefreshLayout() {
         refreshLayout.setRefreshHeader(new DropBoxHeader(context));
        refreshLayout.setRefreshFooter(new BallPulseFooter(context));



        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getDataNetVido();
                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                getNetLoad();
                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });
    }


    private void getDataNetVido() {

        RequestParams params = new RequestParams(Url.NET_VIDEO__URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
//                Toast.makeText(x.app(), result, Toast.LENGTH _LONG).show();
                Log.e("onSuccess", result.toString());

                processData(result);
                adapter = new MyAdapter();
                lv_videPager_item.setAdapter(adapter);

                pb_videPager_progress.setVisibility(View.GONE);//显示完成数据隐藏进度条

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

        });

    }

    private void getNetLoad() {
        RequestParams params = new RequestParams(Url.NET_VIDEO__URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
//                Toast.makeText(x.app(), result, Toast.LENGTH _LONG).show();
                Log.e("onSuccess", result.toString());

                processMoreData(result);

                adapter.notifyDataSetChanged();


            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

        });
    }

    private void processMoreData(String result) {
        JsonMoreParam(result);
    }

    private void JsonMoreParam(String result) {


        try {
            JSONObject object = new JSONObject(result);

            if (object != null) {
                JSONArray array = object.optJSONArray("trailers");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = (JSONObject) array.get(i);

                    if (jsonObject != null) {
                        MediaItem item = new MediaItem();
                        String coverImg = jsonObject.optString("coverImg");
                        item.setImage(coverImg);
                        String url = jsonObject.optString("url");
                        item.setData(url);
                        String movieName = jsonObject.optString("movieName");
                        item.setName(movieName);
                        String videoTitle = jsonObject.optString("videoTitle");
                        item.setDesc(videoTitle);

                        mediaItems.add(item);
                    }

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void processData(String result) {

        JsonParam(result);


    }

    private void JsonParam(String result) {

        mediaItems = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(result);

            if (object != null) {
                JSONArray array = object.optJSONArray("trailers");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = (JSONObject) array.get(i);

                    if (jsonObject != null) {
                        MediaItem item = new MediaItem();
                        String coverImg = jsonObject.optString("coverImg");
                        item.setImage(coverImg);
                        String url = jsonObject.optString("url");
                        item.setData(url);
                        String movieName = jsonObject.optString("movieName");
                        item.setName(movieName);
                        String videoTitle = jsonObject.optString("videoTitle");
                        item.setDesc(videoTitle);

                        mediaItems.add(item);
                    }

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class MyAdapter extends BaseAdapter {

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
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView  = View.inflate(context, R.layout.net_void_list_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            MediaItem item = mediaItems.get(position);

            viewHolder.tv_video_title.setText(item.getName());
            viewHolder.tv_video_desc.setText(item.getDesc());

//            x.image().bind(viewHolder.iv_netVideo_default,item.getImage());

            Glide.with(context)
                    .load(item.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//加载缓存
                    .placeholder(R.drawable.video_default)//正在加载显示的图片
                    .error(R.drawable.video_default)//加载错误显示的图片
                    .into(viewHolder.iv_netVideo_default);//图片显示的位置

            return convertView;
        }


        class ViewHolder {
            public View rootView;
            public ImageView iv_netVideo_default;
            public TextView tv_video_title;
            public TextView tv_video_desc;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.iv_netVideo_default = (ImageView) rootView.findViewById(R.id.iv_netVideo_default);
                this.tv_video_title = (TextView) rootView.findViewById(R.id.tv_video_title);
                this.tv_video_desc = (TextView) rootView.findViewById(R.id.tv_video_desc);
            }

        }
    }
}
