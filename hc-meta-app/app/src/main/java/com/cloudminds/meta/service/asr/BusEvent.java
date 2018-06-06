package com.cloudminds.meta.service.asr;

/**
 * Created by mas on 16/10/8.
 */


public class BusEvent {
    private Event mEvent;
    public BusEvent(Event event){
        //默认值 初始化使用
        mEvent = event;
    }
    public enum Event{

        SPEAKING,SPEAK_FINSH,STOP_ASRlISTENING, START_WAKEUPASR,STOP_WAKEUPASR,
        ASRlISTENING_START,ASRlISTENING_FINISH,PLAYMUSIC_START,PLAYMUSIC_FINISH,

        MOVETASKTOBACK
    }

    public Event getEvent() {
        return mEvent;
    }

    public void setEvent(Event event) {
        mEvent = event;
    }

    @Override
    public String toString() {
        return "BusEvent{" +
                "mEvent=" + mEvent +
                '}';
    }
}

