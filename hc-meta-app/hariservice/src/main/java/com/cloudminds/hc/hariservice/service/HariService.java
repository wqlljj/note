package com.cloudminds.hc.hariservice.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import com.cloudminds.hc.hariservice.manager.CallManager;

import com.cloudminds.hc.hariservice.manager.SessionMonitor;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;

import de.greenrobot.event.EventBus;

/**
 * 负责呼叫呼叫服务的启动和重连
 * Created by mas on 16/10/8.
 */
public class HariService extends Service {
   // public HomeFragment homeFragment;
    public boolean isInit=false;

    /**binder*/
    private HariServiceBinder binder = new HariServiceBinder();
    public class HariServiceBinder extends Binder {
        public HariService getService() {
            return HariService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        LogUtils.i("HariService onBind");
        return binder;
    }

    //所有的管理类
    private CallManager callMgr;

    private boolean hasConnectivity = true;

    @Override
    public void onCreate() {
        LogUtils.i("HariService onCreate");
        super.onCreate();
//        if(!EventBus.getDefault().isRegistered(this))
//        EventBus.getDefault().register(this);//, BaseConstants.SERVICE_EVENTBUS_PRIORITY
        Context ctx = getApplicationContext();
        callMgr = CallManager.instance();
        callMgr.initWithService(this);
        hasConnectivity = true;

        registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isInit=true;
    }

    @Override
    public void onDestroy() {
         LogUtils.i("HariService onDestroy");
        // 在这个地方是否执行 stopForeground呐
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        unregisterReceiver(mConnectivityChanged);
        callMgr.destroy();
        super.onDestroy();
    }

    // 负责初始化 每个manager
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         LogUtils.i("HariService onStartCommand");
        //应用开启初始化 下面这几个怎么释放 todo

        return START_STICKY;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LogUtils.i("hariservice#onTaskRemoved");
        // super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    public  boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo;
        netInfo = cm.getActiveNetworkInfo();

        boolean available = netInfo != null && netInfo.isAvailable() ;//&& netInfo.isConnected();
        if(netInfo==null){
            LogUtils.d("Current network state: netinfo is null, which means there is not active network");
        }else{
            LogUtils.d("Current network state: netinfo != null and isAvailable="+netInfo.isAvailable()
                    +", isConnected="+netInfo.isConnected()+", isConnectedOrConnecting="+netInfo.isConnectedOrConnecting()
                    +", isFailover="+netInfo.isFailover()+", isRoaming="+netInfo.isRoaming());
        }
        return  available;

    }

    /**
     * 网络状态改变,有呼叫请求时，网络可用发起重连
     */

    private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d("Network change event received");
            if(callMgr!=null) {
                boolean isNetAvailable = isNetworkAvailable();
                LogUtils.d("Connectivity changed: available=" + isNetAvailable);
                if (isNetAvailable) {
                    hasConnectivity = true;
                    if(callMgr!=null){
                        SessionMonitor.getInstance().reconnectWhenNetAvailable();
                    }
                } else {
                    // if there no connectivity
                    hasConnectivity = false;
                }
            }
        }
    };


    public boolean hasConnectivity() {
        return hasConnectivity;
    }

    public CallManager getCallManager() {
        return callMgr;
    }

}

