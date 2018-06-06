package com.cloudminds.meta.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.meta.R;
import com.cloudminds.meta.presenter.UpgradePresenter;
import com.cloudminds.meta.activity.IUpgradeView;

/**
 * Created by tiger on 17-4-6.
 */

public class UpgradeFragment extends Fragment implements View.OnClickListener,IUpgradeView {

    public static final String TAG = "Meta:UpgradeFragment";

    private UpgradePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new UpgradePresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upgrade_activity,container,false);
        view.findViewById(R.id.upgrade_btn).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.upgrade_btn:
                Log.d(TAG,"do upgrade for meta");
                break;
        }
    }

    @Override
    public void callBack(int result) {

    }
}
