package com.example.wangqi.mvvm.view;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.wangqi.mvvm.BR;
import com.example.wangqi.mvvm.databinding.ActivityMainBinding;
import com.example.wangqi.mvvm.db.bean.UserBean;
import com.example.wangqi.mvvm.db.bean.WeatherBean;
import com.example.wangqi.mvvm.db.dao.UserDao;
import com.example.wangqi.mvvm.model.api.HCApiClient;
import com.example.wangqi.mvvm.model.bean.FaceResponse;
import com.example.wangqi.mvvm.viewmodel.MainViewModel;
import com.example.wangqi.mvvm.R;
import com.example.wangqi.mvvm.db.DataDBManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG="MainActivity";
    private TextView name;
    private MainViewModel mainViewModel;
    private ViewDataBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HCApiClient.init(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                databaseOperation();
//            }
//        }).start();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.setVariable(BR.name,"Teacher");//给布局文件name="work"传入数据，类型为String字符串
//        name = findViewById(R.id.name);
        init();
        mHandler.sendEmptyMessageDelayed(100,4000);
    }
    int i=0;
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mainViewModel!=null){
                mainViewModel.getCurrentName().setValue("aaa "+i);
                ArrayList<String> strings = new ArrayList<>();
                strings.add("bbb "+i);
                strings.add("ccc "+i);
                strings.add("ddd "+i);
                strings.add("eee "+i);
                strings.add("fff "+i++);
                mainViewModel.getNameList().setValue(strings);
                mainViewModel.updateWeather();
                mHandler.sendEmptyMessageDelayed(100,4000);
            }else{
                mHandler.sendEmptyMessageDelayed(100,4000);
            }
        }
    };

    private void init() {
         mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getCurrentName().observe((LifecycleOwner)this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                name.setText(s);
                binding.setVariable(BR.name,s);
            }
        });
        mainViewModel.getNameList().observe((LifecycleOwner)this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                for (String string : strings) {
                    Log.e(TAG, "onChanged: "+string );
                }
            }
        });
        mainViewModel.getweather().observe((LifecycleOwner)this, new Observer<FaceResponse>() {
            @Override
            public void onChanged(@Nullable FaceResponse weatherBean) {
                Log.e(TAG, "onChanged: "+weatherBean );
            }
        });
    }

    private void databaseOperation() {
        DataDBManager mUserDatabase = DataDBManager.getInstance(this);
        UserDao mUserDao = mUserDatabase.getUserDao();

        //写数据库
        Log.d(TAG, "开始写入数据...");
        writeDatabase(mUserDao, "张三", 18);
        writeDatabase(mUserDao, "李四", 19);
        Log.d(TAG, "写入数据库完毕.");

        //读数据库
        Log.d(TAG, "第1次读数据库");
        readDatabase(mUserDao);

        //更新数据库
        updateUser(mUserDao);

        //读数据库
        Log.d(TAG, "第2次读数据库");
        readDatabase(mUserDao);

        //删除数据，根据主键id
        deleteUser(mUserDao, 1);

        //读数据库
        Log.d(TAG, "第3次读数据库");
        readDatabase(mUserDao);

        Log.d(TAG, "========================");
        Log.d(TAG, "本轮数据库操作事务全部结束");
        Log.d(TAG, "========================");
    }

    private void readDatabase(UserDao dao) {
        Log.d(TAG, "读数据库...");
        List<UserBean> users = dao.getUsers();
        for (UserBean u : users) {
            Log.d(TAG, u.id + "," + u.name + "," + u.age + "," + u.job);
        }
        Log.d(TAG, "读数据库完毕.");
    }

    private void writeDatabase(UserDao dao, String name, int age) {
        UserBean user = new UserBean();
        user.name = name;
        user.age = age;
        user.job = "aa"+System.currentTimeMillis();
        dao.insert(user);
    }

    private void updateUser(UserDao dao) {
        Log.d(TAG, "更新数据库...");
        UserBean u = new UserBean();
        u.id = 2;
        u.name = "赵五";
        u.age = 20;
        u.job = "cccc"+System.currentTimeMillis();
        dao.update(u);
        Log.d(TAG, "更新数据库完毕.");
    }

    private void deleteUser(UserDao dao, int id) {
        Log.d(TAG, "删除数据库...");
        UserBean u = new UserBean();
        u.id = id;
        dao.delete(u);
        Log.d(TAG, "删除数据库完毕.");
    }
}
