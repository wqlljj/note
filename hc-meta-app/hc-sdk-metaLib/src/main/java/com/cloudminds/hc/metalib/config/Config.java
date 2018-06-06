package com.cloudminds.hc.metalib.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.utils.DLog;
import com.google.gson.Gson;

/**
 * Created by willzhang on 15/06/17
 */

public final class Config {

    private static final String SP_KEY_LAST_CHECK_TIME = "cloudminds.updater.last_check_time";
    private static final String SP_KEY_URL_CHECK_UPDATE = "cloudminds.updater.url";
    private static final String SP_KEY_CACHED_VERSION_INFO = "cloudminds.updater.cached.version_info";
    private static final String SP_KEY_IGNORE_BUILD_TIME = "cloudminds.updater.ignore.build_time";
    private static final String SP_KEY_AUTO_DOWNLOAD_WIFI = "cloudminds.updater.auto_download_wifi";
    private static final String SP_KEY_VERSION_TEST = "cloudminds.updater.version_test";
    private static final String SP_KEY_WIPE_DATA_ON_NEXT_REBOOT = "cloudminds.updater.wipe_data";
    private static final String SP_KEY_DOWNLOADING_PACKAGE_INFO = "cloudminds.updater.downloading_package_info";

    private static final String BASE_URL = "http://%s:9064/v3/json/getversion";
    private static final String DEFAULT_CHECKING_URL="hariromupdate.cloudminds.com";
    private static Config INSTANCE;

    private SharedPreferences mSharedPreferences;
    private long lastCheckTime;

    private Config() {
    }

    public static Config getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new Config();
        }
        return INSTANCE;
    }

    private SharedPreferences SP(Context mContext) {
        if (null == mSharedPreferences) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        return mSharedPreferences;
    }
    public long getLastCheckTime(Context mContext) {
        if (lastCheckTime == 0) {
            lastCheckTime = SP(mContext).getLong(SP_KEY_LAST_CHECK_TIME, 0);
        }
        return lastCheckTime;
    }

    public void setLastCheckTime(Context mContext, long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        SP(mContext).edit().putLong(SP_KEY_LAST_CHECK_TIME, lastCheckTime).apply();
    }

    public String getCheckUrl(Context mContext) {
        // 将check url放在SharedPreferences以便以后在测试服务器和正式服务器之间切换
        String url = SP(mContext).getString(SP_KEY_URL_CHECK_UPDATE, DEFAULT_CHECKING_URL);
        return String.format(BASE_URL, url);
    }

    public void setCheckUrl(Context mContext, String serverURL) {
        SP(mContext).edit().putString(SP_KEY_URL_CHECK_UPDATE, serverURL).apply();
    }

    public void setCachedUpdatingInfo(Context mContext, VersionPojo pojo) {
        String versionInfo = new Gson().toJson(pojo);
        SP(mContext).edit().putString(SP_KEY_CACHED_VERSION_INFO, versionInfo).apply();
    }

    public VersionPojo getCachedUpdatingInfo(Context mContext) {
        String versionInfo = SP(mContext).getString(SP_KEY_CACHED_VERSION_INFO, null);
        if (null == versionInfo) {
            return null;
        }
        return new Gson().fromJson(versionInfo, VersionPojo.class);
    }

    public void setIgnoreVersionBuildTime(Context mContext, String buildTime) {
        SP(mContext).edit().putString(SP_KEY_IGNORE_BUILD_TIME, buildTime).apply();
    }

    public String getIgnoreVersionBuildTime(Context mContext) {
        return SP(mContext).getString(SP_KEY_IGNORE_BUILD_TIME, "");
    }

    public void setAutoDownloadOnWifi(Context mContext, boolean autoDownload) {
        SP(mContext).edit().putBoolean(SP_KEY_AUTO_DOWNLOAD_WIFI, autoDownload).apply();
    }

    public boolean isAutoDownloadOnWifi(Context mContext) {
        return SP(mContext).getBoolean(SP_KEY_AUTO_DOWNLOAD_WIFI, true);
    }

    public void setVersionTest(Context mContext, boolean isVersionTest) {
        SP(mContext).edit().putBoolean(SP_KEY_VERSION_TEST, isVersionTest).apply();
    }

    public boolean isVersionTest(Context mContext) {
        boolean formal = false;
        return SP(mContext).getBoolean(SP_KEY_VERSION_TEST, formal);
    }

    public void setWipeDataOnNextReboot(Context mContext, boolean wipeData) {
        SP(mContext).edit().putBoolean(SP_KEY_WIPE_DATA_ON_NEXT_REBOOT, wipeData).apply();
    }

    public boolean needWipeDataOnNextReboot(Context mContext) {
        return SP(mContext).getBoolean(SP_KEY_WIPE_DATA_ON_NEXT_REBOOT, false);
    }

    public void setDownloadingPackageInfo(Context mContext, ActualVersionInfo versionInfo) {
        String versionInfoJson = new Gson().toJson(versionInfo);
        DLog.d("Downloading version info saved:" + versionInfoJson);
        SP(mContext).edit().putString(SP_KEY_DOWNLOADING_PACKAGE_INFO, versionInfoJson).apply();
    }

    public ActualVersionInfo getDownloadingPackageInfo(Context mContext) {
        String versionInfoJson = SP(mContext).getString(SP_KEY_DOWNLOADING_PACKAGE_INFO, null);
        if (TextUtils.isEmpty(versionInfoJson)) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(versionInfoJson, ActualVersionInfo.class);
    }
}
