package com.cloudminds.hc.cloudService.http;

import android.util.Log;

import com.cloudminds.hc.cloudService.bean.Response;
import com.cloudminds.hc.cloudService.service.RequestService;
import com.google.gson.Gson;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/10.
 */

public class HCHttpUnRegister extends HCBaseHttp {
    String userName;
    String password;
    String deviceId;
    private String TAG="HCApiClient";

    public HCHttpUnRegister(String userName, String pwd, String phoneID) {
        super();
        this.userName = userName;
        this.password = pwd;
        this.deviceId = phoneID;
    }

    public String getPhone() {
        return userName;
    }

    public void setPhone(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return password;
    }

    public void setPwd(String pwd) {
        this.password = pwd;
    }

    public String getPhoneID() {
        return deviceId;
    }

    public void setPhoneID(String phoneID) {
        this.deviceId = phoneID;
    }
    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),gson);
        Call<Response> Call = requestService.baseRequest(body, "unRegister");
        return Call;
    }

    @Override
    public String toString() {
        return "HCHttpUnRegister{" +
                "userName='" + userName + '\'' +
                ", pwd='" + password + '\'' +
                ", phoneID='" + deviceId + '\'' +
                '}';
    }
}
