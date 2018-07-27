package com.example.wangqi.developutils.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.databinding.ActivityFitScreenBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.List;

public class FitScreenActivity extends AppCompatActivity {

    private ActivityFitScreenBinding viewDataBinding;
    private static final int REQUESTCODE_DIMENS_X = 1000;
    private static final int REQUESTCODE_DIMENS_Y = 1001;
    private String TAG ="FitScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_screen);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_fit_screen);
        viewDataBinding.setSelectXFile(selectXFile);
        viewDataBinding.setSelectYFile(selectYFile);
        viewDataBinding.setStartRunner(startRunner);
    }
    OnClickListener startRunner = new OnClickListener() {
        @Override
        public void onClick() {
            Log.e(TAG, "onClick: "+viewDataBinding.getXFilePath()+"\n保存路径："+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() );
//            ScreenUtil.gen(viewDataBinding.getXFilePath(),viewDataBinding.getYFilePath(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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
