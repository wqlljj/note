package com.cloudminds.meta.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.BuildConfig;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.FamilyItemBean;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.fragment.BaseFamilyFragment;
import com.cloudminds.meta.fragment.FamilyAddOrUpdateFragment;
import com.cloudminds.meta.fragment.FamilyManageFragment;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.manager.ConflictEventManager;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.DeviceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import me.iwf.photopicker.PhotoPicker;

import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_ON_CONNECTION;
import static com.cloudminds.meta.manager.ConflictEventManager.CALLING_EVENT;
import static com.cloudminds.meta.manager.ConflictEventManager.FAMILYMANAGE_EVENT;

/**
 * Created by tiger on 17-4-10.
 */

public class FamilyManageActivity extends StandardActivity{

    public static final String TAG = "FamilyManageActivity";

    public List<FamilyItemBean> mItems;
    public Handler mHandler;
    public int mPosition;
    public int mType;
    private BaseFamilyFragment mFamilyManage;
    private FamilyAddOrUpdateFragment mFamilyAddFragment;
    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_RESULT_REQUEST = 0xa2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_manage_activity);
        initHandler();
        mType =Constant.FAMILY_TYPE_MANAGE;
        showFamilyManageFragment();
    }

    private void initHandler() {
        mHandler = new Handler(){
            int i=0;
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: i = "+(i++) );
                switch (msg.what){
                    case Constant.FAMILY_DO_UPDATE:
                        Log.d(TAG,"family do update");
                        if (conflictCheck()) return;
                        mType = Constant.FAMILY_TYPE_UPDATE;
                        FamilyAddOrUpdateFragment();
                        break;
                    case Constant.FAMILY_DO_ADD:
                        Log.d(TAG,"family do add");
                        if (conflictCheck()) return;
                        mType = Constant.FAMILY_TYPE_ADD;
                        FamilyAddOrUpdateFragment();
                        break;
                    case Constant.FAMILY_DO_BACK:
                        if(mType!=Constant.FAMILY_TYPE_MANAGE){
                            ConflictEventManager.removeEvent(ConflictEventManager.FAMILYMANAGE_EVENT);
                        }
                        ActivityManager.removeCurrentActivity(this.getClass());
                        startActivity(new Intent(FamilyManageActivity.this,FamilyHelpActivity.class));
                        finish();
                        break;
                    case Constant.FAMILY_DO_ADD_BACK:
                        ConflictEventManager.removeEvent(ConflictEventManager.FAMILYMANAGE_EVENT);
                        mType =Constant.FAMILY_TYPE_MANAGE;
                        showFamilyManageFragment();
                        break;


                }
            }
        };
    }
    //设计第三方，不清理当前Activity
//    ActivityManager.removeCurrentActivity(this.getClass());
    @Override
    public void startActivity(Intent intent) {
        Log.e(TAG, "startActivity: " );
        super.startActivity(intent);
    }

    private boolean conflictCheck() {
        try {
            String confilctEvent = ConflictEventManager.addEvent(ConflictEventManager.FAMILYMANAGE_EVENT, new ConflictEventManager.EventChecker() {
                @Override
                public boolean check() {
                    if (mType == Constant.FAMILY_TYPE_ADD || mType == Constant.FAMILY_TYPE_UPDATE) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            if(HariServiceClient.getCallEngine().isCallOngoing()){
                confilctEvent=CALLING_EVENT;
            }
            switch (confilctEvent){
                case CALLING_EVENT:
                    TTSSpeaker.speak(getString(R.string.event_conflict_calling),TTSSpeaker.HIGH);
                    Log.e(TAG, "handleMessage: 呼叫冲突" );
                    return true;
            }
        } catch (Exception e) {
            TTSSpeaker.speak(getString(R.string.undefined_event),TTSSpeaker.HIGH);
            e.printStackTrace();
            Log.e(TAG, "亲友管理: "+e.getMessage() );
            return true;
        }
        return false;
    }

    private void FamilyAddOrUpdateFragment() {
        Log.e(TAG, "FamilyAddOrUpdateFragment: " );
        if(mFamilyAddFragment != null&&mFamilyAddFragment.isAdded()) {
            ToastUtil.show(this.getApplicationContext(), R.string.family_infor_showed);
            return;
        }
        mFamilyAddFragment = new FamilyAddOrUpdateFragment();
        Log.e(TAG, "FamilyAddOrUpdateFragment: "+mFamilyAddFragment.toString()+"  " +mFamilyAddFragment.hashCode());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.family_manage_frameLayout, mFamilyAddFragment);
//        transaction.addToBackStack(null);
        transaction.commit();

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
    protected void onStop() {
        Log.e(TAG, "onStop: ");
        super.onStop();
    }

    private void showFamilyManageFragment() {
        if(mFamilyManage != null&&mFamilyManage.isAdded()){
            ToastUtil.show(this.getApplicationContext(), R.string.family_manage_showed);
            return;
        }
        mFamilyManage = new FamilyManageFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.family_manage_frameLayout, mFamilyManage);
        transaction.commit();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(mType!=Constant.FAMILY_TYPE_MANAGE) {
                mType = Constant.FAMILY_TYPE_MANAGE;
                showFamilyManageFragment();
            }else{
                Log.e(TAG, "onKeyDown: " );
                ActivityManager.removeCurrentActivity(this.getClass());
                startActivity(new Intent(FamilyManageActivity.this,FamilyHelpActivity.class));
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        ConflictEventManager.removeEvent(FAMILYMANAGE_EVENT);
        if(mFamilyManage!=null){
            mFamilyManage.release();
            mFamilyManage=null;
        }
        if(mFamilyAddFragment!=null){
            mFamilyAddFragment.release();
            mFamilyManage=null;
        }
        if(mItems!=null){
            mItems=null;
        }
        mHandler=null;
        super.onDestroy();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CODE_CAMERA_REQUEST:
                Log.d(TAG,"CODE_CAMERA_REQUEST "+ resultCode+"  "+DeviceUtils.hasSdcard());
                if (DeviceUtils.hasSdcard()) {
                    Log.e(TAG, "onActivityResult: "+mFamilyAddFragment.mCurrentFile.exists());
                    if(mFamilyAddFragment.mCurrentFile.exists()){
                        mFamilyAddFragment.cropRawPhoto(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +
                                ".provider", mFamilyAddFragment.mCurrentFile));
                    }else{
                        ToastUtil.show(this.getApplicationContext(), R.string.no_take_picture);
                    }
                } else {
                    Log.d(TAG,"non't have sd card");
                    ToastUtil.show(this.getApplicationContext(), R.string.sdcard_not_available);
                }
                break;
            case PhotoPicker.REQUEST_CODE:
                if(data!=null){
//                    Log.d(TAG,"CODE_GALLERY_REQUEST uri = "+data.getData());
                    ArrayList<String> photos =
                            data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    Uri uri = FileProvider.getUriForFile(this,this.getPackageName()+".provider", new File(photos.get(0)));
                    mFamilyAddFragment.cropRawPhoto(uri);
                }
                break;
            case CODE_RESULT_REQUEST:
                Log.d(TAG,"CODE_RESULT_REQUEST data = "+(data == null));
                if(data!= null)mFamilyAddFragment.setImageToView(data);
                if(mFamilyAddFragment.mDialog!=null)mFamilyAddFragment.mDialog.dismiss();
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
