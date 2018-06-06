package com.cloudminds.hc.hariservice.manager.http;

import android.util.Log;

import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.IllegalFormatCodePointException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zoey on 2017/12/6.
 */

public class HsConfigFetcher extends HsBaseHttp{
    public interface FetchConfigCallBack{
        public void onSuccess();
        public void onFailure(String error);
    }

    private FetchConfigCallBack callBack;

    public void setCallBack(FetchConfigCallBack fetchCallBack){
        this.callBack = fetchCallBack;
    }

    public void fetchConfigWithAccount(final String account){

        new Thread(new Runnable() {
            @Override
            public void run() {
                startThread(account);
            }
        }).start();


    }

    private void startThread(String account){
        BufferedReader input = null;
        StringBuilder sb = null;
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://"+BaseConstants.ROC_SERVER+":"+BaseConstants.ROC_PORT+"/harilog/configure/"+account);
            try {

                if (url.getProtocol().toLowerCase().equals("https")) {

                    try {
                        HttpsURLConnection.setDefaultHostnameVerifier(new HsConfigFetcher.NullHostNameVerifier());
                        SSLContext sc = SSLContext.getInstance("TLS");
                        sc.init(null, trustAllCerts, new SecureRandom());
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    }catch (Exception e){

                    }

                    HttpsURLConnection https = (HttpsURLConnection)url.openConnection();

                    conn = https;
                } else {
                    conn = (HttpURLConnection)url.openConnection();
                }
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(6000);

                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                conn.connect();
                int code = conn.getResponseCode();
                sb = new StringBuilder();
                if (code == 200){
                    input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String s;
                    while ((s = input.readLine()) != null) {
                        sb.append(s).append("\n");
                    }
                    try {
                        JSONObject object = new JSONObject(sb.toString());
                        JSONObject data = object.getJSONObject("data");
                        if (null != data){
                            if (parseConfig(data)){
                                if (null != callBack)
                                    callBack.onSuccess();
                            } else {
                                if (null != callBack)
                                    callBack.onFailure("Parse result error");
                            }

                        } else {
                            if (null != callBack)
                                callBack.onFailure("Result error");
                        }

                    }catch (JSONException e){
                        if (null != callBack)
                            callBack.onFailure("Parse result to json error");
                    }

                } else {
                    try {
                        sb.setLength(0);
                        input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        while ((line = input.readLine()) != null) {
                            sb.append(line);
                        }
                        if (callBack != null)
                            callBack.onFailure(sb.toString());
                    }catch (IOException e){
                        if (callBack != null)
                            callBack.onFailure(sb.toString());
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                if (callBack != null)
                    callBack.onFailure("");
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            if (callBack != null)
                callBack.onFailure("");
        } finally {
            // close buffered
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 解析服务器返回的配置
     * @param dataJson
     */
    private boolean parseConfig(JSONObject dataJson){

        try {
            JSONObject rcuCamera = dataJson.getJSONObject("rcuCamera");
            if (rcuCamera.has("videoWidth")){
                int vw = rcuCamera.getInt("videoWidth");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_VIDEO_WIDTH,vw);
            }
            if (rcuCamera.has("videoHeight")){
                int vh = rcuCamera.getInt("videoHeight");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_VIDEO_HEIGHT,vh);
            }
            if (rcuCamera.has("audioStartBps")){
                int asBps = rcuCamera.getInt("audioStartBps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_AUDIO_STARTBPS,asBps);
            }
            if (rcuCamera.has("videoStartBps")){
                int vsBps = rcuCamera.getInt("videoStartBps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_VIDEO_STARTBPS,vsBps);
            }
            if (rcuCamera.has("videoBps")){
                int vBps = rcuCamera.getInt("videoBps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_VIDEO_BPS,vBps);
            }
            if (rcuCamera.has("audioCodec")){
                String ac = rcuCamera.getString("audioCodec");
                PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_AUDIO_CODEC,ac);
            }
            if (rcuCamera.has("videoCodec")){
                String vc = rcuCamera.getString("videoCodec");
                PreferenceUtils.setPrefString(BaseConstants.PRE_KEY_VIDEO_CODEC,vc);
            }
            if (rcuCamera.has("videoFps")){
                int vFps = rcuCamera.getInt("videoFps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_VIDEO_FPS,vFps);
            }
            if (rcuCamera.has("msMinBps")){
                int minBps = rcuCamera.getInt("msMinBps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_MS_MIN_BPS,minBps);
            }
            if (rcuCamera.has("msMaxBps")){
                int maxBps = rcuCamera.getInt("msMaxBps");
                PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_MS_MAX_BPS,maxBps);
            }

            return true;
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
    }
}
