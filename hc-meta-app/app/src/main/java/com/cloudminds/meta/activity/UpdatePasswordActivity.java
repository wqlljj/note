package com.cloudminds.meta.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.bean.Response;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.view.EditTextWithDel;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-12.
 */

public class UpdatePasswordActivity extends StandardActivity implements View.OnClickListener {

    private EditTextWithDel mOld,mNew,mReNew;
    private Button mSave;
    private ImageView mBack;
    public static final String TAG = "UpdatePasswordActivity";
    private String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_password_activity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        userName = settings.getString("com.cloudminds.hc.hariservice.key.account","");
        initViews();
    }

    private void initViews() {
        mOld = (EditTextWithDel) findViewById(R.id.update_password_old_password);
        mNew = (EditTextWithDel) findViewById(R.id.update_password_new_password);
        mNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mNew.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mNew.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mReNew = (EditTextWithDel) findViewById(R.id.update_password_re_new_password);
        mReNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mReNew.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mReNew.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mBack = (ImageView) findViewById(R.id.update_password_back);
        mBack.setOnClickListener(this);
        mSave = (Button) findViewById(R.id.update_password_save);
        mSave.setOnClickListener(this);
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
            case R.id.update_password_save:
                if(checkReclick(R.id.update_password_save,1000))return;
                String oldPS = mOld.getText().toString();
                String newPS = mNew.getText().toString();
                if(TextUtils.isEmpty(oldPS)||TextUtils.isEmpty(newPS)){
                    ToastUtil.show(this.getApplicationContext(),TextUtils.isEmpty(oldPS)?getString(R.string.password_null):getString(R.string.new_password_null));
                    return;
                }
                if(!mReNew.getText().toString().equals(newPS)){
                    mReNew.setTextColor(getResources().getColor(R.color.red));
                    ToastUtil.show(this.getApplicationContext(), R.string.password_different);
                    return;
                }
                if(!isQualified(newPS)){
                    mNew.setTextColor(getResources().getColor(R.color.red));
                    ToastUtil.show(this.getApplicationContext(), R.string.format_password_error);
                    return;
                }
                showWaitDialog();
                HCApiClient.changePwd(userName, oldPS, newPS, new HCBaseHttp.CallBack<Response>() {
                    @Override
                    public void onResponse(Response data) {
                        Log.e(TAG, "onResponse: "+data.toString() );
                        dialog.dismiss();
                        switch (data.getCode()){
                            case "200":
                                ToastUtil.show(UpdatePasswordActivity.this.getApplicationContext(), R.string.update_success);
                                finish();
                                break;
                            case "1003":
                                Log.e(TAG, "onResponse: "+data.toString() );
                                ToastUtil.show(UpdatePasswordActivity.this.getApplicationContext(), R.string.password_error);
                                break;
                            default:
                                Log.e(TAG, "onResponse: "+data.toString() );
                                ToastUtil.show(UpdatePasswordActivity.this.getApplicationContext(), getString(R.string.update_failure)+data.getCode());
                                break;
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        dialog.dismiss();
                        ToastUtil.show(UpdatePasswordActivity.this.getApplicationContext(), msg);
                    }
                });
                break;
            case R.id.update_password_back:
                if(checkReclick(R.id.update_password_back,1000))return;
                startActivity(new Intent(this,FamilyHelpActivity.class));
                finish();
                break;
        }
    }
    Dialog dialog;
    private void showWaitDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.waitdialog);
        ImageView icon = (ImageView) dialog.findViewById(R.id.icon);
        Animation rotateAnimation  = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        icon.startAnimation(rotateAnimation);
        dialog.setCancelable(false);
        dialog.show();
    }
}
