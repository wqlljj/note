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

public class ForgetPasswordHttp extends HCBaseHttp {
    String userName;
    String verifiCode;
    String newPwd;
    private String TAG="HCApiClient";

    public ForgetPasswordHttp(String userName, String verifiCode, String newPwd) {
        this.userName = userName;
        this.verifiCode = verifiCode;
        this.newPwd = newPwd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVerifiCode() {
        return verifiCode;
    }

    public void setVerifiCode(String verifiCode) {
        this.verifiCode = verifiCode;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),gson);
        Call<Response> responseCall = requestService.baseRequest(body, "forgetPassword");
        return responseCall;
    }

    @Override
    public String toString() {
        return "ForgetPasswordHttp{" +
                "userName='" + userName + '\'' +
                ", verifiCode='" + verifiCode + '\'' +
                ", newPwd='" + newPwd + '\'' +
                '}';
    }
}
