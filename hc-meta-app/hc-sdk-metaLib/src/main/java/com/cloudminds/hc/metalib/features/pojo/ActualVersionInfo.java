package com.cloudminds.hc.metalib.features.pojo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by willzhang on 16/06/17
 */

public class ActualVersionInfo implements Serializable {

    public static final String VALUE_FORCE_UPGRADE = "1";
    public static final String VALUE_WIPE_DATA = "1";
    public static final String VALUE_VERSION_VISIBLE = "1";

    private String time;

    private String formal;

    private Packages[] packages;

    private String buildtype;

    private String status;

    private String simplename;

    private String description;

    private String name;

    private String software_name;

    private String removeData;

    private String security;

    private String force;

    /////////////////////Fields not in json//////////////////////
    private long downloadedSize;

    private DownloadStatus downloadStatus = DownloadStatus.NOT_STARTED;

    public enum DownloadStatus {
        NOT_STARTED, DOWNLOADING, PAUSED, DOWNLOADED, UserCanceled
    }
    /////////////////////Fields not in json//////////////////////

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormal() {
        return formal;
    }

    public void setFormal(String formal) {
        this.formal = formal;
    }

    public Packages[] getPackages() {
        return packages;
    }

    public void setPackages(Packages[] packages) {
        this.packages = packages;
    }

    public String getBuildtype() {
        return buildtype;
    }

    public void setBuildtype(String buildtype) {
        this.buildtype = buildtype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSimplename() {
        return simplename;
    }

    public void setSimplename(String simplename) {
        this.simplename = simplename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSoftware_name() {
        return software_name;
    }

    public void setSoftware_name(String software_name) {
        this.software_name = software_name;
    }

    public String getRemoveData() {
        return removeData;
    }

    public void setRemoveData(String removeData) {
        this.removeData = removeData;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public String toString() {
        return "ClassPojo [time = " + time + ", formal = " + formal + ", packages = " + Arrays.toString(packages) + ", buildtype = " + buildtype + ", status = " + status + ", simplename = " + simplename + ", description = " + description + ", name = " + name + ", software_name = " + software_name + ", removeData = " + removeData + ", security = " + security + ", force = " + force + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActualVersionInfo && time.equals(((ActualVersionInfo) obj).getTime());
    }

    public boolean isForceUpgradeNeeded() {
        return VALUE_FORCE_UPGRADE.equals(force);
    }

    public boolean needRemoveData() {
        return VALUE_WIPE_DATA.equals(removeData);
    }

    public boolean isVisible() {
        return VALUE_VERSION_VISIBLE.equals(status);
    }
}
