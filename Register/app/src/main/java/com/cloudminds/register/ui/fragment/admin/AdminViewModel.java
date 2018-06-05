package com.cloudminds.register.ui.fragment.admin;

import android.arch.lifecycle.ViewModel;

import com.cloudminds.register.repository.RegisterRepository;
import com.cloudminds.register.repository.network.bean.Admin;
import com.cloudminds.register.repository.network.bean.Response;

import io.reactivex.Flowable;

/**
 * Created
 */

class AdminViewModel extends ViewModel {

    private Admin mEntity;
    private final RegisterRepository mRepository;

    AdminViewModel(RegisterRepository repository) {
        this.mRepository = repository;
        mEntity = new Admin();
    }

    public Admin getEntity() {
        return mEntity;
    }

    /**
     * admin login.
     */
    Flowable<Response> login() {
        return mRepository.login(mEntity);
    }
}
