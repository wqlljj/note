package com.example.wangqi.mvvm.model.bean;

import java.util.List;

/**
 * Created by SX on 2017/11/2.
 */

public class FaceResponse {
    int code;
    String face_id;
    String faceset_id;
    String msg;
    List<FaceBean> faces;

    public FaceResponse(int code, String face_id, String faceset_id) {
        this.code = code;
        this.face_id = face_id;
        this.faceset_id = faceset_id;
    }

    public FaceResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getFace_id() {
        return face_id;
    }

    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    public String getFaceset_id() {
        return faceset_id;
    }

    public void setFaceset_id(String faceset_id) {
        this.faceset_id = faceset_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<FaceBean> getFaces() {
        return faces;
    }

    public void setFaces(List<FaceBean> faces) {
        this.faces = faces;
    }

    @Override
    public String toString() {
        return "FaceResponse{" +
                "code=" + code +
                ", face_id='" + face_id + '\'' +
                ", faceset_id='" + faceset_id + '\'' +
                ", msg='" + msg + '\'' +
                ", faces=\n" + faces +
                '}';
    }
}
