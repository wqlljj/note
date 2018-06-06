package com.cloudminds.hc.hariservice.bean.dcProtocol;

import java.util.List;

/**
 * Created by Think-cc on 2017/5/5.
 */
public class CDPData {

    private CDPHeader header;

    private List<CDPEntity> bodyList;

    public CDPHeader getHeader() {
        return header;
    }

    public void setHeader(CDPHeader header) {
        this.header = header;
    }

    public List<CDPEntity> getBodyList() {
        return bodyList;
    }

    public void setBodyList(List<CDPEntity> bodyList) {
        this.bodyList = bodyList;
    }
}
