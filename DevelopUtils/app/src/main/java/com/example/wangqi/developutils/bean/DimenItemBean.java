package com.example.wangqi.developutils.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cloud on 2018/10/15.
 */

public class DimenItemBean {
    public static final int TYPE_X=0;
    public static final int TYPE_Y=1;
    public static final int TYPE_XSP=2;
    public static final int TYPE_XDP=3;
    public static final int TYPE_XPX=4;
    public static final int TYPE_YSP=5;
    public static final int TYPE_YDP=6;
    public static final int TYPE_YPX=7;
    public static final String TYPE_OPRATION_ADD="+";
    public static final String TYPE_OPRATION_MINUS="-";
    public static final String TYPE_OPRATION_MULTIPLY="*";
    public static final String TYPE_OPRATION_DIVIDE="÷";
    int type;
    String name;
    int value;
    String unit;
    String oprate;
    double num;
    int screenW;
    int ScreenH;
    int dpi;

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public static int getTypeX() {
        return TYPE_X;
    }

    public int getScreenW() {
        return screenW;
    }

    public void setScreenW(int screenW) {
        this.screenW = screenW;
    }

    public int getScreenH() {
        return ScreenH;
    }

    public void setScreenH(int screenH) {
        ScreenH = screenH;
    }

    public DimenItemBean(int type, String name, int value, String unit) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public DimenItemBean() {
    }

    public DimenItemBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOprate() {
        return oprate;
    }

    public void setOprate(String oprate) {
        this.oprate = oprate;
    }

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimenItemBean that = (DimenItemBean) o;
        if(!TextUtils.isEmpty(name)){
            return TextUtils.isEmpty(that.name)?false:name.equals(that.name);
        }else{
            return type==that.type;
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "DimenItemBean{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", oprate='" + oprate + '\'' +
                ", num=" + num +
                '}';
    }
}
