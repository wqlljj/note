package com.example.wangqi.developutils.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.application.Constant;
import com.example.wangqi.developutils.bean.EventBean;
import com.example.wangqi.developutils.bean.ScreenBean;
import com.example.wangqi.developutils.databinding.ActivityFitScreenBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.SharePreferenceUtils;
import com.example.wangqi.developutils.util.SystemUtil;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.google.gson.Gson;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.vincent.filepicker.DividerGridItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.wangqi.developutils.application.Constant.baseDimensPath;
import static com.example.wangqi.developutils.bean.EventBean.CODE.DIMENS_LOG;

public class FitScreenActivity extends AppCompatActivity implements View.OnLongClickListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private ActivityFitScreenBinding viewDataBinding;
    private static final int REQUESTCODE_DIMENS_X = 1000;
    private static final int REQUESTCODE_DIMENS_Y = 1001;
    private String TAG = "FitScreenActivity";
    private ScreenBean baseScreenBean;
    private ScreenInfoAdapter adapter;
    private PopupWindow popupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_screen);
        EventBus.getDefault().register(this);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_fit_screen);
        viewDataBinding.setGenerateBaseDimens(generateBaseDimens);
        viewDataBinding.setSelectXFile(selectXFile);
        viewDataBinding.setSelectYFile(selectYFile);
        viewDataBinding.setStartRunner(startRunner);
        viewDataBinding.setLogInvisible(logInvisible);
        baseScreenBean = new ScreenBean(SystemUtil.Width(), SystemUtil.Height(), SystemUtil.density(), SystemUtil.scaledDensity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        viewDataBinding.screenInfo.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        adapter = new ScreenInfoAdapter();
        adapter.setLongClickListener(this);
        viewDataBinding.screenInfo.setAdapter(adapter);
        viewDataBinding.screenInfo.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                //设定底部边距为1px
                 outRect.set(0, 0, 0, 5);
            }
            });
            //设置增加或删除条目的动画
        viewDataBinding.screenInfo.setItemAnimator(new DefaultItemAnimator());
            initData();
        }

    private void initData() {
        String s = SharePreferenceUtils.getPrefString(Constant.KEY_SCREENINFO, "");
        ToastOrLogUtil.e(TAG, "screenBean_string: " + s);
        ArrayList<ScreenBean> screenBeens = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            screenBeens.add(baseScreenBean);
            screenBeens.add(new ScreenBean(0, 0, 0, 0));
        } else {
            String[] split = s.substring(1, s.length() - 1).split(" , ");
            Gson gson = new Gson();
            for (String s1 : split) {
                ToastOrLogUtil.e(TAG, "screenBean_string: " + s1);
                screenBeens.add(gson.fromJson(s1, ScreenBean.class));
            }
            baseScreenBean = screenBeens.get(0);
        }
        adapter.addDatas(screenBeens);
    }

    OnClickListener logInvisible = new OnClickListener() {
        @Override
        public void onClick() {
            viewDataBinding.tvLog.setText("");
            viewDataBinding.logInfo.setVisibility(View.INVISIBLE);
        }
    };
    OnClickListener startRunner = new OnClickListener() {
        @Override
        public void onClick() {
            viewDataBinding.logInfo.setVisibility(View.VISIBLE);
            Log.e(TAG, "onClick: " + viewDataBinding.getXFilePath() + "\n保存路径：" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/fitScreen");
            ArrayList<ScreenBean> data = new ArrayList<>();
            for (int i = 0; i < adapter.getData().size(); i++) {
                if(i!=1)
                data.add(adapter.getData().get(i));
            }
            Log.e(TAG, "onClick: "+data );
            ScreenUtil.gen(viewDataBinding.getXFilePath(), viewDataBinding.getYFilePath(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/fitScreen"
                    , baseScreenBean, data);
        }
    };
    OnClickListener generateBaseDimens = new OnClickListener() {
        @Override
        public void onClick() {
            viewDataBinding.logInfo.setVisibility(View.VISIBLE);
            ScreenUtil.createBaseDimens(baseDimensPath, baseScreenBean);
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
    StringBuilder log=new StringBuilder();
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(EventBean event) {
        switch (event.getCode()) {
            case DIMENS_LOG:
                log.append(event.getMsg());
                viewDataBinding.tvLog.append("\n"+event.getMsg());
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_DIMENS_X:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG, list.toString());
                        viewDataBinding.setXFilePath(list.get(0));
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG, path);
                    }
                    break;
                case REQUESTCODE_DIMENS_Y:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG, list.toString());
                        viewDataBinding.setYFilePath(list.get(0));
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

    private void selectFile(int requestCode) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(requestCode)
                .withStartPath(baseDimensPath)
                .withTitle("xml文件选择")
                .withChooseMode(true)
                .withMaxNum(1)
                .withFileFilter(new String[]{".xml"})
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.setLongClickListener(null);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onLongClick(View v) {
        int childLayoutPosition = viewDataBinding.screenInfo.getChildLayoutPosition(v);
        Toast.makeText(this, "长按："+"   "+ childLayoutPosition, Toast.LENGTH_SHORT).show();
        if(childLayoutPosition==0){
//            startActivity(new Intent(this,SpecialSetActivity.class));
            if(popupWindow==null) {
                initPopup();
            }
                popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        }
        return false;
    }

    private void initPopup() {
        View pop = View.inflate(this, R.layout.popup_screeninfo, null);
        ListView screenInfo = (ListView) pop.findViewById(R.id.screenInfo);
        pop.findViewById(R.id.back).setOnClickListener(this);
        ArrayList<String> list=new ArrayList<>();
        ArrayList<ScreenBean> data = adapter.getData();
        for (int i = 2; i < data.size(); i++) {
            list.add(data.get(i).getWidth_px()+"*"+data.get(i).getHeight_px());
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        screenInfo.setAdapter(adapter);
        screenInfo.setOnItemClickListener(this);
        popupWindow = new PopupWindow(pop, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScreenBean screenBean = adapter.getData().get(position + 2);
        Toast.makeText(this, "点击  "+position+"  "+id +"  "+screenBean.getWidth_px()+"*"+screenBean.getHeight_px(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SpecialSetActivity.class);
        intent.putExtra("ScreenBean",screenBean);
        startActivity(intent);
        popupWindow.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                popupWindow.dismiss();
                break;
        }
    }
}
