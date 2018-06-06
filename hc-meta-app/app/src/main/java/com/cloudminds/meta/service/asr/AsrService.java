package com.cloudminds.meta.service.asr;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.VoiceRecognitionService;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.service.navigation.IndoorNavigator;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.LogUtils;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.TTSSpeaker;
import com.getui.logful.appender.LogEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ai.kitt.snowboy.SnowboyCallBack;
import ai.kitt.snowboy.SnowboyClient;
import de.greenrobot.event.EventBus;

import static com.cloudminds.meta.service.asr.BusEvent.Event.ASRlISTENING_FINISH;
import static com.cloudminds.meta.service.asr.BusEvent.Event.ASRlISTENING_START;
import static com.cloudminds.meta.service.asr.BusEvent.Event.STOP_ASRlISTENING;
import static com.cloudminds.meta.service.navigation.IndoorNavigator.Type.NAVIING;

/**
 * Created by zoey on 17/5/16.
 */

public class AsrService  extends Service implements RecognitionListener,  com.cloudminds.meta.manager.EventManager.CallBack, SnowboyCallBack {

    public static final String TAG = "META/AsrService";
    private AsrServiceBinder binder = new AsrServiceBinder();

    private EventManager mWpEventManager;  //唤醒监听
    private static SpeechRecognizer speechRecognizer; //语音识别
    private AsrEventListener asrEventListener;


    public static final int STATUS_None = 1;
    public static final int STATUS_NEED = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;

    private static final int WAKEUP_RETRY=101;


    public static boolean isListening=false;
    public static int status=STATUS_None;
    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;
    private PlayerUtil playerUtil;

    private int errorCode = -1;

    private boolean isWakeUp=false;
    private int lastIndex=-1;
    private int index=0;

    public int getStatus(){
        if (errorCode > 0){
            return  errorCode;
        }

        return status==STATUS_None?0:1;
    }
    private void play(int playId){
        playerUtil.playDi(this,playId);
    }

    @Override
    public void event(BusEvent.Event  event) {
        Log.e(TAG, "event: "+event );
        switch (event){
            case SPEAKING:
                break;
            case SPEAK_FINSH:
                if(status!=STATUS_None) {
                    this.event=null;
//                    Log.e(TAG, "onTtsEvent: play_start");
                    index++;
                    startListening();
                }
                break;
            case STOP_ASRlISTENING:
                Log.e(TAG, "onTtsEvent: STOP_ASRlISTENING");
                this.event=STOP_ASRlISTENING;
                stopListening();
                break;
            case START_WAKEUPASR:
                if(!isWakeUp) {
//                    isWakeUp=true;
//                    SnowboyClient.getInstance().startRecording();
//                    TTSSpeaker.speak(getString(R.string.wakeup_start),TTSSpeaker.HIGH);
                    startWakeUpListening();
                }
                break;
            case STOP_WAKEUPASR:
                if(isWakeUp) {
//                    isWakeUp=false;
//                    SnowboyClient.getInstance().stopRecording();
//                    TTSSpeaker.speak(getString(R.string.wakeup_end),TTSSpeaker.HIGH);
                    stopWakeUpListening();
                }
                break;
        }
    }

    @Override
    public void onEvent(SnowboyCallBack.Event event) {
        if(event.name().equals(Event.ACTIVE.name())){
            LogUtils.d(TAG,"唤醒成功, 唤醒词: "+isListening);
            if(!isListening) {
                if(OutdoorNavigator.isStartNavi|| IndoorNavigator.type==NAVIING){
                    wakeUpSuccess(MetaApplication.mContext.getString(R.string.navi_speak_pause));
                }else {
                    wakeUpSuccess(getString(R.string.hello));
                }
            }
        }
    }


    public class AsrServiceBinder extends Binder {
        public AsrService getService() {return AsrService.this;}
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i(TAG,"AsrService onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        LogUtils.i(TAG,"AsrService onCreate");
        super.onCreate();
//        EventBus.getDefault().register(this);
        com.cloudminds.meta.manager.EventManager.getInstance().addCallBack(com.cloudminds.meta.manager.EventManager.TYPE_ASR,this);
        playerUtil = PlayerUtil.getPlayerUtil(this);
        initSpeehRecognizer();
        SnowboyClient.init(this);
        SnowboyClient.getInstance().setCallBack(this);
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG,"AsrService onDestory");
//        EventBus.getDefault().unregister(this);
        SnowboyClient.getInstance().onDestroy();
        com.cloudminds.meta.manager.EventManager.getInstance().removeCallBack(com.cloudminds.meta.manager.EventManager.TYPE_ASR);
        stopWakeUpListening();
        speechRecognizer.destroy();
        super.onDestroy();
    }
    BusEvent.Event event=null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG,"AsrService onStartCommand");

        return START_STICKY;
    }

    public void setAsrEventListener(AsrEventListener listener){
        asrEventListener = listener;
    }

    /*
    * 开起唤醒监听 你好小度，你好魅闼
    */
    private void startWakeUpListening(){
        isWakeUp=true;
        LogUtils.d(TAG,"启动唤醒");
        // 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(getApplicationContext(), "wp");

        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));
                try {
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word");
                        LogUtils.d(TAG,"唤醒成功, 唤醒词: " + word + "\r\n");
                        if(!isListening()) {
                            if(OutdoorNavigator.isStartNavi|| IndoorNavigator.type==NAVIING){
                                wakeUpSuccess(MetaApplication.mContext.getString(R.string.navi_speak_pause));
                            }else {
                                wakeUpSuccess(getString(R.string.hello));
//                                wakeUpSuccess("啊啊啊啊啊啊啊啊啊啊啊啊啊");
                            }
                        }
                    } else if ("wp.exit".equals(name)) {
                        LogUtils.d(TAG,"唤醒已经停止: " + params + "\r\n");
                    }else if("wp.enter".equals(name)){
                        LogUtils.d(TAG,"唤醒已经唤醒: " + params + "\r\n");
                        TTSSpeaker.speak(getString(R.string.wakeup_start),TTSSpeaker.HIGH);
                        mHandler.removeMessages(WAKEUP_RETRY);
                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });

        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);
        mHandler.sendEmptyMessageDelayed(WAKEUP_RETRY,5000);
    }

    public void wakeUpSuccess(String speak) {
        if(isListening()){
            Log.e(TAG, "wakeUpSuccess: "+status+"  "+(System.currentTimeMillis()-startTime ));
            return;
        }
        status=STATUS_NEED;
        Log.e(TAG, "wakeUpSuccess: ");
        if(!TextUtils.isEmpty(speak)) {
            TTSSpeaker.speak(speak, TTSSpeaker.SPEAK_ASR);
        }else{
            index++;
            startListening();
        }
    }

    Handler mHandler=new Handler(){

        int tryNum=0;
        int i=0;
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            super.handleMessage(msg);
            switch (msg.what){
                case WAKEUP_RETRY :
                    tryNum++;
                    if(tryNum<3) {
                        TTSSpeaker.speak(getString(R.string.wakeup_timeout),TTSSpeaker.HIGH);
                        startWakeUpListening();
                    }else{
                        TTSSpeaker.speak(getString(R.string.wakeup_fail),TTSSpeaker.HIGH);
                    }
                    break;
            }
        }
    };

    /*
     * 停止唤醒监听
     */
    private void stopWakeUpListening(){
        if(isListening())stopListening();
        isWakeUp=false;
        LogUtils.d(TAG,"停止唤醒");
        TTSSpeaker.speak(getString(R.string.wakeup_end),TTSSpeaker.HIGH);
        mWpEventManager.send("wp.stop", null, null, 0, 0);
    }

    private void initSpeehRecognizer(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(),
                new ComponentName(getApplicationContext(), VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);
    }
    static long startTime=-1;
    public  void startListening(){
        Log.e(TAG, "startListening: " );
        play(R.raw.bdspeech_recognition_start);
        if(!InternetBroadcast.isNetworkAvailable()){
            isListening=false;
            status=STATUS_None;
            TTSSpeaker.speak(getString(R.string.no_internet)+getString(R.string.speech_recognition_failed),TTSSpeaker.HIGH);
            return;
        }
        if(!com.cloudminds.meta.manager.EventManager.getInstance().isEnableASRListening()){
            status = STATUS_NEED;
            Log.e(TAG, "startListening: isSpeak");
            return;
        }
        if(!isListening)isListening=true;
        Log.e(TAG, "startListening: startTime = "+startTime+"  index = "+index+"  startIndex = "+lastIndex );
        if(startTime==-1){
            startTime=System.currentTimeMillis();
        }else if(System.currentTimeMillis()-startTime>=25000){
            lastIndex=index;
            startTime=-1;
            status = STATUS_None;
            isListening=false;
            play(R.raw.bdspeech_recongition_end);
            Log.e(TAG, "startListening: 已结束" );
            EventBus.getDefault().post(new BusEvent(ASRlISTENING_FINISH));
            return;
        }
        EventBus.getDefault().post(new BusEvent(ASRlISTENING_START));
        Intent intent = new Intent();
        Log.e(TAG, "startListening: language = "+ getResources().getConfiguration().locale.getLanguage());
        intent.putExtra("language", getResources().getConfiguration().locale.getLanguage().equals("zh")?"cmn-Hans-CN":"en-GB");//en-GB,cmn-Hans-CN
        intent.putExtra("prop", 20000);
        speechRecognizer.startListening(intent);
        errorCode = -1;
        Log.e(TAG, "startListening: "+status);
    }
    public static boolean isListening(){
        return isListening&&System.currentTimeMillis()-startTime<25000;
    }
    private  void stopListening(){
        Log.e(TAG, "stopListening: " );
        if(status!=STATUS_None) {
            status = STATUS_None;
            speechRecognizer.stopListening();
            EventBus.getDefault().post(new BusEvent(ASRlISTENING_FINISH));
        }
        isListening=false;
        errorCode = -1;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
        errorCode = -1;
        print("准备就绪，可以开始说话");
        Log.e(TAG, "onReadyForSpeech: " );

    }

    @Override
    public void onBeginningOfSpeech() {
        if(status!=STATUS_None) {
            time = System.currentTimeMillis();
            status = STATUS_Speaking;
            print("检测到用户的已经开始说话");
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        if(status!=STATUS_None)
        speechEndTime = System.currentTimeMillis();
        status = STATUS_Recognition;
        print("检测到用户的已经停止说话");
    }


    @Override
    public void onError(int error) {
        time = 0;
        errorCode = 1000+error;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        if(event!=null&&event==STOP_ASRlISTENING){
            event=null;
            return;
        }
        sb.append(":" + error);
        print("识别失败：" + sb.toString()+"   "+index);
            if(error!=SpeechRecognizer.ERROR_RECOGNIZER_BUSY&&error!=SpeechRecognizer.ERROR_CLIENT){
                Log.e(TAG, "onError: play_start" );
                startListening();
            } else if(error!=SpeechRecognizer.ERROR_RECOGNIZER_BUSY){
                startListening();
            }
    }

    @Override
    public void onResults(Bundle results) {
        long end2finish = System.currentTimeMillis() - speechEndTime;
        play(R.raw.bdspeech_recognition_success);
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()]))+"  "+System.currentTimeMillis());
        String json_res = results.getString("origin_result");
            try {
            print("origin_result=\n" + new JSONObject(json_res).toString(4));
        } catch (Exception e) {
            print("origin_result=[warning: bad json]\n" + json_res);
        }

        String strEnd2Finish = "";
        if (end2finish < 60 * 1000) {
            strEnd2Finish = "(waited " + end2finish + "ms)";
        }
        LogUtils.d(TAG,"OnResults:"+nbest.get(0)+"  "+strEnd2Finish);
        if (null != asrEventListener){
            asrEventListener.onAsrResult(nbest.get(0));
        }
        isListening = false;
        status = STATUS_None;
        lastIndex=index;
        startTime=-1;
        EventBus.getDefault().post(new BusEvent(ASRlISTENING_FINISH));
//        if(IndoorNavigator.type== IndoorNavigator.Type.NAVIING||OutdoorNavigator.getInstance().isStartNavi) {
//            status = STATUS_None;
//        }else {
//            status = STATUS_NEED;
//        }

        time = 0;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
            print("~临时识别结果：" + Arrays.toString(nbest.toArray(new String[0]))+"  "+System.currentTimeMillis());
            //txtResult.setText(nbest.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
                print("EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
                print("*引擎切换至" + (type == 0 ? "在线" : "离线"));
                break;
        }
    }

    long time;
    private void print(String msg) {
        Log.d(TAG, "----" + msg);
    }

}
