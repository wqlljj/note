package com.cloudminds.hc.hariservice.manager.http;

import android.os.Environment;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Created by zoey on 2017/12/9.
 */

public class HsLogUploader extends HsBaseHttp{
    public interface LogUploadCallBack{
        public void onSuccess();
        public void onFailure(String error);
    }

    private static final String TAG = "HS/LogUploader";
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String BOUNDARY = "1a2b3c";

    private LogUploadCallBack callBack;

    public void setCallBack(LogUploadCallBack uploadCallBack){
        this.callBack = uploadCallBack;
    }

    public void uploadLog(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("deviceId", HariUtils.getIMEI());
                params.put("type", HariServiceClient.getCallEngine().getRobotType());

                startUpload(params);
            }
        }).start();
    }

    private void startUpload(Map<String,String> params){
        LogUtils.d(TAG,"Upload log entry!");
        BufferedReader input = null;
        StringBuilder sbResp = null;
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://"+ BaseConstants.ROC_SERVER+":"+BaseConstants.ROC_PORT+"/harilog/log/");
            try {
                //压缩log文件夹
                LogUtils.zipLogFile();
                String sdcardPath = Environment.getExternalStorageDirectory().getPath();
                String zipPath = sdcardPath+"/HS/log.zip";
                File file = new File(zipPath);
                if (!file.exists()){
                    LogUtils.d(TAG,"Can not find log.zip");
                    return;
                }

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
                conn.setRequestMethod("POST"); //请求方式
                conn.setRequestProperty("Charset", CHARSET);//设置编码
                conn.setRequestProperty("connection", "keep-alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + BOUNDARY);
                conn.setDoInput(true); //允许输入流
                conn.setDoOutput(true); //允许输出流
                conn.setUseCaches(false); //不允许使用缓存

                OutputStream outputSteam = conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(LINE_END);
                if (params != null) {//根据格式，开始拼接文本参数
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                        sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
                        sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                        sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                        sb.append(LINE_END);
                        sb.append(entry.getValue());
                        sb.append(LINE_END);//换行！
                    }
                }
                sb.append(PREFIX);//开始拼接文件参数
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"logfile\"; filename=\"log.zip\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                sb.append(LINE_END);
                //写入文件数据
                dos.write(sb.toString().getBytes());

                DataInputStream in = new DataInputStream(new FileInputStream(file));
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    dos.write(bufferOut, 0, bytes);
                }
                in.close();
                // listener.onProgress(curbytes,1.0d*curbytes/totalbytes);
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                outputSteam.close();

                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                int code = conn.getResponseCode();
                sbResp = new StringBuilder();
                if (code == 200){
                    input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String s;
                    while ((s = input.readLine()) != null) {
                        sbResp.append(s).append("\n");
                    }
                    try {
                        JSONObject object = new JSONObject(sbResp.toString());
                        int ret = object.getInt("code");
                        if (ret == 0){
                            LogUtils.d(TAG,"Upload log successful!");
                            callBack.onSuccess();
                        } else {
                            LogUtils.d(TAG,"Upload log failed!");
                            if (null != callBack)
                                callBack.onFailure(object.has("messages")?object.getString("messages"):"Result error");
                        }

                    }catch (JSONException e){
                        LogUtils.d(TAG,"Upload log failed! ");
                        if (null != callBack)
                            callBack.onFailure("Parse result to json error");
                    }

                } else {
                    try {
                        LogUtils.d(TAG,"Upload log failed!");
                        sbResp.setLength(0);
                        input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        while ((line = input.readLine()) != null) {
                            sbResp.append(line);
                        }
                        if (callBack != null)
                            callBack.onFailure(sbResp.toString());
                    }catch (IOException e){
                        if (callBack != null)
                            callBack.onFailure(sbResp.toString());
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.d(TAG,"Upload log failed!");
                if (callBack != null)
                    callBack.onFailure("");
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            LogUtils.d(TAG,"Upload log failed!");
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
