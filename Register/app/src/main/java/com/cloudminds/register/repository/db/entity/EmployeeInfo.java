package com.cloudminds.register.repository.db.entity;

import java.util.List;

/**
 * Created
 */

public class EmployeeInfo {

    private List<EmployeeEntity> data;

    public List<EmployeeEntity> getData() {
        return data;
    }

    public void setData(List<EmployeeEntity> data) {
        this.data = data;
    }
}
