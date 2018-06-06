package com.cloudminds.meta.service.asr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.cloudminds.meta.util.LogUtils;

/**
 * Created by zoey on 17/5/16.
 */

public class AsrServiceConnector {

    private final String TAG = "AsrServiceConnector";
    private AsrService asrService = null;
    public AsrService getAsrService() {return asrService;}
    public  AsrEventListener asrEventListener;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG,"vreg#asrService connected");
            if (asrService == null) {
                AsrService.AsrServiceBinder binder = (AsrService.AsrServiceBinder)iBinder;
                asrService = binder.getService();

                if (asrService == null) {
                    Log.e("TEST","vreg#get asrService failed");
                } else {
                    asrService.setAsrEventListener(asrEventListener);
                    Log.e("Test","vreg#get asrService ok");
                    //start().setVoiceRecogitionEvents(vEvents);
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.i(TAG,"vreg#asrService disconnected");
        }
    };

    public ComponentName start(Context ctx) {
        Intent intent = new Intent();
        intent.setClass(ctx, AsrService.class);

        return ctx.startService(intent);
    }

    public boolean connect(Context ctx) {
        return bindService(ctx);
    }

    public boolean bindService(Context ctx) {
        LogUtils.d(TAG,"vreg#bindService");

        Intent intent = new Intent();
        intent.setClass(ctx, AsrService.class);

        if (!ctx.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)) {
            LogUtils.e(TAG,"vreg#bindService(asrService) failed");
            return false;
        } else {
            LogUtils.i(TAG,"vreg#bindService(asrService) ok");
            return true;
        }
    }

    public void  unbindService(Context ctx) {
        LogUtils.d(TAG,"vreg#unBindService");

        try {
            ctx.unbindService(mServiceConnection);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG,"vreg#got exception becuase of unmatched bind/unbind, we sould place to onStop next version.e:"+exception.getMessage());
        }
        LogUtils.i(TAG,"vreg#unbindservice ok");
    }
}
