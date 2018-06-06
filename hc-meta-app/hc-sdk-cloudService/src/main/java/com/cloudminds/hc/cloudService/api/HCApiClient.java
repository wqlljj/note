package com.cloudminds.hc.cloudService.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cloudminds.hc.cloudService.bean.UserInfo;
import com.cloudminds.hc.cloudService.http.ChangePwdHttp;
import com.cloudminds.hc.cloudService.http.ForgetPasswordHttp;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.cloudService.http.HCHttpEngine;
import com.cloudminds.hc.cloudService.http.HCHttpFace;
import com.cloudminds.hc.cloudService.http.HCHttpRegister;
import com.cloudminds.hc.cloudService.http.HCHttpUnRegister;
import com.cloudminds.hc.cloudService.http.HCHttprecognizer;
import com.cloudminds.hc.cloudService.http.GetUserInfoHttp;
import com.cloudminds.hc.cloudService.http.UpdateUserInfoHttp;
import com.cloudminds.hc.cloudService.http.VerificationCodeHttp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by SX on 2017/4/10.
 */

public class HCApiClient {
    private static String TAG="HCApiClient";
    private static SharedPreferences sharedPreferences;
    private static ConnectivityManager cm;

    public static  void addFace(String name, String imagefile, final HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.FaceResponse> callBack){
        HCHttpEngine.startReq(new HCHttpFace(imagefile,name), CloudServiceContants.FACEHTTP,getCallback(callBack));

    }
    public static  void deleteFace(String face_id, final HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.FaceResponse> callBack){
        HCHttpEngine.startReq(new HCHttpFace(face_id), CloudServiceContants.FACEHTTP,getCallback(callBack));

    }
    public static  void getFaceList( final HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.FaceResponse> callBack){
        HCHttpEngine.startReq(new HCHttpFace(), CloudServiceContants.FACEHTTP,getCallback(callBack));

    }
    public static void downloadPicFromNet(String url,String filePath,final HCBaseHttp.CallBack<String> callBack){
        File file = new File(filePath);
        HCHttpEngine.downloadPicFromNet(url, getCallback(callBack,file,1));
    }
    public static  void register(String userName, String pwd, String phoneID, HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.Response> callBack){
        HCHttpEngine.startReq(new HCHttpRegister(userName,pwd,phoneID), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void unRegister(String userName, String pwd, String phoneID,  HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.Response> callBack){
        HCHttpEngine.startReq(new HCHttpUnRegister(userName,pwd,phoneID), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void changePwd(String userName, String oldPwd, String newPwd,  HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.Response> callBack){
        HCHttpEngine.startReq(new ChangePwdHttp(userName,oldPwd,newPwd), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void getVerificationCode(String userName,  HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.Response> callBack){
        HCHttpEngine.startReq(new VerificationCodeHttp(userName), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void forgetPassword(String userName,String verifiCode,String newPwd,  HCBaseHttp.CallBack<com.cloudminds.hc.cloudService.bean.Response> callBack){
        HCHttpEngine.startReq(new ForgetPasswordHttp(userName,verifiCode,newPwd), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void getUserInfo(String userName,  HCBaseHttp.CallBack<UserInfo> callBack){
        HCHttpEngine.startReq(new GetUserInfoHttp(userName), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static  void updateUserInfo(UserInfo.DataBean userInfo,  HCBaseHttp.CallBack<UserInfo> callBack){
        HCHttpEngine.startReq(new UpdateUserInfoHttp(userInfo), CloudServiceContants.BASEURL,getCallback(callBack));
    }
    public static void init(Context context){
         cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        CloudServiceContants.HTTP=getServiceAddress();
        CloudServiceContants.PORT=getServicePort();
        CloudServiceContants.BASEURL="http://"+CloudServiceContants.HTTP+":"+CloudServiceContants.PORT;
        CloudServiceContants.FACEHTTP=CloudServiceContants.BASEURL;
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

    public static  void recognition(String http, String port, String userName, boolean flag, Callback callback){
        HCHttpEngine.startReq(new HCHttprecognizer(userName, flag), "http:"+http+":"+port,callback
            );
    }
    //下载保存文件
    private static  Callback<ResponseBody> getCallback(final HCBaseHttp.CallBack<String> callBack, final File file, final int type) {
        return new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.e("vivi",response.body().toString()+" length "+response.body().contentLength()+" type "+response.body().contentType());
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        String msg=type==1?"image":"history";
                        try {
                            InputStream is = response.body().byteStream();
                            if(!file.exists()){
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                            }
                            String result="";
                            switch (type){
                                case 1:
                                    BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                                    BufferedInputStream bis = new BufferedInputStream(is);
                                    byte[] data=new byte[1024];
                                    int len=-1;
                                    while  ((len=bis.read(data)) != -1) {
                                        fos.write(data,0,len);
                                        fos.flush();
                                    }
                                    fos.close();
                                    bis.close();
                                    break;
                                case 2:
                                    BufferedReader br= new BufferedReader(new InputStreamReader(is, "gbk"));
                                    String stemp;
                                    while  ((stemp = br.readLine()) != null) {
                                        result+=stemp+"\r\n";
                                    }
                                    br.close();
                                    break;
                            }
                            is.close();
                            callBack.onResponse(msg+" "+result+" success");
                        } catch (IOException e) {
                            e.printStackTrace();
                            callBack.onResponse(msg+" fail");
                        }
                        Log.e(TAG,msg+ " success");
                    }
                }.start();

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String msg=type==1?"image":"history";
                t.printStackTrace();
                Log.d("vivi",msg+"  "+t.getMessage());
                callBack.onFailure(msg+" "+t.getMessage());
            }
        };
    }
    @NonNull
    private static <D> Callback<D> getCallback(final HCBaseHttp.CallBack<D> callBack) {
        return new Callback<D>(){

//            @Override
//            public void onResponse(Response<D> response, Retrofit retrofit) {
//                Log.e(TAG, "onResponse: " +response.toString());
//                if(response.body()!=null) {
//                    Log.e(TAG, "onResponse: " + response.body());
//                    callBack.onResponse(response.body());
////
//                }else{
//                    callBack.onFailure("response.body()=null");
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                Log.e(TAG, "onFailure: "+throwable.getMessage() );
//                callBack.onFailure(throwable.getMessage());
//            }

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
    public static void setServiceAddress(String serviceAddress){
        Log.e(TAG, "setServiceAddress: "+ serviceAddress);
        CloudServiceContants.HTTP=serviceAddress;
        sharedPreferences.edit().putString("serviceAddress",CloudServiceContants.HTTP).apply();
        CloudServiceContants.BASEURL="http://"+CloudServiceContants.HTTP+":"+CloudServiceContants.PORT;
        CloudServiceContants.FACEHTTP=CloudServiceContants.BASEURL;
    }
    public static void setServicePort(String servicePort){
        CloudServiceContants.PORT=servicePort;
        sharedPreferences.edit().putString("servicePort",CloudServiceContants.PORT).apply();
        CloudServiceContants.BASEURL="http://"+CloudServiceContants.HTTP+":"+CloudServiceContants.PORT;
        CloudServiceContants.FACEHTTP=CloudServiceContants.BASEURL;
    }
    public static String getServicePort(){
        return sharedPreferences.getString("servicePort",CloudServiceContants.PORT);
    }
    public static String getServiceAddress(){
        return (sharedPreferences.getString("serviceAddress",CloudServiceContants.HTTP));
    }
}
