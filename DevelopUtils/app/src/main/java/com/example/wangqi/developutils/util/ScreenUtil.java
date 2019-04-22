package com.example.wangqi.developutils.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.InverseMethod;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.example.wangqi.developutils.bean.DimenItemBean;
import com.example.wangqi.developutils.bean.EventBean;
import com.example.wangqi.developutils.bean.ScreenBean;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;

import static com.example.wangqi.developutils.bean.EventBean.CODE.DIMENS_LOG;
import static java.lang.Enum.valueOf;

/**
 * Created by wangqi on 2018/6/5.
 */

public class ScreenUtil {
    private static String TAG = "ScreenUtil";
    public static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void createBaseDimens(String savePath, ScreenBean baseScreen) {
        File file = new File(savePath, "dimens_x.xml");
        final StringBuilder dimens_x = new StringBuilder();
        dimens_x.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        dimens_x.append("<!--").append(baseScreen.toString()).append("-->\n");
        System.out.println("生成默认分辨率：");
        EventBus.getDefault().post(new EventBean(DIMENS_LOG, "生成默认分辨率:dimens_x"));
        dimens_x.append("<resources>");
        dimens_x.append("\n<!--").append("生成默认dp").append("-->\n");
        for (int i = 1; i <= baseScreen.getWidth_dp(); i++) {
            dimens_x.append("<dimen name=\"xdp" + i + "\">" + i + "dp</dimen>\n");
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
        EventBus.getDefault().post(new EventBean(DIMENS_LOG, "生成默认分辨率:dimens_y"));
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

    /**
     *  @param path 文件路径
     * @param type DimenItemBean.TYPE_X DimenItemBean.TYPE_X
     * @param setBeen
     */
    public static ArrayList<DimenItemBean> getItem(String path, int type, HashSet<DimenItemBean> setBeen) {
        setBeen.clear();
        ArrayList<DimenItemBean> items=new ArrayList<>();
        File file = new File(path);
        BufferedReader reader = null;
        int oprate=-1;
        int screenW=0;
        int screenH=0;
        int dpi=0;
        DimenItemBean tempBean;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    String name=start.substring(start.indexOf("\"")+1,start.lastIndexOf("\""));
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));
                    String unit = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    DimenItemBean dimenItemBean = new DimenItemBean(type, name, num, unit);
                    items.add(dimenItemBean);
                }else{
                    switch (oprate){
                        case -1:
                            if(tempString.trim().contains("<!--start")){
                                oprate=0;
                            }
                            break;
                        case 0:
                            String[] split = tempString.trim().split("\\*");
                            screenW=Integer.valueOf(split[0]);
                            screenH=Integer.valueOf(split[1]);
                            dpi=Integer.valueOf(split[2]);
                            oprate=1;
                            break;
                        case 1:
                            if(tempString.trim().contains("end-->")){
                                oprate=-1;
                            }else {
                                tempBean = new DimenItemBean();
                                tempBean.setScreenW(screenW);
                                tempBean.setScreenH(screenH);
                                tempBean.setDpi(dpi);
                                String[] split1 = tempString.trim().split("/");
                                if (split1.length == 4) {
                                    tempBean.setType(Integer.valueOf(split1[0]));
                                    tempBean.setName(split1[1]);
                                    tempBean.setOprate(split1[2]);
                                    tempBean.setNum(Double.valueOf(split1[3]));
                                    setBeen.add(tempBean);
                                } else {//split1.length==3
                                    tempBean.setType(Integer.valueOf(split1[0]));
                                    tempBean.setOprate(split1[1]);
                                    tempBean.setNum(Double.valueOf(split1[2]));
                                    setBeen.add(tempBean);
                                }
                            }
                            break;
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void gen(String dimen_x, String dimen_y, String savePath, ScreenBean baseScreen, ArrayList<ScreenBean> screenBeans) {
        Log.e(TAG, "gen: " + dimen_x + "\n" + dimen_y + "\n" + savePath + "\n" + baseScreen);
        EventBus.getDefault().post(new EventBean(DIMENS_LOG, "适配文件: " + dimen_x + "\n" + dimen_y + "\n" + savePath + "\n" + baseScreen));
        deleteFile(new File(savePath));
        EventBus.getDefault().post(new EventBean(DIMENS_LOG, "文件夹清理完成，开始生成适配"));
        if(!TextUtils.isEmpty(dimen_x)) {
            fitDimens_x(dimen_x, savePath, baseScreen, screenBeans);
        }
        if(!TextUtils.isEmpty(dimen_y)) {
            fitDimens_y(dimen_y, savePath, baseScreen, screenBeans);
        }
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
        HashMap<String,DimenItemBean> itemData=new HashMap<>();
        HashMap<String,DimenItemBean> unitData=new HashMap<>();
        int oprate=-1;
        int screenW =0;
        int screenH =0;
        int dpi=0;
        DimenItemBean dimenItemBean;
        File file = new File(dimen_y);
        BufferedReader reader = null;
        final StringBuilder[] dimen_ys = new StringBuilder[screenBeans.size()];
        for (int i = 0; i < screenBeans.size(); i++) {
            dimen_ys[i] = new StringBuilder();
//            dimen_ys[i].append("<!--").append(screenBeans.get(i).toString()).append("-->\n");
        }
        try {
            System.out.println("生成不同分辨率：");
            EventBus.getDefault().post(new EventBean(DIMENS_LOG, "开始适配dimens_y"));
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
//                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    String name=start.substring(start.indexOf("\"")+1,start.lastIndexOf("\""));
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));
                    String type = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    switch (type) {
                        case "px":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int height_px = screenBean.getHeight_px();
//                                Log.e(TAG, "gen: " + num + "*" + height_px + "/" + baseScreen.getHeight_px());
                                int result = (int) Math.round(1.0 * num * height_px / baseScreen.getHeight_px());
//                                Log.e(TAG, height_px + "  gen: " + start + result + end);
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_YPX, screenBean, result);
                                dimen_ys[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                        case "dp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int height_dp = screenBean.getHeight_dp();
//                                Log.e(TAG, "gen: " + num + "*" + height_dp + "/" + baseScreen.getHeight_dp());
                                int result = (int) Math.round(1.0 * num * height_dp / baseScreen.getHeight_dp());
//                                Log.e(TAG, height_dp + "  gen: " + start + result + end);
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_YPX, screenBean, result);
                                dimen_ys[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                        case "sp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int height_sp = screenBean.getHeight_sp();
//                                Log.e(TAG, "gen: " + num + "*" + height_sp + "/" + baseScreen.getHeight_sp());
                                int result = (int) Math.round(1.0 * num * height_sp / baseScreen.getHeight_sp());
//                                Log.e(TAG, height_sp + " gen: " + start + result + end);
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_YPX, screenBean, result);
                                dimen_ys[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                    }
                } else {
                    Log.e(TAG, "gen: tempString" + tempString+"   "+oprate);
                    switch (oprate){
                        case -1:
                            if(tempString.trim().contains("<!--start")){
                                oprate=0;
                            }
                            break;
                        case 0:
                            String[] split = tempString.trim().split("\\*");
                            screenW=Integer.valueOf(split[0]);
                            screenH=Integer.valueOf(split[1]);
                            dpi=Integer.valueOf(split[2]);
                            oprate=1;
                            break;
                        case 1:
                            if(tempString.trim().contains("end-->")){
                                oprate=-1;
                                Set<String> keySet = itemData.keySet();
                                for (String s : keySet) {
                                    Log.e(TAG, "处理: readInfo "+s+"  "+itemData.get(s) );
                                }
                                keySet = unitData.keySet();
                                for (String s : keySet) {
                                    Log.e(TAG, "处理: readInfo "+s+"  "+unitData.get(s) );
                                }
                            }else {
                                dimenItemBean = new DimenItemBean();
                                dimenItemBean.setScreenW(screenW);
                                dimenItemBean.setScreenH(screenH);
                                dimenItemBean.setDpi(dpi);
                                String[] split1 = tempString.trim().split("/");
                                if (split1.length == 4) {
                                    dimenItemBean.setType(Integer.valueOf(split1[0]));
                                    dimenItemBean.setName(split1[1]);
                                    dimenItemBean.setOprate(split1[2]);
                                    dimenItemBean.setNum(Double.valueOf(split1[3]));
                                    String key = dimenItemBean.getName() + dimenItemBean.getScreenW()
                                            + dimenItemBean.getScreenH() + dimenItemBean.getDpi();
                                    itemData.put(key, dimenItemBean);
                                    Log.e(TAG, "处理: readInfo "+key+"  "+dimenItemBean );
                                } else {//split1.length==3
                                    dimenItemBean.setType(Integer.valueOf(split1[0]));
                                    dimenItemBean.setOprate(split1[1]);
                                    dimenItemBean.setNum(Double.valueOf(split1[2]));
                                    String key = "" + dimenItemBean.getType() + dimenItemBean.getScreenW()
                                            + dimenItemBean.getScreenH() + dimenItemBean.getDpi();
                                    unitData.put(key, dimenItemBean);
                                    Log.e(TAG, "处理: a readInfo "+key+"  "+dimenItemBean );
                                }
                            }
                            break;
                    }
                    for (StringBuilder dimenY : dimen_ys) {
                        dimenY.append(tempString).append("\n");
                    }
                }
            }
            reader.close();
//            for (int i = 0; i < screenBeans.size(); i++) {
//                System.out.println("<!--  " + screenBeans.get(i) + " -->");
//                System.out.println(dimen_ys[i]);
//            }
            for (int i = 0; i < screenBeans.size(); i++) {
                final String filePath = savePath + "/values-" + screenBeans.get(i).getDpi() + "dpi-" + screenBeans.get(i).getWidth_px() + "x" + screenBeans.get(i).getHeight_px() + "/" + file.getName();
                EventBus.getDefault().post(new EventBean(DIMENS_LOG, "生成适配文件：" + filePath));
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
        HashMap<String,DimenItemBean> itemData=new HashMap<>();
        HashMap<String,DimenItemBean> unitData=new HashMap<>();
        int oprate=-1;
        int screenW =0;
        int screenH =0;
        int dpi=0;
        DimenItemBean dimenItemBean;
        File file = new File(dimen_x);
        BufferedReader reader = null;
        final StringBuilder[] dimen_xs = new StringBuilder[screenBeans.size()];
        for (int i = 0; i < screenBeans.size(); i++) {
            dimen_xs[i] = new StringBuilder();
//            dimen_xs[i].append("<!--").append(screenBeans.get(i).toString()).append("-->\n");
        }
        try {
            System.out.println("生成不同分辨率：");
            EventBus.getDefault().post(new EventBean(DIMENS_LOG, "开始适配dimens_x"));
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
//                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    //tempString = tempString.replaceAll(" ", "");
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);
                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    String name=start.substring(start.indexOf("\"")+1,start.lastIndexOf("\""));
                    int num = Integer.valueOf(tempString.substring(tempString.indexOf(">") + 1, tempString.indexOf("</dimen>") - 2));
                    String type = tempString.substring(tempString.indexOf("</dimen>") - 2, tempString.lastIndexOf("<"));
                    switch (type) {
                        case "px":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int width_px = screenBean.getWidth_px();
//                                Log.e(TAG, "gen: " + num + "*" + width_px + "/" + baseScreen.getWidth_px());
                                int result = (int) Math.round(1.0 * num * width_px / baseScreen.getWidth_px());
//                                Log.e(TAG, width_px + "  gen: " + start + result + end);
                                //处理特殊设置
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_XPX, screenBean, result);
                                dimen_xs[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                        case "dp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int width_dp = screenBean.getWidth_dp();
//                                Log.e(TAG, "gen: " + num + "*" + width_dp + "/" + baseScreen.getWidth_dp());
                                int result = (int) Math.round(1.0 * num * width_dp / baseScreen.getWidth_dp());
//                                Log.e(TAG, width_dp + "  gen: " + start + result + end);
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_XDP, screenBean, result);
                                dimen_xs[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                        case "sp":
                            for (int i = 0; i < screenBeans.size(); i++) {
                                ScreenBean screenBean = screenBeans.get(i);
                                int width_sp = screenBean.getWidth_sp();
//                                Log.e(TAG, "gen: " + num + "*" + width_sp + "/" + baseScreen.getWidth_sp());
                                int result = (int) Math.round(1.0 * num * width_sp / baseScreen.getWidth_sp());
//                                Log.e(TAG, width_sp + " gen: " + start + result + end);
                                result=handleSet(itemData,unitData, name, DimenItemBean.TYPE_XSP, screenBean, result);
                                dimen_xs[i].append(start).append(result).append(end).append("\n");
                            }
                            break;
                    }
                } else {
                    //解析特殊设置项
                    switch (oprate){
                        case -1:
                            if(tempString.trim().contains("<!--start")){
                                oprate=0;
                            }
                            break;
                        case 0:
                            String[] split = tempString.trim().split("\\*");
                            screenW=Integer.valueOf(split[0]);
                            screenH=Integer.valueOf(split[1]);
                            dpi=Integer.valueOf(split[2]);
                            oprate=1;
                            break;
                        case 1:
                            if(tempString.trim().contains("end-->")){
                                oprate=-1;
                                Set<String> keySet = itemData.keySet();
                                for (String s : keySet) {
                                    Log.e(TAG, "处理: readInfo "+s+"  "+itemData.get(s) );
                                }
                                keySet = unitData.keySet();
                                for (String s : keySet) {
                                    Log.e(TAG, "处理: "+unitData.get(s) );
                                }
                            }else {
                                dimenItemBean = new DimenItemBean();
                                dimenItemBean.setScreenW(screenW);
                                dimenItemBean.setScreenH(screenH);
                                dimenItemBean.setDpi(dpi);
                                String[] split1 = tempString.trim().split("/");
                                if (split1.length == 4) {
                                    dimenItemBean.setType(Integer.valueOf(split1[0]));
                                    dimenItemBean.setName(split1[1]);
                                    dimenItemBean.setOprate(split1[2]);
                                    dimenItemBean.setNum(Double.valueOf(split1[3]));
                                    itemData.put(dimenItemBean.getName()+dimenItemBean.getScreenW()
                                            +dimenItemBean.getScreenH()+dimenItemBean.getDpi(), dimenItemBean);
                                } else {//split1.length==3
                                    dimenItemBean.setType(Integer.valueOf(split1[0]));
                                    dimenItemBean.setOprate(split1[1]);
                                    dimenItemBean.setNum(Double.valueOf(split1[2]));
                                    unitData.put(""+dimenItemBean.getType()+dimenItemBean.getScreenW()
                                            +dimenItemBean.getScreenH()+dimenItemBean.getDpi(), dimenItemBean);
                                }
                            }
                            break;
                    }
                    for (StringBuilder dimenX : dimen_xs) {
                        dimenX.append(tempString).append("\n");
                    }
                }
            }
            reader.close();
//            for (int i = 0; i < screenBeans.size(); i++) {
//                System.out.println("<!--  " + screenBeans.get(i) + " -->");
//                System.out.println(dimen_xs[i]);
//            }
            for (int i = 0; i < screenBeans.size(); i++) {
                final String filePath = savePath + "/values-" + screenBeans.get(i).getDpi() + "dpi-" + screenBeans.get(i).getWidth_px() + "x" + screenBeans.get(i).getHeight_px() + "/" + file.getName();
                EventBus.getDefault().post(new EventBean(DIMENS_LOG, "生成文件：" + filePath));
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

    private static int handleSet(HashMap<String, DimenItemBean> itemData, HashMap<String, DimenItemBean> unitData,String name, int unitType, ScreenBean screenBean, int result) {
        if(itemData.containsKey(name+screenBean.getWidth_px()+screenBean.getHeight_px()+screenBean.getDpi())){
            DimenItemBean dimenItemBean1 = itemData.get(name + screenBean.getWidth_px() + screenBean.getHeight_px() + screenBean.getDpi());
            Log.e(TAG, " 处理前 name "+name+"  "+result );
            switch (dimenItemBean1.getOprate()){
                case DimenItemBean.TYPE_OPRATION_ADD:
                    result+=dimenItemBean1.getNum();
                    break;
                case DimenItemBean.TYPE_OPRATION_MINUS:
                    result-=dimenItemBean1.getNum();
                    break;
                case DimenItemBean.TYPE_OPRATION_MULTIPLY:
                    result*=dimenItemBean1.getNum();
                    break;
                case DimenItemBean.TYPE_OPRATION_DIVIDE:
                    result=(int)(1.0*result/dimenItemBean1.getNum());
                    break;
            }
            Log.e(TAG, " 处理后 name "+name+"  "+result );
        }else {
            String key = "" + unitType + screenBean.getWidth_px() + screenBean.getHeight_px() + screenBean.getDpi();
            Log.e(TAG, "handleSet: "+key );
            if(unitData.containsKey(key)){
                Log.e(TAG, "handleSet: 处理前 unitType "+unitType+"  "+name+"  "+result );
                DimenItemBean dimenItemBean1 = unitData.get(key);
                switch (dimenItemBean1.getOprate()){
                    case DimenItemBean.TYPE_OPRATION_ADD:
                        result+=dimenItemBean1.getNum();
                        break;
                    case DimenItemBean.TYPE_OPRATION_MINUS:
                        result-=dimenItemBean1.getNum();
                        break;
                    case DimenItemBean.TYPE_OPRATION_MULTIPLY:
                        result*=dimenItemBean1.getNum();
                        break;
                    case DimenItemBean.TYPE_OPRATION_DIVIDE:
                        result=(int)(1.0*result/dimenItemBean1.getNum());
                        break;
                }
                Log.e(TAG, "处理后 unitType "+unitType+"  "+name+"  "+result );
            }
        }
        return result;
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
        EventBus.getDefault().post(new EventBean(DIMENS_LOG, "writeFile完成：" + path));
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

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static float getScaledDensity(Context context) {
        return context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static int getDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static int dip2px(float dpValue, float density) {
        Log.e(TAG, "dip2px: ");
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(float spValue, float scaledDensity) {
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

    public static void saveSet(final String path, StringBuilder sb) {
        File file = new File(path);
        BufferedReader reader = null;
        int oprate=-1;
        final StringBuilder result=new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                Log.e(TAG, "gen: tempString" + tempString);
                if (tempString.contains("</dimen>")) {
                    if(oprate==-1){
                        result.append(sb.toString());
                        oprate=1;
                    }
                    result.append(tempString+"\n");
                }else{
                    switch (oprate){
                        case -1:
                            if(tempString.trim().contains("<!--start")){
                                oprate=0;
                            }else{
                                result.append(tempString+"\n");
                            }
                            break;
                        case 0:
                            if(tempString.trim().contains("end-->")){
                                oprate=-1;
                            }
                            break;
                        case 1:
                            result.append(tempString+"\n");
                            break;
                    }

                }
            }
            reader.close();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        writeFile(path, result.toString());
                    }
                });
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
