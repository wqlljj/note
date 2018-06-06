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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.bean.Response;
import com.cloudminds.hc.cloudService.bean.UserInfo;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.UserBean;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.StringUtils;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-11.
 */

public class PersonalInformationActivity extends StandardActivity implements View.OnClickListener {

    private TextView mName,mTelephone,mAddress,mEmergencyName,mEmergencyTelephone,mEdit;
    private ImageView mBack;
    private TextView mEmergencyAddress;
    private TextView mSave;
    private String TAG="PersonalInfoActivity";
    private String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_information_activity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        userName = settings.getString("com.cloudminds.hc.hariservice.key.account","");
        initViews();

    }
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: " );
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
            Log.e(TAG, "onPause: true" );
            ToastUtil.cancel();
        }else{
            Log.e(TAG, "onPause: false" );
        }
    }

    private void initViews() {
        mName = (TextView) findViewById(R.id.personal_information_tv_name);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mName.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mName.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mTelephone = (TextView) findViewById(R.id.personal_information_tv_telephone);
        mAddress = (TextView) findViewById(R.id.personal_information_tv_address);
        mEmergencyName = (TextView) findViewById(R.id.personal_information_tv_emergency_name);
        mEmergencyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mEmergencyName.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mEmergencyName.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        mEmergencyTelephone = (TextView) findViewById(R.id.personal_information_tv_emergency_telephone);
        mEmergencyAddress = (TextView) findViewById(R.id.personal_information_tv_emergency_address);
        mEdit = (TextView) findViewById(R.id.personal_information_edit);
        mBack = (ImageView) findViewById(R.id.personal_information_back);
        mSave = (TextView) findViewById(R.id.save);
        mSave.setOnClickListener(this);
        mEdit.setOnClickListener(this);
        mBack.setOnClickListener(this);
        getData();
    }
    private void getData(){
        if(TextUtils.isEmpty(userName)){
            ToastUtil.show(this.getApplicationContext(), R.string.username_getFail);
            return;
        }else{
            ToastUtil.show(this.getApplicationContext(), userName);
        }
        HCApiClient.getUserInfo(userName, new HCBaseHttp.CallBack<UserInfo>() {
            @Override
            public void onResponse(UserInfo data) {
                UserInfo.DataBean dataBean = data.getData();
                mName.setText(dataBean.getName());
                mTelephone.setText(dataBean.getPhoneNumber());
                mAddress.setText(dataBean.getAddress());
                UserInfo.DataBean.EmergencyContactBean contact = dataBean.getEmergencyContact();
                mEmergencyName.setText(contact.getName());
                mEmergencyTelephone.setText(contact.getPhoneNumber());
                mEmergencyAddress.setText(contact.getAddress());
            }

            @Override
            public void onFailure(String msg) {
                ToastUtil.show(PersonalInformationActivity.this.getApplicationContext(),msg);
            }
        });
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
            case R.id.personal_information_edit:
                if(checkReclick(R.id.update_password_save,1000))return;
                doInformationEdit(true);
                break;
            case R.id.personal_information_back:
                if(checkReclick(R.id.update_password_save,1000))return;
                startActivity(new Intent(this,FamilyHelpActivity.class));
                finish();
                break;
            case R.id.save:
                if(checkReclick(R.id.update_password_save,1000))return;
                mSave.setEnabled(false);
                savePersonInfo();
                break;
        }

    }

    private void savePersonInfo() {
        if (!checkName(mName)) return;
        if (!TextUtils.isEmpty(mEmergencyName.getText().toString())&&!checkName(mEmergencyName)) return;
        showWaitDialog();
        UserInfo.DataBean dataBean = new UserInfo.DataBean();
        dataBean.setAddress(mAddress.getText().toString());
        dataBean.setName(mName.getText().toString());
        dataBean.setPhoneNumber(mTelephone.getText().toString());
        dataBean.setloginId(userName);
        UserInfo.DataBean.EmergencyContactBean emergencyContactBean = new UserInfo.DataBean.EmergencyContactBean();
        emergencyContactBean.setName(mEmergencyName.getText().toString());
        emergencyContactBean.setPhoneNumber(mEmergencyTelephone.getText().toString());
        emergencyContactBean.setAddress(mEmergencyAddress.getText().toString());
        dataBean.setEmergencyContact(emergencyContactBean);
        HCApiClient.updateUserInfo(dataBean, new HCBaseHttp.CallBack<UserInfo>() {
            @Override
            public void onResponse(UserInfo data) {
                Log.e(TAG, "onResponse: "+data );
                dialog.dismiss();
                mSave.setEnabled(true);
                if(data.getCode().equals("200")){
                    ToastUtil.show(PersonalInformationActivity.this.getApplicationContext(), R.string.update_success);
                }else{
                    ToastUtil.show(PersonalInformationActivity.this.getApplicationContext(),getString(R.string.update_failed_personinfo)+data.getCode());
                }
            }

            @Override
            public void onFailure(String msg) {
                dialog.dismiss();
                mSave.setEnabled(true);
                ToastUtil.show(PersonalInformationActivity.this.getApplicationContext(),msg);
            }
        });
        doInformationEdit(false);
    }

    private boolean checkName(TextView mName) {
        String name = mName.getText().toString();
        int qualified = StringUtils.isNameQualified(name);
        if(qualified !=0){
            int id=-1;
            mName.setTextColor(getResources().getColor(R.color.red));
            switch (qualified){
                case 1:
                    id=R.string.name_error_1;
                    break;
                case 2:
                    id=R.string.name_error_2;
                    break;
                case 3:
                    id=R.string.name_error_3;
                    break;
                case 4:
                    id=R.string.name_error_4;
                    break;
                case 5:
                    id=R.string.name_error_5;
                    break;
                case 6:
                    id=R.string.name_error_6;
                    break;
                default:
                    id=R.string.name_wrong;
                    break;
            }
            ToastUtil.show(this,id);
            mSave.setEnabled(true);
            return false;
        }
        return true;
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

    private void doInformationEdit(boolean enable) {
        mName.setEnabled(enable);
        mAddress.setEnabled(enable);
        mTelephone.setEnabled(enable);
        mEmergencyName.setEnabled(enable);
        mEmergencyTelephone.setEnabled(enable);
        mEmergencyAddress.setEnabled(enable);
        mSave.setVisibility(enable?View.VISIBLE:View.INVISIBLE);
        mEdit.setEnabled(!enable);
    }
}
