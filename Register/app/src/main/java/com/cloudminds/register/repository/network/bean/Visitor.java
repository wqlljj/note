package com.cloudminds.register.repository.network.bean;

import java.io.File;

/**
 * Visitor
 */

public class Visitor {
    /**
     * visitor's image.
     */
    private File avatar;

    /**
     * "Yes" is active.
     * "No" is not active.
     */
    private String live = "Yes";

    /**
     * visitor's name
     */
    private String name;

    /**
     * visitor's gender
     * 1 is male.
     * 0 is female.
     */
    private int gender = 0;

    /*
     * visitor's company
     */
    private String company;

    /**
     * visitor's count
     */
    private int visitors = -1;

    /**
     * visitor's purpose
     */
    private String purpose;

    /**
     * visitor's interviewer
     */
    private String interviewer;

    /**
     * interviewer's ID
     */
    private int interviewerID;

    /**
     * interviewer's phone
     */
    private String phone;

    /**
     * interviewer's email
     */
    private String email;

    /**
     * interviewer's position
     * {PK,CD,SZ,FS}
     */
    private String position = "PK";

    public int getInterviewerID() {
        return interviewerID;
    }

    public void setInterviewerID(int interviewerID) {
        this.interviewerID = interviewerID;
    }

    /**

     * visitor's sign
     */
    private File sign;

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public File getAvatar() {
        return avatar;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    public String getLive() {
        return live;
    }

    public void setLive(String live) {
        this.live = live;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getVisitors() {
        return visitors;
    }

    public void setVisitors(int visitors) {
        this.visitors = visitors;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public File getSign() {
        return sign;
    }

    public void setSign(File sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "avatar=" + avatar +
                ", live='" + live + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", company='" + company + '\'' +
                ", visitors=" + visitors +
                ", purpose='" + purpose + '\'' +
                ", interviewer='" + interviewer + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                ", sign=" + sign +
                '}';
    }
}
