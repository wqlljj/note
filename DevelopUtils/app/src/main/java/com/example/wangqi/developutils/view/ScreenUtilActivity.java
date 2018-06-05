package com.example.wangqi.developutils.view;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.databinding.ActivityScreenUtilBinding;
import com.example.wangqi.developutils.util.ScreenUtil;

public class ScreenUtilActivity extends AppCompatActivity {
    String value="10";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_util);
        ActivityScreenUtilBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_util);
        binding.setValue(value);
//        binding.setScale(ScreenUtil.getScale());
    }
}
