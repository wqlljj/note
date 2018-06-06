package com.cloudminds.hc.hariservice.bean.dcProtocol;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Think-cc on 2017/5/5.
 */
public class CDPEntity {

    private CDPEntityBodyHeader bodyHeader;

    private String body;

    private String type;

    public CDPEntityBodyHeader getBodyHeader() {
        return bodyHeader;
    }

    public void setBodyHeader(CDPEntityBodyHeader bodyHeader) {
        this.bodyHeader = bodyHeader;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {return  type;}

    public void setType(String type) {
      this.type = type;
    }

}
