package com.cloudminds.register.ui.fragment.visitor;

import android.arch.lifecycle.ViewModel;

import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.bean.Response;
import com.cloudminds.register.repository.network.bean.Visitor;
import com.cloudminds.register.repository.RegisterRepository;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created
 */

class VisitorViewModel extends ViewModel {

    private Visitor mVisitor;
    private EmployeeEntity mEmployee;
    private final RegisterRepository mRepository;

    VisitorViewModel(RegisterRepository repository) {
        this.mRepository = repository;
        mVisitor = new Visitor();
        mEmployee = new EmployeeEntity();
    }

    public Visitor getEntity() {
        return mVisitor;
    }

    public void setEntity(Visitor entity) {
        this.mVisitor = entity;
    }

    public EmployeeEntity getEmployee() {
        return mEmployee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.mEmployee = employee;
    }

    Flowable<Response> postVisitor() {
        return mRepository.postVisitor(mVisitor);
    }

    Flowable<List<EmployeeEntity>> getEmployeeListFromDB() {
        return mRepository.getEmployeeListFromDB();
    }

}
