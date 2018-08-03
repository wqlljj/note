package com.example.wangqi.developutils.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.bean.ScreenBean;
import com.example.wangqi.developutils.databinding.ActivityFitScreenBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.ArrayList;
import java.util.List;

public class FitScreenActivity extends AppCompatActivity {

    private ActivityFitScreenBinding viewDataBinding;
    private static final int REQUESTCODE_DIMENS_X = 1000;
    private static final int REQUESTCODE_DIMENS_Y = 1001;
    private String TAG ="FitScreenActivity";
    private ScreenBean baseScreenBean;
    private ArrayList<ScreenBean> screenBeens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_screen);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_fit_screen);
        viewDataBinding.setSelectXFile(selectXFile);
        viewDataBinding.setSelectYFile(selectYFile);
        viewDataBinding.setStartRunner(startRunner);
        baseScreenBean = new ScreenBean(1400,2560,3.5f,3.5f);
        screenBeens=new ArrayList<>();
        screenBeens.add(new ScreenBean(1024,768,1f,1f));
        screenBeens.add(new ScreenBean(1920,1200,1.5f,1.5f));
        screenBeens.add(new ScreenBean(1920,1080,1f,1f));
        screenBeens.add(new ScreenBean(2560,1400,2f,2f));
    }
    OnClickListener startRunner = new OnClickListener() {
        @Override
        public void onClick() {
            Log.e(TAG, "onClick: "+viewDataBinding.getXFilePath()+"\n保存路径："+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/fitScreen" );
            ScreenUtil.gen(viewDataBinding.getXFilePath(),viewDataBinding.getYFilePath(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/fitScreen"
                    ,baseScreenBean,screenBeens);
        }
    };

    OnClickListener selectXFile = new OnClickListener() {
        @Override
        public void onClick() {
            selectFile(REQUESTCODE_DIMENS_X);
        }
    };
    OnClickListener selectYFile = new OnClickListener() {
        @Override
        public void onClick() {
            selectFile(REQUESTCODE_DIMENS_Y);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_DIMENS_X:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG,list.toString());
                        viewDataBinding.setXFilePath(list.get(0));
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG,path);
                    }
                    break;
                case REQUESTCODE_DIMENS_Y:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG,list.toString());
                        viewDataBinding.setYFilePath(list.get(0));
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG,path);
                    }
                    break;
            }
        }else{
            ToastOrLogUtil.show(this,"获取路径失败，请重新选择");
        }

    }

    private void selectFile(int requestCode) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(requestCode)
                .withStartPath("/storage/emulated/0/tencent/QQfile_recv")
                .withTitle("xml文件选择")
                .withChooseMode(true)
                .withMaxNum(1)
                .withFileFilter(new String[]{".xml"})
                .start();
    }
}
