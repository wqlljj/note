package com.cloudminds.meta.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.HubActivity;
import com.cloudminds.meta.activity.IHubView;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.broadcast.SysShutDownReceiver;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.model.HubModel;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.LogUtils;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-10.
 */

public class HubPresenter implements ICallState, SysShutDownReceiver.SysShutDownListener {

    public static final String TAG = "Meta:HubPresenter";
    private IHubView mView;
    private HubModel mModel;
    private long mLastTime=0;
    public int state;

    public HubPresenter(IHubView view){
        this.mView = view;
        mModel = new HubModel((Context)mView,this);
        mModel.setState(MetaApplication.state);
        EventBus.getDefault().register(this);
    }
    public boolean checkUsb(){
        Log.e(TAG, "checkUsb: MetaApplication.state="+ (MetaApplication.state==Constant.HUB_CONN_IN_CONNECTION||MetaApplication.state==Constant.HUB_CONN_ON_CONNECTION)+"  "+mModel.getUsbState());
        ((HubActivity) mView).runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mView.setStateByUsb((MetaApplication.state==Constant.HUB_CONN_IN_CONNECTION||MetaApplication.state==Constant.HUB_CONN_ON_CONNECTION)?true:mModel.getUsbState());
            }
        });
        return mModel.getUsbState();
    }

    public void checkCurrentMeta(){
        mView.setStateByCurrentMeta(mModel.isCurrentMeta());
    }

    public void unbindMeta(){

    }

    private Handler mHandler = new Handler(){
        int i=0;
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            String speak;
            HubActivity mView = (HubActivity) HubPresenter.this.mView;
            switch (msg.what){
                case Constant.HUB_SINGLE_CLICK:
                    switch (mModel.getState()){
                        case Constant.HUB_CONN_NOMAL:
                        case Constant.HUB_CONN_END:
                            speak = mView.getString(R.string.hub_tts_call);
                            sendSpeak(speak);
                            break;
                        case Constant.HUB_CONN_ON_CONNECTION:
                            mModel.callStop();
                            break;
                        case Constant.HUB_CONN_IN_CONNECTION://call back
                            speak = mView.getString(R.string.hub_tts_disconnection);
                            sendSpeak(speak);
                            break;
                    }
                    break;
                case Constant.HUB_DOUBLE_CLICK:
                    switch (mModel.getState()){
                        case Constant.HUB_CONN_NOMAL:
                        case Constant.HUB_CONN_END:
                        case Constant.CALL_FAILED:
                            mModel.callStart(mView.callCallee);
                            break;
                        case Constant.HUB_CONN_IN_CONNECTION:
                        case Constant.HUB_CONN_ON_CONNECTION:
                            mModel.callStop();
                            sendSpeak(mView.getString(R.string.hub_tts_end));
                            break;
                    }
                    break;
                case Constant.SINGLE_CLICK_BACK:
                    LogUtils.e(TAG, "handleMessage: 单击back" );
                    Toast.makeText(mView, ((String) msg.obj), Toast.LENGTH_SHORT).show();
                    sendSpeak(((String) msg.obj));
                    break;
                case Constant.DOUBLE_CLICK_BACK:
                    LogUtils.e(TAG, "handleMessage: 双击back" );
                    Toast.makeText(mView, ((String) msg.obj), Toast.LENGTH_SHORT).show();
                    sendSpeak(((String) msg.obj));
                    ActivityManager.removeCurrentActivity(mView.getClass());
                    if(isCall()&&mView!=null){
                        mView.moveTaskToBack(true);
                    }else if(mView!=null){
                        mView.moveTaskToBack(false);
                        mView.finish();
                    }
                    EventBus.getDefault().post(new BusEvent(BusEvent.Event.MOVETASKTOBACK));
                    break;
            }
            mLastTime=keybackTime;
            keybackTime=0;
        }
    };

    public void init(){
        SysShutDownReceiver.addSysShutDownListener(this);
        mModel.initHCMeta();
        mModel.startHubService();
    }
    public int getState(){
        return mModel.getState();
    }
    public void sendMessage(String msg){
        mModel.sendMessage(msg);
    }
    public void doNext(){
        Log.d(TAG,"do Next");
        HubActivity mView = (HubActivity) this.mView;
        switch (mModel.getState()){
            case Constant.HUB_CONN_NOMAL:
            case Constant.HUB_CONN_END:
            case Constant.CALL_FAILED:
            case Constant.CALL_CLOSED:
                mModel.callStart(mView.callCallee);
                break;
            case Constant.HUB_CONN_IN_CONNECTION:
            case Constant.HUB_CONN_ON_CONNECTION:
                sendSpeak(((HubActivity) HubPresenter.this.mView).getString(R.string.hub_tts_end));

                mModel.callStop();
                break;
        }


    }
    public void sendSpeak(String message){
        TTSSpeaker.speak(message, TTSSpeaker.HIGH);
    }

    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void callStart_Stop(String event){
        switch (event){
            case Constant.CALLSTART:
                mHandler.sendEmptyMessage(Constant.HUB_DOUBLE_CLICK);
                break;
            case Constant.CALLEND:
                mHandler.sendEmptyMessage(Constant.HUB_DOUBLE_CLICK);
                break;
        }
    }
    public void onDestroy(){
        mModel.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    public void updateState( int state){
        mModel.setState(state);
        this.state = state;
    }

    @Override
    public void callBack(final int state, final String message) {
        Log.e(TAG, "callBack: "+state );
        mModel.setState(state);
        this.state = state;
        if(mView != null)
            ((HubActivity) mView).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mView.setUIState(state,message);
                }
            });
    }
    private long keybackTime = 0;
    public boolean keyback() {
        LogUtils.d(TAG,"keyback");
        HubActivity mView = (HubActivity) this.mView;
        Message message = new Message();
        if(System.currentTimeMillis()-mLastTime>1500) {
            if (keybackTime != 0 && (System.currentTimeMillis() - keybackTime) < 1000) {
                mHandler.removeMessages(Constant.SINGLE_CLICK_BACK);
                message.what = Constant.DOUBLE_CLICK_BACK;
                message.obj = isCall() ? mView.getString(R.string.to_back) : mView.getString(R.string.exit);
                mHandler.sendMessage(message);
            } else {
                message.what = Constant.SINGLE_CLICK_BACK;
                message.obj = isCall() ? mView.getString(R.string.again_to_back) : mView.getString(R.string.again_exit);
                mHandler.sendMessageDelayed(message, 1000);
                keybackTime = System.currentTimeMillis();
            }
        }
            return true;
        }
    public boolean isCall() {
        switch (mModel.getState()){
            case Constant.HUB_CONN_IN_CONNECTION:
            case Constant.HUB_CONN_ON_CONNECTION:
                return true;
        }
        return false;
    }

    @Override
    public void sysShutDown() {
        if(isCall()) {
            mModel.callStop();
            SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
        }
    }
}
