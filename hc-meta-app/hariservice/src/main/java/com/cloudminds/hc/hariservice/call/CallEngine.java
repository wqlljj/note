package com.cloudminds.hc.hariservice.call;

import android.util.Log;

import com.cloudminds.hc.hariservice.manager.CallManager;
import com.cloudminds.hc.hariservice.manager.SessionMonitor;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.call.listener.CallEventListener;
import com.cloudminds.hc.hariservice.service.HariServiceConnector;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.hariservice.webrtc.PeerConnectionClient;

import org.webrtc.EglBase;

import java.lang.ref.PhantomReference;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by zoey on 17/4/17.
 */

public class CallEngine {

    private CallEventListener callEventListener;
    private String TAG="HS/CallEngine";

    public enum Callee{
        HARI_CALLEE_AI,
        HARI_CALLEE_HI
    }
    public static Callee getHARI_CALLEE_AI(){
        return Callee.HARI_CALLEE_AI;
    }
    public static Callee getHARI_CALLEE_HI(){
        return Callee.HARI_CALLEE_HI;
    }


    public static int CALL_MODE_ONLY_MSG = 0; //只打通信令通道
    public static int CALL_MODE_MEDIA = 1;    //打开媒体通道 （目前媒体通道基于信令通道

    public static final String PARAM_VIDEO_STARTBPS;  //初始设置的码率
    public static final String PARAM_VIDEO_BPS;    //传输实际码率
    public static final String PARAM_AUDIO_STARTBPS;
    public static final String PARAM_VIDEO_CODEC;   //视频编码格式
    public static final String PARAM_AUDIO_CODEC;    //音频编码格式
    public static final String PARAM_VIDEO_FPS; //视频帧率
    public static final String PARAM_VIDEO_WIDTH; //视频分辨率 w
    public static final String PARAM_VIDEO_HEIGHT; //视频分辨率 h
    public static final String PARAM_MS_MAX_BPS;   //信令使用 最大bps
    public static final String PARAM_MS_MIN_BPS;   //信令使用 最小bps


    private final int MAX_BR_V = 1024*10;      //带宽最大码率
    private final int MIN_BR_V = 10;           //带宽最小码率
    private final int MAX_FR_V = 30;           //视频最大帧率
    private final int MIN_FR_V = 5;            //视频最小帧率

    static {
        PARAM_VIDEO_STARTBPS = BaseConstants.PRE_KEY_VIDEO_STARTBPS;
        PARAM_VIDEO_BPS = BaseConstants.PRE_KEY_VIDEO_BPS;
        PARAM_AUDIO_STARTBPS = BaseConstants.PRE_KEY_AUDIO_STARTBPS;
        PARAM_VIDEO_CODEC = BaseConstants.PRE_KEY_VIDEO_CODEC;
        PARAM_AUDIO_CODEC = BaseConstants.PRE_KEY_AUDIO_CODEC;
        PARAM_VIDEO_FPS = BaseConstants.PRE_KEY_VIDEO_FPS;
        PARAM_VIDEO_WIDTH = BaseConstants.PRE_KEY_VIDEO_WIDTH;
        PARAM_VIDEO_HEIGHT = BaseConstants.PRE_KEY_VIDEO_HEIGHT;
        PARAM_MS_MAX_BPS = BaseConstants.PRE_KEY_MS_MAX_BPS;
        PARAM_MS_MIN_BPS = BaseConstants.PRE_KEY_MS_MIN_BPS;
    }


    /*
     * 设置用户名
     */
    public void setAccount(String account){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_ACCOUNT,account);
    }

    public String getAccount(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"");
    }

    /*
     * 设置密码
     */
    public void setPassword(String password){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_PASSWORD,password);
    }

    /*
     * 设置robot类型
     * type： meta｜pepper
     */
    public void setRobotType(String type){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_ROBOT_TYPE,type);
    }

    public String getRobotType(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ROBOT_TYPE,"meta2");
    }


    public void setTimeOutInterval(int second){
        //PreferenceUtils.setPrefInt(HariUtils.getInstance().getContext(), BaseConstants.PRE_KEY_PASSWORD,second);
    }

    public void setServer(String ip){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS,ip);
    }

    public String getServer(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS,"10.11.32.173");
    }

    public void setPort(String port){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_SERVER_PORT,port);
    }

    public String getPort(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_PORT,"9443");
    }

    /*
     * 设置坐席
     */
    public void setCustomer(String customer){
       PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_CUSTOMER,customer);
    }

    public String  getCustomer(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_CUSTOMER,"");
    }

    /*
     * 设置租户id
     */
    public void setTenantId(String tenantId){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_TENANTID,tenantId);
    }

    public String getTenantId(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_TENANTID,"10086");
    }

    /**
     *
     * @param enable
     */
    public void setRelayEnable(boolean enable){
        PreferenceUtils.setPrefBoolean(BaseConstants.PRE_KEY_WEBRTC_REALY_ENABLE,enable);
    }

    public boolean isRelayEnabled(){
        return PreferenceUtils.getPrefBoolean(BaseConstants.PRE_KEY_WEBRTC_REALY_ENABLE,false);
    }

    public void setLogEnable(boolean enable){
        PreferenceUtils.setPrefBoolean(BaseConstants.PRE_KEY_LOG_ENABLE,enable);
    }

    public boolean isLogEnabled(){
        return PreferenceUtils.getPrefBoolean(BaseConstants.PRE_KEY_LOG_ENABLE,false);
    }

    public void setRcuFirstConnect(boolean firstConnect){
        PreferenceUtils.setPrefBoolean(BaseConstants.PRE_KEY_RCU_FIRST_CONNECT,firstConnect);
    }

    public boolean isRcuFirstConnected(){
        return PreferenceUtils.getPrefBoolean(BaseConstants.PRE_KEY_RCU_FIRST_CONNECT,false);
    }

    public void setRobotId(String id){
        PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_ROBOTID,id);
    }

    public String getRobotId(){
        return PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ROBOTID,"");
    }

    public void setCallMode(int mode){
        PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_CALL_MODE,mode);
    }

    public int getCallMode(){
        return CALL_MODE_MEDIA;
        //return PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_CALL_MODE,CALL_MODE_ONLY_MSG);
    }

    /**
     * 设置带宽 码率等
     * @param key
     * @param value  字符串数值
     */
    public void setParam(String key,String value){
        try {
            if (key.equalsIgnoreCase(PARAM_VIDEO_BPS)){
                int rate = Integer.parseInt(value);
                if (rate >= MIN_BR_V && rate <= MAX_BR_V)
                    PreferenceUtils.setPrefInt(key,rate);
            } else if (key.equalsIgnoreCase(PARAM_VIDEO_FPS)){
                int rate = Integer.parseInt(value);
                if (rate >= MIN_FR_V && rate <= MAX_FR_V)
                    PreferenceUtils.setPrefInt(key, rate);
                if(isCallOngoing()) {
                    stopVideoSource();
                    startVideoSource();
                }
            } else if (key.equalsIgnoreCase(PARAM_VIDEO_STARTBPS)){
                int rate = Integer.parseInt(value);
                if (rate >= MIN_BR_V && rate <= MAX_BR_V)
                    PreferenceUtils.setPrefInt(key,rate);
            } else if (key.equalsIgnoreCase(PARAM_AUDIO_STARTBPS)){
                int rate = Integer.parseInt(value);
                PreferenceUtils.setPrefInt(key,rate);
            } else if (key.equalsIgnoreCase(PARAM_VIDEO_CODEC)){
                PreferenceUtils.setPrefString(key,value);
            } else if (key.equalsIgnoreCase(PARAM_AUDIO_CODEC)){
                PreferenceUtils.setPrefString(key,value);
            } else if (key.equalsIgnoreCase(PARAM_VIDEO_WIDTH)){
                PreferenceUtils.setPrefInt(key,Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(PARAM_VIDEO_HEIGHT)){
                PreferenceUtils.setPrefInt(key,Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(PARAM_MS_MAX_BPS)){
                PreferenceUtils.setPrefInt(key,Integer.parseInt(value));
            } else if (key.equalsIgnoreCase(PARAM_MS_MIN_BPS)){
                PreferenceUtils.setPrefInt(key,Integer.parseInt(value));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getParam(String key){
        if (key.equalsIgnoreCase(PARAM_VIDEO_BPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,0));
        } else if (key.equalsIgnoreCase(PARAM_VIDEO_FPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,30));
        } else if (key.equalsIgnoreCase(PARAM_VIDEO_STARTBPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,BaseConstants.DEFAULT_VIDEO_STARTBPS));
        } else if (key.equalsIgnoreCase(PARAM_AUDIO_STARTBPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,BaseConstants.DEFAULT_AUDIO_STARTBPS));
        } else if (key.equalsIgnoreCase(PARAM_VIDEO_CODEC)){
            return PreferenceUtils.getPrefString(key,BaseConstants.DEFAULT_VIDEO_CODEC);
        } else if (key.equalsIgnoreCase(PARAM_AUDIO_CODEC)){
            return PreferenceUtils.getPrefString(key,BaseConstants.DEFAULT_AUDIO_CODEC);
        } else if (key.equalsIgnoreCase(PARAM_VIDEO_WIDTH)){
            int width = PreferenceUtils.getPrefInt(key,BaseConstants.DEFAULT_VIDEO_WIDTH);
            return String.valueOf(width);
        } else if (key.equalsIgnoreCase(PARAM_VIDEO_HEIGHT)){
            int height = PreferenceUtils.getPrefInt(key,BaseConstants.DEFAULT_VIDEO_HEIGHT);
            return String.valueOf(height);
        } else if (key.equalsIgnoreCase(PARAM_MS_MAX_BPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,0));
        } else if (key.equalsIgnoreCase(PARAM_MS_MIN_BPS)){
            return String.valueOf(PreferenceUtils.getPrefInt(key,0));
        } else {
            return "";
        }
    }

    public void setCallee(Callee callee){
        switch (callee){
            case HARI_CALLEE_AI:
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_CALLEE,0);
                break;
            case HARI_CALLEE_HI:
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_CALLEE,1);
                break;
        }
    }

    public void setCallEventListener(CallEventListener eventListener) {
        callEventListener = eventListener;
    }


    public void startCall(){
        LogUtils.configure(HariUtils.getInstance().getContext());
        LogUtils.i(TAG,"Start call");
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        if(serviceConnector.getHariService().getCallManager().isCallOngoing())
        {
            LogUtils.i(TAG,"Is call on going! So ignored");
            return;
        }
        SessionMonitor.resetInterrupt();
        serviceConnector.getHariService().getCallManager().startCall(null,null,null,true,0);
    }

    public void connectMediaChannel(){
        setCallMode(CALL_MODE_MEDIA);
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().connectMediaChannel();

    }

    public void disconnectMediaChannel(){
        setCallMode(CALL_MODE_ONLY_MSG);
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().disconnectMediaChannel();
    }

    public void stopCall(){
        LogUtils.d(TAG,"Stop call");
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().stopCall();

    }

    /**
     * 重启app 时调用，释放webrtc占用的资源
     */
    public void cleanup(){
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().closeWebRTC();
    }

    /** 
     * 是否处于连接、已连接状态
     * @return
     */
    public boolean isCallOngoing(){
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        boolean ret = serviceConnector.getHariService().getCallManager().isCallOngoing();
        return ret;
    }

    /**
     * 视频是否处于已连接状态
     * @return
     */
    public boolean isMediaChannelConnected(){
        boolean isConnected = false;
        CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();
        PeerConnectionClient pcc = manager.getPeerConnection();
        if (null != pcc){
            isConnected = pcc.isConnected();
        }

        return isConnected;
    }

    /**
     * 消息通道是否已经连接
     * @return
     */
    public boolean isMsgChannelConnected(){
        boolean isConnected = false;
        CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();

        return manager.isSignalChannelConnected();
    }

    public boolean isVideoStopped(){
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        boolean stopped = serviceConnector.getHariService().getCallManager().getPeerConnection().isVideoStreamStopped();
        return stopped;
    }

    public void startVideoSource(){
        Log.i(TAG, "startVideoSource: " );
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().startVideoSource();
    }

    public void stopVideoSource(){
        Log.i(TAG, "stopVideoSource: " );
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().stopVideoSource();
    }

    public void restartCall(){
        LogUtils.i(TAG,"Restart call");
        HariServiceConnector serviceConnector = HariUtils.getInstance().getHariServiceConnector();
        serviceConnector.getHariService().getCallManager().restartCall();
    }


    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onCallEvent(CallEvent event) {
        CallEvent.Event eventType = event.getEvent();
        switch (eventType){
            case CALL_WS_CONNECTED:{
                callEventListener.onMessageChannelConnected();
                break;
            }

            case CALL_CONNECTED: {
                LogUtils.i(TAG, "onCallEvent: CALL_CONNECTED");
                callEventListener.onMediaChannelConnected();

                break;
            }
            case CALL_FAILED: {
                LogUtils.i(TAG, "onCallEvent: CALL_FAILED "+event.getInfo());
                callEventListener.onCallError(event.getCode(),event.getInfo());
                break;
            }
            case SIGNAL_CONNECTION_ERR:{
                LogUtils.i(TAG, "onCallEvent: SIGNAL_CONNECTION_ERR "+event.getInfo());
                callEventListener.onCallError(event.getCode(),event.getInfo());
                break;
            }
            case CALL_REJECTED:{
                LogUtils.i(TAG, "onCallEvent: CALL_REJECTED "+event.getInfo());
                callEventListener.onCallError(event.getCode(),event.getInfo());
                break;
            }
            case CALL_RINGING: {
                LogUtils.i(TAG, "onCallEvent: CALL_RINGING");
                callEventListener.onCallConnecting();
                break;
            }
            case CALL_STOP: {
                LogUtils.i(TAG, "onCallEvent: CALL_STOP "+event.getInfo());
                callEventListener.onMediaChannelDisconnected(event.getCode(),event.getInfo());
                break;
            }
            case ICE_DISCONNECTED: {
                LogUtils.i(TAG, "onCallEvent: ICE_DISCONNECTED "+event.getInfo());
                callEventListener.onCallException(event.getCode());
                break;
            }
            case CALL_RESTART: {
                LogUtils.i(TAG, "onCallEvent: CALL_RESTART "+event.getInfo());
                callEventListener.onCallRestart();
                break;
            }
            case CALL_EXCEPTION:{
                LogUtils.i(TAG, "onCallEvent: CALL_EXCEPTION "+event.getInfo());
                callEventListener.onCallException(event.getCode());
                break;
            }
            case ICE_FAILED:{
                LogUtils.i(TAG, "onCallEvent: ICE_FAILED "+event.getInfo());
                callEventListener.onMediaChannelDisconnected(event.getCode(),event.getInfo());
                break;
            }
            case CALL_CLOSED:{
                LogUtils.i(TAG, "onCallEvent: CALL_CLOSED "+event.getInfo());
                callEventListener.onCallClosed();
                break;
            }
        }
    }

}
