package com.cloudminds.hc.hariservice.manager.http;

import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zoey on 2017/10/25.
 */

public class HsServerFetcher extends HsBaseHttp{

    private FetchServerCallBack callBack;

    public void setCallBack(FetchServerCallBack fetchCallBack){
        this.callBack = fetchCallBack;
    }

    public void fetchServerFromUrl(final String server){

        new Thread(new Runnable() {
            @Override
            public void run() {
                startThread(server);
            }
        }).start();


    }

    private void startThread(String server){
        BufferedReader input = null;
        StringBuilder sb = null;
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL("https://"+server+"/hariGate/client/v1/getSwitchUrl");
            try {

                if (url.getProtocol().toLowerCase().equals("https")) {

                    try {
                        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
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
                            if (null != callBack)
                                callBack.onSuccess(data);
                        } else {
                            if (null != callBack)
                                callBack.onFailure("result error");
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

}

