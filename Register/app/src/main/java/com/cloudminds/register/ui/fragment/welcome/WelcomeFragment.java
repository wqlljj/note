package com.cloudminds.register.ui.fragment.welcome;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cloudminds.register.R;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentWelcomeBinding;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.view.ProgressDialogHandler;
import com.cloudminds.register.utils.InjectorUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.cloudminds.register.BasicApp.isFirst;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends BaseFragment {

    public static final String TAG = "WelcomeFragment";

    private FragmentWelcomeBinding mBinding;

    private WelcomeViewModel mModel;

    private ProgressDialogHandler mProgress;

    private final OnClickCallback visitorCallback = this::onVisitorEnter;
    private final OnClickCallback employeeCallback = this::onEmployeeEnter;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private MainActivity mActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false);
        mBinding.setVisitor(visitorCallback);
        mBinding.setEmployee(employeeCallback);
        setHasOptionsMenu(true);
        mProgress = new ProgressDialogHandler(getActivity(), "Update...");
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WelcomeViewModelFactory factory = InjectorUtils.provideWelcomeViewModelFactory(getActivity().getApplicationContext());
        mModel = ViewModelProviders.of(this, factory).get(WelcomeViewModel.class);
        mActivity = ((MainActivity) getActivity());
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getSupportActionBar().hide();
        Log.i(TAG, "isFirst---------" + isFirst);
        if (isFirst) {
            mProgress.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
            mDisposable.add(mModel.getAllEmployee("all")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnNext(employeeInfo -> {
                        if (employeeInfo.getData() != null) {
                            for (EmployeeEntity employeeEntity : employeeInfo.getData()) {
                                Log.i(TAG, "size = " + employeeInfo.getData().size() + " employees after = " + employeeEntity);
                            }
                            mModel.updateAllEmployee(employeeInfo.getData());
                        } else {
                            Log.w(TAG, "get all employee info is null");
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(entityList -> {
                        for (EmployeeEntity employeeEntity : entityList.getData()) {
                            Log.d(TAG, "size = " + entityList.getData().size() + " employees before = " + employeeEntity);
                        }
                        onSuccess();
                    }, throwable -> onFailure(throwable.getMessage())));
            isFirst = false;
        }
    }

    private void onSuccess() {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), getString(R.string.update_request_success), Toast.LENGTH_SHORT).show();
    }

    private void onFailure(String msg) {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), "request failure : " + msg, Toast.LENGTH_SHORT).show();
        Log.d("WelcomeFragment", "msg = " + msg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.next).setTitle(R.string.admin_login);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                mActivity.showAdminFragment();
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

    private void onVisitorEnter() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            mActivity.showVisitorFragment();
        }
    }

    private void onAdminEnter() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && (getActivity()) != null) {
            if (!mActivity.isLogin()) {
                mActivity.showAdminFragment();
            } else {
                Toast.makeText(mActivity, getString(R.string.already_login), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onEmployeeEnter() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && (getActivity()) != null) {
            if (!mActivity.isLogin()) {
                mActivity.showAdminFragment();
            } else {
                mActivity.showEmployeeFragment();
            }
        }
    }
}
