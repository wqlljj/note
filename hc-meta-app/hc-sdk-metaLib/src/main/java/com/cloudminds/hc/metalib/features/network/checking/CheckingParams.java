package com.cloudminds.hc.metalib.features.network.checking;

/**
 * Created by SX on 2017/9/8.
 */

public class CheckingParams {
    public static final String FORMAL_TEST = "0";
    public static final String FORMAL_MAIN = "1";
    private String product;
    private String formal;
    private String secure;
    private String incremental;
    private String build_time;
    private String build_type;
    private String variant;
    private String display_version;

    public CheckingParams() {
        product="meta";
        formal="1";//0测试版 1正式版
        secure="0";
        incremental="5";
        build_type="user";
        variant="helmet";
//        display_version="meta-1.0.0";
//        build_time="20170627_003147";
        build_time="20170703_003147";
        display_version="meta-1.1.1";
    }

    public CheckingParams(String product, String formal, String secure, String incremental, String build_time, String build_type, String variant, String display_version) {
        this.product = product;
        this.formal = formal;
        this.secure = secure;
        this.incremental = incremental;
        this.build_time = build_time;
        this.build_type = build_type;
        this.variant = variant;
        this.display_version = display_version;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getFormal() {
        return formal;
    }

    public void setFormal(String formal) {
        this.formal = formal;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String getIncremental() {
        return incremental;
    }

    public void setIncremental(String incremental) {
        this.incremental = incremental;
    }

    public String getBuild_time() {
        return build_time;
    }

    public void setBuild_time(String build_time) {
        this.build_time = build_time;
    }

    public String getBuild_type() {
        return build_type;
    }

    public void setBuild_type(String build_type) {
        this.build_type = build_type;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getDisplay_version() {
        return display_version;
    }

    public void setDisplay_version(String display_version) {
        this.display_version = display_version;
    }

    @Override
    public String toString() {
        return "CheckingParams{" +
                "product='" + product + '\'' +
                ", formal='" + formal + '\'' +
                ", secure='" + secure + '\'' +
                ", incremental='" + incremental + '\'' +
                ", build_time='" + build_time + '\'' +
                ", build_type='" + build_type + '\'' +
                ", variant='" + variant + '\'' +
                ", display_version='" + display_version + '\'' +
                '}';
    }
}
