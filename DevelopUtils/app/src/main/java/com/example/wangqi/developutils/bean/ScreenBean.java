package com.example.wangqi.developutils.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.wangqi.developutils.application.BaseApplication;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by cloud on 2018/7/27.
 */

public class ScreenBean implements Serializable {
    int width_px;
    int height_px;
    int width_dp;
    int height_dp;
    int width_sp;
    int height_sp;
    float density;
    float scaledDensity;
    int dpi;
    private String TAG = "ScreenBean";

    public ScreenBean(int width_px, int height_px, float density, float scaledDensity) {
        this.width_px = width_px;
        this.height_px = height_px;
        this.density = density;
        this.scaledDensity = scaledDensity;
        init();
    }
    public void setData(int width_px, int height_px, float density, float scaledDensity) {
        this.width_px = width_px;
        this.height_px = height_px;
        this.density = density;
        this.scaledDensity = scaledDensity;
        if(density!=0&&scaledDensity!=0)
        init();
    }

    private void init() {
        width_dp=ScreenUtil.px2dip(width_px,density);
        height_dp=ScreenUtil.px2dip(height_px,density);
        width_sp=ScreenUtil.px2sp(width_px,scaledDensity);
        height_sp=ScreenUtil.px2sp(height_px,scaledDensity);
        dpi= (int) (density*160);
        Log.e(TAG, "init: "+toString() );
    }

    public int getWidth_px() {
        return width_px;
    }

    public int getHeight_px() {
        return height_px;
    }

    public int getWidth_dp() {
        return width_dp;
    }

    public int getHeight_dp() {
        return height_dp;
    }

    public int getWidth_sp() {
        return width_sp;
    }

    public int getHeight_sp() {
        return height_sp;
    }

    public float getDensity() {
        return density;
    }

    public int getDpi() {
        return dpi;
    }

    public float getScaledDensity() {
        return scaledDensity;
    }
    public String toSimpleString(){
        return "px : "+width_px+"*"+height_px+"  dp : "+width_dp+"*"+height_dp+"  sp : "+width_sp+"*"+height_sp+
                "  density : "+density+"  scaledDensity : "+scaledDensity+"  dpi : "+dpi;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this)+" ";
    }


    //    @Override
//    public String toString() {
//        return "{" +
//                "\nwidth_px=" + width_px +
//                ",\nheight_px=" + height_px +
//                ",\nwidth_dp=" + width_dp +
//                ",\nheight_dp=" + height_dp +
//                ",\nwidth_sp=" + width_sp +
//                ",\nheight_sp=" + height_sp +
//                ",\ndensity=" + density +
//                ",\nscaledDensity=" + scaledDensity +
//                "\n} ";
//    }
}
