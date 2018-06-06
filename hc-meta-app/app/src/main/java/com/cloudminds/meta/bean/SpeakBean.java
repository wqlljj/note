package com.cloudminds.meta.bean;

/**
 * Created by SX on 2017/11/17.
 */

public class SpeakBean {
    int id;
    boolean isStart;
    long updateTime;
    String text;
    int type;
    String language ="zh";//zh中文 en 英文

    public SpeakBean() {
    }

    public SpeakBean(int id, String text, int type, String language) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SpeakBean{" +
                "id=" + id +
                ", isStart=" + isStart +
                ", updateTime=" + updateTime +
                ", text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
