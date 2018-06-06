package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudminds.meta.service.HubService;

/**
 * Created by tiger on 17-4-18.
 */

public class StartUpHubServiceBroadcast extends BroadcastReceiver {

    public static final String TAG = "Meta:StartUpHubServiceBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"onReceive Action = "+intent.getAction());
        Intent service = new Intent(context, HubService.class);
        context.startService(service);
    }
}
