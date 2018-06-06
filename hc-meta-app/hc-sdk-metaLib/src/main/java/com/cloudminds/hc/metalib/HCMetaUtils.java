package com.cloudminds.hc.metalib;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.metalib.bean.AxisData;
import com.cloudminds.hc.metalib.bean.BatteryData;
import com.cloudminds.hc.metalib.bean.DeviceInfo;
import com.cloudminds.hc.metalib.bean.UltrasonicData;
import com.cloudminds.hc.metalib.broadcast.MetaWakeUpReceiver;
import com.cloudminds.hc.metalib.manager.MetaSensorManager;
import com.cloudminds.hc.metalib.utils.ThreadPoolUtils;

import static com.cloudminds.hc.metalib.BaseData.ACTION_WAKEUP;


/**
 * Created by SX on 2017/4/7.
 */

public class HCMetaUtils {
    private static final String TAG="HCMetaUtils";
    public static boolean isConnectMeta=false;
    private static String IMEI;
    private static String testImei = "2919276686923065298";
    private static MetaWakeUpReceiver wakeUpReceiver;
    private static Context context;
    private static boolean isInit=false;
    private static LooperExecutor executor = new LooperExecutor();
    private static SharedPreferences sharedPreferences;

    public static void init(final Context context){
        Log.e(TAG, " init: " );
        if(HCMetaUtils.context!=null){
            Log.e(TAG, "init: HCMetaUtils.context!=null" );
            return;
        }
        ThreadPoolUtils.init();
        UpdaterApplication.init(context);
        isInit=true;
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            }
        });
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                MetaBaseContants.lowHealth=(int)sharedPreferences.getFloat("lowHealth",MetaBaseContants.lowHealth);            }
        });
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                MetaBaseContants.lowPawer=(int)(sharedPreferences.getFloat("lowPawer",MetaBaseContants.lowPawer));
            }
        });

        executor.requestStart();
        USBUtils.init(context);
        HCMetaUtils.context = context;
        MetaSensorManager.getIntance().initWithContext(context);
        wakeUpReceiver = MetaWakeUpReceiver.getIntance();
        IntentFilter filter = new IntentFilter(ACTION_WAKEUP);
        context.registerReceiver(wakeUpReceiver,filter);
    }
    public static void destory(){
        isInit=false;
        USBUtils.destory();
        context.unregisterReceiver(wakeUpReceiver);
        wakeUpReceiver.destory();
        ThreadPoolUtils.destory();
        context=null;
    }

    public static int getUpdateStatus(){
       return MetaSensorManager.getIntance().getMetaUpdateStatus();
    }
    public static float getAxesHz(){
        return MetaSensorManager.getIntance().getAxesHz();
    }
    public static float getUltrasonicHz(){
        return MetaSensorManager.getIntance().getUltrasonicHz();
    }
    public static int setUltrasonicHz(float ultrasonicHz){
        return MetaSensorManager.getIntance().setUltrasonicHz(ultrasonicHz);
    }
    public static int setAxesHz(float axesHz){
        return MetaSensorManager.getIntance().setAxesHz(axesHz);
    }
    public static int updateFirmwareAsync(byte[] data){
        return MetaSensorManager.getIntance().updateMetaFirmwareAsync(data);
    }
    public static DeviceInfo getDeviceInfo(){
        DeviceInfo info = new DeviceInfo();
        int ret = MetaSensorManager.getIntance().getMetaDeviceInfo(info);
        if(ret!=0){
            info=null;
        }
        return info;
    }
    public static String getLibSensorBatteryData(BatteryData batteryData){
        String ret="";
        ret=MetaSensorManager.getIntance().getLibSensorBatteryData(batteryData);
        return ret;
    }
 public static String getLibSensorAxisData(AxisData axisData){
     String ret="";
     ret=MetaSensorManager.getIntance().getLibSensorAxisData(axisData);
     return ret;
 }
    public  static   String getLibSensorUltrasonicData(UltrasonicData ultrasonicData){
        String ret="";
        ret=MetaSensorManager.getIntance().getLibSensorUltrasonicData(ultrasonicData);
        return ret;
    }
//    public  static   String getLibSensorIoData(IoData ioData){
//        String ret="";
//        ret=MetaSensorManager.getIntance().getLibSensorIoData(ioData);
//        return ret;
//    }
    public static final int SENSOR_LIB_SUCCESS = 0;
    public static    void setLibSensorNaviExtend(final int ori) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = MetaSensorManager.getIntance().setLibSensorNaviExtend(0);//clear beep
                if (ret == SENSOR_LIB_SUCCESS) {
                    Log.e(TAG, "setLibSensorNaviExtend: Successfully clear the BEEP");
                } else {
                    Log.e(TAG, "setLibSensorNaviExtend: Failed to clear the BEEP");
                }
                ret=MetaSensorManager.getIntance().setLibSensorNaviExtend(ori);
                if (ret == SENSOR_LIB_SUCCESS) {
                    Log.e(TAG, "setLibSensorNaviExtend: Successfully  the BEEP   "+ori);
                } else {
                    Log.e(TAG, "setLibSensorNaviExtend: Failed to  the BEEP    "+ori);
                }
            }
        });
    }
    public   static void sendLibSensorGuideCommand(final int cmd){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = MetaSensorManager.getIntance().setLibSensorNaviExtend(0);//clear beep
                if (ret == SENSOR_LIB_SUCCESS) {
                    Log.e(TAG, "sendLibSensorGuideCommand: Successfully clear the BEEP");
                } else {
                    Log.e(TAG, "sendLibSensorGuideCommand: Failed to clear the BEEP");
                }
                ret=MetaSensorManager.getIntance().sendLibSensorGuideCommand(cmd);
                if (ret == SENSOR_LIB_SUCCESS) {
                    Log.e(TAG, "sendLibSensorGuideCommand: Successfully  the BEEP   "+cmd);
                } else {
                    Log.e(TAG, "sendLibSensorGuideCommand: Failed to  the BEEP   "+cmd);
                }
            }
        });

    }
    public static String getIMEI() {
        if(TextUtils.isEmpty(IMEI))
        IMEI = MetaSensorManager.getIntance().getIMEI(context);
        return IMEI;
    }
    //头盔连接监听
    public static void addMetaConnectListener(USBUtils.MetaHotSwapListener listener){
        if(isInit) {
            USBUtils.addListener(listener);
        }else{
             throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void removeMetaConnectListener(USBUtils.MetaHotSwapListener listener){
        if(isInit) {
            USBUtils.removeListener(listener);
        }else{
            throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void addMetaWakeUpListener(MetaWakeUpReceiver.WakeUpListener listener){
        if(isInit) {
            MetaWakeUpReceiver.getIntance().addListener(listener);
        }else{
            throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void removeMetaWakeUpListener(MetaWakeUpReceiver.WakeUpListener listener){
        if(isInit) {
            MetaWakeUpReceiver.getIntance().removeListener(listener);
        }else{
            throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void addMetaListener(MetaSensorManager.BatteryListener listener){
        if(isInit) {
            MetaSensorManager.getIntance().addListener(listener);
        }else{
            throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void removeMetaListener(MetaSensorManager.BatteryListener listener){
        if(isInit) {
            MetaSensorManager.getIntance().removeListener(listener);
        }else{
            throwException("请先初始化HCMetaUtils！");
        }
    }
    public static void setLowHealth(int lowHealth){
        MetaBaseContants.lowHealth=lowHealth;
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                sharedPreferences.edit().putFloat("lowHealth",MetaBaseContants.lowHealth).apply();
            }
        });
    }
    public static void setLowPawer(int lowPawer){
        MetaBaseContants.lowPawer=((float) lowPawer);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                sharedPreferences.edit().putFloat("lowPawer",MetaBaseContants.lowPawer).apply();
            }
        });
    }
    public static int getLowHealth(){
        return (int)MetaBaseContants.lowHealth;
    }
    public static int getLowPawer(){
        return (int)MetaBaseContants.lowPawer;
    }
    public static boolean hasMeta() {
       if(isInit){
           return USBUtils.hasMeta();
       }else{
           throwException("请先初始化HCMetaUtils！");
       }
        return false;
    }
    private static boolean throwException(String msg) {
        throw new IllegalStateException(msg);
    }
}
