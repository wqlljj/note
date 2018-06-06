package com.cloudminds.meta.bean;

/**
 * Created by tiger on 17-4-1.
 */

public class UserBean {
    private String user;
    private String pass;

    public UserBean(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public String toString() {
        return "username = "+user+",password = "+pass;
    }
}
