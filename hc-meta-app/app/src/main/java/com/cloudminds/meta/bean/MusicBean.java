package com.cloudminds.meta.bean;

/**
 * Created by SX on 2017/11/27.
 */

public class MusicBean {
    String name;
    String singer;
    String pictureUrl;
    String musicUrl;

    public MusicBean() {
    }

    public MusicBean(String name, String singer, String pictureUrl, String musicUrl) {
        this.name = name;
        this.singer = singer;
        this.pictureUrl = pictureUrl;
        this.musicUrl = musicUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    @Override
    public String toString() {
        return "MusicBean{" +
                "name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", musicUrl='" + musicUrl + '\'' +
                '}';
    }
}
