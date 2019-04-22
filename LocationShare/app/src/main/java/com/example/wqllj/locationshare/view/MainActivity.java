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
import com.example.wqllj.locationshare.db.bean.CoordinatePointBean;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.bean.RouteLineBean;
import com.example.wqllj.locationshare.db.operator.EventOperator;
import com.example.wqllj.locationshare.db.operator.PersonOperator;
import com.example.wqllj.locationshare.db.operator.RouteLineOperator;
import com.example.wqllj.locationshare.util.RequestPermissionUtils;
import com.example.wqllj.locationshare.util.SharePreferenceUtils;
import com.example.wqllj.locationshare.view.adapter.MainContentAdatoer;
import com.example.wqllj.locationshare.viewModel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.example.wqllj.locationshare.broadcast.KeepAliveReceiver.ACTION_KEEPALIVE;

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
        RequestPermissionUtils.getPermissions(this);
    }

    private void init() {
        SharePreferenceUtils.setContext(this.getApplicationContext());
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.init(this);
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(MessageFragment.newInstance("", ""));
        fragments.add(DynamicFragment.newInstance("", ""));
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
        //添加线路信息 测试
        RouteLineOperator routeLineOperator = DbManager.getInstance().getOperator(RouteLineOperator.class);
        if(routeLineOperator.queryByPersonId(1l).size()<=0){
            PersonBean personBean = personOperator.queryByKey(1l);
            List<CoordinatePointBean> list=new ArrayList<>();
            RouteLineBean routeLineBean = new RouteLineBean(list, System.currentTimeMillis(), personBean.getId(), personBean);
            routeLineOperator.insertOrReplace(routeLineBean);
            list.add(new CoordinatePointBean(new LatLng(39.99955126379913,116.48623088414925),routeLineBean.getId(),System.currentTimeMillis()));
            list.add(new CoordinatePointBean(new LatLng(40.00036651906278,116.48677885051033),routeLineBean.getId(),System.currentTimeMillis()));
            list.add(new CoordinatePointBean(new LatLng(40.00145120788882,116.48621291803904),routeLineBean.getId(),System.currentTimeMillis()));
            list.add(new CoordinatePointBean(new LatLng(40.00203154382269,116.48562901945756),routeLineBean.getId(),System.currentTimeMillis()));
            list.add(new CoordinatePointBean(new LatLng(40.00218353574566,116.48625783331455),routeLineBean.getId(),System.currentTimeMillis()));
            routeLineOperator.insertOrReplace(routeLineBean);

            EventOperator eventOperator = DbManager.getInstance().getOperator(EventOperator.class);
            for (CoordinatePointBean pointBean : list) {
                eventOperator.insertOrReplace(new EventBean(personBean.getName(),1,"购物"+pointBean.getId(),personBean.getId(),pointBean.getId(),pointBean));
            }
        }


    }
    int i=0;

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(ACTION_KEEPALIVE));
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
