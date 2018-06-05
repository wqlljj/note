package com.cloudminds.register.ui.fragment.welcome;

import android.arch.lifecycle.ViewModel;

import com.cloudminds.register.repository.RegisterRepository;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.db.entity.EmployeeInfo;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created
 */

class WelcomeViewModel extends ViewModel {

    private final RegisterRepository mRepository;

    WelcomeViewModel(RegisterRepository registerRepository) {
        this.mRepository = registerRepository;
    }

    Flowable<EmployeeInfo> getAllEmployee(String type) {
        return mRepository.getAllEmployee(type);
    }

    void updateAllEmployee(List<EmployeeEntity> entity) {
        mRepository.updateAllEmployee(entity);
    }

}
