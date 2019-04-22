package com.example.wqllj.locationshare.view;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.example.wqllj.locationshare.R;

/**
 * Created by cloud on 2018/9/18.
 */

public abstract class ActionBarEntity {
    public String title;
    public String back_text="返回";
    public String menu_text = "设置";
    public boolean back_visible =true;
    public boolean menu_visible =false;
    public int bg= Color.parseColor("#ff007bba");

    public ActionBarEntity(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBack_text() {
        return back_text;
    }

    public void setBack_text(String back_text) {
        this.back_text = back_text;
    }

    public String getMenu_text() {
        return menu_text;
    }

    public void setMenu_text(String menu_text) {
        this.menu_text = menu_text;
    }

    public boolean isBack_visible() {
        return back_visible;
    }

    public void setBack_visible(boolean back_visible) {
        this.back_visible = back_visible;
    }

    public boolean isMenu_visible() {
        return menu_visible;
    }

    public void setMenu_visible(boolean menu_visible) {
        this.menu_visible = menu_visible;
    }

    public abstract void onClick(View view);
}
