package com.example.tvscreeninfo;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.tvscreeninfo.databinding.ActivityScreenInfoBinding;
import com.example.tvscreeninfo.util.ScreenUtil;
import com.example.tvscreeninfo.util.SystemUtil;
import com.example.tvscreeninfo.util.ToastOrLogUtil;

import java.util.ArrayList;
import java.util.List;

public class ScreenUtilActivity extends Activity {
    private ActivityScreenInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_info);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_info);
        List<String> data=new ArrayList<>();
        data.add("手机的IMEI号:  "+ SystemUtil.IMEI());
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
