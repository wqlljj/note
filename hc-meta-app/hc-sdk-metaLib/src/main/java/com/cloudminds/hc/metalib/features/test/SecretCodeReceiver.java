package com.cloudminds.hc.metalib.features.test;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudminds.hc.metalib.CMUpdaterActivity;


/**
 * Created by willzhang on 09/06/17
 */

public class SecretCodeReceiver extends BroadcastReceiver {

    public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    public static final String CODE_SET_VERSION_TYPE = "68227";
    public static final String CODE_LAUNCH_SYSTEM_UPDATE = "3682";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            try {
                String code = intent.getData().getHost();
                if (CODE_SET_VERSION_TYPE.equals(code)) {
                    Intent secretIntent = new Intent(Intent.ACTION_MAIN);
                    secretIntent.setClass(context, TestActivity.class);
                    secretIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(secretIntent);
                } else if (CODE_LAUNCH_SYSTEM_UPDATE.equals(code)) {
                    Intent launchIntent = new Intent(context, CMUpdaterActivity.class);
                    context.startActivity(launchIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

