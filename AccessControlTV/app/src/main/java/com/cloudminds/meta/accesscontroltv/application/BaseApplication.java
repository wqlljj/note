package com.cloudminds.meta.accesscontroltv.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.cloudminds.meta.accesscontroltv.http.HttpClient;
import com.cloudminds.meta.accesscontroltv.util.CrashHandler;
import com.cloudminds.meta.accesscontroltv.util.SharePreferenceUtils;
import com.cloudminds.meta.accesscontroltv.util.ThreadPoolUtils;
import com.cloudminds.meta.accesscontroltv.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by WQ on 2018/4/10.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharePreferenceUtils.setContext(this.getApplicationContext());
        ThreadPoolUtils.init();
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                //        CrashReport.initCrashReport(getApplicationContext(), "1b0be65288", false);
                Context context = getApplicationContext();
// 获取当前包名
                String packageName = context.getPackageName();
// 获取当前进程名
                String processName = getProcessName(android.os.Process.myPid());
// 设置是否为上报进程
                CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
                strategy.setUploadProcess(processName == null || processName.equals(packageName));
// 初始化Bugly

//		第三个参数为SDK调试模式开关，调试模式的行为特性如下：
//		•输出详细的Bugly SDK的Log；
//		•每一条Crash都会被立即上报；
//		•自定义日志将会在Logcat中输出。
                //建议在测试阶段建议设置成true，发布时设置为false
                CrashReport.initCrashReport(context, "facf87b2cd", true, strategy);
            }
        });
//        CrashHandler.getInstance().initCrashHandler(this);
        HttpClient.init(this);
        Utils.init(this);
    }

    //	其中获取进程名的方法“getProcessName”有多种实现方法，推荐方法如下：
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
