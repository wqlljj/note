package com.cloudminds.register.repository.network.bean;

/**
 * Created
 */

public class Admin {

    /**
     * admin's password
     */
    private String pwd;

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "pwd='" + pwd + '\'' +
                '}';
    }
}
