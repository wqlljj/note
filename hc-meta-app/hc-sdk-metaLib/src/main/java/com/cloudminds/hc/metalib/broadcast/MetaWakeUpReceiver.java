package com.cloudminds.hc.metalib.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudminds.hc.metalib.USBUtils;

import java.util.HashMap;
import java.util.Set;

import static com.cloudminds.hc.metalib.BaseData.ACTION_WAKEUP;

public class MetaWakeUpReceiver extends BroadcastReceiver {

    private static final HashMap<String,WakeUpListener> listeners=new HashMap<>();
    private static MetaWakeUpReceiver metaWakeUpReceiver=null;
    private static String TAG="TEST";
    public MetaWakeUpReceiver() {
//        listeners=new HashMap<>();
    }
    public static MetaWakeUpReceiver getIntance(){
        if(metaWakeUpReceiver==null)
        metaWakeUpReceiver = new MetaWakeUpReceiver();
        return metaWakeUpReceiver;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: "+action );
        switch (action){
            case ACTION_WAKEUP:
                wakeUpMeta();
                break;
        }
    }
    private  void wakeUpMeta(){
        Log.e(TAG, "wakeUpMeta: " );
        if(listeners==null)return;
        Set<String> keySet = listeners.keySet();
        for (String key : keySet) {
            listeners.get(key).wakeUp();
        }
    }
    public void destory(){
        if(listeners!=null) {
            listeners.clear();
        }
    }
    public  void addListener(WakeUpListener listener){
        if(listeners!=null) {
            listeners.put(listener.getClass().getSimpleName(),listener);
        }else{
            throw new IllegalStateException("请先初始化WakeUp");
        }
    }
    public  void removeListener(WakeUpListener listener){
        if(listeners!=null)
            listeners.remove(listener.getClass().getSimpleName());
        else{
            throw new IllegalStateException("请先初始化WakeUp");
        }
    }

    public  interface WakeUpListener{
        void wakeUp();
    }
}
