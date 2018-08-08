package com.example.wangqi.developutils.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.InverseMethod;
import android.os.Build;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.example.wangqi.developutils.bean.EventBean;
import com.example.wangqi.developutils.bean.ScreenBean;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;

import static com.example.wangqi.developutils.bean.EventBean.CODE.DIMENS_LOG;

/**
 * Created by wangqi on 2018/6/5.
 */

public class ScreenUtil {
    private static String TAG = "ScreenUtil";
    public static ExecutorService executorService= Executors.newCachedThreadPool();
    public static void createBaseDimens(String savePath, ScreenBean baseScreen) {
        File file = new File(savePath, "dimens_x.xml");
        final StringBuilder dimens_x = new StringBuilder();
        dimens_x.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
//        dimens_x.append("<!--").append(baseScreen.toString()).append("-->\n");
        System.out.println("生成默认分辨率：");
        EventBus.getDefault().post(new EventBean(DIMENS_LOG,"生成默认分辨率:dimens_x"));
        dimens_x.append("<resources>");
        dimens_x.append("\n<!--").append("生成默认dp").append("-->\n");
        for (int i = 1; i <= baseScreen.getWidth_dp(); i++) {
            dimens_x.append("<dimen name=\"xdp"+i+"\">" + i + "dp</dimen>\n");
        }
        dimens_x.append("\n<!--").append("生成默认px").append("-->\n");
        for (int i = 1; i <= baseScreen.getWidth_px(); i++) {
            dimens_x.append("<dimen name=\"xpx" + i + "\">" + i + "px</dimen>\n");
        }
        dimens_x.append("\n<!--").append("生成默认sp").append("-->\n");
        for (int i = 1; i <= baseScreen.getWidth_sp(); i++) {
            dimens_x.append("<dimen name=\"xsp" + i + "\">" + i + "sp</dimen>\n");
        }
        dimens_x.append("</resources>\n");
        final File finalFile = file;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                writeFile(finalFile.getAbsolutePath(), dimens_x.toString());
            }
        });

        file = new File(savePath, "dimens_y.xml");
        final StringBuilder dimens_y = new StringBuilder();
        dimens_y.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
//        dimens_y.append("<!--").append(baseScreen.toString()).append("-->\n");
        System.out.println("生成默认分辨率：");
        EventBus.getDefault().post(new EventBean(DIMENS_LOG,"生成默认分辨率:dimens_y"));
        dimens_y.append("<resources>");
        dimens_y.append("\n<!--").append("生成默认dp").append("-->\n");
        for (int i = 1; i <= baseScreen.getHeight_dp(); i++) {
            dimens_y.append("<dimen name=\"ydp" + i + "\">" + i + "dp</dimen>\n");
        }
        dimens_y.append("\n<!--").append("生成默认px").append("-->\n");
        for (int i = 1; i <= baseScreen.getHeight_px(); i++) {
            dimens_y.append("<dimen name=\"ypx" + i + "\">" + i + "px</dimen>\n");
        }
        dimens_y.append("\n<!--").append("生成默认sp").append("-->\n");
        for (int i = 1; i <= baseScreen.getHeight_sp(); i++) {
            dimens_y.append("<dimen name=\"ysp" + i + "\">" + i + "sp</dimen>\n");
        }
        dimens_y.append("</resources>\n");
        final File finalFile1 = file;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                writeFile(finalFile1.getAbsolutePath(), dimens_y.toString());
            }
        });
    }

    public static void gen(String dimen_x, String dimen_y, String savePath, ScreenBean baseScreen, ArrayList<ScreenBean> screenBeans) {
        Log.e(TAG, "gen: " + dimen_x + "\n" + dimen_y + "\n" + savePath + "\n" + baseScreen);
        EventBus.getDefault().post(new EventBean(DIMENS_LOG,"适配文件: " + dimen_x + "\n" + dimen_y + "\n" + savePath + "\n" + baseScreen));
        deleteFile(new File(savePath));
        EventBus.getDefault().post(new EventBean(DIMENS_LOG,"文件夹清理完成，开始生成适配"));
        fitDimens_x(dimen_x, savePath, baseScreen, screenBeans);
        fitDimens_y(dimen_y, savePath, baseScreen, screenBeans);
    }
    //flie：要删除的文件夹的所在位置
    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    public static void fitDimens_y(String dimen_y, String savePath, ScreenBean baseScreen, ArrayList<ScreenBean> screenBeans) {
        File file = new File(dimen_y);
        BufferedReader reader = null;
        final StringBuilder[] dimen_ys = new StringBuilder[screenBeans.size()];
        for (int i = 0; i < screenBeans.size(); i++) {
            dimen_ys[i] = new StringBuilder();
//            dimen_ys[i].append("<!--").append(screenBeans.get(i).toString()).append("-->\n");
        }
        try {
            System.out.println("生成不同分辨率：");
            EventBus.getDefault().post(new EventBean(DIMENS_LOG,"开始适配dimens_y"));
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));
                    String type = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    switch (type) {
                        case "px":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int height_px = screenBeans.get(i).getHeight_px();
                                Log.e(TAG, "gen: " + num + "*" + height_px + "/" + baseScreen.getHeight_px());
                                Log.e(TAG, height_px + "  gen: " + start + ((int) Math.round(1.0 * num * height_px / baseScreen.getHeight_px())) + end);
                                dimen_ys[i].append(start).append((int) Math.round(1.0 * num * height_px / baseScreen.getHeight_px())).append(end).append("\n");
                            }
                            break;
                        case "dp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int height_dp = screenBeans.get(i).getHeight_dp();
                                Log.e(TAG, "gen: " + num + "*" + height_dp + "/" + baseScreen.getHeight_dp());
                                Log.e(TAG, height_dp + "  gen: " + start + ((int) Math.round(1.0 * num * height_dp / baseScreen.getHeight_dp())) + end);
                                dimen_ys[i].append(start).append((int) Math.round(1.0 * num * height_dp / baseScreen.getHeight_dp())).append(end).append("\n");
                            }
                            break;
                        case "sp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int height_sp = screenBeans.get(i).getHeight_sp();
                                Log.e(TAG, "gen: " + num + "*" + height_sp + "/" + baseScreen.getHeight_sp());
                                Log.e(TAG, height_sp + " gen: " + start + ((int) Math.round(1.0 * num * height_sp / baseScreen.getHeight_sp())) + end);
                                dimen_ys[i].append(start).append((int) Math.round(1.0 * num * height_sp / baseScreen.getHeight_sp())).append(end).append("\n");
                            }
                            break;
                    }
                } else {
                    for (StringBuilder dimenY : dimen_ys) {
                        dimenY.append(tempString).append("\n");
                    }
                }
                line++;
            }
            reader.close();
            for (int i = 0; i < screenBeans.size(); i++) {
                System.out.println("<!--  " + screenBeans.get(i) + " -->");
                System.out.println(dimen_ys[i]);
            }
            for (int i = 0; i < screenBeans.size(); i++) {
                final String filePath = savePath + "/values-" +screenBeans.get(i).getDpi()+"dpi-" + screenBeans.get(i).getWidth_px() + "x" + screenBeans.get(i).getHeight_px() + "/" + file.getName();
                EventBus.getDefault().post(new EventBean(DIMENS_LOG,"生成适配文件："+filePath));
                final int finalI = i;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        writeFile(filePath, dimen_ys[finalI].toString());
                    }
                });
            }
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

    public static void fitDimens_x(String dimen_x, String savePath, ScreenBean baseScreen, ArrayList<ScreenBean> screenBeans) {
        File file = new File(dimen_x);
        BufferedReader reader = null;
        final StringBuilder[] dimen_xs = new StringBuilder[screenBeans.size()];
        for (int i = 0; i < screenBeans.size(); i++) {
            dimen_xs[i] = new StringBuilder();
//            dimen_xs[i].append("<!--").append(screenBeans.get(i).toString()).append("-->\n");
        }
        try {
            System.out.println("生成不同分辨率：");
            EventBus.getDefault().post(new EventBean(DIMENS_LOG,"开始适配dimens_x"));
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    //tempString = tempString.replaceAll(" ", "");
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));
                    String type = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    switch (type) {
                        case "px":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_px = screenBeans.get(i).getWidth_px();
                                Log.e(TAG, "gen: " + num + "*" + width_px + "/" + baseScreen.getWidth_px());
                                Log.e(TAG, width_px + "  gen: " + start + ((int) Math.round(1.0 * num * width_px / baseScreen.getWidth_px())) + end);
                                dimen_xs[i].append(start).append((int) Math.round(1.0 * num * width_px / baseScreen.getWidth_px())).append(end).append("\n");
                            }
                            break;
                        case "dp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_dp = screenBeans.get(i).getWidth_dp();
                                Log.e(TAG, "gen: " + num + "*" + width_dp + "/" + baseScreen.getWidth_dp());
                                Log.e(TAG, width_dp + "  gen: " + start + ((int) Math.round(1.0 * num * width_dp / baseScreen.getWidth_dp())) + end);
                                dimen_xs[i].append(start).append((int) Math.round(1.0 * num * width_dp / baseScreen.getWidth_dp())).append(end).append("\n");
                            }
                            break;
                        case "sp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                int width_sp = screenBeans.get(i).getWidth_sp();
                                Log.e(TAG, "gen: " + num + "*" + width_sp + "/" + baseScreen.getWidth_sp());
                                Log.e(TAG, width_sp + " gen: " + start + ((int) Math.round(1.0 * num * width_sp / baseScreen.getWidth_sp())) + end);
                                dimen_xs[i].append(start).append((int) Math.round(1.0 * num * width_sp / baseScreen.getWidth_sp())).append(end).append("\n");
                            }
                            break;
                    }
                } else {
                    for (StringBuilder dimenX : dimen_xs) {
                        dimenX.append(tempString).append("\n");
                    }
                }
                line++;
            }
            reader.close();
            for (int i = 0; i < screenBeans.size(); i++) {
                System.out.println("<!--  " + screenBeans.get(i) + " -->");
                System.out.println(dimen_xs[i]);
            }
            for (int i = 0; i < screenBeans.size(); i++) {
                final String filePath = savePath + "/values-" +screenBeans.get(i).getDpi()+"dpi-"+ screenBeans.get(i).getWidth_px() + "x" + screenBeans.get(i).getHeight_px() + "/" + file.getName();
                EventBus.getDefault().post(new EventBean(DIMENS_LOG,"生成文件："+filePath));
                final int finalI = i;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        writeFile(filePath, dimen_xs[finalI].toString());
                    }
                });
            }
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
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
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
        Log.e(TAG, "writeFile完成：" + path);
        EventBus.getDefault().post(new EventBean(DIMENS_LOG,"writeFile完成：" + path));
    }

    @InverseMethod("floatToString")
    public static float stringToFloat(String s) {
        try {
            return Float.valueOf(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String floatToString(float f) {
        return String.valueOf(f);
    }
    public static float getDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }
    public static float getScaledDensity(Context context){
        return context.getResources().getDisplayMetrics().scaledDensity;
    }
    public static int getDensityDpi(Context context){
        return context.getResources().getDisplayMetrics().densityDpi;
    }
    public static int dip2px( float dpValue,float density) {
        Log.e(TAG, "dip2px: ");
       return (int) (dpValue * density + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(float spValue,float scaledDensity) {
        return (int) (spValue * scaledDensity + 0.5f);
    }

    public static int px2dip(float pxValue, float density) {
        Log.e(TAG, "px2dip: " + (int) (pxValue / density + 0.5f) + "  " + density);
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(float pxValue, float scaledDensity) {
        Log.e(TAG, "px2sp: " + (int) (pxValue / scaledDensity + 0.5f) + " sp" + "  " + scaledDensity);
        return (int) (pxValue / scaledDensity + 0.5f);
    }

    //获取虚拟按键的高度
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }
}
