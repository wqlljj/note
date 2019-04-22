package com.example.wangqi.developutils.bean;

import java.util.HashMap;

/**
 * Created by cloud on 2018/11/12.
 */

public class LogBean {
    HashMap<String,String> data=new HashMap<>();

    public LogBean(HashMap<String, String> data) {
        this.data = data;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
    public String getItemData(String name) {
        return data.get(name);
    }

    public void setData(String name,String value) {
        this.data.put(name,value);
    }

    @Override
    public String toString() {
        return "LogBean{" +
                "data=" + data +
                '}';
    }
}
