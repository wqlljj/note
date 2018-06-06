package com.cloudminds.hc.metalib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by willzhang on 16/06/17
 */

public final class ToastUtil {

    private static final long MAX_TOAST_INTERVAL =2500;
    public static Toast toast;
    @SuppressLint("UseSparseArrays")
    private static Map<Integer, Long> lastToastTime = new HashMap<>();

    public static void show(Context mContext, int resId) {
        long currentTime = System.currentTimeMillis();
        long lastTime = 0;
        if (lastToastTime.containsKey(resId)) {
            lastTime = lastToastTime.get(resId);
        }
        long delta = currentTime - lastTime;
        if (delta > MAX_TOAST_INTERVAL) { // 同一个toast 2.5秒之内只显示一次
            lastToastTime.put(resId, currentTime);
            if(toast==null) {
                toast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
            }else{
                toast.setText(resId);
            }
            toast.show();
        }
    }
    public static void show(Context mContext, String text){
            if(toast==null) {
                toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            }else{
                toast.setText(text);
            }
            toast.show();
    }
    public static void cancel(){
        if(toast!=null) {
            toast.cancel();
        }
    }
}
