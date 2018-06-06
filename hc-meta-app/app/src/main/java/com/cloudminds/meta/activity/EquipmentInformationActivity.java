package com.cloudminds.meta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.view.MessageDialog;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-4-11.
 */

public class EquipmentInformationActivity extends StandardActivity implements View.OnClickListener {

    public static final String TAG = "Meta/EquipmentActivity";

    private TextView mId,mVersion,mJId,mJVersion;
    private Button mUnbind;
    private MessageDialog mDialog;
    private ImageView mBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equipment_information_activity);

        initViews();

    }

    private void initViews() {
        mId = (TextView) findViewById(R.id.equipment_information_id);
        mVersion = (TextView) findViewById(R.id.equipment_information_version);
        mJId = (TextView) findViewById(R.id.equipment_information_j_id);
        mJVersion = (TextView) findViewById(R.id.equipment_information_j_version);
        mUnbind = (Button) findViewById(R.id.equipment_information_unbound);
        mUnbind.setOnClickListener(this);
        mBack = (ImageView) findViewById(R.id.callback);
        mBack.setOnClickListener(this);
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
            ActivityManager.setCurrentActivity(this.getClass());
        super.onResume();
    }

    @Override
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }

    @Override
    public void finish() {
        ActivityManager.removeCurrentActivity(this.getClass());
        Log.e(TAG, "finish: " );
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            ActivityManager.removeCurrentActivity(this.getClass());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.equipment_information_unbound:
                Log.d(TAG,"show dialog");
                showUnboundDialog();
                break;
            case R.id.equipment_dialog_cancle:
                mDialog.dismiss();
                break;
            case R.id.equipment_dialog_ok:
                Log.d(TAG,"go to unbind equipment");
                mDialog.dismiss();
                break;
            case R.id.callback:
                Log.e(TAG, "callback " );
                startActivity(new Intent(this,FamilyHelpActivity.class));
                finish();
                break;
        }
    }

    private void showUnboundDialog() {
        MessageDialog.Builder  builder = new MessageDialog.Builder();
        mDialog = builder.setTitle(R.string.equipment_information_unbound)
                .setMessage(R.string.equipment_dialog_message)
                .setCancleListener(this)
                .setCancle(R.string.equipment_dialog_cancle)
                .setOkListener(this)
                .setOk(R.string.equipment_dialog_ok)
                .builder();
        mDialog.show(this);
    }
}
