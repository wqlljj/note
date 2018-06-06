package com.cloudminds.meta.activity;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.fragment.RecordsFragment;
import com.cloudminds.meta.fragment.SettingFragment;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class BaseActivity extends StandardActivity {
    public static final int SETTING=1;
    public static final int HISTORYRECORDS=0;
    public static int type;
    Fragment fragment;
    private String TAG="META/BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    @Override
    protected void onResume() {
        super.onResume();
            ActivityManager.setCurrentActivity(this.getClass());
        init(type);
    }

    @Override
    public void finish() {
        ActivityManager.removeCurrentActivity(this.getClass());
        Log.e(TAG, "finish: " );
        super.finish();
    }

    private void init(int type) {
        Log.e(TAG, "init: "+type );
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentById = manager.findFragmentById(R.id.contentView);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        if(fragmentById!=null){
            fragmentTransaction.remove(fragmentById);
        }
        switch (type){
            case BaseActivity.HISTORYRECORDS:
                 fragment = new RecordsFragment();
                fragmentTransaction.add(R.id.contentView, fragment).commit();
                break;
            default://case BaseActivity.SETTING:
                fragment = new SettingFragment();
                fragmentTransaction.add(R.id.contentView, fragment).commit();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(MetaApplication.isApplicationBroughtToBackground(this)){
            Log.e(TAG, "onPause: true" );
            ToastUtil.cancel();
        }else{
            Log.e(TAG, "onPause: false" );
        }
    }

    @Override
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            ActivityManager.removeCurrentActivity(this.getClass());
            startActivity(new Intent(this,HubActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        super.onDestroy();
        fragment=null;
    }
}
