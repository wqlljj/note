package com.cloudminds.register;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created
 */

public class BasicApp extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static boolean isFirst = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //Bugly
        Bugly.init(getApplicationContext(), "6fcaaecbed", true);
        //Stetho
        Stetho.initializeWithDefaults(this);
        //LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    public static Context getContext() {
        return mContext;
    }

}