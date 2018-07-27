package com.example.wangqi.developutils.util;

import android.content.Context;
import android.databinding.InverseMethod;
import android.util.Log;
import android.widget.Toast;

import com.example.wangqi.developutils.bean.ScreenBean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by wangqi on 2018/6/5.
 */

public class ScreenUtil {
    public static float scale = -1;
    public static float fontScale = -1;
    private static String TAG="ScreenUtil";
    private static String parentFileName ="values-%dx%d";
    public static void gen(String dimen_x,String dimen_y,String savePath,ScreenBean baseScreen,ScreenBean[] screenBeans) {

        File file = new File(dimen_x);
        BufferedReader reader = null;
        StringBuilder[] dimen_xs=new StringBuilder[screenBeans.length];
        for (int i = 0; i < screenBeans.length; i++) {
            dimen_xs[i]= new StringBuilder();
        }
//        StringBuilder sw480 = new StringBuilder();
//        StringBuilder sw600 = new StringBuilder();
//        StringBuilder sw720 = new StringBuilder();
//        StringBuilder sw800 = new StringBuilder();
//        StringBuilder w820 = new StringBuilder();


        try {
            System.out.println("生成不同分辨率：");
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束

            while ((tempString = reader.readLine()) != null) {

                if (tempString.contains("</dimen>")) {
                    //tempString = tempString.replaceAll(" ", "");
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));


//                    sw480.append(start).append((int) Math.round(num * 0.6)).append(end).append("\n");
//                    sw600.append(start).append((int) Math.round(num * 0.75)).append(end).append("\n");
//                    sw720.append(start).append((int) Math.round(num * 0.9)).append(end).append("\n");
//                    sw800.append(tempString).append("\n");
//                    w820.append(tempString).append("\n");

                } else {
//                    sw480.append(tempString).append("\n");
//                    sw600.append(tempString).append("\n");
//                    sw720.append(tempString).append("\n");
//                    sw800.append(tempString).append("\n");
//                    w820.append(tempString).append("\n");
                }
                line++;
            }
            reader.close();
//            System.out.println("<!--  sw480 -->");
//            System.out.println(sw480);
//            System.out.println("<!--  sw600 -->");
//            System.out.println(sw600);
//
//            System.out.println("<!--  sw720 -->");
//            System.out.println(sw720);
//            System.out.println("<!--  sw800 -->");
//            System.out.println(sw800);

            String sw480file = savePath+"/values-sw480dp-land/dimens.xml";
            String sw600file = savePath+"/values-sw600dp-land/dimens.xml";
            String sw720file = savePath+"/values-sw720dp-land/dimens.xml";
            String sw800file = savePath+"/values-sw800dp-land/dimens.xml";
            String w820file = savePath+"/values-w820dp/dimens.xml";
//            writeFile(sw480file, sw480.toString());
//            writeFile(sw600file, sw600.toString());
//            writeFile(sw720file, sw720.toString());
//            writeFile(sw800file, sw800.toString());
//            writeFile(w820file, w820.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void writeFile(String path, String text) {
        PrintWriter out = null;
        try {
            File file = new File(path);
            if(!file.exists()){
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            out = new PrintWriter(new BufferedWriter(new FileWriter(path)));
            out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }
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
    public static int px2dip(float pxValue,float density) {
        Log.e(TAG, "px2dip: "+ (int) (pxValue / density + 0.5f)+"  "+density);
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(float pxValue,float scaledDensity) {
        Log.e(TAG, "px2sp: "+(int) (pxValue / scaledDensity + 0.5f)+" sp"+ "  "+ scaledDensity);
        return (int) (pxValue / scaledDensity + 0.5f);
    }
}
