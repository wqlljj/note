package com.cloudminds.hc.metalib.features.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudminds.hc.metalib.config.Config;


public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UpdaterScheduler.scheduleDailyChecking(context);
        boolean needWipeData = Config.getInstance().needWipeDataOnNextReboot(context);
        if (needWipeData) {
            Config.getInstance().setWipeDataOnNextReboot(context, false);
            Intent clearIntent = new Intent("android.intent.action.MASTER_CLEAR");
            clearIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            clearIntent.putExtra("android.intent.extra.REASON", "Wipe Data");
            context.sendBroadcast(clearIntent);
        }
    }
}
