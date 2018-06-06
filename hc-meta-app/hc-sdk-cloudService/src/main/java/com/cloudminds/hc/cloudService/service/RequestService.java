package com.cloudminds.hc.cloudService.service;

import com.cloudminds.hc.cloudService.bean.FaceResponse;
import com.cloudminds.hc.cloudService.bean.Response;
import com.cloudminds.hc.cloudService.bean.UserInfo;
import com.squareup.okhttp.RequestBody;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


/**
 * Created by SX on 2017/4/10.
 */
public interface RequestService {
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/hi-service/cloudService/api/v1/userInfo")
    Call<UserInfo> getUserInfo(@Body okhttp3.RequestBody body);
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/hi-service/cloudService/api/v1/editUser")
    Call<UserInfo> updateUserInfo(@Body okhttp3.RequestBody body);
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/hi-service/cloudService/api/v1/{type}")
//    @POST("/hello3/servlet/{type}")
    Call<Response> baseRequest(@Body okhttp3.RequestBody body, @Path("type") String type);
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/api/imageRecognitionAction")
    Call<Response> recognition(@Body okhttp3.RequestBody body, @Query("userName") String userName, @Query("state") boolean state);
    @Multipart
//    @POST("/api/v1/face/add")
    @POST("/api/face/add")
    Call<FaceResponse> upLoadPicture(@PartMap Map<String, okhttp3.RequestBody> params,@Part MultipartBody.Part part);
    @Multipart
//    @POST("/api/v1/face/remove")
    @POST("/api/face/remove")
    Call<FaceResponse> deletePicture(@PartMap Map<String, okhttp3.RequestBody> params);
//    @Multipart
////    @POST("/api/v1/face/list")
//    @POST("/api/face/list")
//    Call<FaceResponse> getFaceList(@PartMap Map<String, okhttp3.RequestBody> params);
    @GET("/api/face/list")
    Call<FaceResponse> getFaceList(@Query("app_key") String app_key,@Query("app_secret") String app_secret,@Query("faceset_id") String faceset_id);
    @GET
    Call<ResponseBody> downloadPicFromNet(@Url String fileUrl);

}
