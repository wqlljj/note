package com.cloudminds.meta.accesscontroltv.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.cloudminds.meta.accesscontroltv.R;

/**
 * Created by WQ on 2018/4/20.
 */

public class MediaController implements View.OnClickListener {
    private final SurfaceView mRoot;
    private View contentView;
    private PopupWindow window;
    private long showTime=3000;
    private String TAG = "MediaController";

    public MediaController(SurfaceView mRoot) {
        this.mRoot = mRoot;
        initView();
    }
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    window.dismiss();
                    break;
            }
        }
    };

    private void initView() {
        // 用于PopupWindow的View
         contentView= LayoutInflater.from(mRoot.getContext()).inflate(R.layout.mymediacontroller, null, false);
        contentView.setOnClickListener(this);
        // 创建PopupWindow对象，其中：
        // 第一个参数是用于PopupWindow中的View，第二个参数是PopupWindow的宽度，
        // 第三个参数是PopupWindow的高度，第四个参数指定PopupWindow能否获得焦点
         window=new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        // 设置PopupWindow的背景
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置PopupWindow是否能响应外部点击事件
        window.setOutsideTouchable(true);
        // 设置PopupWindow是否能响应点击事件
        window.setTouchable(true);

    }
    public void show(){
        show(showTime);
    }
    public void show(long showTime){
        window.setHeight(mRoot.getHeight());
        window.setWidth(mRoot.getWidth());
        // 显示PopupWindow，其中：
        // 第一个参数是PopupWindow的锚点，第二和第三个参数分别是PopupWindow相对锚点的x、y偏移
//        window.showAsDropDown(mRoot, 0, 0);
        // 或者也可以调用此方法显示PopupWindow，其中：
        // 第一个参数是PopupWindow的父View，第二个参数是PopupWindow相对父View的位置，
        // 第三和第四个参数分别是PopupWindow相对父View的x、y偏移
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        mRoot.getLocationOnScreen(anchorLoc);
        Log.e(TAG, "show: "+anchorLoc[0]+"  "+anchorLoc[1] );

        window.showAtLocation(mRoot, Gravity.TOP|Gravity.LEFT, anchorLoc[0], anchorLoc[1]);
        mHandler.sendEmptyMessageDelayed(100,showTime);
    }

    public long getShowTime() {
        return showTime;
    }

    public void setShowTime(long showTime) {
        this.showTime = showTime;
    }

    public boolean isShowing(){
        return window.isShowing();
    }
    private void dismiss(){
        if(window.isShowing()) {
            window.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mediaController:
                if(mHandler.hasMessages(100)){
                    mHandler.removeMessages(100);
                }
                window.dismiss();
                break;
        }
    }
}
