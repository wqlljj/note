package com.cloudminds.meta.manager;

import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.service.asr.AsrService;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.TTSSpeaker;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.cloudminds.meta.service.asr.BusEvent.Event.SPEAKING;
import static com.cloudminds.meta.service.asr.BusEvent.Event.SPEAK_FINSH;
import static com.cloudminds.meta.service.asr.BusEvent.Event.START_WAKEUPASR;
import static com.cloudminds.meta.service.asr.BusEvent.Event.STOP_ASRlISTENING;
import static com.cloudminds.meta.service.asr.BusEvent.Event.STOP_WAKEUPASR;
import static com.cloudminds.meta.util.TTSSpeaker.HIGH;
import static com.cloudminds.meta.util.TTSSpeaker.NAVIINTO;

/**
 * 负责TTS、ASR和MUSIC之间的事件交互和处理
 * Created by SX on 2017/12/1.
 */

public class EventManager {
    public static final int MUSIC_NOPLAY=0;
    public static final int MUSIC_NEEDPLAY=1;
    private int music_state=MUSIC_NOPLAY;
    private static EventManager eventManager;
    private boolean isSpeak;
    public static final String TYPE_ASR="ASR";
    HashMap<String,CallBack> callBackHashMap;
    private String music_url;

    public EventManager() {
        EventBus.getDefault().register(this);
        callBackHashMap=new HashMap<>();
    }
    public static EventManager getInstance(){
        if(eventManager==null)
        eventManager = new EventManager();
        return eventManager;
    }
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onEvent(BusEvent ttsEvent){
        CallBack callBack = callBackHashMap.get(TYPE_ASR);
        if(ttsEvent.getEvent().ordinal()<5) {
            switch (ttsEvent.getEvent()) {
                case SPEAKING:
                    isSpeak = true;
                    callBack.event(SPEAKING);
                    break;
                case SPEAK_FINSH:
                    isSpeak = false;
                    callBack.event(SPEAK_FINSH);
                    if(music_state==MUSIC_NEEDPLAY){
                        music_state=MUSIC_NOPLAY;
                        PlayerUtil.getPlayerUtil(MetaApplication.mContext).playUrl(music_url);
                    }
                    break;
                case STOP_ASRlISTENING:
                    if (callBack != null) {
                        callBack.event(STOP_ASRlISTENING);
                    }
                    break;
                case START_WAKEUPASR:
                    if (callBack != null) {
                        callBack.event(START_WAKEUPASR);
                    }
                    break;
                case STOP_WAKEUPASR:
                    if (callBack != null) callBack.event(STOP_WAKEUPASR);
                    break;
            }
        }else if(ttsEvent.getEvent().ordinal()>4&&ttsEvent.getEvent().ordinal()<7){
            switch (ttsEvent.getEvent()){
                case ASRlISTENING_START:
                    if(TTSSpeaker.isSpeak()&&TTSSpeaker.lastType<HIGH){
                        TTSSpeaker.stop();
                    }
                    PlayerUtil playerUtil = PlayerUtil.getPlayerUtil(MetaApplication.mContext);
                    if(playerUtil.isPlayering()){
                        playerUtil.stop();
                    }
                    break;
                case ASRlISTENING_FINISH:
                    break;
            }
        }else if(ttsEvent.getEvent().ordinal()>6){
            switch (ttsEvent.getEvent()){
                case PLAYMUSIC_START:
                    if(TTSSpeaker.isSpeak()&&TTSSpeaker.lastType<NAVIINTO){
                        TTSSpeaker.stop();
                    }
                    break;
                case PLAYMUSIC_FINISH:
                    break;
            }
        }
    }
    public void addSpeak(String message,int type){
        if(music_state==MUSIC_NEEDPLAY){
            music_state=MUSIC_NOPLAY;
            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,MetaApplication.mContext.getString(R.string.music_cancel));
        }
    }
    public boolean isASRListening(){
        return AsrService.isListening();
    }
    public boolean isEnableASRListening(){
        return !(TTSSpeaker.isSpeak()&&TTSSpeaker.lastType>NAVIINTO);
    }
    public void setMusicState(int type,String url){
        music_state=type;
        music_url = url;
    }
    public boolean isEnablePlayMisic(){
        //语音识别未开启，无播报
        if(AsrService.isListening()||(TTSSpeaker.isSpeak()&&TTSSpeaker.lastType>=NAVIINTO)) {
            return false;
        }else {
            return true;
        }
    }
    public void addCallBack(String type, CallBack callBack){
        callBackHashMap.put(type,callBack);
    }
    public void removeCallBack(String type){
        callBackHashMap.remove(type);
        if(callBackHashMap.size()==0&& EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
    public interface CallBack{
        public void event(BusEvent.Event event);
    }
}
