package com.example.wqllj.locationshare.view;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TEST", "onReceive: 收到广播"+intent.getAction());
//        if(!isRuningService(context,"com.example.sx.practicalassistant.service.VoiceService")){
//            context.startService(new Intent(context, VoiceService.class));
//        }
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }
    public static boolean isRuningService(Context context, String serviceName) {

        // 校验服务是否还活着

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);

        for (ActivityManager.RunningServiceInfo info : infos) {

            String name = info.service.getClassName();

            if (serviceName.equals(name)) {

                return true;

            }
           }

        return false;

        }
}
