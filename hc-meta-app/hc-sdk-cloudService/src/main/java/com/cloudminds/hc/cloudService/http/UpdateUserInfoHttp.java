package com.cloudminds.hc.cloudService.http;

import android.util.Log;

import com.cloudminds.hc.cloudService.bean.UserInfo;
import com.cloudminds.hc.cloudService.service.RequestService;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/13.
 */

public class UpdateUserInfoHttp extends HCBaseHttp {
    UserInfo.DataBean dataBean;
    private String TAG="HCApiClient";

    public UpdateUserInfoHttp(UserInfo.DataBean dataBean) {
        this.dataBean = dataBean;
    }

    public UserInfo.DataBean getDataBean() {
        return dataBean;
    }

    public void setDataBean(UserInfo.DataBean dataBean) {
        this.dataBean = dataBean;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(dataBean);
        Log.e(TAG, "getCall: "+gson );
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),gson);
        Call<UserInfo> responseCall = requestService.updateUserInfo(body);
        return responseCall;
    }

    @Override
    public String toString() {
        return "UpdateUserInfoHttp{" +
                "dataBean=" + dataBean +
                ", TAG='" + TAG + '\'' +
                '}';
    }
}
