package com.example.wangqi.mvvm.model.http;

import android.util.Log;


import com.example.wangqi.mvvm.model.RequestService;
import com.example.wangqi.mvvm.model.api.CloudServiceContants;
import com.example.wangqi.mvvm.model.api.HCApiClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * Created by SX on 2017/4/10.
 */

public class HCHttpEngine {
    private static String TAG="HCReqEngine";

    public static void startReq(HCBaseHttp req, String baseUrl, Callback callback){
        Log.e(TAG, "startReq: " +req.toString()+"      "+baseUrl);
        if(!HCApiClient.isNetworkAvailable()){
            callback.onFailure(null,new Throwable("The network is not available."));
            return;
        }
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Call call = req.getCall(retrofit);
        call.enqueue(callback);
        Log.e(TAG, "startReq: enqueue" );
    }
    public static void downloadPicFromNet(String url, Callback callback){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(CloudServiceContants.FACEHTTP)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        RequestService requestService = retrofit.create(RequestService.class);
        Call<ResponseBody> call = requestService.downloadPicFromNet(url);
        call.enqueue(callback);
        Log.d(TAG, "downloadPicFromNet: "+call.toString());

    }
}
