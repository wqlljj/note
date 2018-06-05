package com.cloudminds.register.ui.fragment.visitor;


import android.arch.lifecycle.Lifecycle;
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
import com.cloudminds.register.databinding.FragmentSignBinding;
import com.cloudminds.register.repository.network.bean.Visitor;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.view.ProgressDialogHandler;
import com.cloudminds.register.utils.InjectorUtils;
import com.cloudminds.register.utils.Utils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignFragment extends BaseFragment {

    private static final String TAG = "SignFragment";
    private FragmentSignBinding mBinding;
    private ProgressDialogHandler mHandler;

    private Visitor mVisitor;
    private File mSignFile;

    private final OnClickCallback mClearCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            doClear();
        }
    };

    private final OnClickCallback mFinishCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            doFinish();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign, container, false);
        init();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        VisitorViewModelFactory factory = InjectorUtils.provideVisitorViewModelFactory(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.next).setTitle(getString(R.string.menu_next));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                doFinish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        mSignFile = null;
        mBinding.setClearCallback(mClearCallback);

        mHandler = new ProgressDialogHandler(getActivity(), getResources().getString(R.string.save_sign));
    }

    public void setVisitor(Visitor visitor) {
        this.mVisitor = visitor;
    }

    private void doClear() {
        mBinding.signView.clean();
    }

    private void onNext() {
        boolean isRequest = true;
        if (mSignFile != null) {
            mVisitor.setSign(mSignFile);
        } else {
            isRequest = false;
        }

        if (isRequest) {
            ((MainActivity) getActivity()).showVisitorImageFragment(mVisitor);
        } else {
            Toast.makeText(getActivity(), R.string.employee_request_problem, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "doPostVisitor isn't requested,isRequest is false ");
        }
    }

    private void doFinish() {
        mHandler.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    if (!mBinding.signView.isEmpty()) {
                        mBinding.signView.save();
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable(getString(R.string.need_sign)));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {

                }, throwable -> {
                    mHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
                    Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }, () -> {
                    mSignFile = Utils.getSignImage();
                    onNext();
                    mHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
                });
    }

}
