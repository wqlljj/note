package com.cloudminds.meta.model;

import android.content.Context;

import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.util.SharePreferenceUtils;

/**
 * Created by tiger on 17-4-7.
 */

public class WelcomeModel {

    private static final String TAG = "Meta:WelcomeModel";

    private Context mContext;

    public WelcomeModel(Context context){
        this.mContext = context;
    }

    public boolean getLoginState(){
        return SharePreferenceUtils.getPrefBoolean(Constant.FIRST_LOGIN,true);
    }

    public void saveLoginState(){
        SharePreferenceUtils.setPrefBoolean(Constant.FIRST_LOGIN,false);
    }

    public boolean nextStep(int current,int count){
        if(current == count){
            return false;
        }
        return true;
    }
}
