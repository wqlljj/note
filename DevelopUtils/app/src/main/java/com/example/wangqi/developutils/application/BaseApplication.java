package com.example.wangqi.developutils.application;

import android.app.Application;
import android.content.Context;

import com.example.wangqi.developutils.util.SharePreferenceUtils;

/**
 * Created by cloud on 2018/7/6.
 */

public class BaseApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        SharePreferenceUtils.setContext(this);
    }
}
