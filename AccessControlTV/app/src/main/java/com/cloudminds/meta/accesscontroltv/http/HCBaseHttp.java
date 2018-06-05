package com.cloudminds.meta.accesscontroltv.http;


import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by SX on 2017/4/10.
 */

public abstract class HCBaseHttp {
    abstract Call getCall(Retrofit retrofit);
    public interface CallBack<S>{
        void onResponse(S data);
        void onFailure(String msg);
    }
}
