package com.cloudminds.meta.accesscontroltv.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqi on 2018/5/15.
 */

public class ToastUtil {
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

    public static void show(Context paramContext, int resId)
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
                toast = Toast.makeText(paramContext, resId, Toast.LENGTH_SHORT);
            }else{
                toast.setText(resId);
            }
            toast.show();
        }else {
            toast.setText(resId);
            toast.show();
        }
    }

    public static void show(Context paramContext, int resId, boolean isDebug)
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
            toast = Toast.makeText(paramContext, resId, Toast.LENGTH_SHORT);
        }else{
            toast.setText(resId);
        }
        toast.show();
        return;
    }

    public static void show(Context paramContext, String paramString)
    {
        if (toast == null) {
            toast = Toast.makeText(paramContext, paramString, Toast.LENGTH_SHORT);
        }else{
            toast.setText(paramString);
        }
        toast.show();
        return;
    }

    public static void show(Context paramContext, String paramString, boolean paramBoolean)
    {
        if (!paramBoolean) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(paramContext, paramString, Toast.LENGTH_SHORT);
        }else{
            toast.setText(paramString);
        }
        toast.show();
        return;
    }
}
