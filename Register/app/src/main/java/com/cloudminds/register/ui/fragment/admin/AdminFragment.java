package com.cloudminds.register.ui.fragment.admin;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cloudminds.register.R;
import com.cloudminds.register.broadcast.InternetBroadcast;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentAdminBinding;
import com.cloudminds.register.repository.network.bean.Admin;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.utils.InjectorUtils;
import com.cloudminds.register.utils.MD5;
import com.cloudminds.register.utils.Utils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminFragment extends BaseFragment {

    private FragmentAdminBinding mBinding;
    private AdminViewModel mModel;
    private Admin mEntity;

    private MainActivity mActivity;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false);
        mBinding.setLoginCallback(mLoginCallback);
        if (mBinding.adminPassword.getEditText() != null) {
            mBinding.adminPassword.getEditText().addTextChangedListener(new AdminTextWatcher(mBinding.adminPassword));
        }
        setHasOptionsMenu(true);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AdminViewModelFactory factory = InjectorUtils.provideAdminViewModelFactory(getActivity());
        mModel = ViewModelProviders.of(this, factory).get(AdminViewModel.class);
        mEntity = mModel.getEntity();
        mActivity = ((MainActivity) getActivity());
        if (mActivity != null) {
            getActivity().setTitle(R.string.admin);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.next).setTitle(getString(R.string.menu_next));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBinding != null && mBinding.adminPassword != null) {
            mBinding.adminPassword.getEditText().setText("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                login();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private final OnClickCallback mLoginCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            login();
        }
    };

    private void login() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            String pwd = mBinding.adminPassword.getEditText().getText().toString();
            mEntity.setPwd(MD5.calculateMD5(pwd));
            Log.d(Utils.TAG, "pwd after md5 = " + mEntity.getPwd());
            if (!InternetBroadcast.isNetworkAvailable()) {
                Toast.makeText(this.mActivity, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                return;
            }
            mDisposable.add(mModel.login()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseEntity -> {
                        if ("true".equals(responseEntity.getStatus())) {
                            loginSuccess();
                        } else {
                            loginFailure(responseEntity.getMsg());
                        }
                    }, throwable -> loginFailure(throwable.getMessage()))
            );
        }
    }

    private void loginSuccess() {
        mActivity.setLoginSuccessStatus();
        ((MainActivity) getActivity()).showEmployeeFragment();
        Toast.makeText(getActivity(), getString(R.string.admin_request_success), Toast.LENGTH_SHORT).show();
    }

    private void loginFailure(String msg) {
        Log.d("AdminFragment", "loginFailure msg = " + msg);
        mActivity.setLoginFailureStatus();
        mBinding.adminPassword.setError(getResources().getString(R.string.login_password_incorrect));
        //Snackbar.make(mBinding.containerAdmin, R.string.login_failure, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Login TextWatcher
     */
    private class AdminTextWatcher implements TextWatcher {

        private final View view;

        private AdminTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.admin_password:
                    mBinding.adminPassword.setErrorEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }

}
