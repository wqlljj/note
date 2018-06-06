package com.cloudminds.meta.service;

import android.content.Context;
import android.content.Intent;

/**
 * Created by SX on 2018/1/29.
 */

public class RestartAPPTool {
    /**
     * 重启整个APP
     * @param context
     * @param Delayed 延迟多少毫秒
     */
    public static void restartAPP(Context context,long Delayed){

        /**开启一个新的服务，用来重启本APP*/
        Intent intent1=new Intent(context,KillSelfService.class);
        intent1.putExtra("PackageName",context.getPackageName());
        intent1.putExtra("Delayed",Delayed);
        context.startService(intent1);

        /**杀死整个进程**/
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    /***重启整个APP*/
    public static void restartAPP(Context context){
        restartAPP(context,0);
    }
}
