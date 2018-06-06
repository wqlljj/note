package com.cloudminds.meta.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.adapter.GuideAdapter;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.presenter.WelcomePresenter;
import com.cloudminds.meta.service.asr.BusEvent;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by tiger on 17-3-31.
 */

public class WelcomeActivity extends StandardActivity implements ViewPager.OnPageChangeListener
        ,View.OnClickListener,IWelcomeView,SpeechSynthesizerListener {

    private static final String TAG = "Meta:welcomeActivity";
    private ViewPager mVp;
    private Button mBtn;
    private LinearLayout mTip;
    private int []images;
    private ImageView []mTips;
    private WelcomePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        Log.d(TAG,"firstLogin = "+getPresenter().getLoginState());
        if(!getPresenter().getLoginState()){
            activatAction();
            return;
        }

        int checkWritePermission = ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkWritePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return;
        }
        init();
    }

    private void init(){
        initViews();
    }

    @Override
    protected void onStart() {
        getPresenter().speak(mVp.getCurrentItem());
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: " );
            ActivityManager.setCurrentActivity(this.getClass());
        getPresenter().resume();
        super.onResume();
    }

    @Override
    public void finish() {
        Log.e(TAG, "finish: " );
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
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().stop();
        getPresenter().cancle();
    }

    private WelcomePresenter getPresenter(){
        if(presenter == null){
            presenter = new WelcomePresenter(this);
        }
        return presenter;
    }

    public void activatAction(){
        Log.d(TAG,"activatAction to activate");
        startActivity(new Intent(this,ActivateActivity.class));
        getPresenter().saveLoginState();
        finish();
    }

    private void initViews() {
        mVp = (ViewPager) findViewById(R.id.welcome_vp);
        mBtn = (Button) findViewById(R.id.welcome_next);
        mTip = (LinearLayout) findViewById(R.id.welcome_tips);
        images = new int[]{R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher};
        mTips = new ImageView[images.length];

        for(int i=0;i<mTips.length;i++) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LayoutParams(10, 10));
            mTips[i] = img;
            if (i == 0) {
                // img.setBackgroundResource(R.drawable.page_now);
            } else {
                //img.setBackgroundResource(R.drawable.page);
            }
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(new LayoutParams
                    (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            params.leftMargin=5;
            params.rightMargin=5;
            mTip.addView(img,params);
        }

        GuideAdapter adapter = new GuideAdapter(this,images);
        mVp.setAdapter(adapter);
        mVp.setOnPageChangeListener(this);
        mBtn.setOnClickListener(this);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    protected void onPause() {
        getPresenter().pause();
        super.onPause();
        if(MetaApplication.isApplicationBroughtToBackground(this)){
            Log.e(TAG, "onPause: true" );
            ToastUtil.cancel();
        }else{
            Log.e(TAG, "onPause: false" );
        }
    }


    @Override
    public void onPageSelected(int position) {
//        mTips[mCurrentPage].setBackgroundResource(R.drawable.page);
//        mTips[position].setBackgroundResource(R.drawable.page_now);

        Log.d(TAG,"onPageSelected position = "+position);

        if(position != mTips.length-1){
            mBtn.post(new Runnable() {
                @Override
                public void run() {
                    mBtn.setText(R.string.welcome_skip);
                }
            });
        }else {
            mBtn.post(new Runnable() {
                @Override
                public void run() {
                    mBtn.setText(R.string.welcome_know);
                }
            });
        }

        getPresenter().speak(mVp.getCurrentItem());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.welcome_next:
                getPresenter().nextStep(mVp.getCurrentItem(),mTips.length-1);
                break;
        }
    }

    @Override
    public void nextPage() {
        if(mVp.getCurrentItem() == (mTips.length-1))return;
        mVp.post(new Runnable() {
            @Override
            public void run() {
                mVp.setCurrentItem(mVp.getCurrentItem()+1);
            }
        });
    }

    @Override
    public void onSynthesizeStart(String s) {
        Log.d(TAG,"onSynthesizeStart ,s="+s);
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        Log.d(TAG,"onSynthesizeDataArrived ,s="+s);
    }

    @Override
    public void onSynthesizeFinish(String s) {
        Log.d(TAG,"onSynthesizeFinish ,s="+s);
    }

    @Override
    public void onSpeechStart(String s) {
        Log.d(TAG,"onSpeechStart ,s="+s);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        Log.d(TAG,"onSpeechProgressChanged ,s="+s);
    }

    @Override
    public void onSpeechFinish(String s) {
        Log.d(TAG,"onSpeechFinish ,s="+s);
        nextPage();
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.d(TAG,"onError ,speechError="+speechError);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    init();
                }else {
                    Log.e(TAG,"onRequestPermissionsResult error");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }


}
