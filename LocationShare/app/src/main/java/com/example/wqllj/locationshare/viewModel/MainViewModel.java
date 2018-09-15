package com.example.wqllj.locationshare.viewModel;

import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.example.wqllj.locationshare.model.MainModel;

/**
 * Created by wangqi on 2018/6/7.
 */

public class MainViewModel extends ViewModel {
    MainModel model;

    public MainViewModel() {
    }

    public void init(Context context) {
        model=new MainModel(context);
    }
    public void destory(){
        model.destory();
    }
}
