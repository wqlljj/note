package com.cloudminds.meta.model;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.bean.Response;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.meta.bean.UserBean;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.presenter.ICallBack;
import com.cloudminds.meta.util.DeviceUtils;
import com.cloudminds.meta.util.SharePreferenceUtils;

/**
 * Created by tiger on 17-4-1.
 */

public class ActivateModel {

    public static final String TAG = "Meta:ActivateModel";

    private Fragment mFragment;
    private Context mContext;

    public ActivateModel(Fragment fragment){
        this.mFragment = fragment;
        this.mContext = fragment.getContext();
    }

    public void activateUser(Context context, UserBean user, final ICallBack callBack) {
        boolean connected = DeviceUtils.isNetworkConnected(context);
        if(!connected){
            callBack.callBack(Constant.REGISTER_NETWORK_UNAVAILABLE,null);
            return;
        }
        if(user == null){
            callBack.callBack(Constant.REGISTER_PASSWORD_ERROR,null);
            return;
        }
        if(TextUtils.isEmpty(user.getPass())||TextUtils.isEmpty(user.getUser())){
            callBack.callBack(Constant.REGISTER_USER_NAME_OR_PASSWORD_EMPTY,null);
            return;
        }


//        if(trim(user.getUser()).equals("11111111111")&&user.getPass().equals("admin")){
//            ICallBack.ICallBack(Constant.LOGIN_SUCCESS);
//        }else {
//            ICallBack.ICallBack(Constant.USER_NAME_OR_PASSWORD_ERROR);
//        }

        Log.d(TAG,"current user = "+(trim(user.getUser())));

        HCApiClient.register(trim(user.getUser()), user.getPass(), getPhoneId(), new HCBaseHttp.CallBack<Response>() {
            @Override
            public void onResponse(Response response) {
                Log.d(TAG,"onResponse "+response);
                if(response==null)return;
                checkResponseCode(response, callBack);
            }

            @Override
            public void onFailure(String s) {
                Log.d(TAG,"onFailure "+s);
                callBack.callBack(Constant.REGISTER_ON_FAILURE,s);

            }
        });
    }

    private void checkResponseCode(Response response,ICallBack callBack) {
        Log.d(TAG,"checkResponseCode");
        switch (response.getCode()){
            case Constant.REGISTER_SUCCESS:
                callBack.callBack(Constant.REGISTER_SUCCESS,null);
                saveActivate();
                break;
            case Constant.REGISTER_USER_NOT_EXIST:
            case Constant.REGISTER_PASSWORD_ERROR:
                callBack.callBack(response.getCode(),response.getMessage());
                break;

        }
    }

    public boolean getActivateState(){
        return SharePreferenceUtils.getPrefBoolean(Constant.FIRST_ACTIVATE,true);
    }

    private void saveActivate(){
        SharePreferenceUtils.setPrefBoolean(Constant.FIRST_ACTIVATE,false);
    }

    public int unbindUser() {

        return 0;
    }

    private String getPhoneId(){
        return "12345678";
    }

    private String trim(String phone){
        int max = phone.length();
        if(max<4)return phone;
        StringBuffer buffer = new StringBuffer();
        buffer.append(phone.substring(0,3));
        if(max<9){
            buffer.append(phone.substring(4,max));
            return buffer.toString();
        }
        buffer.append(phone.substring(4,8));
        if(max<10){
            buffer.append(phone.substring(4,max));
            return buffer.toString();
        }
        buffer.append(phone.substring(9));
        return buffer.toString();
    }

    public boolean getUSBStatus(){
        return HCMetaUtils.hasMeta();
    }

    public void initHCMeta(){
//        HCMetaUtils.init(mContext.getApplicationContext());
        HCMetaUtils.addMetaConnectListener((USBUtils.MetaHotSwapListener)mContext);
    }
}
