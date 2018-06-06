package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.util.SharePreferenceUtils;

import java.util.HashMap;
import java.util.Set;

public class SysShutDownReceiver extends BroadcastReceiver {
    static HashMap<String,SysShutDownListener> listeners=new HashMap<>();
    private String TAG="M/A/SysShutDownReceiver";
    public static void addSysShutDownListener(SysShutDownListener listener){
        listeners.put(listener.getClass().getSimpleName(),listener);
    }
    public static void removeSysShutDownListener(SysShutDownListener listener){
        listeners.remove(listener.getClass().getSimpleName());
    }
    //android.intent.action.ACTION_SHUTDOWN
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: "+intent.getAction() );
        Set<String> keySet = listeners.keySet();
        for (String key : keySet) {
            listeners.get(key).sysShutDown();
        }
    }
    public interface SysShutDownListener{
        void sysShutDown();
    }
}
