package com.cloudminds.meta.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.meta.R;
import com.cloudminds.meta.model.WelcomeModel;
import com.cloudminds.meta.activity.IWelcomeView;
import com.cloudminds.meta.util.TTSSpeaker;

/**
 * Created by tiger on 17-4-7.
 */

public class WelcomePresenter{

    public static final String TAG = "Meta:WelcomePresenter";

    private IWelcomeView mView;
    private WelcomeModel mModel;
    public WelcomePresenter(IWelcomeView view){
        this.mView = view;
        mModel = new WelcomeModel((Context)view);
    }

    public boolean getLoginState(){
        return mModel.getLoginState();
    }

    public void saveLoginState(){
        mModel.saveLoginState();
    }

    public void nextStep(int current,int count){
        if(mModel.nextStep(current,count)){
            mView.nextPage();
        }else {
            mView.activatAction();
        }
    }

    public void speak(int position){
        int resources = 0;
        switch (position){
            case 0:
                resources = R.string.setup_0;
                break;
            case 1:
                resources = R.string.setup_1;
                break;
            case 2:
                resources = R.string.setup_2;
                break;
        }
        String speak = ((Context)mView).getString(resources);
        Log.d(TAG,"speak value = "+speak);
        if(!TextUtils.isEmpty(speak)){
            TTSSpeaker.speak(speak, TTSSpeaker.SPEAK);
        }else {
            Log.d(TAG,"speak value is null return");
        }

    }

    public void stop(){
        TTSSpeaker.stop();
    }

    public void cancle(){
        TTSSpeaker.cancleSpeech();
    }

    public void resume(){
        TTSSpeaker.resume();
    }

    public void pause(){
        TTSSpeaker.pause();
    }

}
