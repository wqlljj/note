package com.cloudminds.hc.hariservice.manager;

import com.cloudminds.hc.hariservice.call.CallEvent;

import org.webrtc.StatsReport;

/**
 * Created by zoey on 17/4/25.
 */

public class SessionEvent {

    private SessionEvent.Event mEvent;
    private String mInfo;
    private StatsReport[] mObject;
    public SessionEvent(SessionEvent.Event event){
        //默认值 初始化使用
        mEvent = event;
    }

    public SessionEvent(SessionEvent.Event event, String info){
        mEvent = event;
        mInfo = info;
    }

    public enum Event{
        SESSION_CALL_START,
        SESSION_MC_CONNECT_START,    //start connect media channel
        SESSION_SERVER_CONNECTED,
        SESSION_CALL_SESSION_CREATED,
        SESSION_HEARTBEAT_RESPONSE,
        SESSION_CONNECTED,
        SESSION_DISCONNECTED,
        SESSION_RESTART,
        SESSION_CHANNEL_CLOSE,
        SESSION_CHANNEL_CONNECTION_ERROR,
        SESSION_WB_CLOSED,
        SESSION_WS_CLOSED
    }

    public SessionEvent.Event getEvent() {
        return mEvent;
    }

    public void setEvent(SessionEvent.Event event) {
        mEvent = event;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public StatsReport[] getObject() {
        return mObject;
    }

    public void setObject(StatsReport[] obj) {
        mObject = obj;
    }
}
