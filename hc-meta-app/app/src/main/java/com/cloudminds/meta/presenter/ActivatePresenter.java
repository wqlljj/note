package com.cloudminds.meta.presenter;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.cloudminds.meta.activity.IActivateView;
import com.cloudminds.meta.bean.UserBean;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.fragment.ActivateFragment;
import com.cloudminds.meta.model.ActivateModel;

/**
 * Created by tiger on 17-4-1.
 */

public class ActivatePresenter implements ICallBack {

    public static final String TAG = "Meta:ActivatePresenter";
    private ActivateModel mModel;
    private IActivateView mView;

    public ActivatePresenter(IActivateView view){
        this.mView = view;
        mModel = new ActivateModel((Fragment)mView);
        mModel.initHCMeta();
        if(!mModel.getActivateState()){
            mView.nextSetup();
        }
    }

    public void activateUser(){
        mView.setButtonEnable(false);
        UserBean bean = new UserBean(mView.getUser(),mView.getPass());
        Log.d(TAG,bean.toString());
        mModel.activateUser(((ActivateFragment)mView).getActivity(),bean,this);
    }

    public int unbindUser(){
        return mModel.unbindUser();
    }

    public void checkUsb(){
        mView.setStateByUsb(mModel.getUSBStatus());
    }


    @Override
    public void callBack(String result, String message) {
        switch (result){
            case Constant.REGISTER_SUCCESS:
                mView.setButtonEnable(true);
                mView.nextSetup();
                break;
            case Constant.REGISTER_NETWORK_UNAVAILABLE:
            case Constant.REGISTER_USER_NAME_OR_PASSWORD_EMPTY:
            case Constant.REGISTER_PASSWORD_ERROR:
            case Constant.REGISTER_ON_FAILURE:
            case Constant.REGISTER_USER_NOT_EXIST:
                mView.showDialog(result,message);
                break;
        }
    }

}
