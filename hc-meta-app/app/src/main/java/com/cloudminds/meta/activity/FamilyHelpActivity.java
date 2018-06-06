package com.cloudminds.meta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.adapter.FamilyHelpAdapter;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-10.
 */

public class FamilyHelpActivity extends StandardActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String TAG = "Meta:FamilyHelpActivity";

    private ListView mLv;
    private ImageView mBack;
    private String[] mItems;
    private TextView mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_help_activity);
        initViews();
        Log.d(TAG,"initViews ");
        initDatas();
        mLv.setAdapter(new FamilyHelpAdapter(this,mItems));
        mLv.setOnItemClickListener(this);
        findViewById(R.id.family_help_back).setOnClickListener(this);
    }

    private void initDatas() {
        mItems = getResources().getStringArray(R.array.family_help);
        Log.d(TAG,"item length = "+mItems.length);
    }

    private void initViews() {
        mLv = (ListView) findViewById(R.id.family_help_lv);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        doNextSetup(i);

    }
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: " );
        Class<?> currentActivity = ActivityManager.getCurrentActivity();
            ActivityManager.setCurrentActivity(this.getClass());
        super.onResume();
    }


    @Override
    public void finish() {
        Log.e(TAG, "finish: " );
        ActivityManager.removeCurrentActivity(this.getClass());
        super.finish();
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

    private void doNextSetup(int i) {
        switch (i){
            case 0://亲友管理
                doFamilyManager();
                break;
//            case 1://地图管理
//
//                break;
            case 1://个人信息
                doPersonalInformation();

                break;
//            case 3://设备信息
//                doEquipmentInformation();
//
//                break;
            case 2://修改密码
                doUpdatePassword();

                break;
//            case 5://找回密码
//                doRetrievePassword();
//
//                break;
        }
    }

    private void doRetrievePassword() {
        Intent intent = new Intent(this,RetrievePasswordActivity.class);
        startActivity(intent);
    }

    private void doUpdatePassword() {
        Intent intent = new Intent(this,UpdatePasswordActivity.class);
        startActivity(intent);
    }

    private void doEquipmentInformation() {
        Intent intent = new Intent(this,EquipmentInformationActivity.class);
        startActivity(intent);
    }

    private void doPersonalInformation() {
        Intent intent = new Intent(this,PersonalInformationActivity.class);
        startActivity(intent);
    }

    private void doFamilyManager() {
        Intent intent = new Intent(this,FamilyManageActivity.class);
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.family_help_back:
                startActivity(new Intent(this,HubActivity.class));
                finish();
                break;
        }
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
}
