package com.cloudminds.register.ui.fragment.employee;

import android.arch.lifecycle.ViewModel;

import com.cloudminds.register.repository.RegisterRepository;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.HttpUtils;
import com.cloudminds.register.repository.network.bean.Department;
import com.cloudminds.register.repository.network.bean.Response;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created
 */

public class EmployeeViewModel extends ViewModel {

    private EmployeeEntity mEntity;

    private int mSelected = -1;
    private final RegisterRepository mRepository;

    EmployeeViewModel(RegisterRepository repository) {
        mRepository = repository;
        mEntity = new EmployeeEntity();
    }

    public EmployeeEntity getEntity() {
        return mEntity;
    }

    public void setEntity(EmployeeEntity entity) {
        this.mEntity = entity;
    }

    public int getSelected() {
        return mSelected;
    }

    public void setSelected(int selected) {
        this.mSelected = selected;
    }

    Flowable<Response> postEmployee() {
        return mRepository.postEmployee(mEntity);
    }

    Flowable<List<EmployeeEntity>> getEmployeeListFromDB() {
        return mRepository.getEmployeeListFromDB();
    }

    public Flowable<Department> getAllDepartment() {
        return mRepository.getAllDepartment();
    }
}
