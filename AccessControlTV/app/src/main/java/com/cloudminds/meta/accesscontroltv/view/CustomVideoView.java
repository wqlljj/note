package com.cloudminds.meta.accesscontroltv.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import java.lang.reflect.Field;


/**
 * Created by WQ on 2018/4/21.
 */

public class CustomVideoView extends VideoView {
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");
        Class<?> superclass = this.getClass().getSuperclass();
        int mVideoWidth=0;
        int mVideoHeight=0;
        try {
            Field mVideoWidthField = superclass.getDeclaredField("mVideoWidth");
            mVideoWidthField.setAccessible(true);
            mVideoWidth = mVideoWidthField.getInt(this);
            Field mVideoHeightField = superclass.getDeclaredField("mVideoHeight");
            mVideoHeightField.setAccessible(true);
            mVideoHeight = mVideoHeightField.getInt(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        Log.e("TEST", "onMeasure: "+mVideoWidth+"  "+mVideoHeight+"  " +width+"  "+height);
        setMeasuredDimension(width, height);
    }
}
