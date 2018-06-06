package com.cloudminds.meta.bean;

/**
 * Created by SX on 2017/5/24.
 */

public class NaviBean {
    String type;
    int angle;
    int distance;

    public NaviBean() {
    }

    public NaviBean(String type, int angle, int distance) {
        this.type = type;
        this.angle = angle;
        this.distance = distance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "NaviBean{" +
                "type='" + type + '\'' +
                ", angle=" + angle +
                ", distance=" + distance +
                '}';
    }
}
