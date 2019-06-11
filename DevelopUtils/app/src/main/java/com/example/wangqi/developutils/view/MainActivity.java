package com.example.wangqi.developutils.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.databinding.ActivityMainBinding;
import com.example.wangqi.developutils.util.SystemUtil;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(this);
        SystemUtil.isH265EncoderSupport();
        SystemUtil.isH265DecoderSupport();
    }
    public void click(View view){
        switch (view.getId()){
            case R.id.screenUtil:
                Toast.makeText(this, "跳转屏幕及系统信息获取", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,ScreenUtilActivity.class));
                break;
            case R.id.fitScreen:
                Toast.makeText(this, "跳转屏幕适配", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,FitScreenActivity.class));
                break;
            case R.id.statistics:
                Toast.makeText(this, "跳转屏幕适配", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,StatisticsActivity.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityPermissionsDispatcher.needPermissionWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needPermission() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
