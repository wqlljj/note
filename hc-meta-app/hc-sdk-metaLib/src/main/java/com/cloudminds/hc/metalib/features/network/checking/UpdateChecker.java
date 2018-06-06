package com.cloudminds.hc.metalib.features.network.checking;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.network.BaseNetworkTask;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloadService;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.utils.DLog;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by willzhang on 16/06/17
 */

public class UpdateChecker extends BaseNetworkTask {

    public static final String ERROR_CODE_101 = "101"; // 版本未找到，通常由于属性错误引起
    public static final String ERROR_CODE_102 = "102"; // 升级包缺失
    public static final String ERROR_CODE_103 = "103"; // 未查到版本，及已为最新版本

    private String checkingUrl;
    private Context mContext;
    private VersionPojo versionPojo;
    private CheckUpdateCallback callback;

    public interface CheckUpdateCallback {
        void onCheckDone(int responseCode, VersionPojo versionPojo);
    }

    public UpdateChecker(Context mContext) {
        this.mContext = mContext;
        checkingUrl = Config.getInstance().getCheckUrl(mContext);
    }

    public void check(String postData, CheckUpdateCallback callback) {
        this.callback = callback;
        Log.d("check","postData  =  "+postData);
        post(checkingUrl, postData);
    }

    @Override
    protected int parseData(long contentLength, InputStream is) {
        if (null == is) {
            DLog.e("Empty response data");
            return RESPONSE_CODE_DATA_PARSE_ERROR;
        }
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            int byteInt;
            while ((byteInt = is.read()) >= 0) {
                byteArray.write(byteInt);
            }
            String responseData = byteArray.toString();
            Gson gson = new Gson();
            DLog.d(responseData);
            versionPojo = gson.fromJson(responseData, VersionPojo.class);
            // 如果errorCode不为空，则认为出错
            String errorCode = "";
            if (null == versionPojo || !TextUtils.isEmpty(errorCode = versionPojo.getErrorCode())) {
                if (ERROR_CODE_103.equals(errorCode)) { // 未查到版本，及已为最新版本
                    versionPojo.setHaveNewest(String.valueOf(false));
                    // ASW-16565 update cache when version is empty
                    Config.getInstance().setCachedUpdatingInfo(mContext, versionPojo);
                    return RESPONSE_CODE_SUCCESS;
                }
                return RESPONSE_CODE_REQUEST_FAILED;
            }
            Config.getInstance().setCachedUpdatingInfo(mContext, versionPojo);
            return RESPONSE_CODE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RESPONSE_CODE_REQUEST_FAILED;
    }

    @Override
    public void onWorkFinished(int responseCode) {
        if (null != callback) {
            callback.onCheckDone(responseCode, versionPojo);
        }
        if (needHandleForceUpgrade()) {
            PackageDownloadService.forceDownloadIfNeeded(mContext, versionPojo);
        }
    }

    /**
     * By default, updater need handle force upgrade.
     * But not for downloader.
     *
     * @return Need handle force upgrade or not
     */
    protected boolean needHandleForceUpgrade() {
        return true;
    }
}
