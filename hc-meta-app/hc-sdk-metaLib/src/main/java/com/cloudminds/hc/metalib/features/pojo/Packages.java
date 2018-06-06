package com.cloudminds.hc.metalib.features.pojo;

import java.io.Serializable;

/**
 * Created by willzhang on 16/06/17
 */

public class Packages implements Serializable {

    public static final String PACKAGE_TYPE_1 = "1"; // Incremental package
    public static final String PACKAGE_TYPE_2 = "2"; // Full package

    private String token;

    private String name;

    private String md5;

    private String type;

    private String size;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ClassPojo [token = " + token + ", name = " + name + ", md5 = " + md5 + ", type = " + type + ", size = " + size + "]";
    }
}
