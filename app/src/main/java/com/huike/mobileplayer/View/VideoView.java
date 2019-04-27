package com.huike.mobileplayer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class VideoView extends android.widget.VideoView {

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
//    }

//    public void  setVidoSize(int width,int height){
//        ViewGroup.LayoutParams params = getLayoutParams();
//        params.width = width;
//        params.height = height;
//        setLayoutParams(params);
//    }
}
