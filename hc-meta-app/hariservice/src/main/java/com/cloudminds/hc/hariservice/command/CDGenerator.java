package com.cloudminds.hc.hariservice.command;

import android.util.Log;

import com.cloudminds.hc.hariservice.manager.CallManager;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Created by zoey on 17/5/5.
 */

public class CDGenerator {

    public static int sequence = 0;
    public static int groupId = 0;

    public static final String TAG = "HS/CDGenerator";

    public static int generateSeqNumber(){

        int maxSeq = (int) (2147483648L-1);
        if (sequence == maxSeq){
            sequence = 0;
        }else {
            sequence++;
        }

        return sequence;
    }

    public static int generateGroupId(){

        int maxId = (int) (2147483648L-1);
        if (groupId == maxId){
            groupId = 0;
        }else {
            groupId++;
        }

        return groupId;
    }

    public static ByteBuffer createSpeakData(String text){

       // LogUtils.d(TAG,"Begin Decorate speak text:"+text);

        if (null==text || text.isEmpty()){
           // LogUtils.d(TAG,"Text is empty , return");
            return null;
        }

        /*
          header头 2个字节datatype  2个字节reserve
          body header头 4个字节len 2个字节contenttype
         */

        JSONObject data = new JSONObject();
        try {

            JSONObject qaObj = new JSONObject();
            qaObj.put("lang",CDGenerator.getSysLanguage());
            qaObj.put("text",text);
            // data.put("textType","question");
            data.put("question",qaObj);

        }catch (Exception e){
            e.printStackTrace();
        }

        ByteBuffer buffer = CDGenerator.generateBuffer(data,"qa");

        return buffer;
    }

    public static JSONObject createSpeakJSON(String text){
        //LogUtils.d(TAG,"Begin Decorate speak text:"+text);

        if (null==text || text.isEmpty()){
           // LogUtils.d(TAG,"Text is empty , return");
            return null;
        }

        JSONObject data = new JSONObject();
        try {

            JSONObject qaObj = new JSONObject();
            qaObj.put("lang",CDGenerator.getSysLanguage());
            qaObj.put("text",text);
            // data.put("textType","question");
            data.put("question",qaObj);

        }catch (Exception e){
            e.printStackTrace();
        }

        return generateJSON(data, "qa");
    }

    public static ByteBuffer createReportData(final JSONObject dataJson){
        //LogUtils.d(TAG,"Begin Decorate report info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
            //LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        try {
            dataJson.put("lang",CDGenerator.getSysLanguage());
            dataJson.put("netstate", CallManager.statReports);
        }catch (JSONException e){
            e.printStackTrace();
        }


        ByteBuffer buffer = CDGenerator.generateBuffer(dataJson,"reportStatus");

        return buffer;
    }

    public static JSONObject createReportJSON(final JSONObject dataJson){
       // LogUtils.d(TAG,"Begin Decorate report info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
        //    LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        try {
            dataJson.put("lang",CDGenerator.getSysLanguage());
            dataJson.put("netstate", CallManager.statReports);
        }catch (JSONException e){
            e.printStackTrace();
        }


        return generateJSON(dataJson, "reportStatus");
    }


    public static ByteBuffer createStartNaviData(final  JSONObject dataJson){
        //LogUtils.d(TAG,"Begin Decorate start navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
            LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        ByteBuffer buffer = CDGenerator.generateBuffer(dataJson,"startNavi");

        return buffer;
    }

    public static JSONObject createStartNaviJSON(final  JSONObject dataJson){

        //LogUtils.d(TAG,"Begin Decorate start navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
            //LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        return generateJSON(dataJson, "startNavi");
    }


    public static ByteBuffer createStopNaviData(final  JSONObject dataJson){

        //LogUtils.d(TAG,"Begin Decorate stop navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
            LogUtils.i(TAG,"info is empty , return");
            return null;
        }

        ByteBuffer buffer = CDGenerator.generateBuffer(dataJson,"stopNavi");

        return buffer;
    }

    public static JSONObject createStopNaviJSON(final  JSONObject dataJson){

        //LogUtils.d(TAG,"Begin Decorate stop navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
            //LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        return generateJSON(dataJson, "stopNavi");
    }

    public static ByteBuffer createNaviInfoData(final JSONObject dataJson){
       // LogUtils.d(TAG,"Begin Decorate navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
          //  LogUtils.d(TAG,"info is empty , return");
            return null;
        }

        ByteBuffer buffer = CDGenerator.generateBuffer(dataJson,"naviInfo");

        return buffer;
    }

    public static JSONObject createNaviInfoJSON(final  JSONObject dataJson){
        //LogUtils.d(TAG,"Begin Decorate navi info:"+dataJson.toString());

        if (null==dataJson || dataJson.toString().isEmpty()){
          //  LogUtils.d(TAG,"info is empty , return");
            return null;
        }


        return generateJSON(dataJson,"naviInfo");
    }

    public static JSONObject generateJSON(JSONObject jsonData, String  type){
        JSONObject json = new JSONObject();
        try {
            json.put("type",type);
            json.put("seq", CDGenerator.generateSeqNumber());
            json.put("data",jsonData);
        }catch (Exception e){
            e.printStackTrace();
        }

        return json;
    }

    public static ByteBuffer generateBuffer(JSONObject jsonData,String type){

        JSONObject json = new JSONObject();
        try {
            json.put("type",type);
            json.put("seq", CDGenerator.generateSeqNumber());
            json.put("data",jsonData);
        }catch (Exception e){
            e.printStackTrace();
        }

        String jsonString = json.toString();
        LogUtils.i(TAG,"Json body:"+jsonString);

        byte[] string = jsonString.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(string.length);
        buffer.put(string);
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer generateBuffer(JSONObject json){

        try {
            json.put("seq",CDGenerator.generateSeqNumber());
        }catch (Exception e){
            e.printStackTrace();
        }

        String jsonString = json.toString();
        //LogUtils.d(TAG,"Json body:"+jsonString);

        byte[] string = jsonString.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(string.length);
        buffer.put(string);
        buffer.flip();

        return buffer;
    }

    public static JSONObject generateJSON(JSONObject json){
        try {
            json.put("seq",CDGenerator.generateSeqNumber());
        }catch (Exception e){
            e.printStackTrace();
        }

        return json;
    }


    public static String getSysLanguage(){
        String devLang = Locale.getDefault().getLanguage();
        if (devLang.equalsIgnoreCase("zh")){
            return "CH";
        } else if(devLang.equalsIgnoreCase("ja")){
            return "EN";
        } else {
            return "EN";
        }
    }



}
