package com.cloudminds.hc.hariservice.command;

/**
 * Created by zoey on 17/4/17.
 */

public class CmdEvent {

    private CmdEvent.Event mEvent;
    private long mTimestamp;
    private Object mInfo;
    public CmdEvent(CmdEvent.Event event){
        //默认值 初始化使用
        mEvent = event;
        mTimestamp = System.currentTimeMillis();
    }

    public CmdEvent(CmdEvent.Event event, Object info){
        mEvent = event;
        mInfo = info;
        mTimestamp = System.currentTimeMillis();
    }

    public enum Event{
        OBJECT_IDENTIFIED,
        FACE_IDENTIFIED,
        COMMAND_RECEIVED,
        INFO_RECEIVED,
        PUSH_RECEIVED,
        SPEAK_RECEIVED,
        DC_MSG_RECEIVED,
        SLAM_NAVIGATION_START,
        TRACE,
    }


    public Object getInfo() {
        return mInfo;
    }

    public void setInfo(Object info) {
        mInfo = info;
    }

    public CmdEvent.Event getEvent() {
        return mEvent;
    }

    public void setEvent(CmdEvent.Event event) {
        mEvent = event;
    }


    public long getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
