package com.cloudminds.hc.hariservice.service;

/**
 * Created by mas on 16/10/8.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.cloudminds.hc.hariservice.utils.Log.LogUtils;


/**
 * HariService绑定
 * 1. 供上层使用【activity】
 * 同层次的manager没有必要使用。
 */
public  class HariServiceConnector {

    public  void onHariServiceConnected(){}
    public  void onServiceDisconnected(){}
    private HariService hariService = null;
    public HariService getHariService() {
        return hariService;
    }

    // todo eric when to release?
    private ServiceConnection imServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.i("onService(imService)Disconnected");
            HariServiceConnector.this.onServiceDisconnected();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TEST","im#onService(imService)Connected");

            if (hariService == null) {
                HariService.HariServiceBinder binder = (HariService.HariServiceBinder) service;
                hariService = binder.getService();
//                hariService.homeFragment=homeFragment;
                if (hariService == null) {
                   Log.i("TEST","im#get imService failed");
                    return;
                }
                Log.i("Test","im#get imService ok");
            }
            HariServiceConnector.this.onHariServiceConnected();
        }
    };

    public boolean connect(Context ctx) {
        return bindService(ctx);
//        return true;
    }

    public void disconnect(Context ctx) {
        LogUtils.i("im#disconnect");
        unbindService(ctx);
        HariServiceConnector.this.onServiceDisconnected();
    }

    public boolean bindService(Context ctx) {
        LogUtils.i("im#bindService");

        Intent intent = new Intent();
        intent.setClass(ctx, HariService.class);

        if (!ctx.bindService(intent, imServiceConnection, Context.BIND_AUTO_CREATE)) {
            LogUtils.i("im#bindService(imService) failed");
            return false;
        } else {
            LogUtils.i("im#bindService(imService) ok");
            return true;
        }
//        return true;
    }

    public void unbindService(Context ctx) {
        try {
            // todo eric .check the return value .check the right place to call it
            ctx.unbindService(imServiceConnection);
        } catch (IllegalArgumentException exception) {
            LogUtils.w("im#got exception becuase of unmatched bind/unbind, we sould place to onStop next version.e:%s", exception.getMessage());
        }
        LogUtils.i("unbindservice ok");
    }

}
