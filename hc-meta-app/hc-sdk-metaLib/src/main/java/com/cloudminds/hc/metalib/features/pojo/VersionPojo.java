package com.cloudminds.hc.metalib.features.pojo;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by willzhang on 16/06/17
 */

public class VersionPojo implements Serializable {
    private Software[] software;

    private String haveNewest;

    private String errorCode;

    private String errorDescription;

    private Newest newest;

    private String checkImei;

    public Software[] getSoftware() {
        return software;
    }

    public void setSoftware(Software[] software) {
        this.software = software;
    }

    public String getHaveNewest() {
        return haveNewest;
    }

    public void setHaveNewest(String haveNewest) {
        this.haveNewest = haveNewest;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Newest getNewest() {
        return newest;
    }

    public void setNewest(Newest newest) {
        this.newest = newest;
    }

    public String getCheckImei() {
        return checkImei;
    }

    public void setCheckImei(String checkImei) {
        this.checkImei = checkImei;
    }

    @Override
    public String toString() {
        return "ClassPojo [software = " + Arrays.toString(software) + ", haveNewest = " + haveNewest + ", errorCode = " + errorCode + ", errorDescription = " + errorDescription + ", newest = " + newest + ", checkImei = " + checkImei + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VersionPojo && newest.equals(((VersionPojo) obj).getNewest());
    }

    public ActualVersionInfo getVersionInfo(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        if (hasNewVersion() && null != newest) {
            Packages packageInfo = newest.getPackages()[0];
            if (packageName.equals(packageInfo.getName())) {
                return newest;
            }
        }
        Versions[] versions = software[0].getVersions();
        for (Versions version : versions) {
            if (null != version && packageName.equals(version.getPackages()[0].getName())) {
                return version;
            }
        }
        return null;
    }

    public boolean hasNewVersion() {
        if (TextUtils.isEmpty(haveNewest)) {
            return false;
        }
        boolean hasNewVersion = Boolean.valueOf(haveNewest);
        return hasNewVersion && isNewVersionVisible();
    }

    public boolean isNewVersionVisible() {
        return null != newest && newest.isVisible();
    }
}
