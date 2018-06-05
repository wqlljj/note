package com.cloudminds.register.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.cloudminds.register.R;
import com.cloudminds.register.broadcast.InternetBroadcast;
import com.cloudminds.register.databinding.ActivityMainBinding;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.bean.Visitor;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.fragment.MainContentFragment;
import com.cloudminds.register.ui.fragment.SuccessFragment;
import com.cloudminds.register.ui.fragment.admin.AdminFragment;
import com.cloudminds.register.ui.fragment.employee.EmployeeFragment;
import com.cloudminds.register.ui.fragment.employee.EmployeeImageFragment;
import com.cloudminds.register.ui.fragment.visitor.SignFragment;
import com.cloudminds.register.ui.fragment.visitor.VisitorFragment;
import com.cloudminds.register.ui.fragment.visitor.VisitorImageFragment;
import com.cloudminds.register.ui.fragment.welcome.WelcomeFragment;
import com.cloudminds.register.utils.InjectorUtils;
import com.cloudminds.register.utils.Utils;

public class MainActivity extends AppCompatActivity implements InternetBroadcast.InternetChangeListener {

    private final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private final static String TAG = "MainActivity";
    private ActivityMainBinding mBinding;

    /**
     * Permissions required to save the pic.
     */
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private InternetBroadcast internetBroadcast;
    boolean isFrist=true;
    @Override
    public void changeInternet(boolean isAvailable) {
        if(!isFrist||!isAvailable)
        Toast.makeText(this, isAvailable?R.string.network_available:R.string.network_unavailable, Toast.LENGTH_SHORT).show();
    }

    private enum LoginStatus {
        SUCCESS,
        FAILURE,
    }

    private LoginStatus mCurrentStatus = LoginStatus.FAILURE;

    private VisitorFragment visitorFragment;
    private EmployeeFragment employeeFragment;
    private AdminFragment adminFragment;
    private MainContentFragment mainContentFragment;
    private SignFragment signFragment;
    private WelcomeFragment welcomeFragment;
    private EmployeeImageFragment employeeImageFragment;
    private SuccessFragment successFragment;
    private VisitorImageFragment visitorImageFragment;

    private BaseFragment mCurrentFragment;
    private MainViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        internetBroadcast = new InternetBroadcast();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetBroadcast,intentFilter);
        InternetBroadcast.setInternetChangeListener(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setSupportActionBar(mBinding.toolbar);
        setTitle(R.string.app_name);

        if (savedInstanceState == null) {
            if (welcomeFragment == null) {
                welcomeFragment = new WelcomeFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, welcomeFragment, WelcomeFragment.TAG)
                    .commit();
            mCurrentFragment = welcomeFragment;
        }
        //Get MainViewModel
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(this);
        mModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);
        mModel.initClient();
        //check permission
        checkStoragePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                break;
            }
            default:
                Log.w(TAG, "onRequestPermissionsResult requestCode is not exists");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(Utils.TAG, "onBackPressed, mCurrentFragment = " + mCurrentFragment);
        if (mCurrentFragment instanceof SuccessFragment) {
            successFragment.onNext();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mModel.disconnect();
        unregisterReceiver(internetBroadcast);
    }

    public void showVisitorFragment() {
        if (visitorFragment == null) {
            visitorFragment = new VisitorFragment();
        }
        showFragment(visitorFragment);
    }

    public void showEmployeeFragment() {
        if (employeeFragment == null) {
            employeeFragment = new EmployeeFragment();
        }
        showFragment(employeeFragment);
    }

    public void showAdminFragment() {
        if (adminFragment == null) {
            adminFragment = new AdminFragment();
        }
        showFragment(adminFragment);
    }

    public void showMainContentFragment() {
        if (mainContentFragment == null) {
            mainContentFragment = new MainContentFragment();
        }
        showFragment(mainContentFragment);
    }

    public void showEmployeeImageFragment(EmployeeEntity employee) {
        if (employeeImageFragment == null) {
            employeeImageFragment = new EmployeeImageFragment();
        }
        employeeImageFragment.setEmployee(employee);
        showFragment(employeeImageFragment);
    }

    public void showSuccessFragment() {
        if (successFragment == null) {
            successFragment = new SuccessFragment();
        }
        showFragment(successFragment);
    }

    public void showSignFragment(Visitor visitor) {
        if (signFragment == null) {
            signFragment = new SignFragment();
        }
        signFragment.setVisitor(visitor);
        showFragment(signFragment);
    }

    public void showVisitorImageFragment(Visitor visitor) {
        if (visitorImageFragment == null) {
            visitorImageFragment = new VisitorImageFragment();
        }
        visitorImageFragment.setVisitor(visitor);
        showFragment(visitorImageFragment);
    }

    private void showFragment(BaseFragment fragment) {
        if (checkLoginSuccess(fragment)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter,
                            R.animator.fragment_slide_right_exit)
                    .replace(R.id.fragment_container, fragment, null)
                    .addToBackStack("content")
                    .commit();
            mCurrentFragment = fragment;
        } else {
            Toast.makeText(this, R.string.please_admin_login, Toast.LENGTH_SHORT).show();
        }

    }

    private void adminLogin() {
        Log.d(Utils.TAG, "adminLogin = " + mCurrentFragment);
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof AdminFragment) {
            return;
        }
        showAdminFragment();
    }

    /**
     * change admin login status.
     *
     * @param status login status.
     */
    private void setLoginStatus(LoginStatus status) {
        this.mCurrentStatus = status;
    }

    /**
     * admin login status is success.
     */
    public void setLoginSuccessStatus() {
        setLoginStatus(LoginStatus.SUCCESS);
    }

    /**
     * admin login status is failure.
     */
    public void setLoginFailureStatus() {
        setLoginStatus(LoginStatus.FAILURE);
    }

    /**
     * Whether to login.
     */
    public boolean isLogin() {
        return mCurrentStatus == LoginStatus.SUCCESS;
    }

    /**
     * Check to login.
     *
     * @param fragment VisitorFragment and EmployeeFragment need to login to be able to show.
     * @return true is can show fragment.
     */
    public boolean checkLoginSuccess(Fragment fragment) {
        if (!isLogin()) {
            return !(fragment instanceof EmployeeFragment);
        }
        return true;
    }

    /**
     * check storage permission,WRITE_EXTERNAL_STORAGE
     */
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i(TAG,
                        "Displaying storage permission");
                Snackbar.make(mBinding.root, R.string.permission_storage,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, view -> ActivityCompat
                                .requestPermissions(
                                        MainActivity.this,
                                        PERMISSIONS_STORAGE,
                                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                                ))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_STORAGE,
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}

