package com.cloudminds.meta.util;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.cloudminds.hc.hariservice.utils.ThreadPoolUtils;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.bean.SpeakBean;
import com.cloudminds.meta.broadcast.LocaleChangeReceiver;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.EventManager;
import com.cloudminds.meta.service.asr.AsrService;
import com.cloudminds.meta.service.asr.BusEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

import static com.cloudminds.meta.service.asr.AsrService.STATUS_None;

/**
 * Created by zoey on 17/5/4.
 */

public class TTSSpeaker implements LocaleChangeReceiver.LocaleChangeListener {

    public static final String TAG = "Meta/TTSSpeaker";
    private static TTSSpeaker ttsSpeaker = null;
    private static boolean isZh=false;
    private static TextToSpeech tts;
    private static boolean restartAPP=false;
    private SpeechSynthesizer speechSynthesizer;
    private String mSampleDirPath;
    public Context mContext;
    private static AudioManager manager;
    public static final int SPEAK =0;
    public static final int SPEAK_ANSWER=1;
    public static final int NAVIINTO=2;
    public static final int SPEAK_ASR=3;
    public static final int HIGH=4;
    private static HashMap<String,SpeakBean> speaks = new HashMap<>();
    private static int initStatus=-1;

    public static TTSSpeaker instance(Context context){
        if (null == ttsSpeaker){
            ttsSpeaker = new TTSSpeaker(context);
        }

        return ttsSpeaker;
    }

    public static TTSSpeaker getInstance(){
        return ttsSpeaker;
    }


    private TTSSpeaker(Context context){
        LocaleChangeReceiver.addListener(this);
        this.mContext = context;
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                initTTSEnv();
                initTTS();
            }
        });
    }
    private void initTTSEnv() {
        if (mSampleDirPath == null) {
            mSampleDirPath = FileUtils.getSDPath();
        }
        FileUtils.makeDir(mSampleDirPath);
        FileUtils.copyFromAssetsToSdcard(mContext, Constant.SPEECH_FEMALE_MODEL_NAME,
                mSampleDirPath + "/" + Constant.SPEECH_FEMALE_MODEL_NAME);
        FileUtils.copyFromAssetsToSdcard(mContext, Constant.SPEECH_MALE_MODEL_NAME,
                mSampleDirPath + "/" + Constant.SPEECH_MALE_MODEL_NAME);
        FileUtils.copyFromAssetsToSdcard(mContext, Constant.TEXT_MODEL_NAME,
                mSampleDirPath + "/" + Constant.TEXT_MODEL_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_SPEECH_MALE_MODEL_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_TEXT_MODEL_NAME);
    }


    public void initTTS(){
        Log.d(TAG,"initTTS");

        speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(mContext);
        speechSynthesizer.setSpeechSynthesizerListener(synthesizerListener);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE,
                mSampleDirPath + "/"+Constant.TEXT_MODEL_NAME);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                mSampleDirPath + "/"+Constant.SPEECH_FEMALE_MODEL_NAME);

        speechSynthesizer.setAppId(Constant.BAIDU_TTS_APPID);
        speechSynthesizer.setApiKey(Constant.BAIDU_TTS_APPKEY,Constant.BAIDU_TTS_SCREENKEY);

        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "10");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        AuthInfo authInfo = speechSynthesizer.auth(TtsMode.ONLINE);

        if (authInfo.isSuccess()) {
            Log.d(TAG,"auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d(TAG,"auth failed errorMsg=" + errorMsg);
        }
        speechSynthesizer.initTts(TtsMode.ONLINE);

        speechSynthesizer.loadModel(mSampleDirPath + "/" + Constant.TEXT_MODEL_NAME, mSampleDirPath
                + "/" + Constant.SPEECH_FEMALE_MODEL_NAME);


         manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean isOn = manager.isSpeakerphoneOn();
        Log.d(TAG,"speak isOn = "+isOn);


        Log.d(TAG,"speak isOn = "+manager.getMode());
        speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if(tts!=null&&isSpeak()){
            stop();
        }
        tts = new TextToSpeech(mContext,new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
//                    tts.setEngineByPackageName("com.google.android.tts");
                    Log.e(TAG, "onInit: "+tts.getDefaultEngine() );
//                    List<TextToSpeech.EngineInfo> engines = tts.getEngines();
//                    for (TextToSpeech.EngineInfo engine : engines) {
//                        Log.e(TAG, "onInit: engine  "+engine.name );
//                    }
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.e(TAG, "onStart: "+ utteranceId);
                            if(speakingIndex.size()>0){
                                ListIterator<String> stringListIterator = speakingIndex.listIterator();
                                while (stringListIterator.hasNext()){
                                    String index = stringListIterator.next();
                                    if(Integer.valueOf(index)<Integer.valueOf(utteranceId)){
                                        Log.e(TAG, "onStart: remove "+index );
                                        speaks.remove(index);
                                        speakingIndex.remove(index);
                                    }
                                }
                            }
                            speakingIndex.add(utteranceId);
                            EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAKING));
                            updateTime=System.currentTimeMillis();
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            speaks.remove(utteranceId);
                            speakingIndex.remove(utteranceId);
                            Log.e(TAG, "onDone: "+utteranceId +"  "+speaks.size());
                            if(speaks.size()==0) {
                                EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAK_FINSH));
                            }else{
                                updateTime=System.currentTimeMillis();
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e(TAG, "onError: "+utteranceId );
                            if(speaks.containsKey(utteranceId)) {
                                SpeakBean remove = speaks.remove(utteranceId);
                                MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"speakError:"+remove.getText());
                            }
                            if(speaks.size()==0) {
                                EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAK_FINSH));
                            }else{
                                updateTime=System.currentTimeMillis();
                            }
                        }
                    });
                    Log.e(TAG, "onInit: success" );
                    initStatus=TextToSpeech.SUCCESS;
                    if(speaks.size()>0){
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Set<String> keySet = speaks.keySet();
                                for (String key : keySet) {
                                    SpeakBean speakBean = speaks.get(key);
                                    if(!speakBean.isStart())
                                    speak(speakBean.getText(),speaks.get(key).getType(),speaks.get(key).getLanguage(),key);
                                }
                            }
                        }, 1000);
                    }
                }
            }
        },LocaleChangeReceiver.language.equals("zh")?"com.iflytek.speechcloud":"com.google.android.tts");
//        com.svox.pico
//        com.google.android.tts
//        com.iflytek.speechcloud
//        net.eguidedog.ekho.cantonese
    }
    ArrayList<String> speakingIndex=new ArrayList<>();

    public void setSpeechSynthesizerListener(SpeechSynthesizerListener listener){
        speechSynthesizer.setSpeechSynthesizerListener(listener);
    }

    public static void resume(){
        TTSSpeaker speaker = TTSSpeaker.getInstance();
        if(speaker!=null && speaker.speechSynthesizer!=null){
            speaker.speechSynthesizer.resume();
        }
    }

    public static void pause(){
        TTSSpeaker speaker = TTSSpeaker.getInstance();
        if(speaker!=null && speaker.speechSynthesizer!=null){
            speaker.speechSynthesizer.pause();
        }
    }

    public static void stop(){
//        if(IndoorNavigator.type== IndoorNavigator.Type.NAVIING&& lastType>=NAVIINTO)return;
        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"speak stop");
        speaks.clear();
        if(tts.isSpeaking()){
            tts.stop();
            return;
        }
        TTSSpeaker speaker = TTSSpeaker.getInstance();
        if(speaker!=null && speaker.speechSynthesizer!=null){
            speaker.speechSynthesizer.stop();
        }
    }
    public static boolean isSpeak(){
        if(speaks.size()==0||tts==null)return false;
        return tts.isSpeaking();
    }

    public static void cancleSpeech(){
        TTSSpeaker speaker = TTSSpeaker.getInstance();
        TTSSpeaker.stop();
        if(speaker!=null && speaker.speechSynthesizer!=null){
            speaker.speechSynthesizer.release();
        }
    }
    public static void restartAPP(boolean flag){
        Log.e(TAG, "restartAPP: "+flag );
        restartAPP=flag;
    }
    public static int lastType= SPEAK;
    static int id=0;//播报ID，递增
    static long updateTime=0l;
    public static synchronized void speak(String message,int type){
        Log.e(TAG, "speak: enter "+message+"  "+type );
        int typeNum=(isContainChinese(message)?1:0)+(isContainNum(message)?4:0)+(isContainEnglish(message)?2:0);
        switch (typeNum){
            case 1://中
            case 5://中数
                speak(message,type,"zh");
                break;
            case 2://英
            case 6://英数
                speak(message,type,"en");
                break;
            case 4://数
                speak(message,type,LocaleChangeReceiver.language);
                break;
            case 3://中英
            case 7://中英数
                ArrayList<String> words = StringUtils.split(message);
                for (String text : words) {
                    speak(text,type,isContainChinese(text)?"zh":isContainNum(text)?LocaleChangeReceiver.language:"en");
                }
                break;
        }
    }
    private static synchronized void speak(String message,int type,String language,String ... utteranceId){
        if(restartAPP&&!message.equals(MetaApplication.mContext.getString(R.string.app_restart))){
            Log.e(TAG, "speak: restartAPP = "+restartAPP+" "+message );
            return;
        }
        if(initStatus!=TextToSpeech.SUCCESS){
            Log.e(TAG, "speak: initStatus = " +initStatus);
            if(utteranceId.length==0) {
                Log.e(TAG, "speak: "+id+"  "+message );
                speaks.put("" + id, new SpeakBean(id, message, type,language));
                id++;
            }
            return;
        }
        if(EventManager.getInstance().isASRListening()&&type!=HIGH){
            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,message+" 取消播报1  "+AsrService.isListening()+"  "+type);
            Log.e(AsrService.TAG, "speak: 取消播报1"+message+"  "+ AsrService.isListening()+"  "+type);
            return;
        }
        TTSSpeaker speaker = TTSSpeaker.getInstance();
        if(PlayerUtil.getPlayerUtil(speaker.mContext).isPlayering()){
            PlayerUtil.getPlayerUtil(speaker.mContext).stop();
        }
        switch (lastType){
            case SPEAK_ANSWER:
                if(isSpeak()&&type<SPEAK_ANSWER) {
                    MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,message+" 取消播报2  "+isSpeak()+"  "+type+"  "+speaks.size());
                    Log.e(AsrService.TAG, "speak: 取消播报2  "+message+"  "+ isSpeak()+"  "+type+"  "+speaks.size());
                    return;
                }
                break;
            case NAVIINTO:
            case SPEAK_ASR:
            case HIGH:
                if(isSpeak()&&type<NAVIINTO){
                    MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,message+" 取消播报3  "+isSpeak()+"  "+type+"  "+speaks.size());
                    Log.e(AsrService.TAG, "speak: 取消播报3  "+message+"  "+ isSpeak()+"  "+type+"  "+speaks.size());
                    return;
                }
                break;
        }
        Log.e(TAG, "speak: "+isSpeak()+"   "+type+"  "+lastType +"  "+message);
        if(isSpeak()&&type>lastType){
            stop();
        }
        lastType=type;
        if(AsrService.status!=STATUS_None){
            EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_ASRlISTENING));
        }
        EventManager.getInstance().addSpeak(message,type);
        SpeakBean speakBean;
        if(utteranceId.length==0) {
             speakBean = new SpeakBean(id, message, type,language);
            speaks.put("" + id, speakBean);
            id++;
        }else {
            speakBean = speaks.get(utteranceId[0]);
        }
        if(speakBean!=null)
            speakBean.setStart(true);
        updateTime=System.currentTimeMillis();
        if(language.equals("zh")) {
//            if (speaker != null && speaker.speechSynthesizer != null) {
                Log.e(TAG, "speak:  Chinese  "+(utteranceId.length==0?""+(id-1):utteranceId[0])+"  " +message );
                speakChinese(message,utteranceId.length==0?""+(id-1):utteranceId[0]);
//                 speaker.speechSynthesizer.speak(message,utteranceId.length==0?""+(id-1):utteranceId[0]);
//            }
        }else{
            Log.e(TAG, "speak:   english  " +(utteranceId.length==0?""+(id-1):utteranceId[0])+"  " +message);
            speakEng(message,utteranceId.length==0?""+(id-1):utteranceId[0]);
        }
    }
    public static boolean isContainEnglish(String str) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    public static boolean isContainNum(String str) {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    private static void speakChinese(String content,String utteranceId){
        tts.setLanguage(Locale.CHINESE);
        int speak = tts.speak(content, TextToSpeech.QUEUE_ADD, null, utteranceId);
        Log.e(TAG, "speakChinese: "+speak );
    }
    private static void speakEng(String content,String utteranceId){
//        tts.speak(content, TextToSpeech.QUEUE_ADD, null);
        tts.setLanguage(Locale.ENGLISH);
        int speak = tts.speak(content, TextToSpeech.QUEUE_ADD, null, utteranceId);
        Log.e(TAG, "speakEng: "+speak );
    }

    /* ---------------------------     语音播报监听接口      --------------------------------------*/
    private   SpeechSynthesizerListener synthesizerListener = new TTSSpeechSynthesizerListener();

    @Override
    public void onChange(String language) {
        initTTS();
    }

    //    static int speak_num=0;
    public static class TTSSpeechSynthesizerListener implements SpeechSynthesizerListener {
        @Override
        public void onSynthesizeStart(String s) {
            LogUtils.d(TAG,"speak: Start synthesize :"+s);
           // stopListening();
            EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAKING));
//            isSpeak=true;
        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        }

        @Override
        public void onSynthesizeFinish(String s) {
            LogUtils.d(TAG,"speak:Finish synthesize :"+s);
        }

        @Override
        public void onSpeechStart(String s) {
            LogUtils.d(TAG,"speak:Speech start :"+s);
            SpeakBean speakBean = speaks.get(s);
            if(speakBean!=null)
            speakBean.setStart(true);
        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {
            Log.e(TAG, "onSpeechProgressChanged: "+s+"  "+i );
            SpeakBean speakBean = speaks.get(s);
            if(speakBean!=null) {
                speakBean.setUpdateTime(updateTime = System.currentTimeMillis());
            }else {
                Log.e(TAG, "onSpeechProgressChanged: speakBean!=null" );
                Set<String> keys =speaks.keySet();
                for (String key : keys) {
                    Log.e(TAG, "onSpeechProgressChanged: "+speaks.get(key) );
                }
                updateTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onSpeechFinish(String s) {
            LogUtils.d(TAG,"speak:Speech finished :"+s+"   "+speaks.size());
            speaks.remove(s);
            if(speaks.size()==0) {
                EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAK_FINSH));
            }
            //startListening();
//            AIBridge.getInstance().voiceServiceConnector.getAsrService().startListening();
        }

        @Override
        public void onError(String s, SpeechError speechError) {
            LogUtils.d(TAG,"speak:Speech error :"+speechError.description);
            //startListening();
            if(speaks.size()==0) {
                EventBus.getDefault().post(new BusEvent(BusEvent.Event.SPEAK_FINSH));
            }
        }
    };

}
