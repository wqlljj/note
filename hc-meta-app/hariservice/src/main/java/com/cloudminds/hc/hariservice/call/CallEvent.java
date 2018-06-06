package com.cloudminds.hc.hariservice.call;
/**
 * Created by mas on 16/10/8.
 */

import org.webrtc.StatsReport;

public class CallEvent {
    private Event mEvent;
    private String mInfo;
    private String mCode;
    private StatsReport[] mObject;

    /**
     * 呼叫过程中异常code
     * */
    // hariservice 本地错误 1000开始   媒体 2000开始    信令 3000开始
    public static final String CODE_NO_PERMSSION_RPS = "1001";
    public static final String CODE_CAMERA_ERROR = "1002";
    public static final String CODE_ICE_FAILED = "2000";
    public static final String CODE_CONNECT_TIMEOUT = "2001";

    public static final String CODE_STOPPED = "3050";
    public static final String CODE_SIGNAL_REGISTER_ERROR = "3051";
    public static final String CODE_SIGNAL_ELSE_ERROR = "3052";
    public static final String CODE_SIGNAL_REJECTED = "3053";
    public static final String CODE_SIGNAL_CLOSED = "3054";

    public static final String CODE_EXPN_WEBRTC_DELAY = "3001";
    public static final String CODE_EXPN_SINGAL_HEARTBEAT_TIMEOUT = "3002";
    public static final String CODE_EXPN_WEBRTC_LOW_BITRATE = "3003";
    public static final String CODE_EXPN_ICE_DISCONNECTED = "2001";
    public static final String CODE_EXPN_HELPER_EXCEPTION = "10000";
    public static final String CODE_EXPN_NO_FREE_HELPER = "100001";   //切换人工 无人工坐席
    public static final String CODE_EXPN_HELPER_SLOW = "100002";   //切换人工无响应 超过20秒未切换成功


    public CallEvent(Event event){
        //默认值 初始化使用
        mEvent = event;
    }

    public CallEvent(Event event, String code, String info){
        mEvent = event;
        mInfo = info;
        mCode = code;
    }

    public enum Event{
        CALL_RINGING,
        CALL_WS_CONNECTED,
        CALL_CONNECTED,
        CALL_STOP,
        CALL_REJECTED,
        CALL_FAILED,
        CALL_RESTART,
        CALL_CLOSED,
        ICE_DISCONNECTED,
        ICE_FAILED,
        CALL_EXCEPTION,
        SIGNAL_CONNECTION_ERR,
        SIGNAL_CONNECTION_CLOSED,
        STATS_REPORT
    }

    public Event getEvent() {
        return mEvent;
    }

    public void setEvent(Event event) {
        mEvent = event;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public String getCode() { return mCode; }

    public void setCode(String code) { mCode = code; }

    public StatsReport[] getObject() {
        return mObject;
    }

    public void setObject(StatsReport[] obj) {
        mObject = obj;
    }
}

