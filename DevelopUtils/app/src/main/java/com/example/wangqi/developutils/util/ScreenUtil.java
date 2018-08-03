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
import java.util.ArrayList;

/**
 * Created by wangqi on 2018/6/5.
 */

public class ScreenUtil {
    public static float scale = -1;
    public static float fontScale = -1;
    private static String TAG="ScreenUtil";
    private static String parentFileName ="values-%dx%d";
    public static void gen(String dimen_x, String dimen_y, String savePath, ScreenBean baseScreen, ArrayList<ScreenBean> screenBeans) {
        Log.e(TAG, "gen: "+dimen_x+"\n"+dimen_y+"\n"+savePath+"\n"+baseScreen );
        File file = new File(dimen_x);
        BufferedReader reader = null;
        StringBuilder[] dimen_xs=new StringBuilder[screenBeans.size()];
        for (int i = 0; i < screenBeans.size(); i++) {
            dimen_xs[i]= new StringBuilder();
            dimen_xs[i].append("<!--").append(screenBeans.get(i).toString()).append("-->");
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
                    String type = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    Log.e(TAG, "gen: "+tempString );
                    switch (type){
                        case "px":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_px = screenBeans.get(i).getWidth_px();
                                Log.e(TAG, "gen: "+num+"*"+width_px+"/"+baseScreen.getWidth_px() );
                                Log.e(TAG, width_px+"  gen: "+start+((int) Math.round(1.0*num*width_px/baseScreen.getWidth_px()))+end );
                                dimen_xs[i].append(start).append((int) Math.round(1.0*num*width_px/baseScreen.getWidth_px())).append(end).append("\n");
                            }
                            break;
                        case "dp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_dp = screenBeans.get(i).getWidth_dp();
                                Log.e(TAG, "gen: "+num+"*"+width_dp+"/"+baseScreen.getWidth_dp() );
                                Log.e(TAG, width_dp+"  gen: "+start+((int) Math.round(1.0*num*width_dp/baseScreen.getWidth_dp()))+end );
                                dimen_xs[i].append(start).append((int) Math.round(1.0*num*width_dp/baseScreen.getWidth_dp())).append(end).append("\n");
                            }
                            break;
                        case "sp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_sp = screenBeans.get(i).getWidth_sp();
                                Log.e(TAG, "gen: "+num+"*"+width_sp+"/"+baseScreen.getWidth_sp() );
                                Log.e(TAG, width_sp+ " gen: "+start+((int) Math.round(1.0*num*width_sp/baseScreen.getWidth_sp()))+end );
                                dimen_xs[i].append(start).append((int) Math.round(1.0*num*width_sp/baseScreen.getWidth_sp())).append(end).append("\n");
                            }
                            break;
                    }
//                    sw480.append(start).append((int) Math.round(num * 0.6)).append(end).append("\n");
//                    sw600.append(start).append((int) Math.round(num * 0.75)).append(end).append("\n");
//                    sw720.append(start).append((int) Math.round(num * 0.9)).append(end).append("\n");
//                    sw800.append(tempString).append("\n");
//                    w820.append(tempString).append("\n");
                } else {
                    for (StringBuilder dimenX : dimen_xs) {
                        dimenX.append(tempString).append("\n");
                    }
//                    sw480.append(tempString).append("\n");
//                    sw600.append(tempString).append("\n");
//                    sw720.append(tempString).append("\n");
//                    sw800.append(tempString).append("\n");
//                    w820.append(tempString).append("\n");
                }
                line++;
            }
            reader.close();
            for (int i = 0; i < screenBeans.size(); i++) {
                System.out.println("<!--  "+screenBeans.get(i)+" -->");
                System.out.println(dimen_xs[i]);
            }
            for (int i = 0; i < screenBeans.size(); i++) {
                String filePath = savePath+"/values-"+screenBeans.get(i).getWidth_dp()+"x"+screenBeans.get(i).getHeight_dp()+"/dimens.xml";
                writeFile(filePath, dimen_xs[i].toString());
            }
//            String sw480file = savePath+"/values-"+1920x1080+"/dimens.xml";
//            String sw600file = savePath+"/values-sw600dp-land/dimens.xml";
//            String sw720file = savePath+"/values-sw720dp-land/dimens.xml";
//            String sw800file = savePath+"/values-sw800dp-land/dimens.xml";
//            String w820file = savePath+"/values-w820dp/dimens.xml";
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
        Log.e(TAG, "writeFile完成："+path );
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
