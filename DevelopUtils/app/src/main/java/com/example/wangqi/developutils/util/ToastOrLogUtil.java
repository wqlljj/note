package com.example.wangqi.developutils.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqi on 2018/5/15.
 */

public class ToastOrLogUtil {
    private static final long MAX_TOAST_INTERVAL = 2500L;
    @SuppressLint({"UseSparseArrays"})
    private static Map<Integer, Long> lastToastTime = new HashMap();
    public static Toast toast;

    public static void cancel()
    {
        if (toast != null) {
            toast.cancel();
        }
    }

    public static void showIsDebug(Context context, int resId)
    {
        long timeMillis = System.currentTimeMillis();
        long lastTime = 0L;
        if (lastToastTime.containsKey(Integer.valueOf(resId))) {
            lastTime = ((Long)lastToastTime.get(Integer.valueOf(resId))).longValue();
        }
        if (timeMillis - lastTime > 2500L)
        {
            lastToastTime.put(Integer.valueOf(resId), timeMillis);
            if (toast != null) {
                toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
            }else{
                toast.setText(resId);
            }
            toast.show();
        }else {
            toast.setText(resId);
            toast.show();
        }
    }

    public static void show(Context context, int resId)
    {
        if (!isDebug) return;
        long timeMillis;
        long lastTime;
            timeMillis = System.currentTimeMillis();
            lastTime = 0L;
            if (lastToastTime.containsKey(Integer.valueOf(resId))) {
                lastTime = ((Long)lastToastTime.get(Integer.valueOf(resId))).longValue();
            }
         if(timeMillis - lastTime > 2500L)
        lastToastTime.put(Integer.valueOf(resId), Long.valueOf(timeMillis));
        if (toast == null) {
            toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        }else{
            toast.setText(resId);
        }
        toast.show();
        return;
    }

    public static void show(Context context, String msg)
    {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }else{
            toast.setText(msg);
        }
        toast.show();
        return;
    }

    public static void showIsDebug(Context context, String msg)
    {
        if (!isDebug) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }else{
            toast.setText(msg);
        }
        toast.show();
        return;
    }

    public static boolean isDebug = true;
    private static final String TAG = "smart";

    // 下面四个是默认tag的函数
    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }
}
