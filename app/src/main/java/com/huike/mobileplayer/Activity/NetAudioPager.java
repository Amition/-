package com.huike.mobileplayer.Activity;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.huike.mobileplayer.Bean.BeanPager;
import com.huike.mobileplayer.R;
import com.huike.mobileplayer.domain.NetAudio;
import com.huike.mobileplayer.utils.Url;
import com.huike.mobileplayer.utils.Utils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import pl.droidsonroids.gif.GifImageView;

public class NetAudioPager extends BeanPager {

    /**
     * 视频
     */
    private static final int TYPE_VIDEO = 0;

    /**
     * 图片
     */
    private static final int TYPE_IMAGE = 1;

    /**
     * 文字
     */
    private static final int TYPE_TEXT = 2;

    /**
     * GIF图片
     */
    private static final int TYPE_GIF = 3;


    /**
     * 软件推广
     */
    private static final int TYPE_AD = 4;

    private ListView lv_main;
    private Myadapter adapter;
    private List<NetAudio.ListBean> list;
    private Utils utils;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.net_audio, null);
        lv_main = view.findViewById(R.id.lv_mian);

        return view;

    }

    @Override
    public void initData() {
        super.initData();
        utils = new Utils();
        getData();
    }

    private void getData() {

        RequestParams params = new RequestParams(Url.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
//                Toast.makeText(x.app(), result, Toast.LENGTH _LONG).show();
                Log.e("onSuccess", result.toString());

                processMoreData(result);
                adapter = new Myadapter();
                lv_main.setAdapter(adapter);
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
        NetAudio json = getJson(result);
        list = json.getList();
    }

    private NetAudio getJson(String result) {

       return  new Gson().fromJson(result, NetAudio.class);
    }

    class Myadapter extends BaseAdapter{

        /**
         * @return live 的类型数
         */
        @Override
        public int getViewTypeCount() {
            return 5;
        }

        /**
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
            NetAudio.ListBean bean = list.get(position);
            String type = bean.getType();
            int itemViewType ;
            if ("video".equals(type)) {
                itemViewType = TYPE_VIDEO;
            } else if ("image".equals(type)) {
                itemViewType = TYPE_IMAGE;
            } else if ("text".equals(type)) {
                itemViewType = TYPE_TEXT;
            } else if ("gif".equals(type)) {
                itemViewType = TYPE_GIF;
            } else {
                itemViewType = TYPE_AD;//广播
            }
                return itemViewType;
        }

        @Override
        public int getCount() {
            return list.size() == 0 ? 0 :list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            int itemViewType = getItemViewType(position);
            if(convertView == null){
                viewHolder  = new ViewHolder();
            convertView = initView(convertView, viewHolder, itemViewType);

                //-----------实例化公共部分的View

                initViewCommon(convertView, viewHolder, itemViewType);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            bindData(position, viewHolder, itemViewType);

            return convertView;
        }
    }
    private void bindData(int position, ViewHolder viewHolder, int itemViewType) {
        //根据位置得到数据
        NetAudio.ListBean listEntity = list.get(position);
        //绑定数据
        switch (itemViewType) {
            case TYPE_VIDEO://视频
                bindData(viewHolder, listEntity);
                //第一个参数是视频播放地址，第二个参数是显示封面的地址，第三参数是标题
                viewHolder.jcv_videoplayer.setUp(listEntity.getVideo().getVideo().get(0), listEntity.getVideo().getThumbnail().get(0), null);
                viewHolder.tv_play_nums.setText(listEntity.getVideo().getPlaycount() + "次播放");

                viewHolder.tv_video_duration.setText(utils.stringForTime(listEntity.getVideo().getDuration() * 1000) + "");

                break;
            case TYPE_IMAGE://图片
                bindData(viewHolder, listEntity);
//                System.out.println("mediaItem.getImage().getSmall().get(0)" + mediaItem.getImage().getSmall().get(0));
                viewHolder.iv_image_icon.setImageResource(R.drawable.bg_item);
                if(listEntity.getImage() != null &&  listEntity.getImage().getSmall()!= null&&listEntity.getImage().getSmall().size() >0){
                    x.image().bind(viewHolder.iv_image_icon, listEntity.getImage().getSmall().get(0).toString());
                }else if(listEntity.getImage() != null &&  listEntity.getImage().getBig()!= null&&listEntity.getImage().getBig().size() >0){
                    x.image().bind(viewHolder.iv_image_icon, listEntity.getImage().getBig().get(0));
                }

                break;
            case TYPE_TEXT://文字
                bindData(viewHolder, listEntity);
                break;
            case TYPE_GIF://gif
                bindData(viewHolder, listEntity);
                System.out.println("mediaItem.getGif().getImages().get(0)" + listEntity.getGif().getImages().get(0));
                Glide.with(context).load(listEntity.getGif().getImages().get(0)).into(viewHolder.iv_image_gif);

                break;
            case TYPE_AD://软件广告
                break;
        }


        //设置文本
        viewHolder.tv_context.setText(listEntity.getText());
    }

    private void bindData(ViewHolder viewHolder, NetAudio.ListBean mediaItem) {
        if(mediaItem.getU()!=null&& mediaItem.getU().getHeader()!=null&&mediaItem.getU().getHeader().get(0)!=null){
            x.image().bind(viewHolder.iv_headpic, mediaItem.getU().getHeader().get(0));
        }
        if(mediaItem.getU() != null&&mediaItem.getU().getName()!= null){
            viewHolder.tv_name.setText(mediaItem.getU().getName()+"");
        }

        viewHolder.tv_time_refresh.setText(mediaItem.getPasstime());

        //设置标签
        List<NetAudio.ListBean.TagsBean> tagsEntities = mediaItem.getTags();
        if (tagsEntities != null && tagsEntities.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < tagsEntities.size(); i++) {
                buffer.append(tagsEntities.get(i).getName() + " ");
            }
            viewHolder.tv_video_kind_text.setText(buffer.toString());
        }

        //设置点赞，踩,转发
        viewHolder.tv_shenhe_ding_number.setText(mediaItem.getUp());
        viewHolder.tv_shenhe_cai_number.setText(mediaItem.getDown() + "");
        viewHolder.tv_posts_number.setText(mediaItem.getForward() +"");

    }
    private void initViewCommon(View convertView, ViewHolder viewHolder, int itemViewType) {
        switch (itemViewType) {
            case TYPE_VIDEO://视频
            case TYPE_IMAGE://图片
            case TYPE_TEXT://文字
            case TYPE_GIF://gif
                //加载除开广告部分的公共部分视图
                //user info
                viewHolder.iv_headpic = (ImageView) convertView.findViewById(R.id.iv_headpic);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_time_refresh = (TextView) convertView.findViewById(R.id.tv_time_refresh);
                viewHolder.iv_right_more = (ImageView) convertView.findViewById(R.id.iv_right_more);
                //bottom
                viewHolder.iv_video_kind = (ImageView) convertView.findViewById(R.id.iv_video_kind);
                viewHolder.tv_video_kind_text = (TextView) convertView.findViewById(R.id.tv_video_kind_text);
                viewHolder.tv_shenhe_ding_number = (TextView) convertView.findViewById(R.id.tv_shenhe_ding_number);
                viewHolder.tv_shenhe_cai_number = (TextView) convertView.findViewById(R.id.tv_shenhe_cai_number);
                viewHolder.tv_posts_number = (TextView) convertView.findViewById(R.id.tv_posts_number);
                viewHolder.ll_download = (LinearLayout) convertView.findViewById(R.id.ll_download);

                break;
        }


        //中间公共部分 -所有的都有
        viewHolder.tv_context = (TextView) convertView.findViewById(R.id.tv_context);
    }
    private View initView(View convertView, ViewHolder viewHolder, int itemViewType) {
        //根据类型加载不同的布局
        switch (itemViewType) {
            case TYPE_VIDEO://视频
                convertView = View.inflate(context, R.layout.all_video_item, null);
                //在这里实例化特有的
                viewHolder.tv_play_nums = (TextView) convertView.findViewById(R.id.tv_play_nums);
                viewHolder.tv_video_duration = (TextView) convertView.findViewById(R.id.tv_video_duration);
                viewHolder.iv_commant = (ImageView) convertView.findViewById(R.id.iv_commant);
                viewHolder.tv_commant_context = (TextView) convertView.findViewById(R.id.tv_commant_context);
                viewHolder.jcv_videoplayer = (JCVideoPlayer) convertView.findViewById(R.id.jcv_videoplayer);
                break;
            case TYPE_IMAGE://图片
                convertView = View.inflate(context, R.layout.all_image_item, null);
                viewHolder.iv_image_icon = (ImageView) convertView.findViewById(R.id.iv_image_icons);
                break;
            case TYPE_TEXT://文字
                convertView = View.inflate(context, R.layout.all_text_item, null);
                break;
            case TYPE_GIF://gif
                convertView = View.inflate(context, R.layout.all_gif_item, null);
                viewHolder.iv_image_gif = (GifImageView) convertView.findViewById(R.id.iv_image_gif);
                break;
            case TYPE_AD://软件广告
                convertView = View.inflate(context, R.layout.all_ad_item, null);
                viewHolder.btn_install = (Button) convertView.findViewById(R.id.btn_install);
                viewHolder.iv_image_icon = (ImageView) convertView.findViewById(R.id.iv_image_icons);
                break;
        }
        return convertView;
    }
    static class ViewHolder {
        //user_info
        ImageView iv_headpic;
        TextView tv_name;
        TextView tv_time_refresh;
        ImageView iv_right_more;
        //bottom
        ImageView iv_video_kind;
        TextView tv_video_kind_text;
        TextView tv_shenhe_ding_number;
        TextView tv_shenhe_cai_number;
        TextView tv_posts_number;
        LinearLayout ll_download;

        //中间公共部分 -所有的都有
        TextView tv_context;


        //Video
//        TextView tv_context;
        TextView tv_play_nums;
        TextView tv_video_duration;
        ImageView iv_commant;
        TextView tv_commant_context;
        JCVideoPlayer jcv_videoplayer;

        //Image
        ImageView iv_image_icon;
//        TextView tv_context;

        //Text
//        TextView tv_context;

        //Gif
        GifImageView iv_image_gif;
//        TextView tv_context;

        //软件推广
        Button btn_install;
//        TextView iv_image_icon;
        //TextView tv_context;


    }
}
