package com.cloudminds.meta.accesscontroltv.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by WQ on 2018/4/24.
 */

public class USBBroadcastReceiver extends BroadcastReceiver {
    static HashMap<String ,OnUSBListener> listenerList=new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("TAG", "action === " + intent.getAction());

        if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {//U盘插入

            String path = intent.getDataString();

            String pathString = path.split("file://")[1];//U盘路径
            Toast.makeText(context, "USB路径："+pathString, Toast.LENGTH_SHORT).show();
//            File file = new File(pathString);
//            String[] list = file.list();
//            for (String s : list) {
//                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
//            }
            for (String s : listenerList.keySet()) {
                listenerList.get(s).pullIn(pathString);
            }
        }else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")) {//U盘拔出
            for (String s : listenerList.keySet()) {
                listenerList.get(s).pullOut();
            }
            Toast.makeText(context, "USB拔出 "+ (TextUtils.isEmpty(intent.getDataString())?"null":intent.getDataString()), Toast.LENGTH_SHORT).show();
        }

    }
    public static void addUSBListener(OnUSBListener listener){
        listenerList.put(listener.getClass().getSimpleName(),listener);
    }
    public static void removeUSBListener(OnUSBListener listener){
        listenerList.remove(listener.getClass().getSimpleName());
    }
    public interface OnUSBListener{
        void pullIn(String filepath);
        void pullOut();
    }
}
