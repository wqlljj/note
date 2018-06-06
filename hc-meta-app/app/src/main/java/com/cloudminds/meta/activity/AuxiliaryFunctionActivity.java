package com.cloudminds.meta.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;

public class AuxiliaryFunctionActivity extends StandardActivity implements IHubView {
    public static View.OnClickListener listener;
    private String TAG="META/Auxiliary";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auxiliary_function);
        findViewById(R.id.face_Recognition).setOnClickListener(listener);
        findViewById(R.id.object_Recognition).setOnClickListener(listener);
        findViewById(R.id.money_Recognition).setOnClickListener(listener);
        findViewById(R.id.scene).setOnClickListener(listener);
        findViewById(R.id.ocr).setOnClickListener(listener);
        findViewById(R.id.navi).setOnClickListener(listener);
        findViewById(R.id.hub_call_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener==null){
                    startActivity(new Intent(AuxiliaryFunctionActivity.this,HubActivity.class));
                    finish();
                    return;
                }
                if(v.getId()==R.id.hub_call_btn){
                    v.setEnabled(false);
                }
                listener.onClick(v);
                startActivity(new Intent(AuxiliaryFunctionActivity.this,HubActivity.class));
                finish();
            }
        });
        MetaApplication.setState_listenr(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(MetaApplication.isApplicationBroughtToBackground(this)){
            ToastUtil.cancel();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(listener==null||MetaApplication.state!=HUB_CONN_IN_CONNECTION){
            startActivity(new Intent(this,HubActivity.class));
            finish();
        }else {
            ActivityManager.setCurrentActivity(this.getClass());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            ActivityManager.removeCurrentActivity(this.getClass());
            startActivity(new Intent(AuxiliaryFunctionActivity.this,HubActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setStateByUsb(boolean conn) {

    }

    @Override
    public void setStateByCurrentMeta(boolean current) {

    }

    @Override
    public void setUIState(int state, String message) {
        switch (state){
            case Constant.HUB_CONN_DISCONNECT:
            case Constant.HUB_CONN_END:
            case Constant.CALL_FAILED:
                ActivityManager.removeCurrentActivity(this.getClass());
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        Log.e(TAG, "finish: " );
        ActivityManager.removeCurrentActivity(this.getClass());
        MetaApplication.setState_listenr(null);
        super.finish();
    }
}
