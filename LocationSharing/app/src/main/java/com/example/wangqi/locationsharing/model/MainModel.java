package com.example.wangqi.locationsharing.model;


import android.content.Context;
import android.content.Intent;

import com.example.wangqi.locationsharing.model.baidumap.BaiDuClient;
import com.example.wangqi.locationsharing.model.mqtt.MQTTService;

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
    }
    public void destory(){
//        context.stopService(intentService);
        BaiDuClient.getInstance().stop();
        context=null;
    }
}
