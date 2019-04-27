package com.huike.mobileplayer.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huike.mobileplayer.Bean.BeanPager;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class ReplaceFragment extends Fragment {

    public ReplaceFragment(){};

    private ArrayList<BeanPager> beanPagers;
    private int position;
    public ReplaceFragment(ArrayList<BeanPager> beanPagers,int position) {
        this.beanPagers = beanPagers;
        this.position = position;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BeanPager beanPager = getBeanPager();
        if(beanPager != null){
            return beanPager.rootView;
        }
        return null;
    }

    private BeanPager getBeanPager() {
        BeanPager pager = beanPagers.get(position);
        if(pager != null && !pager.isInitData){
            pager.isInitData = true;
            pager.initData();
        }

        return  pager;
    }
}