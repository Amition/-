package com.huike.mobileplayer.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.huike.mobileplayer.domain.Lyric;
import com.huike.mobileplayer.utils.Utils;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class LyricView  extends TextView {

    private int w;
    private int h;
    private ArrayList<Lyric> lyrics;
    private int index;
    private Paint whitePaint;
    private Paint paint;
    private int textHeight = 20;
    private float sheepTime;
    private float timePoint;
    private float currentPosition;


    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textHeight = Utils.dx2px(getContext(), 20);
//        initView();
        initPaint();
    }

    public void setLyrice(ArrayList<Lyric> lyrics){
            this.lyrics = lyrics;
    }

    private void initPaint() {
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setTextSize(textHeight);
        whitePaint.setTextAlign(Paint.Align.CENTER);
        whitePaint.setColor(Color.WHITE);


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textHeight);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.GREEN);
    }

    private void initView() {

        lyrics = new ArrayList<>();
        Lyric lyric = new Lyric();

        for (int i = 0; i < 1000; i++) {
            lyric.setContent("aaa" + i);
            lyric.setTimePoint(2000 * i);
            lyric.setSheepTime(2000 + i);
            lyrics.add(lyric);//添加数据
            lyric = new Lyric();
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (lyrics != null && lyrics.size() > 0&&index< lyrics.size()) {

            //平移动画
            float dy = 0;
            if(sheepTime ==0){
                dy = 0;
            }else{
                //花的时间:休眠时间 = 移动距离：行高
//                float push = ((currentPosition-timePoint)/sleepTime)*textHeight;

                // 坐标 = 行高 + 移动距离
                dy = textHeight +((currentPosition-timePoint)/sheepTime)*textHeight;
            }
            canvas.translate(0,-dy);

            //有歌词：
            // 1.绘制当前句；
            String currentContent = lyrics.get(index).getContent();
            canvas.drawText(currentContent, w / 2, h / 2, paint);
            // 2.绘制前面部分；
            float tempY = h / 2;
            for (int i = index - 1; i >= 0; i--) {
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0) {
                    break;
                }

                canvas.drawText(preContent, w / 2, tempY, whitePaint);

            }
            // 3,绘制后面部分
            tempY = h / 2;
            for (int i = index + 1; i < lyrics.size(); i++) {
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if (tempY > h) {
                    break;
                }

                canvas.drawText(nextContent, w / 2, tempY, whitePaint);

            }
        } else {
            //没有歌词
            canvas.drawText("没有找到歌词", w / 2, h / 2, paint);
        }

    }

    public void setShowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;

        if (lyrics == null)
            return;

        for (int i = 1; i < lyrics.size(); i++) {

            //划出区域
            if (currentPosition < lyrics.get(i).getTimePoint()) {

                int tempIndex = i - 1;//0->1
                if (currentPosition >= lyrics.get(tempIndex).getTimePoint()) {

                    index = tempIndex;//歌词下标索引
                    sheepTime = lyrics.get(index).getSheepTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }


            }

        }


        invalidate();//强制绘制-onDraw()方法再次执行


    }
}

