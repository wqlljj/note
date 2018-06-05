package com.cloudminds.register.repository.network.bean;

import com.cloudminds.register.repository.db.entity.EmployeeEntity;

/**
 * Created
 */

public class MqttMessageInfo {


    /**
     * list : {"Id":17,"Live":"1","Eid":"","Name":"111","Ename":"","DepartmentID":3,"Gender":"Female","Birthday":"2018-01-03","Ed":"2018-01-02","Phone":"13901234567","PhotoPath":"","Position":"PK","Email":"trr@qq.com"}
     * status : edit
     */
    public static final String EDIT = "edit";
    public static final String DELETE = "del";
    public static final String ADD = "add";

    private EmployeeEntity list;
    private String status;

    public EmployeeEntity getList() {
        return list;
    }

    public void setList(EmployeeEntity list) {
        this.list = list;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
