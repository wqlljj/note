package com.example.wqllj.locationshare.application;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;
import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.model.baidumap.location.service.LocationService;

/**
 * Created by wqllj on 2018/6/10.
 */

public class AppApplication extends Application {
    public static Context context ;
    public LocationService locationService;
    public Vibrator mVibrator;
    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        context=this;
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(this);
        DbManager.init(this);
    }
}
