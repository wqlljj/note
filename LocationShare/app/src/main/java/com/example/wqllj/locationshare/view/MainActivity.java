package com.example.wqllj.locationshare.view;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.databinding.ActivityMainBinding;
import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.bean.RouteLineBean;
import com.example.wqllj.locationshare.db.operator.EventOperator;
import com.example.wqllj.locationshare.db.operator.PersonOperator;
import com.example.wqllj.locationshare.db.operator.RouteLineOperator;
import com.example.wqllj.locationshare.model.baidumap.navi_bike_wake.BNaviMainActivity;
import com.example.wqllj.locationshare.util.DateUtil;
import com.example.wqllj.locationshare.util.SharePreferenceUtils;
import com.example.wqllj.locationshare.viewModel.MainViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private ActivityMainBinding dataBinding;
    private TabLayout.OnTabSelectedListener tabListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            dataBinding.contentView.setCurrentItem(tab.getPosition(), true);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };
    private MainContentAdatoer mainContentAdatoer;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "ViewPager点击", Toast.LENGTH_SHORT).show();
            }
        });

        init();
    }

    private void init() {
        SharePreferenceUtils.setContext(this.getApplicationContext());
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.init(this);
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(MessageFragment.newInstance("", ""));
        fragments.add(FootPrintFragment.newInstance("", ""));
        fragments.add(InteractionFragment.newInstance("", ""));
        mainContentAdatoer = new MainContentAdatoer(getSupportFragmentManager(), fragments);
        dataBinding.contentView.setAdapter(mainContentAdatoer);
        dataBinding.navigateBar.setupWithViewPager(dataBinding.contentView);
        dataBinding.navigateBar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }



            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //添加人员信息 测试
        PersonOperator personOperator = DbManager.getInstance().getOperator(PersonOperator.class);
        if(personOperator.queryAll().size()<=0) {
                PersonBean personBean = new PersonBean();
                personBean.setName("aa" + (i));
                personBean.setSex(1);
                personOperator.insertOrReplace(personBean);
                Log.e(TAG, "onTabSelected: add " + personBean);
        }

    }
    int i=0;

    @Override
    protected void onResume() {
        super.onResume();
//        startActivity(new Intent(this,BNaviMainActivity.class));
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needPermission() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRational(final PermissionRequest request) {
        request.proceed();
    }

    @Override
    protected void onDestroy() {
        if (mainViewModel != null) {
            mainViewModel.destory();
        }
        super.onDestroy();
    }
}
