package com.cloudminds.meta.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.util.Log;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.call.listener.CallEventListener;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.metalib.bean.AlarmData;
import com.cloudminds.hc.metalib.broadcast.MetaWakeUpReceiver;
import com.cloudminds.hc.metalib.manager.MetaSensorManager;
import com.cloudminds.meta.R;
import com.cloudminds.meta.aidl.IHubReceiver;
import com.cloudminds.meta.aidl.IHubService;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.model.HubServiceModel;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

//import static com.cloudminds.hc.hariservice.call.CallEngine.Callee.HARI_CALLEE_AI;
//import static com.cloudminds.hc.hariservice.call.CallEngine.Callee.HARI_CALLEE_HI;

/**
 * Created by tiger on 17-4-17.
 */

public class HubService extends Service implements SpeechSynthesizerListener
        ,RecognitionListener ,MetaWakeUpReceiver.WakeUpListener,
        MetaSensorManager.BatteryListener, CallEventListener  {

    public static final String TAG = "META/HubService";

    private HubServiceModel mModel;
    private PlayerUtil playerUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"oncreate ");
        mModel = new HubServiceModel(this);
        playerUtil = PlayerUtil.getPlayerUtil(this);
        EventBus.getDefault().register(this);

    }
    private final int HEARTBEAT=1001;
    private final int CALLHI=1002;
    public Handler handler=new Handler(){
        int i=0;
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            super.handleMessage(msg);
            Log.e(TAG, "handleMessage: " );
            switch (msg.what) {
                case HEARTBEAT:
                playerUtil.playDi(HubService.this, R.raw.beep);
                handler.sendEmptyMessageDelayed(HEARTBEAT, 10000);
                    break;
                case CALLHI:
                    mModel.callStart(CallEngine.getHARI_CALLEE_HI());
                    break;
            }
        }
    };
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onTtsEvent(BusEvent ttsEvent){
        switch (ttsEvent.getEvent()) {
            case SPEAK_FINSH:
                isSpeak=false;
                if(hasNoSpeak){
                    mModel.speak(messages.get(0));
                    messages.remove(0);
                    if(messages.size()==0)
                        hasNoSpeak=false;
                }
                break;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
     CallEngine.Callee callee=CallEngine.getHARI_CALLEE_AI();
    public class HubBinder extends IHubService.Stub{

        @Override
        public void setCallStateListener(IHubReceiver receiver) throws RemoteException {
            mModel.setReceiver(receiver);
        }

        @Override
        public void callStart(int type) throws RemoteException {
            callee=type==0?CallEngine.getHARI_CALLEE_AI():CallEngine.getHARI_CALLEE_HI();
            mModel.callStart(callee);
        }

        @Override
        public void callStop() throws RemoteException {
            if(handler.hasMessages(HEARTBEAT))
            handler.removeMessages(HEARTBEAT);
            mModel.callStop();
        }

        @Override
        public void sendMessage(String msg) throws RemoteException {
            mModel.sendMessage( msg);
        }

        @Override
        public void sendData(String data) throws RemoteException {
            mModel.sendData(data);
        }
    }


    private HubBinder mBinder = new HubBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onSynthesizeStart(String s) {
        Log.d(TAG,"onSynthesizeStart ,s="+s);
    }
    @Override
    public void onSpeechStart(String s) {
        Log.d(TAG,"onSpeechStart ,s="+s);
    }
    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        Log.d(TAG,"onSynthesizeDataArrived ,s="+s);
    }

    @Override
    public void onSynthesizeFinish(String s) {
        Log.d(TAG,"onSynthesizeFinish ,s="+s);
    }



    @Override
    public void onSpeechProgressChanged(String s, int i) {
        Log.d(TAG,"onSpeechProgressChanged ,s="+s);
    }

    @Override
    public void onSpeechFinish(String s) {
        Log.d(TAG,"onSpeechFinish ,s="+s);
        isSpeak=false;
        if(hasNoSpeak){
            mModel.speak(messages.get(0));
            messages.remove(0);
            if(messages.size()==0)
                hasNoSpeak=false;
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.d(TAG,"onError ,speechError="+speechError);
        isSpeak=false;
        if(hasNoSpeak){
            mModel.speak(messages.get(0));
            messages.remove(0);
            if(messages.size()==0)
                hasNoSpeak=false;
        }
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {

        Log.d(TAG,"onReadyForSpeech --"+bundle.toString());

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG,"onBeginningOfSpeech -- ");
    }

    @Override
    public void onRmsChanged(float v) {
//        Log.d(TAG,"onRmsChanged -- "+v);

    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d(TAG,"onBufferReceived -- ");

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG,"onEndOfSpeech -- ");
        mModel.onEndOfSpeech();
    }

    @Override
    public void onError(int i) {
        Log.d(TAG,"onError -- "+i);
        if(i!=8)
        mModel.onError();
    }

    @Override
    public void onResults(Bundle bundle) {
        Log.e(TAG, "onResults: "+bundle);
        mModel.handleSpeechResult(bundle);

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d(TAG,"onPartialResults -- "+bundle);


    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG,"onEvent -- "+i+",bundle = "+bundle);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.d(TAG,"onDestroy ");
        mModel.onDestroy();
        mModel=null;
    }

    @Override
    public void wakeUp() {
        callee=CallEngine.getHARI_CALLEE_AI();
        mModel.callStart(callee);
        Log.d(TAG,"wakeUp---" );

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG,"onTaskRemoved ");
        mModel.reStart();
    }
    ArrayList<String> messages=new ArrayList<>();
    static boolean hasNoSpeak=false;
   static boolean isSpeak=false;
    int pawerLevel=0;//0正常，1低于20，2低于15，3低于10，4低于5，5低于1
    long speakTime =0;
    @Override
    public void alarm(AlarmData alarmData) {
        String message;
        switch (alarmData.getCode()){
            case 100://电量过低
                int dump_energy = (int) alarmData.getData();
                message = String.format(getString(R.string.dump_energy),alarmData.getMsg(), dump_energy);
                switch (pawerLevel){
                    case 0:
                        energyAlarm(message,dump_energy);
                        break;
                    case 1:
                        if(dump_energy<=15){
                            energyAlarm(message,dump_energy);
                        }
                        break;
                    case 2:
                        if(dump_energy<=10){
                            energyAlarm(message,dump_energy);
                        }
                        break;
                    case 3:
                        if(dump_energy<=5){
                            energyAlarm(message,dump_energy);
                        }
                        break;
                    case 4:
                        if(dump_energy<=1){
                            energyAlarm(message,dump_energy);
                        }
                        break;
                    case 5:
                        if(dump_energy<=1&&System.currentTimeMillis()-speakTime>=30000){
                            energyAlarm(message,dump_energy);
                        }else if(dump_energy>1){
                            energyAlarm(message,dump_energy);
                        }
                        break;
                }
                Log.e(TAG, "alarm: 100  "+isSpeak);
                break;
            case 101://电池老化严重
                message=String.format(getString(R.string.aging_degree),alarmData.getMsg(),(100-(int)alarmData.getData()));
                if(isSpeak){
                    Log.e(TAG, "alarm: 101" );
                    hasNoSpeak=true;
                    messages.add(message);
                }else {
                    isSpeak=true;
                    mModel.speak(message);
                }
                Log.e(TAG, "alarm: 101  "+isSpeak);
                break;
        }
        Log.d(TAG,"alarm--- " +alarmData.toString());
    }

    private void energyAlarm(String message,int dump_energy) {
        if(isSpeak){
            Log.e(TAG, "alarm: 100" );
            hasNoSpeak=true;
            messages.add(message);
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
        }else {
            isSpeak=true;
            mModel.speak(message);
        }
        if(dump_energy<=1){
            pawerLevel=5;
            speakTime=System.currentTimeMillis();
        }else if(dump_energy<=5){
            pawerLevel=4;
        }else if(dump_energy<=10){
            pawerLevel=3;
        }else if(dump_energy<=15){
            pawerLevel=2;
        }else if(dump_energy<=20){
            pawerLevel=1;
        }
    }

    @Override
    public void keyEventCallback(int code, int status) {
        Log.e(TAG, "keyEventCallback: "+"code = "+code+"  status = "+status );
        if(status==0){
            switch (code){
                case 0://中
                    if(mModel.getState()==1||mModel.getState()==2){
                        if(callee!=CallEngine.getHARI_CALLEE_HI()) {
                            if(handler.hasMessages(HEARTBEAT))
                                handler.removeMessages(HEARTBEAT);
                            mModel.callStop();
                            callee = CallEngine.getHARI_CALLEE_HI();
                            handler.sendEmptyMessageDelayed(CALLHI,10000);
                        }else{
                            TTSSpeaker.speak(getString(R.string.call_many_times), TTSSpeaker.HIGH);
                            return;
                        }
                    }else {
                        if(!handler.hasMessages(CALLHI)) {
                            callee = CallEngine.getHARI_CALLEE_HI();
                            handler.sendEmptyMessage(CALLHI);
                        }else {
                            TTSSpeaker.speak(getString(R.string.call_many_times), TTSSpeaker.HIGH);
                            return;
                        }
                    }
                    break;
                case 1://左
                    mModel.recognizeOne();
                    break;
                case 2://右
                    adjustVolume();
                    break;
            }

        }
    }

    private void adjustVolume() {
        //音量控制,初始化定义
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(currentVolume>=maxVolume/3*2&&currentVolume<maxVolume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume , 0); //tempVolume:音量绝对值
        }else if(currentVolume<maxVolume/3*2&&currentVolume>=maxVolume/5){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume /3*2, 0); //tempVolume:音量绝对值
        }else{
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume /5, 0); //tempVolume:音量绝对值
        }
    }

    @Override
    public void onCallConnecting() {
        Log.d(TAG,"onCallConnecting--");
    }

    @Override
    public void onMessageChannelConnected() {
        //test
        mModel.onCallConnected();
    }

    @Override
    public void onMediaChannelConnected() {
        Log.d(TAG,"onCallConnected--");
        if(!handler.hasMessages(HEARTBEAT))
        handler.sendEmptyMessageDelayed(HEARTBEAT,10000);
        mModel.onCallConnected();
    }

    @Override
    public void onMediaChannelDisconnected(String code,String data) {
        Log.d(TAG,"onCallDisconnected--"+data);
        if(handler.hasMessages(HEARTBEAT))
            handler.removeMessages(HEARTBEAT);
        mModel.onCallDisconnected(code,data);
    }

    @Override
    public void onCallError(String code,String error) {
        Log.d(TAG,"onCallError--"+error);
        mModel.onCallError(error);
    }

    @Override
    public void onCallClosed() {
        Log.e(TAG, "onCallClosed: "+handler.hasMessages(CALLHI) );
        //挂断上次会话，发起紧急呼叫
        if(handler.hasMessages(CALLHI)) {
            handler.removeMessages(CALLHI);
            handler.sendEmptyMessageDelayed(CALLHI, 0);
        }
        mModel.onCallClosed();
    }

    @Override
    public void onCallRestart() {
        Log.e(TAG, "onCallRestart: " );

    }

    @Override
    public void onCallException(String code) {
        Log.e(TAG, "onCallException: " );
        mModel.onCallException(code);
    }
}
