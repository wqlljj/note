package com.example.wangqi.developutils.bean;

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
                "width_px=" + width_px +
                ", height_px=" + height_px +
                ", width_dp=" + width_dp +
                ", height_dp=" + height_dp +
                ", width_sp=" + width_sp +
                ", height_sp=" + height_sp +
                ", density=" + density +
                ", scaledDensity=" + scaledDensity +
                '}';
    }
}
