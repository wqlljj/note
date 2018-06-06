package com.cloudminds.meta.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.meta.aidl.IHubReceiver;
import com.cloudminds.meta.aidl.IHubService;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.broadcast.BatteryReceiver;
import com.cloudminds.meta.presenter.ICallState;
import com.cloudminds.meta.service.HubService;
import com.cloudminds.meta.service.HubServiceConnector;
import com.cloudminds.meta.service.navigation.IndoorNavigator;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.LogUtils;

/**
 * Created by tiger on 17-4-10.
 */

public class HubModel {

    public static final String TAG = "Meta:HubModel";

    private Context mContext;
    private HubServiceConnector hubServiceConnector;

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    private int mState;
    private ICallState mICallBack;

    public HubModel(Context context, ICallState back) {
        this.mContext = context;
        this.mICallBack = back;
    }

    public boolean getUsbState(){
        return HCMetaUtils.hasMeta();
    }

    public boolean isCurrentMeta(){
        return true;
    }


    public void initHCMeta(){
        HCMetaUtils.addMetaConnectListener((USBUtils.MetaHotSwapListener)mContext);
    }

    public void startHubService(){
//        LogUtils.d(TAG,"startHubService");
//        Intent intent = new Intent(mContext, HubService.class);
//        mContext.startService(intent);
//        mContext.bindService(intent,conn,0);
         hubServiceConnector = HubServiceConnector.getIntance(mContext);
//        hubServiceConnector.startHubService(mContext);
        hubServiceConnector.setICallBack(mICallBack);
    }

//    private IHubReceiver mReceiver = new IHubReceiver.Stub(){
//
//        @Override
//        public void callResult(int state) throws RemoteException {
//            LogUtils.d(TAG,"IHubReceiver state = "+state);
//            mState = state;
//            mICallBack.callBack(mState);
//        }
//
//        @Override
//        public void onSpeak(String content) throws RemoteException {
//            mICallBack.onSpeak(content);
//        }
//
//        @Override
//        public void onInfo(String info) throws RemoteException {
//            mICallBack.onInfo(info);
//        }
//    };

//    private ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            LogUtils.d(TAG,"onServiceConnected");
//            mService = IHubService.Stub.asInterface(service);
//            try {
//                mService.setCallStateListener(mReceiver);
//            } catch (RemoteException e) {
//                LogUtils.d(TAG,"service is error "+e);
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            LogUtils.d(TAG,"onServiceDisconnected");
//        }
//    };

    public void onDestroy(){
//        hubServiceConnector.destory();
//        if(conn != null )
//            mContext.unbindService(conn);
    }

    public void callStop(){
        IndoorNavigator.sendStopNavi(IndoorNavigator.Type.END_NAVI,"已到达目的地，结束导航！");
        OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_USER_STOP);
        hubServiceConnector.callStop();
    }

    public void sendMessage(String msg){
        hubServiceConnector.sendMessage(msg);
    }
    private final int CALLDELAY=1001;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CALLDELAY:
                    callStart((CallEngine.Callee) msg.obj);
                    break;
            }
        }
    };
    public void callStart(CallEngine.Callee callee){
        Log.e(TAG, "callStart: "+callee );
        if(!MetaApplication.hariServiceInitialized){
            Message message = new Message();
            message.what=CALLDELAY;
            message.obj=callee;
//            handler.sendEmptyMessageDelayed(1001,1000);
            handler.sendMessageDelayed(message,1000);
            return;
        }
        hubServiceConnector.callStart(callee);
    }
}
