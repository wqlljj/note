package com.cloudminds.meta.accesscontroltv.http;

import com.cloudminds.meta.accesscontroltv.bean.NewsBean;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Created by SX on 2017/4/10.
 */
public interface RequestService {
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("{path}")
    Call<NewsBean> getAllNews(@Path("path") String path);
}
