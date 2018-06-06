package com.cloudminds.meta.model;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.call.CallEvent;
import com.cloudminds.hc.hariservice.call.listener.CallEventListener;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.broadcast.MetaWakeUpReceiver;
import com.cloudminds.hc.metalib.manager.MetaSensorManager;
import com.cloudminds.meta.R;
import com.cloudminds.meta.aidl.IHubReceiver;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.broadcast.BatteryReceiver;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.broadcast.VPNStateReceiver;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.ConflictEventManager;
import com.cloudminds.meta.manager.MetaManager;
import com.cloudminds.meta.service.AIBridge;
import com.cloudminds.meta.service.HubService;
import com.cloudminds.meta.service.RestartAPPTool;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.service.navigation.IndoorNavigator;
import com.cloudminds.meta.service.navigation.LocationMonitor;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.FileUtils;
import com.cloudminds.meta.util.LogUtils;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.UsbCameraEnumerator;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

import static android.content.Context.POWER_SERVICE;
//import static com.cloudminds.hc.hariservice.call.CallEngine.Callee.HARI_CALLEE_AI;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_ON_CONNECTION;
import static com.cloudminds.meta.manager.ConflictEventManager.CALLING_EVENT;
import static com.cloudminds.meta.manager.ConflictEventManager.FAMILYMANAGE_EVENT;

/**
 * Created by tiger on 17-4-26.
 */

public class HubServiceModel implements PlayerUtil.MusicPlayHandler, VPNStateReceiver.VPNStateChangeListener {

    public static final String TAG = "Meta:HubServiceModel";

    public Context mContext;
    private CallEngine mCall;
    private boolean isInit = false;
    private PlayerUtil playerUtil;
    private CommandEngine commandEngine;
    private VPNStateReceiver vpnStateReceiver;
    private PowerManager.WakeLock wakeLock;

    public void setReceiver(IHubReceiver mReceiver) {
        Log.e(TAG, "setReceiver: "+mState );
        this.mReceiver = mReceiver;
        setCallState(mState,null);
    }

    private IHubReceiver mReceiver;
    private static int mState = Constant.HUB_CONN_NOMAL;

    public static int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public HubServiceModel(Context context){
        this.mContext = context;
        init();
    }

    private void setCallState(int state,String message){
        Log.e(TAG, "setCallState: "+state+"  "+message );
        MetaApplication.setState(state);
        try {
            if(mReceiver == null)return;
            mReceiver.callResult(state,message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void init(){
//        initWackUp();
        PowerManager powerManager = (PowerManager)mContext.getSystemService(POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");

        initHCMeta();
        initHari();
        vpnStateReceiver = new VPNStateReceiver();
        vpnStateReceiver.setListener(this);
        vpnStateReceiver.init(mContext);
        IntentFilter vpnfilter = new IntentFilter("android.intent.action.VPN_STATE");
        mContext.registerReceiver(vpnStateReceiver, vpnfilter);
    }

    private void initWackUp() {
        LogUtils.d(TAG,"initWackUp");
        playerUtil = PlayerUtil.getPlayerUtil(mContext);
        playerUtil.musicPlayHandler=this;
        EventManager wakeup = EventManagerFactory.create(mContext, "wp");
        wakeup.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] bytes, int i, int i1) {
            try {
                Log.d(TAG,"onEvent name = "+name+", params = "+params);
                JSONObject json = new JSONObject(params);
                if ("wp.data".equals(name)) {
                    String word = json.getString("word");
                    LogUtils.d(TAG,"current word = "+word);
                    playId=R.raw.bdspeech_recognition_start;
                    playerUtil.playDi(mContext,playId);
                } else if ("wp.exit".equals(name)) {
                    Log.d(TAG,"wp.exit so error");
                }
            } catch (JSONException e) {
                throw new AndroidRuntimeException(e);
            }

            }
        });
        HashMap params = new HashMap();
        params.put("kws-file", FileUtils.getSDPath() + "/" + Constant.WAKE_UP);
        wakeup.send("wp.start",new JSONObject(params).toString(), null, 0, 0);
    }

    private void initHCMeta(){
        HCMetaUtils.addMetaWakeUpListener((MetaWakeUpReceiver.WakeUpListener)mContext);
        HCMetaUtils.addMetaListener((MetaSensorManager.BatteryListener)mContext);
        BatteryReceiver.addListener((MetaSensorManager.BatteryListener)mContext);
        isInit = true;
    }

    public void handleSpeechResult(Bundle bundle){
        Log.d(TAG,"onResults -- "+bundle);
        ArrayList<String> nbest = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String json_res = bundle.getString("origin_result");
        LogUtils.d(TAG,"onResults -- "+json_res);
        try {
            JSONArray object = new JSONObject(json_res).getJSONObject("content").getJSONArray("item");
            String result = (String) object.get(0);
            TTSSpeaker.speak(result, TTSSpeaker.SPEAK);
            LogUtils.d(TAG,"onResults -- "+object.get(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        if(wakeLock.isHeld())wakeLock.release();
        wakeLock=null;
        vpnStateReceiver.destory();
        mContext.unregisterReceiver(vpnStateReceiver);
        vpnStateReceiver=null;
        mContext=null;
        BatteryReceiver.removeListener((MetaSensorManager.BatteryListener)mContext);
        cancleMeta();
        cancleVoice();
        cancleHari();
    }

    private void cancleMeta(){
        if(!isInit)return;
        HCMetaUtils.removeMetaListener((MetaSensorManager.BatteryListener)mContext);
        HCMetaUtils.removeMetaWakeUpListener((MetaWakeUpReceiver.WakeUpListener)mContext);
        HCMetaUtils.destory();
        isInit = false;
    }

    private void cancleVoice(){
        TTSSpeaker.cancleSpeech();
    }
    boolean networkAvailable=true;
    private boolean checkInternet(){
        boolean networkAvailable = InternetBroadcast.isNetworkAvailable();
        if(!networkAvailable&&this.networkAvailable) {
            TTSSpeaker.speak(mContext.getString(R.string.no_internet), TTSSpeaker.HIGH);
        }
        this.networkAvailable=networkAvailable;
        return networkAvailable;
    }
    public void sendData(String data){
        if(!checkInternet()) return;
            try {
                commandEngine.sendData(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }
    private void cancleHari(){
        if(mCall == null)return;

    }

    public void initHari(){

        mCall = HariServiceClient.getCallEngine();
        mCall.setCallEventListener((CallEventListener)mContext);
 //       mCall.setRobotType("pepper");
        commandEngine = HariServiceClient.getCommandEngine();
//        commandEngine.setCmdEventListener(this);
    }

    public void callStart(CallEngine.Callee callee){
        LogUtils.d(TAG,"callStart"+callee);
        if (addConflictEvent()) return;
        if(!checkInternet()) {
            speak(R.string.hub_tts_network_error);
            onCallError(mContext.getString(R.string.hub_tts_network_error));
            onCallClosed();
            return;
        }
        if(!SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_NO_META,false)&&!UsbCameraEnumerator.isSupported()){
            if(HCMetaUtils.hasMeta()) {
                speak(R.string.hub_tts_camera_error);
            }else{
                speak(R.string.meta_unconnect);
            }
            onCallError(mContext.getString(R.string.hub_tts_camera_error));
            onCallClosed();
            return;
        }
        wakeLock.acquire();
        speak(R.string.hub_tts_on_connection);
        mState = HUB_CONN_ON_CONNECTION;
        LogUtils.d(TAG," doConnect ");
        if(mCall==null){
            initHari();
        }
        mCall.setCallee(callee);
        mCall.setCallMode(CallEngine.CALL_MODE_ONLY_MSG);
        mCall.startCall();
        setCallState(mState,"");
        SharePreferenceUtils.setPrefInt(Constant.PRE_KEY_CALLCALLEE,callee== CallEngine.getHARI_CALLEE_AI()?0:1);
    }
    //添加冲突事件，冲突返回true
    private boolean addConflictEvent() {
        try {
            String confilctEvent = ConflictEventManager.addEvent(ConflictEventManager.CALLING_EVENT, new ConflictEventManager.EventChecker() {
                @Override
                public boolean check() {
                    if (mState == HUB_CONN_ON_CONNECTION || mState == HUB_CONN_IN_CONNECTION) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            switch (confilctEvent){
                case FAMILYMANAGE_EVENT:
                    TTSSpeaker.speak(mContext.getString(R.string.call_conflict),TTSSpeaker.HIGH);
                    Log.e(TAG, "callStart: 亲友管理冲突" );
                    return true;
            }
        } catch (Exception e) {
            TTSSpeaker.speak(mContext.getString(R.string.undefined_event),TTSSpeaker.HIGH);
            e.printStackTrace();
            Log.e(TAG, "callStart: "+e.getMessage() );
            return true;
        }
        return false;
    }

    public void recognizeOne(){
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {
//            data.put("type",1);
            json.put("type","volumeCtrlRecognize");
            json.put("data",data);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e(TAG, "recognizeOne: volumeCtrlRecognize" );
        commandEngine.sendData(json);
    }

    public void callStop(){
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
        Log.d(TAG," doDisconnect ");
        if(mCall==null){
            initHari();
        }

        switch (mState){
            case HUB_CONN_IN_CONNECTION:
                mState = Constant.HUB_CONN_END;
                break;
            case HUB_CONN_ON_CONNECTION:
                mState = Constant.HUB_CONN_NOMAL;
                break;
        }
        setCallState(mState,"");

        LocationMonitor.getInstance().stopLocation();
        mCall.stopCall();
    }

    public void onCallConnected(){
        LogUtils.d(TAG,"callStart");
        if(mState== HUB_CONN_IN_CONNECTION){
            return;
        }
        SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, true);
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.START_WAKEUPASR));
        speak(R.string.hub_tts_in_connection);

        mState = HUB_CONN_IN_CONNECTION;
        setCallState(mState,"");
        LocationMonitor.getInstance().setEnableReportLocation(true);
        LocationMonitor.getInstance().startLocation(null);

        if(IndoorNavigator.type== IndoorNavigator.Type.INTERRUPT&&!TextUtils.isEmpty(IndoorNavigator.to)){
            commandEngine.sendMessage("室内导航到"+IndoorNavigator.to);
        } else {
            OutdoorNavigator.getInstance().hariConnected();
        }

        //每次hari连接上，上报坐席需要的状态信息
        AIBridge.getInstance().queryRobotStatusResponse();
//        if (addConflictEvent()) return;
        try {
            String confilctEvent = ConflictEventManager.addEvent(ConflictEventManager.CALLING_EVENT, new ConflictEventManager.EventChecker() {
                @Override
                public boolean check() {
                    if (mState == HUB_CONN_ON_CONNECTION || mState == HUB_CONN_IN_CONNECTION) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            switch (confilctEvent){
                case FAMILYMANAGE_EVENT:
//                    TTSSpeaker.speak(mContext.getString(R.string.call_conflict),TTSSpeaker.HIGH);
                    Log.e(TAG, "callStart: 亲友管理冲突" );
            }
        } catch (Exception e) {
//            TTSSpeaker.speak(mContext.getString(R.string.undefined_event),TTSSpeaker.HIGH);
            e.printStackTrace();
            Log.e(TAG, "callStart: "+e.getMessage() );
        }
    }

    public void onCallStop(String data){
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        switch (data){
            case "no free helper":
                speak(R.string.hub_tts_rejected);
                break;
            default:
                speak(R.string.hub_tts_end);
                break;
        }
        mState = Constant.HUB_CONN_END;
        setCallState(mState,"");
        LocationMonitor.getInstance().stopLocation();
    }
    public void onCallFailed(String data){
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        speak(mContext.getString(R.string.hub_tts_service_error));
        mState = Constant.CALL_FAILED;
        setCallState(mState,data==null?"":data);
        LocationMonitor.getInstance().stopLocation();
    }

    private void speak(int resource){
        String message = mContext.getString(resource);
        speak(message);
    }

    public void speak(String message){
        if(!TextUtils.isEmpty(message)){
            TTSSpeaker.speak(message, TTSSpeaker.HIGH);
        }
    }

//    public void speak(Intent intent){
//        String message = intent.getStringExtra(Constant.SPEECH_MESSAGE_KEY);
//        Log.d(TAG,"message = "+message);
//        speak(message);
//    }

    public void onCallRejected(String data){
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        if(!data.contains("no free helper")){
            speak(R.string.call_reject);
        }else {
            speak(R.string.hub_tts_rejected);
        }
        mState = Constant.HUB_CONN_NOMAL;
        setCallState(mState,data==null?"":data);
    }

    public void reStart(){
        Intent intent = new Intent(mContext,HubService.class);
        mContext.startService(intent);
    }
    int playId=-1;
    public void onEndOfSpeech(){
        playId=R.raw.bdspeech_recognition_success;
        Log.e(TAG, "onEndOfSpeech: " );
        playerUtil.playDi(mContext,playId);
    }
    public void onError(){
        playId=R.raw.bdspeech_recognition_error;
        Log.e(TAG, "onError: " );
        playerUtil.playDi(mContext,playId);
    }
    @Override
    public void onMusic(int status, int select) {
        switch (status) {
            case 2:
                Log.e(TAG, "onMusic: startRecognizer   "+status+"   "+select );
//                if(playId==R.raw.bdspeech_recognition_start)
            //startRecognizer();
                break;
        }
    }
    //     {
//         language: "EN",
//         asrStatus: 1,
//         robotStatus: 1,
//         battery:{“capacity":2917,“residueCapacity":481 }
//            }

    public void sendMessage(String msg) {
        if(!checkInternet()) return;
        Log.e(TAG, "sendMessage: "+msg.toLowerCase() );
        if(msg.toLowerCase().matches(mContext.getString(R.string.check_battery))){
//        if(msg.equalsIgnoreCase(mContext.getString(R.string.check_battery))){
            if(HCMetaUtils.hasMeta()) {
                TTSSpeaker.speak(String.format(mContext.getString(R.string.helmet_power), "" +(int)Math.floor(MetaManager.getInstance().getBattery())), TTSSpeaker.SPEAK_ANSWER);
            }
            TTSSpeaker.speak(String.format(mContext.getString(R.string.phone_power),""+ BatteryReceiver.curlevel),TTSSpeaker.SPEAK_ANSWER);
            return;
        }
        if(mState== HUB_CONN_IN_CONNECTION) {
            commandEngine.sendMessage(msg);
            Log.e(TAG, "sendMessage: "+msg );
        }else{
            TTSSpeaker.speak(mContext.getString(R.string.no_connect_service), TTSSpeaker.HIGH);
        }
    }

    public void onCallDisconnected(String code,String data) {
        Log.e(TAG, "onCallDisconnected: "+(data==null?"null":data) );
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        if(code.equals("3050")){
            SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
        }
        if(!TextUtils.isEmpty(data)) {
            if (data.equals("PeerConnectionClient create fail")) {
                speak(mContext.getString(R.string.hub_tts_device_error));
            } else if (data.equals("no free helper")) {
                speak(mContext.getString(R.string.hub_tts_rejected));
            }else if (!TextUtils.isEmpty(data)) {
                speak(mContext.getString(R.string.hub_tts_service_error));
            }
            mState = Constant.CALL_FAILED;
            IndoorNavigator.sendStopNavi(IndoorNavigator.Type.INTERRUPT, "异常中断");
        }else{
            speak(R.string.hub_tts_end);
            mState = Constant.HUB_CONN_END;
            IndoorNavigator.sendStopNavi(IndoorNavigator.Type.INTERRUPT,"媒体中断");
        }
        OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_INTERRUPT);
        setCallState(mState,data==null?"":data);
    }

    public void onCallError(String error) {
        Log.e(TAG, "onCallError: "+error );
        EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
        IndoorNavigator.sendStopNavi(IndoorNavigator.Type.INTERRUPT,"异常中断");
        OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_INTERRUPT);
        mState = Constant.CALL_FAILED;
        setCallState(mState,error==null?"":error);
    }
    public void onCallClosed() {
        Log.e(TAG, "onCallClosed: restartAPP 1" );
        ConflictEventManager.removeEvent(CALLING_EVENT);
        if(wakeLock.isHeld())
        wakeLock.release();
        mState = Constant.CALL_CLOSED;
        setCallState(mState,"");
        if(SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_RESTARTCALL, false)) {
            Log.e(TAG, "onCallClosed: restartAPP 2" );
            SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_RESTARTCALL, false);
            RestartAPPTool.restartAPP(mContext);
        }
    }

    public void onCallException(String code){
       if (code.equalsIgnoreCase(CallEvent.CODE_EXPN_WEBRTC_DELAY)){
           speak(mContext.getString(R.string.hari_warn_net_delay));
       } else if (code.equalsIgnoreCase(CallEvent.CODE_EXPN_SINGAL_HEARTBEAT_TIMEOUT)){
           speak(mContext.getString(R.string.hari_warn_singal_exception));
       } else if (code.equalsIgnoreCase(CallEvent.CODE_EXPN_WEBRTC_LOW_BITRATE)){
           speak(mContext.getString(R.string.hari_warn_net_lowBitrate));
       } else if (code.equalsIgnoreCase(CallEvent.CODE_SIGNAL_CLOSED)){
           speak(mContext.getString(R.string.hari_warn_singal_exception));
       }
    }

    @Override
    public void vpnState(VPNStateReceiver.VPNState state) {
        switch (state){
            case VPN_STATE_INIT:
                speak("VPN初始化");
                break;
            case VPN_STATE_CONNECTED:
                speak("VPN已连接");
                break;
            case VPN_STATE_DISCONNECTED:
                speak("VPN已断开");
                break;
            case VPN_STATE_OTHER:
                speak("VPN不可用");
                break;
        }
    }
}
