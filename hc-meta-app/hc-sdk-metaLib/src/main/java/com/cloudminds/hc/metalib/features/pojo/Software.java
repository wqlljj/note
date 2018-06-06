package com.cloudminds.hc.metalib.features.pojo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by willzhang on 16/06/17
 */

public class Software implements Serializable {
    private Versions[] versions;

    private String name;

    public Versions[] getVersions() {
        return versions;
    }

    public void setVersions(Versions[] versions) {
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ClassPojo [versions = " + Arrays.toString(versions) + ", name = " + name + "]";
    }
}
