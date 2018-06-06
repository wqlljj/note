package com.cloudminds.hc.hariservice.manager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEvent;
import com.cloudminds.hc.hariservice.manager.SessionEvent;
import com.cloudminds.hc.hariservice.service.HariService;
import com.cloudminds.hc.hariservice.service.HariServiceConnector;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;

import org.webrtc.EglBase;

import javax.net.ssl.SSLContext;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by zoey on 17/4/25.
 */

public class SessionMonitor {

    private static SessionMonitor sessionMonitor;
    private static final String TAG = "HS/SessionMonitor";
    static public  String mSessionId;
    static boolean  isInterrupt = false;    //异常中断 （webrtcclose）
    //reconnect signal channel
    private final long SIGNAL_CHANNEL_RETRY_INTERVAL = 6 *1000;

    private final long CALL_START_TIMEOUT_MILLISECONDS = 60 * 1000;

    //keepAlive
    private static final long KEEP_ALIVE_INTERVAL = 10 * 1000l;
    private static final long KEEP_ALIVE_RESPONSE_TIMEOUT = 10 * 1000l;
    private final int KEEP_ALIVE_TIMEOUT_MAX_RETRIES = 2;
    private int keepAliveRetries = 0;
    private Handler mTimeoutHandler;

    public static SessionMonitor getInstance(){
        if (null == sessionMonitor){
            sessionMonitor = new SessionMonitor();
            sessionMonitor.init();

        }
        return sessionMonitor;
    }

    private void init(){
        if(!EventBus.getDefault().isRegistered(sessionMonitor)){
            EventBus.getDefault().register(sessionMonitor);
        }
        mTimeoutHandler = new Handler();
    }

    private Runnable callingTimeoutCb = new Runnable() {
        @Override
        public void run() {
            //Call timeout
            //close WebRTC
            LogUtils.i(TAG,"Calling time out...");
            CallManager.instance().closeWebRTC();
            triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED,CallEvent.CODE_CONNECT_TIMEOUT ,"Calling timeout"));
        }
    };

    private Runnable keepAliveCb = new Runnable() {
        @Override
        public void run() {
            sendKeepAlive();
            mTimeoutHandler.postDelayed(keepAliveCb,KEEP_ALIVE_INTERVAL);
        }
    };

    private Runnable keepAliveTimeoutCb = new Runnable() {
        @Override
        public void run() {

            if(keepAliveRetries++<=KEEP_ALIVE_TIMEOUT_MAX_RETRIES){
                LogUtils.i(TAG,"Timeout to receive PING response....then retry seconds latter,where retries="+keepAliveRetries);
                triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION, CallEvent.CODE_EXPN_SINGAL_HEARTBEAT_TIMEOUT,CallEvent.CODE_EXPN_SINGAL_HEARTBEAT_TIMEOUT));

            }else {
                if(!isInterrupt)
                    reconnectWithDelay(500);
            }
        }
    };

    /**
     * 开始与信令服务器的心跳
     */
    private void startHeartbeat(){
        mTimeoutHandler.removeCallbacks(keepAliveCb);
        mTimeoutHandler.postDelayed(keepAliveCb,KEEP_ALIVE_INTERVAL);
    }

    /**
     * 停止与信令服务器的心跳
     */
    private void stopHeartbeat(){
        mTimeoutHandler.removeCallbacks(keepAliveCb);
        mTimeoutHandler.removeCallbacks(keepAliveTimeoutCb);
        keepAliveRetries = 0;
    }

    /**
     * 在hariserver中网可用时重连
     */
    public void reconnectWhenNetAvailable(){
        if(!TextUtils.isEmpty(mSessionId)){

           mTimeoutHandler.removeCallbacks(reconnectRunnable);
           mTimeoutHandler.postDelayed(reconnectRunnable,5000);
        }
    }

    private Runnable reconnectRunnable= new Runnable() {
        @Override
        public void run() {
            LogUtils.i(TAG,"Network available with interrupt:"+isInterrupt);
            if (!isInterrupt){
                LogUtils.i(TAG,"Network available ready to reconnect");
                keepAliveRetries = 0;
                reconnectWithDelay(1000);
            } else {
                LogUtils.i(TAG,"Network available ready to restart call");
                mTimeoutHandler.removeCallbacks(reconnectRunnable);
                mTimeoutHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        triggerEvent(new CallEvent(CallEvent.Event.CALL_RESTART, "",""));
                        HariServiceClient.getCallEngine().startCall();
                    }
                },1000);

            }
        }
    };

    public void sendKeepAlive(){
        if (!TextUtils.isEmpty(mSessionId)){
            if(CallManager.instance().canSendHeartbeat()){
                CallManager.instance().sendHeartbeat();

                mTimeoutHandler.postDelayed(keepAliveTimeoutCb,KEEP_ALIVE_RESPONSE_TIMEOUT);
            }
        }
    }


    public static void resetInterrupt(){
        isInterrupt = false;
    }



    private void connect(){
        if (CallManager.instance().canReconnect()) {

            LogUtils.i(TAG,"Reconnect signal channel now...");
            if(TextUtils.isEmpty(mSessionId)){
                if(startFailTime < 0) {
                    startFailTime = System.currentTimeMillis();
                }else if(System.currentTimeMillis()-startFailTime>10000){
                    startFailTime = -1;
                    CallManager.instance().onPeerConnectionError("连接客服失败，请检查网络或稍后重试");
                    return;
                }
            }
            stopHeartbeat();
            CallManager.instance().reconnectSingnalServer();
        }
    }

    long startFailTime=-1l;
    private void reconnectWithDelay(long delay){
        LogUtils.i(TAG,"On reconnectWithDelay entry with delay:"+delay);
        if(CallManager.instance().canReconnect()){
            LogUtils.i(TAG,"Reconnect signal channel when it is closed abnormally or due to other exception");
            //reconnect the channel with delay
            if (delay > 0) {
                mTimeoutHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                }, delay);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connect();
            }
        }
    }


    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }


    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.PostThread)
    public void onSessionEvent(SessionEvent event) {
        SessionEvent.Event eventType = event.getEvent();
        switch (eventType){
            case SESSION_CALL_START: {
                LogUtils.i(TAG,"Received SessionCallStart event");
                //mTimeoutHandler.postDelayed(callingTimeoutCb, CALL_START_TIMEOUT_MILLISECONDS);
                break;
            }

            case SESSION_MC_CONNECT_START:{
                LogUtils.i(TAG,"Received SessionMCConnectStart event");
                mTimeoutHandler.postDelayed(callingTimeoutCb, CALL_START_TIMEOUT_MILLISECONDS);
                break;
            }

            case SESSION_SERVER_CONNECTED:{
                LogUtils.i(TAG,"Received SessionServerConnected event");
                keepAliveRetries = 0;

                if(!TextUtils.isEmpty(mSessionId)) {
                    startHeartbeat();
                }
                break;
            }

            case SESSION_CALL_SESSION_CREATED:{
                LogUtils.i(TAG,"Received SessionCallCreated event");
                break;
            }

            case SESSION_CONNECTED:{
                LogUtils.i(TAG,"Received SessionConnected event");
                isInterrupt = false;
                mTimeoutHandler.removeCallbacks(callingTimeoutCb);
                break;
            }

            case SESSION_HEARTBEAT_RESPONSE:{
                LogUtils.i(TAG,"Received SessionHeartbeatResponse event");
                mTimeoutHandler.removeCallbacks(keepAliveTimeoutCb);
                keepAliveRetries =0 ;
                break;
            }

            case SESSION_DISCONNECTED:{
                LogUtils.i(TAG,"Received SessionDisconnected event");
                break;
            }

            case SESSION_RESTART:{
                LogUtils.i(TAG,"Received SessionRestart event");
                //mTimeoutHandler.postDelayed(callingTimeoutCb, CALL_START_TIMEOUT_MILLISECONDS);
                break;
            }

            case SESSION_CHANNEL_CONNECTION_ERROR:{
                LogUtils.i(TAG,"Received SessionChannelConnectionError event");
                stopHeartbeat();
                mTimeoutHandler.removeCallbacks(reconnectRunnable);
                mTimeoutHandler.postDelayed(reconnectRunnable,SIGNAL_CHANNEL_RETRY_INTERVAL);
                break;
            }

            case SESSION_CHANNEL_CLOSE:{
                LogUtils.i(TAG,"Received SessionChannelClose event");
                stopHeartbeat();
                mTimeoutHandler.removeCallbacks(reconnectRunnable);
                mTimeoutHandler.postDelayed(reconnectRunnable,SIGNAL_CHANNEL_RETRY_INTERVAL);
                break;
            }

            case SESSION_WB_CLOSED:{
                LogUtils.i(TAG,"Received SessionWBClosed event");
                mTimeoutHandler.removeCallbacks(callingTimeoutCb);
                break;
            }

            case SESSION_WS_CLOSED:{
                LogUtils.i(TAG,"Received SessionWSClosed event");
                stopHeartbeat();
                break;
            }
        }
    }

    Runnable reCallRunnable = new Runnable() {
        @Override
        public void run() {
            triggerEvent(new CallEvent(CallEvent.Event.CALL_RESTART,"",""));
            HariService service = HariUtils.getInstance().getHariServiceConnector().getHariService();
            service.getCallManager().startCall(null,null,null,true,CallManager.gDestroyMedia);
        }
    };

    public void webrtcClosed(){
        LogUtils.i(TAG,"On webrtcClosed");
        HariService service = HariUtils.getInstance().getHariServiceConnector().getHariService();
        if (service!=null && service.isNetworkAvailable()){
            if (isInterrupt){
                mTimeoutHandler.postDelayed(reCallRunnable,2000);
            }
        }
    }

}
