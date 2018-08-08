package com.example.wqllj.locationshare.model;


import android.content.Context;
import android.content.Intent;

import com.example.wqllj.locationshare.model.baidumap.BaiDuClient;
import com.example.wqllj.locationshare.model.baidumap.navi_bike_wake.BNaviMainActivity;
import com.example.wqllj.locationshare.model.mqtt.MQTTService;
import com.example.wqllj.locationshare.model.baidumap.location.demo.MainActivity;


/**
 * Created by wangqi on 2018/6/7.
 */

public class MainModel {

    private Intent intentService;
    private Context context;

    public MainModel() {

    }

    public void init(Context context) {
        this.context = context;
        intentService = new Intent(context, MQTTService.class);
//        context.startService(intentService);
        BaiDuClient.getInstance().init(context);
//        BaiDuClient.getInstance().start();
//        context.startActivity(new Intent(context, BNaviMainActivity.class));
        context.startActivity(new Intent(context, MainActivity.class));
    }
    public void destory(){
//        context.stopService(intentService);
        BaiDuClient.getInstance().stop();
        context=null;
    }
}
