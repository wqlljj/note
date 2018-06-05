package com.cloudminds.register.ui.fragment;


import android.arch.lifecycle.Lifecycle;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.register.R;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentMainContentBinding;
import com.cloudminds.register.ui.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainContentFragment extends BaseFragment {

    public static final String TAG = MainContentFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMainContentBinding mainContentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_content, container, false);
        mainContentBinding.setEmployeeCallback(mEmployeeClickCallback);
        mainContentBinding.setVisitorCallback(mVisitorClickCallback);

        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().show();

        return mainContentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private final OnClickCallback mEmployeeClickCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && (getActivity()) != null) {
            ((MainActivity) getActivity()).showEmployeeFragment();
        }
    };

    private final OnClickCallback mVisitorClickCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            ((MainActivity) getActivity()).showVisitorFragment();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.next).setTitle(R.string.admin_login);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                ((MainActivity) getActivity()).showAdminFragment();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
