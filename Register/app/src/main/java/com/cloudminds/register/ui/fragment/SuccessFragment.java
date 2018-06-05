package com.cloudminds.register.ui.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.register.R;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentSuccessBinding;
import com.cloudminds.register.ui.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessFragment extends BaseFragment {

    private static final String TAG = "SuccessFragment";
    private FragmentSuccessBinding mBinding;

    private OnClickCallback mCompleteCallback = this::onNext;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_success, container, false);
        mBinding.setNext(mCompleteCallback);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().hide();
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
                onNext();
                break;
            case android.R.id.home:
                onPrev();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onPrev() {
        Log.d(TAG,"onPrev");
        if (getActivity() == null) {
            return;
        }
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        for (int i = 0; i < count - 1; ++i) {
            fm.popBackStack();
        }
    }

    public void onNext() {
        Log.d(TAG,"onNext");
        if (getActivity() == null) {
            return;
        }
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        for (int i = 0; i < count; ++i) {
            fm.popBackStack();
        }
    }

}
