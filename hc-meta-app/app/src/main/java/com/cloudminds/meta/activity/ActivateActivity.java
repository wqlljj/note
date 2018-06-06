package com.cloudminds.meta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;


import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.fragment.ActivateFragment;
import com.cloudminds.meta.fragment.UpgradeFragment;
import com.cloudminds.meta.manager.ActivityManager;

/**
 * Created by tiger on 17-3-31.
 */

public class ActivateActivity extends StandardActivity
        implements USBUtils.MetaHotSwapListener
        {

    private final static String TAG = "Meta:ActivateActivity";
    private Fragment mUpgradeFragment,mActivateFragment;
    private boolean isActivate = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_activity);

        Log.d(TAG,"oncreate --- ");
        showActivateFragment();
//        showUpgradeFragment();
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


    private void showActivateFragment() {
        isActivate = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mActivateFragment == null){
            mActivateFragment = new ActivateFragment();
        }
        transaction.replace(R.id.activate_fragment, mActivateFragment);
        transaction.commit();
    }

    private void showUpgradeFragment(){
        isActivate = false;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(mUpgradeFragment == null){
            mUpgradeFragment = new UpgradeFragment();
        }
        transaction.replace(R.id.activate_fragment, mUpgradeFragment);
        transaction.commit();
    }
            @Override
            protected void onResume() {
                Log.e(TAG, "onResume: " );
                    ActivityManager.setCurrentActivity(this.getClass());
                super.onResume();
            }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void cutIn() {
        if(!isActivate || mActivateFragment==null)return;
        ((ActivateFragment)mActivateFragment).cutIn();
        Log.d(TAG,"cutIn");
    }

    @Override
    public void cutOut() {
        Log.d(TAG,"cutOut");
        if(!isActivate || mActivateFragment==null)return;
        ((ActivateFragment)mActivateFragment).cutOut();
    }
            private long exitTime = 0;
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        ToastUtil.show(getApplicationContext(), getString(R.string.again_exit));
                        exitTime = System.currentTimeMillis();
                    } else {
                        ActivityManager.removeCurrentActivity(this.getClass());
                        moveTaskToBack(false);
                        this.finish();
                    }
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            }

            @Override
            public void finish() {
                ActivityManager.removeCurrentActivity(this.getClass());
                super.finish();
            }

            @Override
            public void startActivity(Intent intent) {
                ActivityManager.removeCurrentActivity(this.getClass());
                super.startActivity(intent);
            }
        }
