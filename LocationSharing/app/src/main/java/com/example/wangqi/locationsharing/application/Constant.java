package com.example.wangqi.locationsharing.application;

import android.util.Log;

import com.example.wangqi.locationsharing.util.SharePreferenceUtils;


/**
 * Created by WQ on 2018/4/9.
 */

public class Constant {
    //http://10.11.36.202:8088/aifile/screen/amov_bbb.mp4
    public static String IP="10.11.36.202";
    private static  String IMAGEBASE_URL ="http://%s:8088/";
    //mqtt
    private static  String HOST = "tcp://%s:1883";
    public static final  String USERNAME = "cloud";
    public static final  String PASSWORD = "123456";
    public static final  String TOPIC = "Employee";
    public static   String CLIENTID = "aface";

    //SharePreference
    public static final  String MAIN_BG_KEY="com.cloudminds.meta.accesscontroltv.MAIN_BG_KEY";
    public static final  String IP_KEY="com.cloudminds.meta.accesscontroltv.IP_KEY";
    private static String TAG="Constant";

    public static String getImagebaseUrl() {
        String imageUrl = String.format(IMAGEBASE_URL, SharePreferenceUtils.getPrefString(IP_KEY, IP));
        Log.e(TAG, "getImagebaseUrl: "+imageUrl );
        return imageUrl;
    }

    public static String getHOST() {
        String host = String.format(HOST, SharePreferenceUtils.getPrefString(IP_KEY, IP));
        Log.e(TAG, "getHOST: "+host );
        return host;
    }
}
