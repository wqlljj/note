package com.cloudminds.hc.metalib.features.network;

import android.text.TextUtils;


import com.cloudminds.hc.metalib.utils.DLog;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by willzhang on 16/06/17
 */

public abstract class BaseNetworkTask {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final String FOLDER_NAME = "ota";
    public static final int RESPONSE_CODE_SUCCESS = 1;
    public static final int RESPONSE_CODE_CANCEL = 0;
    public static final int RESPONSE_CODE_REQUEST_FAILED = -1;
    public static final int RESPONSE_CODE_INVALID_PARAMS = -2;
    public static final int RESPONSE_CODE_DATA_PARSE_ERROR = -3;

    /**
     * @param serverUrl server url
     * @param postData post data in json format
     */
    protected void post(String serverUrl, String postData) {
        int responseCode = RESPONSE_CODE_REQUEST_FAILED;
        if (TextUtils.isEmpty(serverUrl)) {
            DLog.e("Empty url");
            responseCode = RESPONSE_CODE_INVALID_PARAMS;
        }
        try {
            DLog.d(serverUrl);
            OkHttpClient httpClient = new OkHttpClient();
            RequestBody postBody = RequestBody.create(JSON, postData);
            Request request = new Request.Builder()
                    .url(serverUrl)
                    .post(postBody)
                    .cacheControl(CacheControl.FORCE_NETWORK) // disable cache
                    .build();
            responseCode = request(httpClient, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onWorkFinished(responseCode);
    }

    protected void get(String serverUrl) {
        int responseCode = RESPONSE_CODE_REQUEST_FAILED;
        try {
            DLog.d(serverUrl);
            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false) // disable retry, This will handled by PackageDownloadService
                    .build();
            Request.Builder builder = new Request.Builder();
            builder.url(serverUrl);
            setAdditionalParams(builder);
            Request request = builder.build();
            responseCode = request(client, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onWorkFinished(responseCode);
    }

    private int request(OkHttpClient client, Request request) throws IOException {
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        if (response.isSuccessful()) {
            if (null == responseBody) {
                DLog.e("Empty response");
            } else {
                InputStream is = responseBody.byteStream();
                return parseData(responseBody.contentLength(), is);
            }
        }
        return RESPONSE_CODE_REQUEST_FAILED;
    }

    protected void setAdditionalParams(Request.Builder builder) throws IOException {
    }

    /**
     *
     * @param is input stream to parse data from
     * @return response code should be {@link #RESPONSE_CODE_DATA_PARSE_ERROR},
     * {@link #RESPONSE_CODE_SUCCESS} or {@link #RESPONSE_CODE_REQUEST_FAILED}
     */
    protected abstract int parseData(long contentLength, InputStream is);

    protected abstract void onWorkFinished(int responseCode);
}
