package com.cloudminds.hc.metalib.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by willzhang on 14/06/17
 */
public class UIThreadDispatcher {
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void dispatch(Runnable r) {
        mHandler.post(r);
    }

    public static void dispatchDelay(Runnable r, long delay) {
        mHandler.postDelayed(r, delay);
    }
}