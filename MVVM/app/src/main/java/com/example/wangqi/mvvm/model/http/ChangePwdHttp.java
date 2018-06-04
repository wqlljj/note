package com.example.wangqi.mvvm.model.http;

import android.util.Log;

import com.example.wangqi.mvvm.model.RequestService;
import com.example.wangqi.mvvm.model.bean.Response;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/13.
 */

public class ChangePwdHttp extends HCBaseHttp {
    String loginId;
    String oldPassword;
    String newPassword;
    private String TAG="HCApiClient";

    public ChangePwdHttp(String loginId, String oldPassword, String newPassword) {
        this.loginId = loginId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson);
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),gson);
        Call<Response> responseCall = requestService.baseRequest(body, "changePassword");
        return responseCall;
    }

    @Override
    public String toString() {
        return "ChangePwdHttp{" +
                "loginId='" + loginId + '\'' +
                ", oldPassword='" + oldPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", TAG='" + TAG + '\'' +
                '}';
    }
}
