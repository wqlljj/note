package com.example.wqllj.locationshare.application;

import android.util.Log;

import com.example.wqllj.locationshare.util.SharePreferenceUtils;


/**
 * Created by WQ on 2018/4/9.
 */

public class Constant {
    public static String IP="10.11.102.76";
    private static String IMAGEBASE_URL ="http://%s:8088/";
    //mqtt
    private static String HOST = "tcp://%s:61613";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "password";
    public static final String TOPIC = "Employee";
    public static String CLIENTID = "aface";

    //SharePreference
    public static final String MAIN_BG_KEY="com.cloudminds.meta.accesscontroltv.MAIN_BG_KEY";
    public static final String IP_KEY="com.cloudminds.meta.accesscontroltv.IP_KEY";
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
