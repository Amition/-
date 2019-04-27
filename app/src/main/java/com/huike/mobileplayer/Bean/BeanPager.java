package com.huike.mobileplayer.Bean;

import android.content.Context;
import android.view.View;

import java.io.Serializable;

public abstract class BeanPager  {

    public Context context;
    public boolean isInitData  =   false; //标识fragment是否被初始化
    public View rootView;
    public BeanPager(Context context){
        this.context = context;
        rootView = initView();
        isInitData = false;//默认为 false
    }

    public abstract View initView();

    public void initData(){

    }

}
