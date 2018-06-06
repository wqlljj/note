package com.cloudminds.meta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.view.EditTextForNumberWithDel;
import com.cloudminds.meta.view.EditTextWithDel;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-12.
 */

public class RetrievePasswordActivity extends StandardActivity implements View.OnClickListener {

    private ImageView mBack;
    private EditTextForNumberWithDel mTel;
    private EditTextWithDel mCode,mSetPass;
    private Button mSubmit,mGetCode;
    private String TAG="META/Retrieve";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrieve_password_activity);
        initViews();
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
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            ActivityManager.removeCurrentActivity(this.getClass());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(MetaApplication.isApplicationBroughtToBackground(this)){
            ToastUtil.cancel();
        }
    }

    private void initViews() {
        mBack = (ImageView) findViewById(R.id.retrieve_password_back);
        mTel = (EditTextForNumberWithDel) findViewById(R.id.retrieve_password_telephone);
        mCode = (EditTextWithDel) findViewById(R.id.retrieve_password_code);
        mCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mCode.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mCode.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mSetPass = (EditTextWithDel) findViewById(R.id.retrieve_password_set_password);
        mSetPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mSetPass.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mSetPass.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mSubmit = (Button) findViewById(R.id.retrieve_password_set_submit);
        mGetCode = (Button) findViewById(R.id.retrieve_password_get_code);
        mSubmit.setOnClickListener(this);
        mGetCode.setOnClickListener(this);
        mBack.setOnClickListener(this);

    }
    public static boolean isQualified(String str) {
        String englishRegex="[a-zA-Z0-9]{6,10}";
        if(str.matches(englishRegex)){
            return true;
        }
        return false;
    }
    @IdRes
    int btId;
    long clickTime=0l;
    private boolean checkReclick(@IdRes int id,long timeLimit) {
        Log.e(TAG, "checkReclick: "+id+"  "+timeLimit );
        if(btId==id&& System.currentTimeMillis()-clickTime<timeLimit){
            ToastUtil.show(this.getApplicationContext(), R.string.repetitive_operation);
            Log.e(TAG, "checkReclick: true" );
            return true;
        }
        Log.e(TAG, "checkReclick: false" );
        btId=id;
        clickTime=System.currentTimeMillis();
        return false;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.retrieve_password_back:
                if(checkReclick(R.id.update_password_save,1000))return;
                startActivity(new Intent(this,FamilyHelpActivity.class));
                finish();
                break;
            case R.id.retrieve_password_get_code:
                if(checkReclick(R.id.update_password_save,1000))return;
                doGetCode();
                break;
            case R.id.retrieve_password_set_submit:
                if(checkReclick(R.id.update_password_save,1000))return;
                doSubmit();
                break;
        }
    }

    private void doSubmit() {
        String code = mCode.getText().toString();
        if(TextUtils.isEmpty(code)){
            ToastUtil.show(this.getApplicationContext(),getString(R.string.verifying_code_empty));
            return;
        }
        if(code.length()!=6){
            mCode.setTextColor(getResources().getColor(R.color.red));
            ToastUtil.show(this.getApplicationContext(), getString(R.string.verifying_code_wrong));
            return;
        }
        String newPS = mSetPass.getText().toString();
        if(TextUtils.isEmpty(newPS)){
            ToastUtil.show(this.getApplicationContext(),getString(R.string.new_password_null));
            return;
        }
        if(!isQualified(newPS)){
            mSetPass.setTextColor(getResources().getColor(R.color.red));
            ToastUtil.show(this.getApplicationContext(), R.string.format_password_error);
            return;
        }
    }

    private void doGetCode() {
        String phone = mTel.getText().toString();
        if(TextUtils.isEmpty(phone)&&phone.length()!=11){

        }
    }
}
