package com.cloudminds.meta.service;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.hariservice.command.listener.CmdEventListener;
import com.cloudminds.hc.hariservice.webrtc.LooperExecutor;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.bean.AxisData;
import com.cloudminds.hc.metalib.bean.BatteryData;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.bean.MusicBean;
import com.cloudminds.meta.broadcast.BatteryReceiver;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.MetaManager;
import com.cloudminds.meta.model.HubServiceModel;
import com.cloudminds.meta.service.asr.AsrEventListener;
import com.cloudminds.meta.service.asr.AsrService;
import com.cloudminds.meta.service.asr.AsrServiceConnector;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.service.navigation.IndoorNavigator;
import com.cloudminds.meta.service.navigation.LocationMonitor;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.LogUtils;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.cloudminds.meta.service.asr.AsrService.STATUS_None;

/**
 * Created by zoey on 17/5/16.
 */

public class AIBridge {

    private static final String TAG = "META:AIBridge";

    private static AIBridge aiBridge;
    private final HubServiceConnector hubServiceConnector;
    private Context mContext;
    public AsrServiceConnector voiceServiceConnector;
    private static LooperExecutor executor;

    private AIBridge(Context context){
        mContext = context;
        EventBus.getDefault().register(this);
        TTSSpeaker.instance(context);
        initAsrService();
        initHariService();
        LocationMonitor.instance(context);
        OutdoorNavigator.instance(context);
        hubServiceConnector = HubServiceConnector.getIntance(context);
        this.executor = new LooperExecutor();
        executor.requestStart();
        Log.e(TAG, "AIBridge: init");
    }

    public synchronized static AIBridge instance(Context context) {
        if (aiBridge == null) {
            aiBridge = new AIBridge(context);
        }
        return aiBridge;
    }

    public static AIBridge getInstance(){
        return aiBridge;
    }

    public void startListener(String speak){
        voiceServiceConnector.getAsrService().wakeUpSuccess(speak);
    }
    private void initHariService(){

        //HariServiceClient.createService(mContext);
        HariServiceClient.createService(MetaApplication.gApplication, new HariServiceClient.InitServiceCallback() {
            @Override
            public void onServiceInitialized() {
                Log.d(TAG,"on hs initialized");
                MetaApplication.hariServiceInitialized=true;
            }
        });
        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        cmdEngine.setCmdEventListener(cmdEventListener);
        cmdEngine.setUploadRobotInfoEnable(true);
        cmdEngine.setUploadRobotInfoRate(10);

    }

    private void initAsrService(){
        voiceServiceConnector = new AsrServiceConnector();
        voiceServiceConnector.asrEventListener = asrEventListener;
        ComponentName componentName =  voiceServiceConnector.start(mContext);
        if (componentName != null) {
            LogUtils.d(TAG, "语音识别服务启动成功");
            //绑定服务
            voiceServiceConnector.connect(mContext);
        } else {
            LogUtils.d(TAG, "语音识别服务启动失败");
        }
    }
    private boolean checkInternet(){
        boolean networkAvailable = InternetBroadcast.isNetworkAvailable();
        if(!networkAvailable) {
            TTSSpeaker.speak(mContext.getString(R.string.no_internet), TTSSpeaker.HIGH);
        }
        return networkAvailable;
    }
    private AsrEventListener asrEventListener = new AsrEventListener() {
        @Override
        public void onAsrResult(final String result) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG,"Asr result:"+result);
                    MetaApplication.addMessage(ChatMessage.Type.CHAT_RGIHT,result);
                    if(result.equalsIgnoreCase(mContext.getString(R.string.hub_call_stop))){
                        EventBus.getDefault().post(Constant.CALLEND);
                        return;
                    }
                    if(result.toLowerCase().matches(mContext.getString(R.string.check_battery))){
//                    if(result.equalsIgnoreCase(mContext.getString(R.string.check_battery))){
                        if(HCMetaUtils.hasMeta()) {
                            TTSSpeaker.speak(String.format(mContext.getString(R.string.helmet_power), "" + MetaManager.getInstance().getBattery()), TTSSpeaker.SPEAK_ANSWER);
                        }
                        TTSSpeaker.speak(String.format(mContext.getString(R.string.phone_power),""+BatteryReceiver.curlevel),TTSSpeaker.SPEAK_ANSWER);
                        return;
                    }
                    if(!checkInternet()) return;
                    if(HubServiceModel.getState()== Constant.HUB_CONN_IN_CONNECTION) {
                        HariServiceClient.getCommandEngine().sendMessage(result);
                        Log.e(TAG, "发送文本: "+result+"  "+System.currentTimeMillis() );
                    }else{
                        TTSSpeaker.speak(mContext.getString(R.string.no_connect_service), TTSSpeaker.HIGH);
                    }
                }
            });
        }
    };

    private CmdEventListener cmdEventListener = new CmdEventListener() {
        @Override
        public void onSpeak(String type,String content,String operate) {
            Log.e(AsrService.TAG, "onSpeak: "+type+"  "+content +"  "+System.currentTimeMillis());
            if(type.equals("speakOr")&&!MetaApplication.isRecognition()){
                Log.e(TAG, "onSpeak: 取消物体识别播报  "+content );
                return;
            }
            TTSSpeaker.speak(content,type.equals("qa")? TTSSpeaker.SPEAK_ANSWER: TTSSpeaker.SPEAK);
            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,content);
        }

        @Override
        public void onInfo(String info) {
            handleReceivedInfo(info);
        }

        @Override
        public JSONObject robotInfo() {
            return getStatusInfo();
        }
    };

    /*
    获取状态信息  语音识别、meta连接、电量情况
     */
    public   JSONObject getStatusInfo(){
        JSONObject json= null;

        try {
            final BatteryData batteryData = new BatteryData();
            HCMetaUtils.getLibSensorBatteryData(batteryData);
            Log.i("batteryData", "batteryData="+batteryData.toString());

            //当前方位
            AxisData axisData = new AxisData();
            HCMetaUtils.getLibSensorAxisData(axisData);
            float bearing = axisData.getGyro_x();

            json = new JSONObject();
            try {
                //json.put("language", "EN");   //hariservice 自动设置语言参数
                json.put("asrStatus", voiceServiceConnector.getAsrService().getStatus());
                json.put("robotStatus", HCMetaUtils.hasMeta()?1:0);
                //电量信息
                JSONObject batteJson = new JSONObject();
                batteJson.put("capacity", batteryData.getCapacity());
                batteJson.put("residueCapacity", batteryData.getResidue_capacity());
                json.put("battery", batteJson);
                //当前位置信息
                JSONArray curLoc = new JSONArray();
                curLoc.put(LocationMonitor.curLongitude);
                curLoc.put(LocationMonitor.curLatitude);
                json.put("location",curLoc);
                json.put("orientation",bearing);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
        Log.e(TAG, "Meta status info:   ret = "+((json==null)?"null":json.toString()));
        return json;
    }

    private void handleReceivedInfo(final String info){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject object = new JSONObject(info);
                    String type = object.getString("type");
                    if (type.equalsIgnoreCase("motion")){ //手柄指令

                        JSONObject data = object.getJSONObject("data");
                        MetaManager.getInstance().handleMotionCommand(data,MetaManager.NAVICOMMAND_HI);

                    } else if (type.equalsIgnoreCase("intent")){

                        JSONObject dataObj = object.getJSONObject("data");
                        String action = dataObj.getString("action");
                        if(action.equalsIgnoreCase("stopNavi")){

                            if(IndoorNavigator.type!= IndoorNavigator.Type.END_NAVI)
                                IndoorNavigator.sendStopNavi(IndoorNavigator.Type.END_NAVI,"已到达目的地，结束导航！");
                            else if(OutdoorNavigator.getInstance().isStartNavi)
                                OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_USER_STOP);

                            return;
                        }
                        JSONObject paramsObj=null;
                        if(dataObj.has("param")) paramsObj = dataObj.getJSONObject("param");
                        if (action.equalsIgnoreCase("indoorNavi")){   //室内导航

                            Log.e(TAG, "run: inNavi");
                            String endPoint = paramsObj.getString("to");
                            if(OutdoorNavigator.getInstance().isStartNavi)
                                OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_USER_STOP);
                            IndoorNavigator.sendStartNavi(paramsObj.getString("from"),endPoint);
                            AsrService.status=STATUS_None;
                            EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_ASRlISTENING));

                        } else if(action.equalsIgnoreCase("outdoorNavi")){  //室外导航

                            String endPoint = paramsObj.getString("to");
                            Log.e(TAG, "run: outNavi");
                            if(IndoorNavigator.type!= IndoorNavigator.Type.END_NAVI)
                                IndoorNavigator.sendStopNavi(IndoorNavigator.Type.END_NAVI,"用户停止导航");

                            OutdoorNavigator.getInstance().startNaviTo(endPoint);
                            AsrService.status=STATUS_None;
                            EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_ASRlISTENING));
                        }else if(action.equalsIgnoreCase("playMusic")){
                            Log.e(TAG, "run: playMusic" +paramsObj.toString());
                            MusicBean musicBean = new MusicBean();
                            musicBean.setName(paramsObj.getString("name"));
                            musicBean.setSinger(paramsObj.getString("singer"));
                            musicBean.setPictureUrl(paramsObj.getString("picUrl"));
//                           musicBean.setMusicUrl(dataObj.getString("musicUrl"));
                            musicBean.setMusicUrl("http://sc1.111ttt.com/2017/1/11/11/304112004168.mp3");
                            TTSSpeaker.speak("播放"+musicBean.getSinger()+"的"+musicBean.getName(),TTSSpeaker.HIGH);
                            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"播放"+musicBean.getSinger()+"的"+musicBean.getName());
                            PlayerUtil.getPlayerUtil(MetaApplication.mContext).playUrl(musicBean.getMusicUrl());
                        }if(action.equalsIgnoreCase("stopMusic")){
                            PlayerUtil playerUtil = PlayerUtil.getPlayerUtil(MetaApplication.mContext);
                            if(playerUtil.isPlayering()) {
                                TTSSpeaker.speak(mContext.getString(R.string.music_stop),TTSSpeaker.SPEAK);
                                MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,mContext.getString(R.string.music_stop));
                                playerUtil.stop();
                            }
                        }

                    } else if (type.equalsIgnoreCase("naviInfo")){//AI导航指令

                        JSONObject data = object.getJSONObject("data");
                        if(data.has("navi")) {
                            JSONObject navi = data.getJSONObject("navi");
                            IndoorNavigator.handleNaviInfo(navi);
                        }

                    } else if (type.equalsIgnoreCase("startNavi")){   //坐席发起室外导航

                        JSONObject data = object.getJSONObject("data");
                        JSONArray location = data.getJSONArray("to");
                        OutdoorNavigator.getInstance().startNaviTo(location.getDouble(0),location.getDouble(1));

                    } else if (type.equalsIgnoreCase("warn")) {  //服务器端异常

                        JSONObject data = object.getJSONObject("data");
                        String code = data.getString("code");
                        if (code.equalsIgnoreCase("10000")){  //坐席侧异常
                            if (OutdoorNavigator.getInstance().isStartNavi || IndoorNavigator.type == IndoorNavigator.Type.NAVIING){
                                TTSSpeaker.speak(mContext.getString(R.string.hari_warn_hi_error), TTSSpeaker.HIGH);
                            }
                        }
                    } else if (type.equalsIgnoreCase("naviInfoResponse") || type.equalsIgnoreCase("startNaviResponse") || type.equalsIgnoreCase("updateNaviResponse")) {

                        OutdoorNavigator.getInstance().onNaviInfoResponse(type);
					} else if (type.equalsIgnoreCase("queryRobotStatusReq")) {

                        onQueryRobotStatusReq(object);

                    } else if (type.equalsIgnoreCase("operate")) {

                        JSONObject dataObj = object.getJSONObject("data");
                        String action = dataObj.getString("action");
                        if (action.equalsIgnoreCase("configure")){
                            JSONObject paramObj = dataObj.getJSONObject("param");
                            onConfiguration(paramObj);
                         }else if (action.equalsIgnoreCase("app_restart")){
                            Log.e(TAG, "run: app_restart" );
                            TTSSpeaker.speak(mContext.getString(R.string.app_restart),TTSSpeaker.HIGH);
                            if (OutdoorNavigator.getInstance().isStartNavi)
                                    OutdoorNavigator.getInstance().stopNavi(OutdoorNavigator.STOP_REASON_USER_STOP);
                                if (IndoorNavigator.type != IndoorNavigator.Type.END_NAVI)
                                    IndoorNavigator.sendStopNavi(IndoorNavigator.Type.END_NAVI, mContext.getString(R.string.navi_end_1));
                            TTSSpeaker.restartAPP(true);
                            isRestartApp=true;

                        }
                    }

                }catch (JSONException e){
//                    TTSSpeaker.speak("不太明白您的意思",TTSSpeaker.NORMAL);
                    MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,info);
                    LogUtils.d(TAG,"Parse JSON info error:"+e.getMessage());
                }
            }
        });
    }
    boolean isRestartApp=false;
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onTtsEvent(BusEvent ttsEvent){
        switch (ttsEvent.getEvent()) {
            case SPEAK_FINSH:
                Log.e(TAG, "onTtsEvent: SPEAK_FINSH  restartAPP "+isRestartApp );
                if(isRestartApp) {
                    isRestartApp=false;
                    EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
                    SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_RESTARTCALL, true);
                    HariServiceClient.getCallEngine().cleanup();
                }
                break;
        }
    }

    /**
     * 坐席侧设置bps fps
     * @param object
     */
    private void onConfiguration(JSONObject object){
        try {
            if (object.has("rcuCamera")){
                JSONObject rcuCamObj = object.getJSONObject("rcuCamera");
                boolean hasVideoBpsChange = false;
                CallEngine callEngine = HariServiceClient.getCallEngine();
                if (rcuCamObj.has("videoBps")) {
                    int videoBps = rcuCamObj.getInt("videoBps");
                    if (videoBps != Integer.parseInt(callEngine.getParam(CallEngine.PARAM_VIDEO_BPS))) {
                        hasVideoBpsChange = true;
                    }
                    callEngine.setParam(CallEngine.PARAM_VIDEO_BPS, Integer.toString(videoBps));
                }
                if (rcuCamObj.has("videoFps")) {
                    int videoFps = rcuCamObj.getInt("videoFps");
                    callEngine.setParam(CallEngine.PARAM_VIDEO_FPS, Integer.toString(videoFps));
                }

                if (hasVideoBpsChange){
                    callEngine.restartCall();
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * 坐席侧查询客户端当前的bps fps  视频流状态
     * @param object
     */
    private void onQueryRobotStatusReq(JSONObject object){
        try {
            JSONObject data = object.getJSONObject("data");
            JSONArray parameters = data.getJSONArray("parameters");
            if(parameters.length()>0&&parameters.get(0).equals("config.rcuCamera")){
                queryRobotStatusResponse();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * 上传客户端当前的bps fps  视频流状态
     */
    public void queryRobotStatusResponse(){

        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        CallEngine callEngine = HariServiceClient.getCallEngine();
        try {
            JSONObject config = new JSONObject();
            JSONObject rcuCamera = new JSONObject();
            rcuCamera.put("rcuCameraStatus", callEngine.isVideoStopped()?"paused":"streaming");
            rcuCamera.put("videoBps",callEngine.getParam(CallEngine.PARAM_VIDEO_BPS));
            rcuCamera.put("videoFps",callEngine.getParam(CallEngine.PARAM_VIDEO_FPS));
            config.put("rcuCamera",rcuCamera);
            data.put("config",config);
            json.put("type","queryRobotStatusResp");
            json.put("data",data);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e(TAG, "queryRobotStatusResp " );
        HariServiceClient.getCommandEngine().sendData(json);
    }

}
