package com.cloudminds.meta.accesscontroltv.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cloudminds.meta.accesscontroltv.bean.NewsBean;
import com.cloudminds.meta.accesscontroltv.constant.Constant;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.attr.name;

/**
 * Created by WQ on 2018/4/23.
 */

public class HttpClient {
    private static ConnectivityManager cm;
    private static String TAG="HttpClient";

    public static  void getAllNews(String path, final HCBaseHttp.CallBack<NewsBean> callBack){
        HCHttpEngine.startReq(new HCHttpNews(path), Constant.getImagebaseUrl(),getCallback(callBack));

    }
    @NonNull
    private static <D> Callback<D> getCallback(final HCBaseHttp.CallBack<D> callBack) {
        return new Callback<D>(){

            @Override
            public void onResponse(Call<D> call, Response<D> response) {
                Log.e(TAG, "onResponse: " +response.toString());
                if(response.body()!=null) {
                    Log.e(TAG, "onResponse: " + response.body());
                    callBack.onResponse(response.body());
//
                }else{
                    callBack.onFailure("response.body()=null");
                }
            }

            @Override
            public void onFailure(Call<D> call, Throwable t) {
                Log.e(TAG, "onFailure: "+t.getMessage() );
                callBack.onFailure(t.getMessage());
            }
        };
    }

    public static void init(Context context){
        cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public static boolean isNetworkAvailable() {
        NetworkInfo netInfo;
        netInfo = cm.getActiveNetworkInfo();

        boolean available = netInfo != null && netInfo.isAvailable() ;//&& netInfo.isConnected();
        if(netInfo==null){
            Log.d(TAG,"Current network state: netinfo is null, which means there is not active network");
        }else{
            Log.d(TAG,"Current network state: netinfo != null and isAvailable="+netInfo.isAvailable()
                    +", isConnected="+netInfo.isConnected()+", isConnectedOrConnecting="+netInfo.isConnectedOrConnecting()
                    +", isFailover="+netInfo.isFailover()+", isRoaming="+netInfo.isRoaming());
        }
        return available;

    }
}
