package com.example.wangqi.mvvm.model.http;

import android.util.Log;

import com.example.wangqi.mvvm.model.RequestService;
import com.example.wangqi.mvvm.model.api.CloudServiceContants;
import com.example.wangqi.mvvm.model.bean.FaceResponse;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Created by SX on 2017/4/10.
 */

public class HCHttpFace extends HCBaseHttp {
    int type=-1;
    String face_id;
    String imagefile;
    String name;
    private String TAG="HCApiClient";

    public HCHttpFace() {
        type=2;
    }

    public HCHttpFace(String imagefile, String name) {
        this.imagefile = imagefile;
        this.name = name;
        type=0;
    }

    public HCHttpFace(String face_id) {
        this.face_id = face_id;
        type=1;
    }

    public String getImagefile() {
        return imagefile;
    }

    public void setImagefile(String imagefile) {
        this.imagefile = imagefile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFace_id() {
        return face_id;
    }

    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    @Override
    Call getCall(Retrofit retrofit) {
        RequestService requestService = retrofit.create(RequestService.class);
        String gson = new Gson().toJson(this);
        Log.e(TAG, "getCall: "+gson );
        switch (type){
            case 2:
                return getFaceList(requestService);
            case 1:
                return getRemoveCall(requestService);
            default://type=0
                return getAddCall(requestService);
        }
    }
    private Call<FaceResponse> getFaceList(RequestService requestService) {
        Map<String, RequestBody> params = new HashMap<String, RequestBody>();

        okhttp3.RequestBody o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), CloudServiceContants.app_key);
        params.put("app_key",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.app_secret);
        params.put("app_secret",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.faceset_id);
        params.put("faceset_id",o3requestBody);

//        return requestService.getFaceList(params);
        return requestService.getFaceList(CloudServiceContants.app_key,CloudServiceContants.app_key,CloudServiceContants.faceset_id);
    }
    private Call<FaceResponse> getRemoveCall(RequestService requestService) {
        Map<String, RequestBody> params = new HashMap<String, RequestBody>();

       okhttp3.RequestBody o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), CloudServiceContants.app_key);
        params.put("app_key",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.app_secret);
        params.put("app_secret",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.faceset_id);
        params.put("faceset_id",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),face_id);
        params.put("face_id",o3requestBody);

        return requestService.deletePicture(params);
    }

    private Call<FaceResponse> getAddCall(RequestService requestService) {
        Map<String, RequestBody> params = new HashMap<String, RequestBody>();
        File file = new File(imagefile);
        okhttp3.RequestBody fileRequestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/octet-stream"), file);

        MultipartBody.Part partFile = MultipartBody.Part.createFormData("imagefile","aaa",fileRequestBody);
        okhttp3.RequestBody o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), CloudServiceContants.app_key);
        params.put("app_key",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.app_secret);
        params.put("app_secret",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),CloudServiceContants.faceset_id);
        params.put("faceset_id",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),name);
        params.put("name",o3requestBody);
        o3requestBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"),file.getName());
        params.put("imagename",o3requestBody);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), CloudServiceContants.app_key);
//        params.put("app_key", requestBody);
//        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), CloudServiceContants.app_secret);
//        params.put("app_secret", requestBody);
//        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), CloudServiceContants.faceset_id);
//        params.put("faceset_id", requestBody);
//        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), name);
//        params.put("name", requestBody);
//        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());
//        params.put("imagename", requestBody);

        return requestService.upLoadPicture(params,partFile);
    }

    @Override
    public String toString() {
        return "HCHttpFace{" +
                "type=" + type +
                ", face_id='" + face_id + '\'' +
                ", imagefile='" + imagefile + '\'' +
                ", name='" + name + '\'' +
                ", TAG='" + TAG + '\'' +
                '}';
    }
}
