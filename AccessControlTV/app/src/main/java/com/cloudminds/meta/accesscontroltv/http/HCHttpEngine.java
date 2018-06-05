package com.cloudminds.meta.accesscontroltv.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
    private static ConnectivityManager cm;



    public static  void startReq(HCBaseHttp req, String baseUrl, Callback callback){
        Log.e(TAG, "startReq: " +req.toString()+"      "+baseUrl);
        if(!HttpClient.isNetworkAvailable()){
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

}
