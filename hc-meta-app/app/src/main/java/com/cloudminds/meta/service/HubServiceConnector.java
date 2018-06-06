package com.cloudminds.meta.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.meta.aidl.IHubReceiver;
import com.cloudminds.meta.aidl.IHubService;
import com.cloudminds.meta.presenter.ICallState;
import com.cloudminds.meta.util.LogUtils;

/**
 * Created by SX on 2017/5/17.
 */

public class HubServiceConnector {
    private  ICallState mICallback;
    private  String TAG="HubServiceConnector";
    private static HubServiceConnector hubServiceConnector;
    private  Context mContext;

    public HubServiceConnector(Context mContext) {
        this.mContext = mContext;
    }

    public static HubServiceConnector getIntance(Context mContext){
        if(hubServiceConnector==null)
        hubServiceConnector = new HubServiceConnector(mContext);
        return hubServiceConnector;
    }
    public  void startHubService(Context mContext){
        if(mService!=null){
            Log.e(TAG, "startHubService: 已绑定HubService" );
            return;
        }
        LogUtils.d(TAG,"startHubService");
        Intent intent = new Intent(mContext, HubService.class);
        mContext.startService(intent);
        mContext.bindService(intent,conn,0);
        this.mContext = mContext;
    }
    public void destory(){
        mContext.unbindService(conn);
    }
    private IHubService mService;
    public static boolean isStartService=false;
    private  ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected");
            mService = IHubService.Stub.asInterface(service);
            isStartService=true;
            try {
                mService.setCallStateListener(mReceiver);
            } catch (RemoteException e) {
                LogUtils.d(TAG, "service is error " + e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG,"onServiceDisconnected");
        }
    };
    public  void setICallBack(ICallState mICallback){
        hubServiceConnector.mICallback = mICallback;
    }
    private IHubReceiver mReceiver = new IHubReceiver.Stub(){

        @Override
        public void callResult(int state,String message) throws RemoteException {
            LogUtils.d(TAG,"IHubReceiver state = "+state);
            if(mICallback!=null)
            mICallback.callBack(state,message);
        }



    };

    public void callStart(CallEngine.Callee callee) {
        try {
            mService.callStart(callee.ordinal());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void sendData(String data){
        try {
            mService.sendData(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String msg) {
        try {
            mService.sendMessage(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void callStop() {
        try {
            mService.callStop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
