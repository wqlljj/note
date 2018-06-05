package com.cloudminds.register.ui.fragment.employee;


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
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.cloudminds.register.R;
import com.cloudminds.register.databinding.FragmentEmployeeBinding;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.ErrorConsumer;
import com.cloudminds.register.repository.network.bean.Department;
import com.cloudminds.register.repository.network.bean.Event;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.view.DepartmentAdapter;
import com.cloudminds.register.ui.view.NameFilterAdapter;
import com.cloudminds.register.utils.InjectorUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * EmployeeFragment
 */
public class EmployeeFragment extends BaseFragment {

    private static final String TAG = "EmployeeFragment";

    private FragmentEmployeeBinding mBinding;
    private EmployeeViewModel mModel;
    private EmployeeEntity mEmployee;
    private Department.DataBean mSelectedDepartment;
    private Map<Integer, String> mDepartmentMap = new HashMap<>();

    DepartmentAdapter mDepartmentAdapter;
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_employee, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        init();
        setHasOptionsMenu(true);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        showEmployee();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.employee);
        EmployeeViewModelFactory factory = InjectorUtils.provideEmployeeViewModelFactory(getActivity());
        mModel = ViewModelProviders.of(this, factory).get(EmployeeViewModel.class);
        mEmployee = mModel.getEntity();

        NameFilterAdapter namesAdapter = new NameFilterAdapter(
                getActivity(),
                R.layout.fragment_employee,
                R.id.lbl_name
        );
        ((AutoCompleteTextView) mBinding.employeeUsername.getEditText()).setAdapter(namesAdapter);
        ((AutoCompleteTextView) mBinding.employeeUsername.getEditText()).setOnItemClickListener((parent, view, position, id) -> {
            mEmployee = namesAdapter.getItem(position);
            mModel.setEntity(mEmployee);
            showEmployee();
        });
        getEmployeelist(namesAdapter);


        //get all department name.
        mDisposable.add(mModel.getAllDepartment()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    List<Department.DataBean> dataList = list.getData();
                    if (dataList != null && dataList.size() > 0) {
                        Log.i(TAG, "mDepartmentAdapter = " + dataList);
                        mDepartmentAdapter.setList(dataList);
                        for (int i = 0; i < dataList.size(); i++) {
                            mDepartmentMap.put(i, dataList.get(i).getId());
                        }
                        if (mModel.getSelected() != -1) {
                            mBinding.spinnerEmployeeDepartment.setSelection(mModel.getSelected());
                        }
                    }
                }, new ErrorConsumer<>())
        );
    }
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MAIN)
    public void onEvent(Event<Object> event){
        Log.e(TAG, "onEvent: "+event +"   "+mEmployee.getId());
        if(event.getCode()==1) {
            if(mEmployee.getId()==(int)event.getData()){
                mEmployee=new EmployeeEntity();
                mModel.setEntity(mEmployee);
                clearShow();
            }else if(mEmployee.getId()==-1){
                clearShow();
            }
            getEmployeelist((NameFilterAdapter) ((AutoCompleteTextView) mBinding.employeeUsername.getEditText()).getAdapter());
        }
    }

    private void getEmployeelist(NameFilterAdapter namesAdapter) {
        //get all employees.
        mDisposable.add(mModel.getEmployeeListFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(entityList -> {
                    if (entityList != null && entityList.size() > 0) {
                        Log.d(TAG, "entityList = " + entityList);
                        namesAdapter.setList(entityList);
                    }
                }, new ErrorConsumer<>()));
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
                getActivity().getSupportFragmentManager().popBackStack();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        mDisposable.clear();
    }

    private void init() {
        setHasOptionsMenu(true);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addTextChangedListener(mBinding.employeeUsername);
        addTextChangedListener(mBinding.employeeEname);
        addTextChangedListener(mBinding.employeeEid);

        //department
        mDepartmentAdapter = new DepartmentAdapter(
                getActivity(),
                R.layout.fragment_employee,
                android.R.id.text1
        );
        mBinding.spinnerEmployeeDepartment.setAdapter(mDepartmentAdapter);
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void addTextChangedListener(TextInputLayout view) {
        if (view.getEditText() != null) {
            view.getEditText().addTextChangedListener(new EmployeeTextWatcher(view));
        } else {
            Log.w(TAG, "addTextChangedListener is null");
        }
    }

    /**
     * Upload employee information
     */
    private void onNext() {
        boolean hasEmployee=true;
        boolean isRequest = true;
        String name = mBinding.employeeUsername.getEditText().getText().toString();
        if(mEmployee.getId()==-1&&!"".equals(name)){
            hasEmployee=false;
            isRequest=false;
        }
        int departmentSelected = mBinding.spinnerEmployeeDepartment.getSelectedItemPosition();
        if(departmentSelected!=-1) {
            Department.DataBean department = (Department.DataBean) mBinding.spinnerEmployeeDepartment.getSelectedItem();
            Log.i(TAG, "spinnerEmployeeDepartment = " + departmentSelected);
            mModel.setSelected(departmentSelected);
            String departmentId = department.getId();
            mEmployee.setDepartmentID(Integer.parseInt(departmentId));
        }else{
            isRequest=false;
        }
        String eName = mBinding.employeeEname.getEditText().getText().toString();
        String eId = mBinding.employeeEid.getEditText().getText().toString();

        //Is it possible to upload information.

        if (!"".equals(name)) {
            mEmployee.setName(name);
        } else {
            isRequest = false;
            setErrorMsg(mBinding.employeeUsername, getResources().getString(R.string.employee_user_name));
        }

        Log.i(TAG, "ename = " + eName);
        if (!"".equals(eName)) {
            mEmployee.setEname(eName);
        }

        if (!"".equals(eId)) {
            mEmployee.setEid(eId);
        } else {
            mEmployee.setEid(UUID.randomUUID().toString());
        }

        mEmployee.setLive("Yes");
        mEmployee.setPosition(getString(R.string.config_default_position));

        Calendar cale = Calendar.getInstance();
        mEmployee.setBirthday(mSimpleDateFormat.format(cale.getTime()));
        mEmployee.setEd(mSimpleDateFormat.format(cale.getTime()));

        //set entity.
        Log.d(TAG, "doPostEmployee employee = " + mEmployee.toString());
        if (isRequest) {
            if ((getActivity()) != null) {
                ((MainActivity) getActivity()).showEmployeeImageFragment(mEmployee);
            }
        } else {
            if(hasEmployee) {
                Toast.makeText(getActivity(), R.string.employee_request_problem, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "doPostEmployee isn't requested,isRequest is false ");
            }else{
                Toast.makeText(getActivity(), R.string.employee_request_problem_1, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "doPostEmployee isn't requested,hasEmployee is false ");
            }
        }
        Log.i("EmployeeImageFragment", "onnext  isRequest= " + isRequest);
    }

    private void setErrorEnabledFalse(TextInputLayout view) {
        view.setErrorEnabled(false);
    }

    private void setErrorMsg(TextInputLayout view, String type) {
        view.setError(String.format(getResources().getString(R.string.employee_msg), type.replaceAll("\\s*", "")));
    }
    private void clearShow(){
        Toast.makeText(getActivity(), R.string.data_update, Toast.LENGTH_SHORT).show();
        mBinding.employeeUsername.getEditText().setText("");
        mBinding.employeeEname.getEditText().setText("");
        this.mBinding.employeeEid.getEditText().setText("");
    }
    private void showEmployee() {
        if (mBinding != null) {
            Log.i(TAG, "showEmployee mEmployee = " + mEmployee + "mModel.getSelected() = " + mModel.getSelected());
            if (!"".equals(mEmployee.getName()) && mBinding.employeeUsername != null) {
                mBinding.employeeUsername.getEditText().setText(mEmployee.getName());
            }

            if (!"".equals(mEmployee.getEname()) && mBinding.employeeEname != null) {
                mBinding.employeeEname.getEditText().setText(mEmployee.getEname());
            }
            if ((!"".equals(this.mEmployee.getEid())) && (this.mBinding.employeeEid != null)) {
                this.mBinding.employeeEid.getEditText().setText(this.mEmployee.getEid());
            }
            for (Map.Entry<Integer, String> entry : mDepartmentMap.entrySet()) {
                if (entry.getValue().equals(mEmployee.getDepartmentID() + "")) {
                    System.out.println(entry.getKey());
                    mBinding.spinnerEmployeeDepartment.setSelection(entry.getKey());
                }
            }
        }
    }


    /**
     * Employee TextWatcher
     */
    private class EmployeeTextWatcher implements TextWatcher {

        private final View view;

        private EmployeeTextWatcher(View view) {
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
                case R.id.employee_username:
                    setErrorEnabledFalse(mBinding.employeeUsername);
                    break;
                case R.id.employee_ename:
                    setErrorEnabledFalse(mBinding.employeeEname);
                    break;
                case R.id.employee_eid:
                    setErrorEnabledFalse(mBinding.employeeEid);
                    break;
                default:
                    Log.w(TAG, "EmployeeTextWatcher no id = " + view.getId());
                    break;
            }
        }
    }
}
