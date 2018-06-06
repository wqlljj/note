package com.cloudminds.hc.hariservice;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.push.PushClient;
import com.cloudminds.hc.hariservice.service.HariService;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.getui.logful.LoggerConfigurator;
import com.getui.logful.LoggerFactory;

/**
 * Created by zoey on 17/4/17.
 */

public class HariServiceClient {

    private static HariServiceClient serviceClient;
    public static InitServiceCallback initServiceCallback;
    private static Application currentApplication;

    public static void createService(Application application){
        if (null == serviceClient){
            currentApplication = application;
            Context context = application.getApplicationContext();
            serviceClient = new HariServiceClient();
            PreferenceUtils.setContext(context);
            HariUtils hariUtils = HariUtils.getInstance();
            hariUtils.setContext(context);
            hariUtils.createPushClient();
            hariUtils.getHariServiceConnector();
            initLogger();
        }
    }

    public static void createService(Application application,InitServiceCallback callback){
        if (null == serviceClient){
            initServiceCallback = callback;
            createService(application);
        }
    }

    public static CallEngine getCallEngine(){
        return HariUtils.getCallEngine();
    }

    public static CommandEngine getCommandEngine(){

        return HariUtils.getCmdEngine();
    }


    public interface InitServiceCallback {
        public void onServiceInitialized();
    }

    private static boolean initialized = false;

    public static void initLogger(){

        if (initialized) {
            return;
        }

        Context context = HariUtils.getInstance().getContext();
        boolean readPhoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        if (!readPhoneGranted){
            return;
        }
        initialized = true;
        LoggerConfigurator.Builder builder = LoggerConfigurator.newBuilder();
        builder.setCaughtException(true);
        builder.setDefaultLoggerName("HSLog");
        builder.setUseNativeCryptor(false);
        builder.setUpdateSystemFrequency(60*30);   //半小时传一次
        builder.setDeleteUploadedLogFile(true);

        LoggerFactory.setApiUrl("http://10.10.26.30:9600");
        LoggerFactory.setAppKey("b43499d1a0e9b222653c809353b48a2e");
        LoggerFactory.setAppSecret("b8becc2c0d665adb8f85729b21743c32");
        LoggerFactory.init(currentApplication,builder.build());
    }

}
