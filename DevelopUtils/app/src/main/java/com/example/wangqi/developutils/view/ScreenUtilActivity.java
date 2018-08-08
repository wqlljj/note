package com.example.wangqi.developutils.view;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ArrayAdapter;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.bean.ScreenBean;
import com.example.wangqi.developutils.databinding.ActivityScreenUtilBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.SystemUtil;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.example.wangqi.developutils.view.listener.OnClickListener2;

import java.util.ArrayList;
import java.util.List;

public class ScreenUtilActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_util);
        ActivityScreenUtilBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_util);
        binding.setValue("10");
        binding.setDppx(""+ScreenUtil.getDensity(this));
        binding.setPxsp(""+ScreenUtil.getScaledDensity(this));
        binding.setDp2px(new OnClickListener2() {
            @Override
            public void onClick(float value, float f) {
                ToastOrLogUtil.show(ScreenUtilActivity.this,value+" dp = "+ ScreenUtil.dip2px(value,f)+" px");
            }
        });
        binding.setPx2dp(new OnClickListener2() {
            @Override
            public void onClick(float value, float f) {
                ToastOrLogUtil.show(ScreenUtilActivity.this,value+" px = "+ScreenUtil.px2dip(value,f)+" dp");
            }
        });
        binding.setPx2sp(new OnClickListener2() {
            @Override
            public void onClick(float value, float f) {
                ToastOrLogUtil.show(ScreenUtilActivity.this,value+" px = "+ScreenUtil.px2sp(value,f)+" sp");
            }
        });
        binding.setSp2px(new OnClickListener2() {
            @Override
            public void onClick(float value, float f) {
                ToastOrLogUtil.show(ScreenUtilActivity.this,value+" sp = "+ScreenUtil.sp2px(value,f)+" px");
            }
        });

        List<String> data=new ArrayList<>();
        data.add("手机的IMEI号:  "+SystemUtil.IMEI());
        data.add("手机产品的序列号:  "+ SystemUtil.SN());
        data.add("手机的sim号:  "+SystemUtil.SIM());
        data.add("手机的ID:  "+SystemUtil.ID());
        data.add("手机的mac地址:  "+ SystemUtil.MAC());
        data.add("系统国家:  "+ SystemUtil.Country());
        data.add("系统语言:  "+ SystemUtil.Language());
        data.add("屏幕的高px:  "+ SystemUtil.Height() + "");
        data.add("屏幕的宽px： "+ SystemUtil.Width() + "");
        data.add("屏幕的高dp:  "+ ScreenUtil.px2dip(SystemUtil.Height(),SystemUtil.density()) + "");
        data.add("屏幕的宽dp： "+ ScreenUtil.px2dip(SystemUtil.Width(),SystemUtil.density()) + "");
        data.add("屏幕的高sp:  "+ ScreenUtil.px2sp(SystemUtil.Height(),SystemUtil.scaledDensity()) + "");
        data.add("屏幕的宽sp： "+ ScreenUtil.px2sp(SystemUtil.Width(),SystemUtil.scaledDensity()) + "");
        data.add("屏幕density： "+ SystemUtil.density() + "");
        data.add("屏幕scaledDensity： "+ SystemUtil.scaledDensity() + "");
        data.add("屏幕densityDpi： "+ SystemUtil.densityDpi(this) + "");
        data.add("屏幕虚拟键高度： "+ ScreenUtil.getNavigationBarHeight(this) + "");
        data.add("系统版本名:  "+ Build.VERSION.RELEASE);
        data.add("系统版本号:  "+ Build.VERSION.SDK_INT + "");
        data.add("系统型号:  "+ Build.MODEL);
        data.add("系统定制商:  "+ Build.BRAND);
        data.add("系统的主板:  "+ Build.BOARD);
        data.add("手机制造商:  "+ Build.PRODUCT);
        data.add("系统硬件执照商:  "+ Build.MANUFACTURER);
        data.add("builder类型:  "+ Build.MANUFACTURER);
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
        binding.listView.setAdapter(arrayAdapter);
    }

}
