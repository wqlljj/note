package com.cloudminds.meta.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SX on 2017/2/23.
 */

public class ChatMessage implements Parcelable{
    private int type;//左右
    private int contentType;

    private String time;
    private  Object data;

    public ChatMessage(int type, String time, Object data) {
        this.type = type;
        this.time = time;
        this.data = data;
    }

    public ChatMessage(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    protected ChatMessage(Parcel in) {
        type = in.readInt();
        contentType = in.readInt();
        time = in.readString();
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public int getContentType() {
        return contentType;
    }
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(type);
        parcel.writeInt(contentType);
        parcel.writeString(time);
    }

    public static class Type{
        public static final int CHAT_LEFT=0;
        public static final int CHAT_RGIHT=1;
    }
    public static class ContentType{
        public static final int CONTENT_STRING=0;
    }
}
