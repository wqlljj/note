package com.cloudminds.meta.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by tiger on 17-4-11.
 */
@Entity
public class FamilyItemBean  {
    @Id
    private String face_id;
    private String name;
    private String imageLocalPath;
    private String remark;
    private String imageNetPath;

    public String getImageNetPath() {
        return imageNetPath;
    }

    public void setImageNetPath(String imageNetPath) {
        this.imageNetPath = imageNetPath;
    }

    public FamilyItemBean(String name, String imageLocalPath, String remark) {
        this.name = name;
        this.imageLocalPath = imageLocalPath;
        this.remark = remark;
    }

    public FamilyItemBean() {
    }

    @Generated(hash = 421089622)
    public FamilyItemBean(String face_id, String name, String imageLocalPath,
            String remark, String imageNetPath) {
        this.face_id = face_id;
        this.name = name;
        this.imageLocalPath = imageLocalPath;
        this.remark = remark;
        this.imageNetPath = imageNetPath;
    }
    

    public String getImageLocalPath() {
        return imageLocalPath;
    }

    public void setImageLocalPath(String imageLocalPath) {
        this.imageLocalPath = imageLocalPath;
    }
    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    public String getFace_id() {
        return this.face_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    public void copy(FamilyItemBean bean){
        this.face_id=bean.face_id;
        this.name = bean.name;
        this.imageLocalPath = bean.imageLocalPath;
        this.remark = bean.remark;
        this.imageNetPath = bean.imageNetPath;
    }


    @Override
    public String toString() {
        return "FamilyItemBean{" +
                "face_id='" + face_id + '\'' +
                ", name='" + name + '\'' +
                ", imageLocalPath='" + imageLocalPath + '\'' +
                ", remark='" + remark + '\'' +
                ", imageNetPath='" + imageNetPath + '\'' +
                '}';
    }
}
