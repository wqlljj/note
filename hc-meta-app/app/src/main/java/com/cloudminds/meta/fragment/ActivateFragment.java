package com.cloudminds.meta.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.HubActivity;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.presenter.ActivatePresenter;
import com.cloudminds.meta.view.DetermineDialog;
import com.cloudminds.meta.view.EditTextForNumberWithDel;
import com.cloudminds.meta.view.EditTextWithDel;
import com.cloudminds.meta.activity.IActivateView;

/**
 * Created by tiger on 17-4-6.
 */

public class ActivateFragment extends Fragment implements View.OnClickListener,IActivateView {

    public static final String TAG = "Meta:ActivateFragment";

    private EditTextForNumberWithDel mUserName;
    private EditTextWithDel mPassWord;
    private TextView mResult;
    private Button mActivate;
    private ActivatePresenter mPresenter;
    private DetermineDialog dialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new ActivatePresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activate_fragment,container,false);
        mUserName = (EditTextForNumberWithDel) view.findViewById(R.id.activate_username);
        mPassWord = (EditTextWithDel) view.findViewById(R.id.activate_password);
        mResult = (TextView) view.findViewById(R.id.activate_result_info);
        mActivate = (Button) view.findViewById(R.id.activate_btn);
        mActivate.setOnClickListener(this);
        view.findViewById(R.id.skip_activation).setOnClickListener(this);
        view.findViewById(R.id.forget_password).setOnClickListener(this);
        mPresenter.checkUsb();
        if(getActivity().getIntent().getBooleanExtra("startCall",false)){
            nextSetup();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activate_btn:
                Log.d(TAG,"activate_btn");
                if(mPresenter != null){
                    mPresenter.activateUser();
                }else {
                    Log.d(TAG,"mPresenter is null,cann't activate user");
                }
                break;
            case R.id.prop_ok:
                setButtonEnable(true);
                if(dialog != null){
                    dialog.dismiss();
                    dialog = null;
                }
                break;
            case R.id.skip_activation:
                nextSetup();
                break;
            case R.id.forget_password:
                ToastUtil.show(getActivity().getApplicationContext(), R.string.being_developed);
                break;
        }
    }

    public void setButtonEnable(boolean enable){
        if(mActivate!=null){
            mActivate.setEnabled(enable);
        }
    }

    @Override
    public void nextSetup() {
        Intent intent = new Intent(getActivity(), HubActivity.class);
        intent.putExtra("startCall",getActivity().getIntent().getBooleanExtra("startCall",false));
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void setStateByUsb(boolean state) {
        if(state){
            mResult.setVisibility(View.GONE);
            mActivate.setEnabled(true);
        }else {
            mResult.setVisibility(View.VISIBLE);
            mActivate.setEnabled(true);
        }
    }

    @Override
    public void showDialog(String code, String message) {
        DetermineDialog.Builder builder = new DetermineDialog.Builder();
        switch (code){
            case Constant.REGISTER_NETWORK_UNAVAILABLE:
                builder.setTitle(R.string.activate_network_unavailable);
                break;
            case Constant.REGISTER_USER_NAME_OR_PASSWORD_EMPTY:
                builder.setTitle(R.string.activate_user_name_or_password_empty);
                break;
            case Constant.REGISTER_PASSWORD_ERROR:
            case Constant.REGISTER_USER_NOT_EXIST:
            case Constant.REGISTER_ON_FAILURE:
                builder.setMessage(message);
                break;
        }
        dialog = builder.setOkListener(this)
                .setOk(R.string.btn_ok)
                .builder();
        dialog.show(getActivity());
    }

    @Override
    public String getUser() {
        Log.d(TAG,"getUser = "+mUserName.getText().toString());
        return mUserName.getText().toString();
    }

    @Override
    public String getPass() {
        Log.d(TAG,"getPass = "+mPassWord.getText().toString());
        return mPassWord.getText().toString();
    }

    public void cutIn(){
        setStateByUsb(true);
    }

    public void cutOut(){
        setStateByUsb(false);
    }
}
