package com.cloudminds.hc.hariservice.bean.dcProtocol;

/**
 * Created by Think-cc on 2017/5/5.
 *
 * datatype  2 bytes
 * reserve   2  bytes
 * len       4 bytes
 * contentType 2 bytes (0 ： json)
 * Body  len-2 bytes
 */
public class CDPHeader {

    private Short datatype;

    private Short reserve;

    public Short getDatatype() {
        return datatype;
    }

    public void setDatatype(Short datatype) {
        this.datatype = datatype;
    }

    public Short getReserve() {
        return reserve;
    }

    public void setReserve(Short reserve) {
        this.reserve = reserve;
    }
}
