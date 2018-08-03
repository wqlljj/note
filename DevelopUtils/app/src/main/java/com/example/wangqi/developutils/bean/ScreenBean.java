package com.example.wangqi.developutils.bean;

import android.util.Log;

import com.example.wangqi.developutils.util.ScreenUtil;

/**
 * Created by cloud on 2018/7/27.
 */

public class ScreenBean {
    int width_px;
    int height_px;
    int width_dp;
    int height_dp;
    int width_sp;
    int height_sp;
    float density;
    float scaledDensity;
    private String TAG = "ScreenBean";

    public ScreenBean(int width_px, int height_px, float density, float scaledDensity) {
        this.width_px = width_px;
        this.height_px = height_px;
        this.density = density;
        this.scaledDensity = scaledDensity;
        init();
    }

    private void init() {
        width_dp=ScreenUtil.px2dip(width_px,density);
        height_dp=ScreenUtil.px2dip(height_px,density);
        width_sp=ScreenUtil.px2sp(width_px,scaledDensity);
        height_sp=ScreenUtil.px2sp(height_px,scaledDensity);
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

    @Override
    public String toString() {
        return "ScreenBean{" +
                "\nwidth_px=" + width_px +
                ",\nheight_px=" + height_px +
                ", \nwidth_dp=" + width_dp +
                ", \nheight_dp=" + height_dp +
                ", \nwidth_sp=" + width_sp +
                ", \nheight_sp=" + height_sp +
                ", \ndensity=" + density +
                ", \nscaledDensity=" + scaledDensity +
                "\n}";
    }
}
