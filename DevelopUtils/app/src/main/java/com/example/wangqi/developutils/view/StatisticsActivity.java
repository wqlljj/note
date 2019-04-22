package com.example.wangqi.developutils.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.bean.LogBean;
import com.example.wangqi.developutils.databinding.ActivityStatisticsBinding;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.wangqi.developutils.application.Constant.baseDimensPath;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding dataBinding;
    private String TAG = "StatisticsActivity";
    HashMap<String, ArrayList<LogBean>> data = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_statistics);
        dataBinding.setSelectXFile(new OnClickListener() {
            @Override
            public void onClick() {
                selectFile(100);
            }
        });
        dataBinding.setStatistic(new OnClickListener() {
            @Override
            public void onClick() {
                statistic();
            }
        });
    }

    private void statistic() {
        CharSequence text = dataBinding.filePath.getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        Log.e(TAG, "statistic: filePath = " + text.toString());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(text.toString()));
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                String[] split = temp.split("( +)|:");
                if (split != null && split.length > 0) {
                    HashMap<String, String> dataMap = new HashMap<>();
                    for (int i = 0; i < split.length; i++) {
                        switch (i) {
                            case 0:
                                dataMap.put("day", split[i]);
                                break;
                            case 1:
                                dataMap.put("time", split[i]);
                                break;
                            case 2:
                                dataMap.put("tag", split[i]);
                                break;
                            case 3:
                                if (split[i].contains("-")) {
                                    String[] split1 = split[i].split("-");
                                    dataMap.put("id", split1[0]);
                                    dataMap.put("toId", split1[1]);
                                } else {
                                    dataMap.put("id", split[i]);
                                }
                                break;
                            case 4:
                                dataMap.put("sign", split[i]);
                                break;
                            case 5:
                                dataMap.put("longTime", split[i]);
                                break;
                            case 6:
                                dataMap.put(split[i].matches("\\d+.*") ? "pictureSize" : "flag", split[i]);
                                break;
                        }
                    }
                    LogBean logBean = new LogBean(dataMap);
//                    Log.e(TAG, "statistic: " + logBean);
//                    Log.e(TAG, "statistic: " + temp);
                    ArrayList<LogBean> data = this.data.get(logBean.getItemData("id"));
                    if (data == null) {
                        data = new ArrayList<>();
                        this.data.put(logBean.getItemData("id"), data);
                    }
                    data.add(logBean);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data.size() > 0) {
//            for (String s : data.keySet()) {
//                Log.e(TAG, "statistic: " + s + "  " + data.get(s).size());
//            }
            calcTime("CameraManager.rotate", "onResponse");
        }
    }

    private void calcTime(String startTag, String endTag) {
        for (String s : data.keySet()) {
//            Log.e(TAG, "calcTime: " + s);
            ArrayList<LogBean> logBeen = data.get(s);
            LogBean start = null, end = null;
            out:
            for (LogBean logBean : logBeen) {
                String sign = logBean.getItemData("sign");
                if (!TextUtils.isEmpty(sign)) {
//                    Log.e(TAG, "calcTime:" + sign + ":" + startTag + ":" + endTag + ":" + sign.contains(startTag) + ":" + sign.contains(endTag));
                    if (sign.equals(startTag)) {
                        start = logBean;
                        Log.e(TAG, "calcTime: start = "+start );
                    } else if (sign.equals(endTag)&&end==null) {
                        end = logBean;
                        Log.e(TAG, "calcTime: end = "+end );
                    }
                    Log.e(TAG, "calcTime: 跳转 "+logBean.getItemData("id")+"  "+logBean.getItemData("toId") +(start != null)+"  "+(end != null) );
                    if (logBean.getData().containsKey("toId")) {
                        String toId = logBean.getItemData("toId");
//                        Log.e(TAG, "calcTime: " + toId);
                        ArrayList<LogBean> logBeen1 = data.get(toId);
                        if(logBeen1!=null) {
                            for (LogBean bean : logBeen1) {
                                String sign1 = bean.getItemData("sign");
//                            Log.e(TAG, "calcTime: " + sign1 + "  " + startTag + "  " + endTag);
                                if (!TextUtils.isEmpty(sign1)) {
                                    if (sign1.equals(startTag)&&start==null) {
                                        start = bean;
                                        Log.e(TAG, "calcTime: start = "+start +"  size = "+logBeen1.size());
                                    } else if (sign1.equals(endTag)) {
                                        end = bean;
                                        Log.e(TAG, "calcTime: end = "+end );
                                    }
                                    if (start != null && end != null) {
                                        Log.e(TAG, "calcTime:in " + start.getItemData("id") + "-" + end.getItemData("id") + "  " + (Long.valueOf(end.getItemData("longTime")) - Long.valueOf(start.getItemData("longTime"))));
                                        break out;
                                    }
                                }

                            }
                        }
                    }
//                    Log.e(TAG, "calcTime: " + (start != null) + "  " + (end != null));
                    if (start != null && end != null) {
                        Log.e(TAG, "calcTime:out " + start.getItemData("id") + "   " + (Long.valueOf(end.getItemData("longTime")) - Long.valueOf(start.getItemData("longTime"))));
                        break;
                    }
                }
            }
        }
    }

    private void selectFile(int requestCode) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(requestCode)
                .withStartPath(baseDimensPath)
                .withTitle("文件选择")
                .withChooseMode(true)
                .withMaxNum(1)
//                .withFileFilter(new String[]{"txt"})
                .start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 100:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG, list.toString());
                        dataBinding.setFilePath(list.get(0));
                        dataBinding.statistics.setEnabled(true);
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG, path);
                    }
                    break;
            }
        } else {
            ToastOrLogUtil.show(this, "获取路径失败，请重新选择");
        }

    }
}
