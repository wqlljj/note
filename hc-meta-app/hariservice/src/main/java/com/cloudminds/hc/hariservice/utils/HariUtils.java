package com.cloudminds.hc.hariservice.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.hariservice.push.PushClient;
import com.cloudminds.hc.hariservice.service.HariService;
import com.cloudminds.hc.hariservice.service.HariServiceConnector;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static android.content.ContentValues.TAG;

/**
 * Created by zoey on 17/4/17.
 */

public class HariUtils {

    private static HariUtils hariUtils;
    HariServiceConnector hariServiceConnector;
    Context context;
    public static String IMEI;
    private static String testImei = "2919276686923065298";// "12345678900987654321123456789009";//
    private static boolean isConnectSuccess=false;
    private static CallEngine callEngine;
    private static CommandEngine cmdEngine;
    private static PushClient pushClient;

    public synchronized static HariUtils getInstance() {
        if (hariUtils == null) {

            hariUtils = new HariUtils();
            callEngine = new CallEngine();
            cmdEngine = new CommandEngine();
        }
        return hariUtils;
    }

    public static boolean isAppActive = true;

    public HariUtils() {
        //getIMEI();
    }

    public void createPushClient(){
        if (null == pushClient){
            pushClient = PushClient.instance(context);
            pushClient.connect();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static CallEngine getCallEngine(){ return  callEngine;}

    public static CommandEngine getCmdEngine(){ return cmdEngine;}


    public static String getIMEI() {

        if (IMEI == null || IMEI.isEmpty()){
            try {
                TelephonyManager telephonyManager = (TelephonyManager)hariUtils.getContext().getSystemService(hariUtils.getContext().TELEPHONY_SERVICE);
                IMEI = telephonyManager.getDeviceId();
                PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_RCUID,IMEI);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return IMEI;
    }


    public HariServiceConnector getHariServiceConnector() {

        if(hariServiceConnector==null) {
            hariServiceConnector = new HariServiceConnector() {
                @Override
                public void onHariServiceConnected() {
                    Log.e("TEST", "onHariServiceConnected: 1" );
                    isConnectSuccess=true;
                    if (null != HariServiceClient.initServiceCallback){
                        HariServiceClient.initServiceCallback.onServiceInitialized();
                    }
                }
                @Override
                public void onServiceDisconnected() {
                }
            };
            Intent intent = new Intent();
            intent = new Intent(context, HariService.class);
            ComponentName componentName = context.startService(intent);
            if(componentName!=null){
                hariServiceConnector.connect(context);
            }
            if(!EventBus.getDefault().isRegistered(callEngine)) {
                EventBus.getDefault().register(callEngine);
            }
            if(!EventBus.getDefault().isRegistered(cmdEngine)) {
                EventBus.getDefault().register(cmdEngine);
            }
        }
        Log.e("TEST", "getHariServiceConnector: ");
        return hariServiceConnector;
    }

    public boolean isConnectSuccess() {
        return isConnectSuccess;
    }


//    public void sendInfo(String data){
//        this.getHariServiceConnector().getHariService().getCallManager().sendInfo(data);
//    }

}
