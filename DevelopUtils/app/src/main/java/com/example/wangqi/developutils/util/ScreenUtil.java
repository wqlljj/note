package com.example.wangqi.developutils.util;

import android.content.Context;
import android.databinding.InverseMethod;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wangqi on 2018/6/5.
 */

public class ScreenUtil {
    public static float scale = -1;
    public static float fontScale = -1;
    private static String TAG="ScreenUtil";
    @InverseMethod("floatToString")
    public static float stringToFloat(String s){
        try {
            return Float.valueOf(s);
        }catch (NumberFormatException e){
            return -1;
        }
    }
    public static String floatToString(float f){
        return String.valueOf(f);
    }

    /**
     * 根据手机分辨率从DP转成PX
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        Log.e(TAG, "dip2px: " );
        if (scale == -1) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        Log.e(TAG, "dip2px: " +(int) (dpValue * scale + 0.5f)+"   "+scale);
        Toast.makeText(context, scale+" scale   "+dpValue+" dp = "+(int) (dpValue * scale + 0.5f)+" px", Toast.LENGTH_SHORT).show();
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        Log.e(TAG, "sp2px: " );
        if (fontScale == -1) {
            fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        }
        Log.e(TAG, "sp2px: "+(int) (spValue * fontScale + 0.5f)+"  "+fontScale );
        Toast.makeText(context, fontScale+" fontScale    "+spValue+" sp  =  "+(int) (spValue * fontScale + 0.5f)+" px", Toast.LENGTH_SHORT).show();
        return (int) (spValue * fontScale + 0.5f);
    }

        /**
         * 根据手机的分辨率PX(像素)转成DP
         *
         * @param context
         * @param pxValue
         * @return
         */

    public static int px2dip(Context context, float pxValue) {
        Log.e(TAG, "px2dip: " );
        if (scale == -1) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        Log.e(TAG, "px2dip: "+ (int) (pxValue / scale + 0.5f)+"  "+scale);
        Toast.makeText(context, scale+" scale  "+pxValue+" px  =  "+(int) (pxValue / scale + 0.5f)+"  dp", Toast.LENGTH_SHORT).show();
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */

    public static int px2sp(Context context, float pxValue) {
        Log.e(TAG, "px2sp: " );
        if (fontScale == -1) {
            fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        }
        Log.e(TAG, "px2sp: "+(int) (pxValue / fontScale + 0.5f)+" sp"+ "  "+ fontScale);
        Toast.makeText(context, fontScale +" fontScale  "+pxValue+"  px  =  "  +(int) (pxValue / fontScale + 0.5f)+" sp" , Toast.LENGTH_SHORT).show();
        return (int) (pxValue / fontScale + 0.5f);
    }
}
