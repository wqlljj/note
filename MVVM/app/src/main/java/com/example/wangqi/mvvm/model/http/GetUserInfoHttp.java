package com.example.wangqi.mvvm.model.http;

import android.util.Log;

import com.example.wangqi.mvvm.model.RequestService;
import com.example.wangqi.mvvm.model.bean.UserInfo;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/13.
 */

public class GetUserInfoHttp extends HCBaseHttp {
    String userName;
    private String TAG="HCApiClient";

    public GetUserInfoHttp(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),gson);
        Call<UserInfo> responseCall = requestService.getUserInfo(body);
        return responseCall;
    }

    @Override
    public String toString() {
        return "UserInfoHttp{" +
                "userName='" + userName + '\'' +
                ", TAG='" + TAG + '\'' +
                '}';
    }
}
