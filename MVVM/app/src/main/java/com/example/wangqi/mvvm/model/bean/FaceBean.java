package com.example.wangqi.mvvm.model.bean;

/**
 * Created by SX on 2017/11/9.
 */

public class FaceBean {

    /**
     * date : 2017-07-10 15:03:58
     * face_id : w8qDXt5tGrZ3X4aHtgBWm053d7XCnXHj
     * image : http://39.155.168.50:1688/download/face_img/2017-07-10/CNqk8ikP3UDp2R74.jpg
     * name : fbb
     */

    private String date;
    private String face_id;
    private String image;
    private String name;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFace_id() {
        return face_id;
    }

    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FaceBean{" +
                "date='" + date + '\'' +
                ", face_id='" + face_id + '\'' +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                "}\n";
    }
}
