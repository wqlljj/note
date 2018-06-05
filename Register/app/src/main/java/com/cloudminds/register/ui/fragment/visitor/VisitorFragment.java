package com.cloudminds.register.ui.fragment.visitor;


import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.cloudminds.register.R;
import com.cloudminds.register.databinding.FragmentVisitorBinding;
import com.cloudminds.register.repository.network.bean.Visitor;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.view.NameFilterAdapter;
import com.cloudminds.register.utils.InjectorUtils;
import com.cloudminds.register.utils.Utils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * VisitorFragment
 */
public class VisitorFragment extends BaseFragment {

    private static final String TAG = "VisitorFragment";

    private FragmentVisitorBinding mBinding;
    private VisitorViewModel mModel;
    private Visitor mEntity;

    private ArrayAdapter<CharSequence> mPurposeAdapter;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_visitor, container, false);
        init();
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBinding != null) {
            if (!"".equals(mEntity.getName())) {
                mBinding.visitorUsername.getEditText().setText(mEntity.getName());
            }

            if (!"".equals(mEntity.getCompany())) {
                mBinding.visitorCompany.getEditText().setText(mEntity.getCompany());
            }

            if (!"".equals(mEntity.getInterviewer())) {
                mBinding.visitorInterviewer.getEditText().setText(mEntity.getInterviewer());
            }

            if (!"".equals(mEntity.getPhone())) {
                mBinding.visitorPhone.getEditText().setText(mEntity.getPhone());
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.visitor);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        VisitorViewModelFactory factory = InjectorUtils.provideVisitorViewModelFactory(getActivity());
        mModel = ViewModelProviders.of(this, factory).get(VisitorViewModel.class);
        mEntity = mModel.getEntity();
        if (mEntity.getName() != null) {
            mBinding.visitorUsername.getEditText().setText(mEntity.getName());
        }

        NameFilterAdapter namesAdapter = new NameFilterAdapter(
                getActivity(),
                R.layout.fragment_visitor,
                R.id.lbl_name
        );
        ((AutoCompleteTextView) mBinding.visitorInterviewer.getEditText()).setAdapter(namesAdapter);
        ((AutoCompleteTextView) mBinding.visitorInterviewer.getEditText()).setOnItemClickListener((parent, view, position, id) -> {
            mModel.setEmployee(namesAdapter.getItem(position));
        });

        mDisposable.add(mModel.getEmployeeListFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(entityList -> {
                    if (entityList != null && entityList.size() > 0) {
                        Log.d(TAG, "entityList = " + entityList);
                        namesAdapter.setList(entityList);
                    }
                }, throwable -> Log.w(TAG, "error" + throwable.getMessage())));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
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
                doPostVisitor();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        setHasOptionsMenu(true);

        addTextChangedListener(mBinding.visitorUsername);
        addTextChangedListener(mBinding.visitorCompany);
        addTextChangedListener(mBinding.visitorInterviewer);
        addTextChangedListener(mBinding.visitorPhone);

        //purpose
        mPurposeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.array_purpose, android.R.layout.simple_list_item_1);
        mBinding.spinnerVisitorPurpose.setAdapter(mPurposeAdapter);
    }

    private void addTextChangedListener(TextInputLayout view) {
        view.getEditText().addTextChangedListener(new VisitorTextWatcher(view));
    }

    /**
     * Upload visitor information
     */
    private void doPostVisitor() {
        int purpose = mBinding.spinnerVisitorPurpose.getSelectedItemPosition();
        String name = mBinding.visitorUsername.getEditText().getText().toString();
        String company = mBinding.visitorCompany.getEditText().getText().toString();
        String interviewer = mBinding.visitorInterviewer.getEditText().getText().toString();
        String phone = mBinding.visitorPhone.getEditText().getText().toString();

        //Is it possible to upload information.
        boolean isRequest = true;
        mEntity.setGender(Integer.parseInt(getString(R.string.config_default_gender)));
        mEntity.setPosition(getString(R.string.config_default_position));
        mEntity.setPurpose(mPurposeAdapter.getItem(purpose).toString());
        mEntity.setVisitors(Integer.parseInt(getString(R.string.config_default_visitor_number)));
        mEntity.setCompany("".equals(company) ? getString(R.string.config_default_company) : company);

        if (!"".equals(name)) {
            mEntity.setName(name);
        } else {
            isRequest = false;
            setErrorMsg(mBinding.visitorUsername, getResources().getString(R.string.visitor_user_name));
        }

        if (!"".equals(interviewer)) {
            mEntity.setInterviewer(interviewer);
            int employeeId = mModel.getEmployee().getId();
            mEntity.setInterviewerID(employeeId == -1 ? 0 : employeeId);
        } else {
            isRequest = false;
            setErrorMsg(mBinding.visitorInterviewer, getResources().getString(R.string.visitor_interviewer));
        }

        if (isPhoneCorrectOrNull()) {
            mEntity.setPhone(phone);
        } else {
            isRequest = false;
            mBinding.visitorPhone.setError(getResources().getString(R.string.phone_error));
        }

        Log.i(TAG, "doPostVisitor = " + mEntity);
        if (isRequest) {
            ((MainActivity) getActivity()).showSignFragment(mEntity);
        } else {
            Toast.makeText(getActivity(), R.string.employee_request_problem, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "doPostVisitor isn't requested,isRequest is false ");
        }
    }

    /**
     * When phone number is correct or phone is null, it can be upload
     *
     * @return phone number correct or null.
     */
    private boolean isPhoneCorrectOrNull() {
        String phone = mBinding.visitorPhone.getEditText().getText().toString().trim();
        Log.d(TAG, "isPhoneCorrectsOrNull phone = " + phone + " Utils.isPhone(phone) = " + Utils.isPhone(phone));
        if (!"".equals(phone) && Utils.isPhone(phone)) {
            setErrorEnabledFalse(mBinding.visitorPhone);
            return true;
        }
        return false;
    }

    private void setErrorEnabledFalse(TextInputLayout view) {
        view.setErrorEnabled(false);
    }

    private void setErrorMsg(TextInputLayout view, String type) {
        view.setError(String.format(getResources().getString(R.string.employee_msg), type.replaceAll("\\s*", "")));
    }

    /**
     * Employee TextWatcher
     */
    private class VisitorTextWatcher implements TextWatcher {

        private final View view;

        private VisitorTextWatcher(View view) {
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
                case R.id.visitor_username:
                    setErrorEnabledFalse(mBinding.visitorUsername);
                    break;
                case R.id.visitor_company:
                    setErrorEnabledFalse(mBinding.visitorCompany);
                    break;
                case R.id.visitor_interviewer:
                    setErrorEnabledFalse(mBinding.visitorInterviewer);
                    break;
                case R.id.visitor_phone:
                    setErrorEnabledFalse(mBinding.visitorPhone);
                    break;
                default:
                    Log.w(TAG, "AdminTextWatcher no id = " + view.getId());
                    break;
            }
        }
    }
}
