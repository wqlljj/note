package com.cloudminds.hc.hariservice.bean.dcProtocol;

/**
 * Created by Think-cc on 2017/5/5.
 */
public class CDPEntityBodyHeader {

    private Integer len;

    public Integer getLen() {
        return len;
    }

    public void setLen(Integer len) {
        this.len = len;
    }

    public Short getContentType() {
        return contentType;
    }

    public void setContentType(Short contentType) {
        this.contentType = contentType;
    }

    private Short contentType;
}
