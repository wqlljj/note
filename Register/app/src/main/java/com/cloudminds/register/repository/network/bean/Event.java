package com.cloudminds.register.repository.network.bean;

/**
 * Created by wangqi on 2018/5/25.
 */

public class Event<T> {
    int code;
    T data;

    public Event() {
    }

    public Event(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Event{" +
                "code=" + code +
                ", data=" + data +
                '}';
    }
}
