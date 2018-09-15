package com.example.wqllj.locationshare.view.customview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by cloud on 2018/8/31.
 */

public class CViewPager extends ViewPager {
    private String TAG="CViewPager";

    public CViewPager(@NonNull Context context) {
        super(context);
    }

    public CViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG, "onInterceptTouchEvent: "+ev.getAction() );
//        return true;

        switch (ev.getAction()) {

        }
        boolean b = super.onInterceptTouchEvent(ev);
        Log.e(TAG, "onInterceptTouchEvent: "+ev.getAction() +"  "+b);
        return b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "dispatchTouchEvent: "+ev.getAction() );
//        return true;
        switch (ev.getAction()) {

        }
        boolean b = super.dispatchTouchEvent(ev);
        Log.e(TAG, "dispatchTouchEvent: "+ev.getAction() +"  " +b);
        return b;
    }

    private float xDistance, yDistance, xLast, yLast;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.e(TAG, "onTouchEvent: "+ev.getAction() );
            super.onTouchEvent(ev);
//        switch (ev.getAction()) {
//
//            case MotionEvent.ACTION_DOWN:
//                return true;
//                //获取第一次按下点的坐标
////                xDistance = yDistance = 0f;
////                xLast = ev.getX();
////                yLast = ev.getY();
////                break;
//
//            case MotionEvent.ACTION_MOVE:
//                //获取移动结束点的坐标　
//                final float curX = ev.getX();
//                final float curY = ev.getY();
//                //差值
//                xDistance += Math.abs(curX - xLast);
//                yDistance += Math.abs(curY - yLast);
//                xLast = curX;
//                yLast = curY;
//                if(xDistance <= yDistance) {
//                    return true;  // 消耗
//                }
//                else {
//                    return true;
//                }// 往下传递
//
//        }
        return true;
    }

//    dispatchTouchEvent ，这个方法主要是用来分发事件的
//    onInterceptTouchEvent，这个方法主要是用来拦截事件的（需要注意的是ViewGroup才有这个方法，View没有onInterceptTouchEvent这个方法
//    onTouchEvent 这个方法主要是用来处理事件的
//    requestDisallowInterceptTouchEvent(true)，这个方法能够影响父View是否拦截事件，true 表示父 View 不拦截事件，false 表示父 View 拦截事件
}
