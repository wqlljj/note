package com.example.wangqi.developutils.bean;

/**
 * Created by cloud on 2018/8/7.
 */

public class EventBean {
    CODE code;
    String msg;
    Object obj;
    public enum CODE{
        DIMENS_LOG
    }

    public CODE getCode() {
        return code;
    }

    public void setCode(CODE code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public EventBean() {
    }

    public EventBean(CODE code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public EventBean(CODE code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", obj=" + obj +
                '}';
    }
}
