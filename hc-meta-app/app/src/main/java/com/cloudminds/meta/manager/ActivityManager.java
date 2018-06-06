package com.cloudminds.meta.manager;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by WQ on 2018/3/13.
 */

public class ActivityManager {
    private static Class<?> currentActivity=null;
    private static String TAG="META/ActivityManager";
    public static Class<?> getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Class<?> currentActivity) {
        Log.e(TAG, "setCurrentActivity: "+(currentActivity==null?"null":currentActivity.getName()) );
        if(currentActivity!=null) {
            ActivityManager.currentActivity = currentActivity;
        }
    }
    public static void removeCurrentActivity(Class<?> currentActivity) {
        Log.e(TAG, "removeCurrentActivity: "+currentActivity.getName() );
        if(ActivityManager.currentActivity!=null&&ActivityManager.currentActivity.getName().equals(currentActivity.getName())) {
            ActivityManager.currentActivity = null;
        }
    }
    public static void clear() {
        Log.e(TAG, "clear: " );
        ActivityManager.currentActivity = null;
    }
}
