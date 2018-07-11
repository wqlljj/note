package com.example.wangqi.developutils.view;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ArrayAdapter;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.databinding.ActivityScreenUtilBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

public class ScreenUtilActivity extends AppCompatActivity {
    String value="10";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_util);
        ActivityScreenUtilBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_util);
        binding.setValue(value);

        List<String> data=new ArrayList<>();
        data.add("手机的IMEI号:  "+SystemUtil.IMEI());
        data.add("手机产品的序列号:  "+ SystemUtil.SN());
        data.add("手机的sim号:  "+SystemUtil.SIM());
        data.add("手机的ID:  "+SystemUtil.ID());
        data.add("手机的mac地址:  "+ SystemUtil.MAC());
        data.add("系统国家:  "+ SystemUtil.Country());
        data.add("系统语言:  "+ SystemUtil.Language());
        data.add("屏幕的高:  "+ SystemUtil.Height() + "");
        data.add("屏幕的宽： "+ SystemUtil.Width() + "");
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
