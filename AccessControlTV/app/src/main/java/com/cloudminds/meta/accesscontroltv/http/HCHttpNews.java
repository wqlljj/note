package com.cloudminds.meta.accesscontroltv.http;

import android.util.Log;

import com.cloudminds.meta.accesscontroltv.bean.NewsBean;
import com.google.gson.Gson;


import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/10.
 */

public class HCHttpNews extends HCBaseHttp {
    String path;
    private String TAG="HCApiClient";

    public HCHttpNews(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        return getFaceList(requestService);
    }
    private Call<NewsBean> getFaceList(RequestService requestService) {


//        return requestService.getFaceList(params);
        return requestService.getAllNews(path);
    }

    @Override
    public String toString() {
        return "HCHttpNews{" +
                "path='" + path + '\'' +
                ", TAG='" + TAG + '\'' +
                '}';
    }
}
