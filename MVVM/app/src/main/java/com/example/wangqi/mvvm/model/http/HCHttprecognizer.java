package com.example.wangqi.mvvm.model.http;

import android.util.Log;

import com.example.wangqi.mvvm.model.RequestService;
import com.example.wangqi.mvvm.model.bean.Response;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/7/6.
 */

public class HCHttprecognizer extends HCBaseHttp {
    private String TAG="HCHttprecognizer";
    String userName;
    boolean state;

    public HCHttprecognizer(String userName, boolean state) {
        this.userName = userName;
        this.state = state;
    }
    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        okhttp3.RequestBody body=okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),"");
//        Call<Response> call = requestService.baseRequest(body, "CloudMinds");
        Call<Response> call = requestService.recognition(body,userName,state);
        return call;
    }
}
