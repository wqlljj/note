package com.cloudminds.hc.hariservice.utils.Log;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.log4j.Level;

import java.io.File;
import java.util.Date;
import java.util.List;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by zoey on 2017/11/21.
 */

public class ConfigureLog4j {

    private static final int MAX_FILE_SIZE = 1024 * 1024 * 5;
    private static final int MAX_FILE_NUMBER = 5;
    private static final String DEFAULT_LOG_DIR = "//HS//Log//";
    private static final String DEFAULT_LOG_FILE_NAME = "hs.log";
    private static final String TAG = "Log4jConfigure";
    private static Context curContext = null;
    private static boolean configured = false;

    public static void configure(String fileName) {
        if (configured) return;
        configured = true;
        final LogConfigurator logConfigurator = new LogConfigurator();
        Date nowtime = new Date();
        //日志文件路径地址:SD卡下/HS/Log/
        String fileFullName = Environment.getExternalStorageDirectory()
                + DEFAULT_LOG_DIR+getAppProcessName()+"_"+fileName;
        //设置文件名
        logConfigurator.setFileName(fileFullName);
        //设置root日志输出级别 默认为DEBUG
        logConfigurator.setRootLevel(Level.DEBUG);
        // 设置日志输出级别
        logConfigurator.setLevel("org.apache", Level.INFO);
        //设置 输出到日志文件的文字格式 默认 %d %-5p [%c{2}]-[%L] %m%n
        logConfigurator.setFilePattern("%d %-5p [%c{2}] %m%n");
        //设置输出到控制台的文字格式 默认%m%n
        logConfigurator.setLogCatPattern("%m%n");
        //设置总文件大小
        logConfigurator.setMaxFileSize(MAX_FILE_SIZE);
        //设置最大产生的文件个数
        logConfigurator.setMaxBackupSize(MAX_FILE_NUMBER);
        //设置所有消息是否被立刻输出 默认为true,false 不输出
        logConfigurator.setImmediateFlush(true);
        //是否本地控制台打印输出 默认为true ，false不输出
        logConfigurator.setUseLogCatAppender(true);
        //设置是否启用文件附加,默认为true。false为覆盖文件
        logConfigurator.setUseFileAppender(true);
        //设置是否重置配置文件，默认为true
        logConfigurator.setResetConfiguration(true);
        //是否显示内部初始化日志,默认为false
        logConfigurator.setInternalDebugging(false);

        logConfigurator.configure();

    }

    public static void configure(Context context) {
        curContext = context;
        configure(DEFAULT_LOG_FILE_NAME);
    }

    private static boolean isSdcardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取当前应用程序的包名
     */
    public static String getAppProcessName() {
//当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager)curContext.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

}
