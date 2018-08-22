package com.example.tvscreeninfo;

import android.app.Application;

/**
 * Created by cloud on 2018/8/21.
 */

public class BaseApplication extends Application {
    public static BaseApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
