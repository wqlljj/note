package com.cloudminds.hc.hariservice.manager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPData;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPEntity;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.command.CDGenerator;
import com.cloudminds.hc.hariservice.command.CDParser;
import com.cloudminds.hc.hariservice.manager.http.FetchServerCallBack;
import com.cloudminds.hc.hariservice.manager.http.HsConfigFetcher;
import com.cloudminds.hc.hariservice.manager.http.HsServerFetcher;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.call.CallEvent;
import com.cloudminds.hc.hariservice.command.CmdEvent;
import com.cloudminds.hc.hariservice.service.HariService;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.hariservice.webrtc.AppRTCAudioManager;
import com.cloudminds.hc.hariservice.webrtc.AppRTCClient;
import com.cloudminds.hc.hariservice.webrtc.CallStatsHelper;
import com.cloudminds.hc.hariservice.webrtc.LooperExecutor;
import com.cloudminds.hc.hariservice.webrtc.PeerConnectionClient;
import com.cloudminds.hc.hariservice.webrtc.WebSocketRTCClient;

import org.apache.log4j.pattern.LogEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.NetworkMonitor;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by zoey on 17/4/17.
 */

/**
 * 呼叫会话管理
 */
public class CallManager implements  AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents  {
    private static final String TAG = "HS/CallManager";
    private static CallManager inst = new CallManager();
    private static MediaPlayer mMediaPlayer;
    private static AudioManager audioManager;

    public static CallManager instance() {
        return inst;
    }
    public static String statReports = "";
    private static String hsServer = "";
    private static String hsPort = "";
    private static int NET_DELAY_WARNING_TIMEINTERVAL = 30*1000; //webrtc delay告警时间间隔
    private static int NET_DELAY_THRESHOLD = 400;
    private static int WEBRTC_LOW_BITRATE_WARNING_TIMEINTERVAL = 10*1000; //webrtc 带宽过低告警时间间隔

    public CallManager() {
    }

    private static final String PCC_CREATE_ERROR = "PeerConnectionClient create fail";

    private AppRTCClient appRtcClient;
    private long callStartedTimeMs = 0;

    private PeerConnectionClient mPeerConnectionClient;
    private AppRTCAudioManager mAudioManager = null;

    private AppRTCClient.AssistantConnectionParameters assistantConnectionParameters;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private String mRemoteSDP;


    //allocated by server
    private String mSessionId;
    private HariService mService;
    private boolean naviStarted;

    //    private CallKeepAliveTimer keepAliveTimer;
    public static long interruptTime=-1l;


    private enum CALL_STATE
    {
        PRE_STOP,      //ready to stop
        STOPPED,       // initial status
        CALLING,       // call is started
        WS_CONNECTED,  //signal is connected
        RINGING,       // call ringing on callee side
        ACCEPT,        // call accepted
        MC_CONNECTED, //media channel connected
        UNKNOWN
    }
    private CALL_STATE mCallState = CALL_STATE.STOPPED;

    //    private boolean mAcceptCandidate = false;
    private List<IceCandidate> mCachedLocalCandidates =new ArrayList<>();
    private Object candidateListLock = new Object();
    //    boolean mIsVideoEnabled = true;
    private boolean hasSdpOfferReceived = false;
    private boolean shallRestartICE = false;
    private static boolean isDefaultConfigFetched = true;

    //Constant for wakelock
    static public final String PING_WAKELOCK = "com.cloudminds.robot.callmanager";
    private PowerManager.WakeLock wakelock;
    private enum SIGNAL_EVENT
    {
        ON_SIGNAL_SERVER_CONNECTED,
        CLOSE_WEBRTC,
        RESTART_WEBRTC,
    }

    public void initWithService(HariService service) {
        mService = service;

        if (null ==mAudioManager) {
            mAudioManager = AppRTCAudioManager.create(mService.getApplicationContext(), new Runnable() {
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            LogUtils.i(TAG,"Initializing the WebRTC audio manager...");
            mAudioManager.init();
        }

        PowerManager pm = (PowerManager)mService
                .getSystemService(Service.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                PING_WAKELOCK);

        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().register(inst);
        }

        SessionMonitor.getInstance();
    }

    public Service getService(){
        return mService;
    }



    public PeerConnectionClient getPeerConnection(){

       return  mPeerConnectionClient;
    }

    public boolean isSignalChannelConnected(){
        boolean isConnected = false;
        if (null != appRtcClient){
            isConnected = appRtcClient.isWsConnected();
        }

        return isConnected;
    }

    public static class SettingRecceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("com.cloudminds.manager.CallManager.videoSettingReceiver")){
            }
        }

    }

    public void destroy() {
        if (mAudioManager != null) {
            mAudioManager.close();
            mAudioManager = null;
        }
        LogUtils.i(TAG, "Destory entry");
        if(EventBus.getDefault().isRegistered(inst)) {
            EventBus.getDefault().unregister(inst);
        }
        closeWebSocket();
        closeWebRTC();
    }

    /**
     * 自身的事件驱动
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public void startCall(EglBase base, SurfaceViewRenderer localRender,
                          final SurfaceViewRenderer remoteRender, boolean isVideoEnabled, final int destroyMedia) {
        LogUtils.i(TAG,"startCall entry");

        if (mCallState != CALL_STATE.STOPPED) {
            LogUtils.i(TAG,"Ignore to start call when in calling state :"+mCallState);
            return;
        }

        String imei = HariUtils.getIMEI();
        if (null == imei){
            triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED,CallEvent.CODE_NO_PERMSSION_RPS, "Requires READ_PHONE_STATE"));
            LogUtils.i(TAG,"Ignore to start call when can't read the device's imei");
            return;
        }

        if (null == this.rootEglBase){
            this.rootEglBase = EglBase.create();
        }

        this.localRender = localRender;
        this.remoteRender = remoteRender;

        channelCloseWarned = false;

        if (isDefaultConfigFetched){
            startCall(destroyMedia);
        } else {
            fetchDefaultConfig();
        }

        //获取switch地址
       /*
        if (hsServer.isEmpty()){
            LogUtils.d(TAG,"Begin fetch hs switch address...");
            HsServerFetcher serverFetcher = new HsServerFetcher();
            serverFetcher.setCallBack(new FetchServerCallBack() {
                @Override
                public void onSuccess(JSONObject result) {
                    LogUtils.d(TAG,"Fetch hs switch address success");
                    try {
                        hsServer = result.getString("host");
                        hsPort = result.getString("wsPort");
                        startCall(destroyMedia);
                    }catch (JSONException e){
                        triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED, "Parse HS Server failure!"));
                    }
                }

                @Override
                public void onFailure(String error) {
                    LogUtils.d(TAG,"Fetch hs switch address failure:"+error);

                    triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED, "Fetch HS Server failure!"));
                }
            });
            String server = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, BaseConstants.SERVER_ADDRESS);
            String port = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_PORT,BaseConstants.SERVER_PORT);
            serverFetcher.fetchServerFromUrl(server+":"+port);
        } else {
            startCall(destroyMedia);
        }
        */

    }

    private void fetchDefaultConfig(){
        LogUtils.i(TAG,"Begin fetch default config...");
        HsConfigFetcher configFetcher = new HsConfigFetcher();
        configFetcher.setCallBack(new HsConfigFetcher.FetchConfigCallBack() {
            @Override
            public void onSuccess() {
                LogUtils.i(TAG,"Fetch default config successful!");
                isDefaultConfigFetched = true;
                startCall(gDestroyMedia);
            }

            @Override
            public void onFailure(String error) {
                LogUtils.i(TAG,"Fetch default config failed");
            }
        });
        configFetcher.fetchConfigWithAccount(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298"));
    }

    private void startCall(int destroyMedia){

        int videoFps = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_FPS,BaseConstants.DEFAULT_VIDEO_FPS);
        int videoBps = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_STARTBPS,BaseConstants.DEFAULT_VIDEO_STARTBPS);
        int audioBps = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_AUDIO_STARTBPS,BaseConstants.DEFAULT_AUDIO_STARTBPS);
        String audioCodec = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_AUDIO_CODEC,BaseConstants.DEFAULT_AUDIO_CODEC);
        String videoCodec = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_VIDEO_CODEC,BaseConstants.DEFAULT_VIDEO_CODEC);
        int videoWidth = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_WIDTH,BaseConstants.DEFAULT_VIDEO_WIDTH);
        int videoHeight = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_HEIGHT,BaseConstants.DEFAULT_VIDEO_HEIGHT);

        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(true,  //videoEnabled
                false,true, //loopback, tracing
                false,//useCamera2
                videoWidth,videoHeight,videoFps, videoBps,
                videoCodec,
                true, //HW Accessorate
                false, //capture to texture
                audioBps, //AUDIO_BITRATE
                audioCodec, //OPUS ISAC
                false, //NOAUDIOPROCESSING_ENABLED
                false, //aecDump
                false, //EXTRA_OPENSLES_ENABLED useOpenSLES
                false, //EXTRA_DISABLE_BUILT_IN_AEC
                false, //EXTRA_DISABLE_BUILT_IN_AGC
                false, //EXTRA_DISABLE_BUILT_IN_NS
                true); //EXTRA_ENABLE_LEVEL_CONTROL

        if (null == appRtcClient){
            appRtcClient = new WebSocketRTCClient(this, new LooperExecutor());
        }

        assistantConnectionParameters = new AppRTCClient.AssistantConnectionParameters();
        assistantConnectionParameters.destroyMedia = destroyMedia;
        if (!hsServer.isEmpty())
            assistantConnectionParameters.wssUrl= "ws://"+ hsServer+":"+hsPort+"/hari";
        gDestroyMedia = 0;

        LogUtils.i(TAG,"To connect signal server with URL<"+assistantConnectionParameters.wssUrl+">");
        mSessionId = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SESSION_ID,"");
        appRtcClient.connectSignalServer(assistantConnectionParameters,mSessionId);

        mCallState = CALL_STATE.CALLING;
        naviStarted = false;
        hasSdpOfferReceived = false;
        shallRestartICE = false;
        mCachedLocalCandidates.clear();
        callStartedTimeMs = System.currentTimeMillis();

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CALL_START, ""));

        LogUtils.d(TAG,"startCall exit");
    }

    public void connectMediaChannel(){
        if (null!=mPeerConnectionClient && mPeerConnectionClient.isConnected()){
            LogUtils.d(TAG,"Media channel is connected!");
            return;
        }

        if (!isSignalChannelConnected()){
            LogUtils.d(TAG,"Message channel is not connected, so start call!");
            HariServiceClient.getCallEngine().startCall();
            return;
        }

        if (mCallState != CALL_STATE.WS_CONNECTED){
            LogUtils.d(TAG,"Message channel is connecting, so ignored! call state:"+mCallState);
            return;
        }

        startCall(gDestroyMedia);
    }

    public boolean isCallOngoing(){
        boolean ret = true;
        if(mCallState == CALL_STATE.STOPPED || mCallState == CALL_STATE.UNKNOWN){
            ret = false;
        }
        return ret;
    }

    private void onAudioManagerChangedState() {

    }

    private void handleCloseWebrtcEvent(){
        LogUtils.i(TAG,"handleCloseWebrtcEvent entry");
        //mCallState =CALL_STATE.STOPPED;
        if (!SessionMonitor.isInterrupt){
            if (mCallState==CALL_STATE.PRE_STOP || mCallState==CALL_STATE.STOPPED){
                mSessionId = "";
                SessionMonitor.mSessionId = "";
            }
        }

        if (this.rootEglBase != null){
            this.rootEglBase.release();
            this.rootEglBase = null;
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mPeerConnectionClient != null) {
            try {
                mPeerConnectionClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            mPeerConnectionClient = null;
        }else{
            onPeerConnectionClosed();
        }

        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.release();
            remoteRender = null;
        }

        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }

        // Reset members
        naviStarted = false;
        mRemoteSDP = null;
        LogUtils.i(TAG,"handleCloseWebrtcEvent exit");

        SessionMonitor.getInstance().webrtcClosed();
    }


//    public void close() {
//        LogUtils.d(TAG,"Close entry");
//        mCallState = CALL_STATE.STOPPED;
//        triggerEvent(SIGNAL_EVENT.CLOSE_WEBRTC);
//        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CLOSED, ""));
//    }

    public void closeWebRTC(){
        LogUtils.i(TAG,"CloseWebRTC entry");
        //mCallState = CALL_STATE.STOPPED;
        triggerEvent(SIGNAL_EVENT.CLOSE_WEBRTC);
        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_WB_CLOSED, ""));
    }

    public void closeWebSocket(){
        LogUtils.i(TAG,"CloseWebSocket entry");
        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_WS_CLOSED, ""));
        if (appRtcClient != null) {
            appRtcClient.disconnectFromSignalServer();
            appRtcClient = null;
        }
    }

//    public void switchSpeaker(boolean on) {
//        mAudioManager.setSpeakerphoneOn(on);
//    }
//
//    public void muteMicPhone(boolean on) {
//        mAudioManager.setMicrophoneMute(on);
//    }

    public void switchCamera() {
        mPeerConnectionClient.switchCamera();
    }
    public void startVideoSource() {
        if (mPeerConnectionClient != null) {
            int videoFps = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_FPS,0);
            if (videoFps == 0){
                videoFps = 30;
            }
            mPeerConnectionClient.startVideoSource(videoFps);
        }
    }
    public void stopVideoSource() {
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.stopVideoSource();
        }
    }



    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onSignalEvent(SIGNAL_EVENT event) {
        switch (event){
            case ON_SIGNAL_SERVER_CONNECTED: {
                handleSignalServerConnectedEvent();
                break;
            }
            case CLOSE_WEBRTC:{
                handleCloseWebrtcEvent();
            }
            break;
            case RESTART_WEBRTC:{
                handleRestartWebrtcEvent();
            }
        }
    }

    //Below cases will entry this function:
    // 1, callstart  2, ICE reconnect 3, signal channel disconnected
    // case1: pc shall be created, offer shall be created also
    // case2: only offer shall be created to restart ICE
    // case3: none of them shall be done again
    private void handleSignalServerConnectedEvent(){
        LogUtils.i(TAG,"On SingalServerConnected event received.");
        if(mCallState!=CALL_STATE.CALLING&&mCallState!=CALL_STATE.ACCEPT){
            LogUtils.i(TAG,"Ignore SingalServerConnected event, because current call state is:"+mCallState);
            return;
        }

        mCallState = CALL_STATE.WS_CONNECTED;

        if (HariServiceClient.getCallEngine().getCallMode() == CallEngine.CALL_MODE_ONLY_MSG){
            return;
        }

        connectMediaServer();

    }

    private void connectMediaServer(){
        boolean justCreateWebrtc = false;

        LogUtils.i(TAG,"To create WebRTC if necessary...");
        if (mPeerConnectionClient == null) {
			 Log.e(TAG, "handleSignalServerConnectedEvent: mPeerConnectionClient == null" );
            mPeerConnectionClient = PeerConnectionClient.getInstance();
            mPeerConnectionClient.createPeerConnectionFactory(mService.getApplicationContext(), peerConnectionParameters, this);
            justCreateWebrtc = true;
            if(!wakelock.isHeld())
                wakelock.acquire();
        }

        if(justCreateWebrtc){

            triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_MC_CONNECT_START, ""));
            if (this.rootEglBase == null){
                this.rootEglBase = EglBase.create();
            }
            mPeerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(),
                    localRender, remoteRender);
            LogUtils.i(TAG,"Create local offer...");
            mPeerConnectionClient.createOffer();
            //超过10秒 sdp未创建成功，返回失败
            Message message = new Message();
            message.what=100;
            message.obj=PCC_CREATE_ERROR;
            mHandler.sendMessageDelayed(message,10000);
        } else {
            LogUtils.i(TAG,"WebRTC is alive,do not need to create");
        }

        if(!NetworkMonitor.isInitialized()){
            NetworkMonitor.init(mService.getApplicationContext());
            NetworkMonitor.setAutoDetectConnectivityState(false);
        }

        AudioManager audioManager = ((AudioManager) mService.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE));
        Log.d("Android audioManager", "---audioManager--" + audioManager.getMode());
        Log.d("Android audioManager", "---audioManager--" + audioManager.isSpeakerphoneOn());
    }


    /*
     * login信令服务器成功的回调
     */
    @Override
    public void onSignalServerConnected(final  String sessionId) {
        LogUtils.i(TAG,"Connected with signal server after successful registration, where callState="+mCallState.toString());

        mSessionId = sessionId;
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SESSION_ID,mSessionId);
        SessionMonitor.mSessionId = sessionId;

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_SERVER_CONNECTED, ""));

        //to check whether it is reconnected
        //if(mCallState==CALL_STATE.CALLING)
        if((mCallState==CALL_STATE.ACCEPT&&SessionMonitor.isInterrupt)||mCallState==CALL_STATE.CALLING)
        {
            triggerEvent(SIGNAL_EVENT.ON_SIGNAL_SERVER_CONNECTED);

            CallEvent event = new CallEvent(CallEvent.Event.CALL_WS_CONNECTED);
            triggerEvent(event);
        }
    }

    public static int gDestroyMedia = 0;
    public void restartCall(){
        Log.e(TAG, "restartCall: " );
        SessionMonitor.isInterrupt = true;
        gDestroyMedia = 1;
        closeWebRTC();
    }

    public void restartIce()
    {
        LogUtils.i(TAG,"To restart ICE...");
        triggerEvent(SIGNAL_EVENT.RESTART_WEBRTC);

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_RESTART, ""));
    }
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "handleMessage: "+ msg.obj);
            onPeerConnectionError((String) msg.obj);
        }
    };
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        LogUtils.i(TAG,"To send local offer to signal server after "+delta+" ms, where sdp is "+sdp.description);
        if (appRtcClient != null) {
            mHandler.removeMessages(100);
            appRtcClient.sendCallStart(mSessionId, sdp);
          //  hasSdpOfferSent = true;
        }

    }

    @Override
    public void onCallSessionCreated(final String sessionId){
        LogUtils.i(TAG,"Call Session <"+sessionId+"> is created in server side");

        mCallState = CALL_STATE.RINGING;

        String oldSessionId = mSessionId;

        //send all cached candidates to server
//        synchronized (candidateListLock){
//            if (!sessionId.equals(oldSessionId)){
//                mSessionId = sessionId;
//                PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SESSION_ID,mSessionId);
//                SessionMonitor.mSessionId = sessionId;
//            }
//
//            if (appRtcClient != null) {
//                LogUtils.d(TAG,"onCallSessionCreated to send all cached candidates for the sessionId="+mSessionId + " candidate num:"+mCachedLocalCandidates.size());
//                for (IceCandidate candidate : mCachedLocalCandidates) {
//                    appRtcClient.sendCallUpdate(mSessionId, candidate);
//                }
//                mCachedLocalCandidates.clear();
//            }
//        }

        if(!mSessionId.equalsIgnoreCase(oldSessionId)) {
            CallEvent event = new CallEvent(CallEvent.Event.CALL_RINGING,"","Ringing");
            triggerEvent(event);
        }

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CALL_SESSION_CREATED, ""));

//        keepAliveTimer.restartTimer();
    }


    @Override
    public void onIceCandidate(final IceCandidate candidate) {

        LogUtils.i(TAG,"onIceCandidate entry with local candidate:" + candidate.toString() + " where sessionId="+mSessionId);

        // Initiator need wait callee ringing then start send update.
        synchronized (candidateListLock) {
            if (TextUtils.isEmpty(mSessionId)||!hasSdpOfferReceived) {
                mCachedLocalCandidates.add(candidate);
            }
//                candidateListLock.notify();
        }

        if (!TextUtils.isEmpty(mSessionId) && hasSdpOfferReceived) {
            if (null!=appRtcClient &&  null!=mPeerConnectionClient && !mPeerConnectionClient.isIceCompleted()) {
                appRtcClient.sendCallUpdate(mSessionId,candidate);
            }
        }
        LogUtils.i(TAG,"onIceCandidate exit");
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        //TODO:: appRtcClient.sendLocalIceCandidateRemovals(candidates);
        LogUtils.i(TAG,"onIceCandidatesRemoved entry  where sessionId="+mSessionId);
        if(candidates!=null && candidates.length>0){
            for(IceCandidate candidate:candidates){
                LogUtils.i(TAG,"to remove local candidate:" + candidate.toString() + " where sessionId="+mSessionId);
            }
        }
    }

    @Override
    public void onCallUpdate(final String sessionId, final IceCandidate candidate) {
       // LogUtils.d(TAG,"onCallUpdate entry with sessionId:"+(TextUtils.isEmpty(sessionId)?"NULL":sessionId)+",remote candidate:"+candidate);
        if(!TextUtils.isEmpty(sessionId)){
            if(mSessionId==null || mSessionId.isEmpty() || sessionId.equalsIgnoreCase(mSessionId)){
                if(candidate==null){
                    triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_HEARTBEAT_RESPONSE, ""));
                }else{
                    if (mPeerConnectionClient != null) {
                        //LogUtils.d(TAG,"Add remote candidate");
                        mPeerConnectionClient.addRemoteIceCandidate(candidate);
                    } else {
                        LogUtils.i(TAG,"PeerConnectionClient is null, ignore candicate");
                    }
                }

            } else {
                LogUtils.i(TAG,"onCallUpdate exception: local sid is +"+mSessionId + "received sid is +"+sessionId);
            }
        }
    }

    @Override
    public void onHeartbeatResponse(String sessionId) {
        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_HEARTBEAT_RESPONSE, ""));
    }

    @Override
    public void onCallAccepted(final SessionDescription sdp) {
        // TODO: support for non-initiator case
        mCallState = CALL_STATE.ACCEPT;
        LogUtils.i(TAG,"onCallAccepted entry with remote SDP <"+sdp.description+">.");
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.setRemoteDescription(sdp);
            hasSdpOfferReceived = true;
        }

        synchronized (candidateListLock){
            if (null!=appRtcClient &&  null!=mPeerConnectionClient && !mPeerConnectionClient.isIceCompleted()) {
                LogUtils.i(TAG,"onCallSessionCreated to send all cached candidates for the sessionId="+mSessionId + " candidate num:"+mCachedLocalCandidates.size());
                for (IceCandidate candidate : mCachedLocalCandidates) {
                    appRtcClient.sendCallUpdate(mSessionId, candidate);
                }
                mCachedLocalCandidates.clear();
            }
        }
    }

    @Override
    public void onIceConnected() {
        LogUtils.i(TAG,"onIceConnected...");
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        mPeerConnectionClient.enableStatsEvents(true, 2000);
        CallEvent event = new CallEvent(CallEvent.Event.CALL_CONNECTED);
        triggerEvent(event);

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CONNECTED, ""));

    }

    /**
     * 网络连接不可用
     */
    @Override
    public void onIceDisconnected() {
        LogUtils.i(TAG,"onIceDisconnected...");
        //ToastUtil.showLongDefault(baseApplication,"onIceDisconnected...");
        CallEvent event = new CallEvent(CallEvent.Event.ICE_DISCONNECTED,CallEvent.CODE_EXPN_ICE_DISCONNECTED,"Ice disconnected");

        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_DISCONNECTED, ""));
    }


//    public boolean isInterrupt() {
//        return isInterrupt;
//    }

    @Override
    public void onPeerConnectionError(final String description) {
        LogUtils.i(TAG,"onPeerConnectionError... with description:" + description);
        if(!SessionMonitor.isInterrupt&&!description.equals("连接客服失败，请检查网络或稍后重试")&&!description.equals(PCC_CREATE_ERROR)) {
            LogUtils.i(TAG,"Set isInterrupt value to true");
            SessionMonitor.isInterrupt = true;
        }

        if (description.contains("camera")){
            CallEvent event = new CallEvent(CallEvent.Event.CALL_FAILED,CallEvent.CODE_CAMERA_ERROR,description);
            triggerEvent(event);
        } else {
            CallEvent event = new CallEvent(CallEvent.Event.ICE_FAILED,CallEvent.CODE_ICE_FAILED,description);
            triggerEvent(event);
        }

        if(mCallState != CALL_STATE.STOPPED) {
            if (!SessionMonitor.isInterrupt){
                closeWebSocket();
                if (HariServiceClient.getCallEngine().getCallMode() == CallEngine.CALL_MODE_ONLY_MSG){
                    mCallState = CALL_STATE.STOPPED;
                } else {
                    mCallState = CALL_STATE.PRE_STOP;
                }
            }
            closeWebRTC();
        }
    }

    private void handleRestartWebrtcEvent(){
        //to reconnect the media channel
        if(mCallState!=CALL_STATE.STOPPED && mCallState!=CALL_STATE.CALLING){
            // mPeerConnectionClient.clearError();
            shallRestartICE =true;
            //rebind on signal channel
            appRtcClient.rebindSignalServer(mSessionId);
            //switch callState into calling
            mCallState = CALL_STATE.CALLING;
            //create local offer after signal channel is confirmed to be ready

        }
    }

    @Override
    public void onCallStop(String sessionId,String detail) {

        LogUtils.i(TAG,"onCallStop entry with sessionId:"+sessionId);
        if(!TextUtils.isEmpty(sessionId)){
            if(sessionId.equalsIgnoreCase(mSessionId)){
                //close webRTC
//                closeWebSocket();
                mCallState = CALL_STATE.WS_CONNECTED;
                closeWebRTC();
                triggerEvent(new CallEvent(CallEvent.Event.CALL_STOP,CallEvent.CODE_STOPPED,detail));
            }
        }
    }

    public void sendInfo(final JSONObject data){

        if(!TextUtils.isEmpty(mSessionId)) {
            if (appRtcClient!=null) {
                appRtcClient.sendInfo(mSessionId,data);
            }
        }

    }

    public void stopCall(){

        LogUtils.i(TAG,"StopCall entry");

        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SESSION_ID,"");
        if(mCallState!=CALL_STATE.STOPPED) {
            LogUtils.i(TAG,"Stop call");
            //mCallState = CALL_STATE.STOPPED;
            if(mHandler.hasMessages(100))
            mHandler.removeMessages(100);
            if (!TextUtils.isEmpty(mSessionId)){
                if(appRtcClient!=null){
                    //appRtcClient.sendCallStop(mSessionId);
                    appRtcClient.logout(mSessionId);
                }
            }
            closeWebSocket();
            if (HariServiceClient.getCallEngine().getCallMode() == CallEngine.CALL_MODE_ONLY_MSG){
                mCallState = CALL_STATE.STOPPED;
            } else {
                mCallState = CALL_STATE.PRE_STOP;
            }
            //close webRTC
            closeWebRTC();
        }
    }

    public void disconnectMediaChannel(){

        if (!TextUtils.isEmpty(mSessionId)){
            if(appRtcClient!=null){
                appRtcClient.sendCallStop(mSessionId);
            }
        }
        if (isSignalChannelConnected()){
            mCallState = CALL_STATE.WS_CONNECTED;
        }

        closeWebRTC();
    }

    public boolean canReconnect(){
        boolean canRecon = false;
        canRecon = (SessionMonitor.isInterrupt || (mCallState!=CALL_STATE.STOPPED));
        if (!canRecon ){
            LogUtils.i(TAG,"Can not reconnect signal channel due to call state is stopped!");
            return canRecon;
        }

        canRecon = mService.hasConnectivity();
        if (!canRecon){
            LogUtils.i(TAG,"Can not reconnect signal channel due to no connectivity!");
            return canRecon;
        }

        canRecon = null!=appRtcClient;
        if (!canRecon){
            LogUtils.i(TAG,"Can not reconnect signal channel due to RTCClient is null!");
            return canRecon;
        }

        return canRecon;
    }

    public boolean canSendHeartbeat(){
        return appRtcClient!=null;
    }

    public void sendHeartbeat(){
        appRtcClient.sendHeartbeat(mSessionId);
    }

    public void reconnectSingnalServer(){
        appRtcClient.reconnectSignalServer(assistantConnectionParameters,mSessionId);
    }



    @Override
    public void onPeerConnectionClosed() {
        LogUtils.i(TAG,"onPeerConnectionClosed...");
        if (mCallState == CALL_STATE.PRE_STOP){
            mCallState = CALL_STATE.STOPPED;
        } else {
            if(!isSignalChannelConnected() || SessionMonitor.isInterrupt){
                mCallState = CALL_STATE.STOPPED;
            }
        }
        CallEvent event = new CallEvent(CallEvent.Event.CALL_CLOSED,"","connection closed");
        triggerEvent(event);
    }

    private long lastWarnTime = -1;
    private int lowBitrateCounter = 0;
    private long lowBitrateLastWarnTime = -1;
    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
       // LogUtils.d(TAG,"onPeerConnectionStatsReady...");
      //  打印统计信息
        int track = CallStatsHelper.getTrackId(true);
        List<CallStatsHelper.NetworkQualityReport> qualityReports = CallStatsHelper.parseStatsReport(track, reports);
        try {
            for (CallStatsHelper.NetworkQualityReport report : qualityReports){
                if (Integer.parseInt(report.getCurrentDelay()) >= NET_DELAY_THRESHOLD){
                    if (lastWarnTime<0){
                        lastWarnTime = System.currentTimeMillis();
                        triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION,CallEvent.CODE_EXPN_WEBRTC_DELAY, CallEvent.CODE_EXPN_WEBRTC_DELAY));
                    } else if (System.currentTimeMillis()-lastWarnTime > NET_DELAY_WARNING_TIMEINTERVAL){
                        lastWarnTime = System.currentTimeMillis();
                        triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION, CallEvent.CODE_EXPN_WEBRTC_DELAY,CallEvent.CODE_EXPN_WEBRTC_DELAY));
                    }

                    break;
                }

                if (!report.getCurrentBitrate().isEmpty()){
                    int bitRate = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_VIDEO_BPS,512);
                    int actualBitrate = Integer.parseInt(report.getCurrentBitrate())/1024;
                    if(actualBitrate < bitRate*0.5){
                        lowBitrateCounter ++;
                        LogUtils.i(TAG,"low bitrate: "+actualBitrate +" counter:"+lowBitrateCounter);

                        if (lowBitrateCounter >= 6){
                            lowBitrateCounter = 0;

                            if (lowBitrateLastWarnTime<0){
                                lowBitrateLastWarnTime = System.currentTimeMillis();
                                triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION,CallEvent.CODE_EXPN_WEBRTC_LOW_BITRATE, CallEvent.CODE_EXPN_WEBRTC_LOW_BITRATE));
                            } else if (System.currentTimeMillis()-lowBitrateLastWarnTime > WEBRTC_LOW_BITRATE_WARNING_TIMEINTERVAL){
                                lowBitrateLastWarnTime = System.currentTimeMillis();
                                triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION, CallEvent.CODE_EXPN_WEBRTC_LOW_BITRATE,CallEvent.CODE_EXPN_WEBRTC_LOW_BITRATE));
                            }
                        }
                    } else {
                        if (lowBitrateCounter > 0)
                            lowBitrateCounter --;
                    }
                }
            }
        }catch (Exception e){

        }
        statReports = CallStatsHelper.formatStatsReport(qualityReports);
 //       LogUtils.d(TAG,statReports);
//        CallEvent event = new CallEvent(CallEvent.Event.STATS_REPORT);
//        event.setObject(reports);
//        triggerEvent(event);
    }

    private boolean channelCloseWarned  = false;
    @Override
    public void onChannelClose() {
        LogUtils.e(TAG,"Singal channel close");
        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CHANNEL_CLOSE, ""));
        if (mCallState == CALL_STATE.ACCEPT){
            if (!channelCloseWarned){
                triggerEvent(new CallEvent(CallEvent.Event.CALL_EXCEPTION,CallEvent.CODE_SIGNAL_CLOSED,""));
                channelCloseWarned = true;
            }

        }
    }

    @Override
    public void onChannelConnectionError(String description) {
        LogUtils.i(TAG,"Signal channel connect exception with message: "+description);
        triggerEvent(new SessionEvent(SessionEvent.Event.SESSION_CHANNEL_CONNECTION_ERROR, description));
    }

    @Override
    public void onChannelAuthenticationError(String description) {
        if(mCallState!=CALL_STATE.STOPPED) {
            LogUtils.i(TAG,"Signal channel authentication exception with message: "+description);

            triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED,CallEvent.CODE_SIGNAL_REGISTER_ERROR,description));

            closeWebSocket();
            closeWebRTC();
        }
    }

    @Override
    public void onChannelSignalError(String description) {
        if(mCallState!=CALL_STATE.STOPPED) {
            LogUtils.i(TAG,"Signal message invalid exception with message: "+description);
//            //Close WebRTC
            if(description.equals("客服未接听")||description.equals("客服已挂断")){
                closeWebRTC();
                triggerEvent(new CallEvent(CallEvent.Event.CALL_FAILED,"",description));
            }else {
                triggerEvent(new CallEvent(CallEvent.Event.SIGNAL_CONNECTION_ERR,CallEvent.CODE_SIGNAL_ELSE_ERROR,description));
            }
        }
    }

    @Override
    public void onChannelCallRejected(String description) {
        if(mCallState!=CALL_STATE.STOPPED) {
            LogUtils.i(TAG,"Signal channel exception with message: "+description);
            //Close WebRTC
            closeWebSocket();
            closeWebRTC();
            triggerEvent(new CallEvent(CallEvent.Event.CALL_REJECTED,CallEvent.CODE_SIGNAL_REJECTED,description));
        }
    }

    @Override
    public void onObjectIdentified(String objectName) {
        CmdEvent event = new CmdEvent(CmdEvent.Event.OBJECT_IDENTIFIED,objectName);
        triggerEvent(event);
    }

    @Override
    public void onFaceIdentified(String faceName) {
        CmdEvent event = new CmdEvent(CmdEvent.Event.FACE_IDENTIFIED,faceName);
        triggerEvent(event);
    }

    @Override
    public void onCommandReceived(String command) {

    }

    @Override
    public void onInfo(String type, String data) {
        if("speak".equalsIgnoreCase(type)){
            if(!TextUtils.isEmpty(data)){
                CmdEvent event = new CmdEvent(CmdEvent.Event.SPEAK_RECEIVED,data);
                triggerEvent(event);
            }
        }else {
            if(!TextUtils.isEmpty(data)){
                CmdEvent event = new CmdEvent(CmdEvent.Event.INFO_RECEIVED,data);
                triggerEvent(event);
            }
        }
    }

    /*
      信令数据通道
     */
    @Override
    public void onInfo(JSONObject data) {

        try {
            CDPData cdpData = CDParser.parseJSON(data);
            CDPEntity body = cdpData.getBodyList().get(0);
            String type = body.getType();
            if (type.equalsIgnoreCase("mediaReconnect")){   //坐席侧重打，保证sid一致性，使用坐席侧发过来的sid
                LogUtils.i(TAG,"Received mediaReconnect command");
                try {
                    if (data.has("sid")){
                        String sid = data.getString("sid");
                        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SESSION_ID,sid);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            CmdEvent event = new CmdEvent(CmdEvent.Event.INFO_RECEIVED,cdpData);
            triggerEvent(event);
        }catch (Exception e){
            LogUtils.i(TAG,"onInfo exception :"+e.getLocalizedMessage());
        }
    }

    @Override
    public void onSlamNavigationStart(final String startPoint, final String finishPoint) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {

        CDPData cdpData = CDParser.parseData(buffer.data);

        CmdEvent event = new CmdEvent(CmdEvent.Event.DC_MSG_RECEIVED,cdpData);
        triggerEvent(event);
    }
}
