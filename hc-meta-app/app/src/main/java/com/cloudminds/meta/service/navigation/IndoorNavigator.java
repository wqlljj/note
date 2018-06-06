package com.cloudminds.meta.service.navigation;

import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.manager.MetaManager;
import com.cloudminds.meta.util.TTSSpeaker;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by SX on 2017/5/27.
 */

public class IndoorNavigator {
    private static String TAG="IndoorNavigator";
//    public static boolean isStartNavi=false;
    public static String to;
    public static void sendStartNavi(String from, String to){
        //路线计算成功
        if(type==Type.NAVIING){
            sendStopNavi(IndoorNavigator.Type.END_NAVI, TTSSpeaker.getInstance().mContext.getString(R.string.navi_end_1));
        }
        TTSSpeaker.speak(TTSSpeaker.getInstance().mContext.getString(R.string.start_indoorNavi), TTSSpeaker.HIGH);
        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT, TTSSpeaker.getInstance().mContext.getString(R.string.start_indoorNavi));
            IndoorNavigator.to=to;
        type=Type.NAVIING;
        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        if (cmdEngine!=null){
            JSONObject object = new JSONObject();
            try {
                object.put("naviType","Indoor");
//                object.put("fromPos",from);
//
//                object.put("toPos",IndoorNavigator.to);
                object.put("fromPos","T1");

                object.put("toPos","T2");
            } catch (Exception e){

            }
            cmdEngine.sendNaviInfo(object,"startNavi");
            MetaManager.getInstance().sendUltrasound(true);
            Log.e(TAG, "sendStartNavi: "+object.toString() );
        }
    }
    public  enum Type{
    NAVIING,END_NAVI,INTERRUPT
}
    public static Type type=Type.END_NAVI;
    public static void sendStopNavi(Type type,String reason){
        if(type==Type.END_NAVI){
            to=null;
        }
        Log.e(TAG, "sendStopNavi: "+IndoorNavigator.type );
        if((IndoorNavigator.type!=Type.NAVIING)){
            if(IndoorNavigator.type==Type.END_NAVI)return;
            else
            IndoorNavigator.type=type;
            return;
        }else{
            IndoorNavigator.type=type;
        }
        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        if (cmdEngine!=null){
            JSONObject object = new JSONObject();
            try {
                object.put("reason",reason);
                object.put("naviType","Indoor");
            }catch (Exception e){
                e.printStackTrace();
            }
            cmdEngine.sendNaviInfo(object,"stopNavi");
            MetaManager.getInstance().sendUltrasound(false);
            TTSSpeaker.speak(TTSSpeaker.getInstance().mContext.getString(R.string.indoor_navi_end), TTSSpeaker.HIGH);
            Log.e(TAG, "sendStopNavi: "+object.toString() );
        }
    }
//    {
//            type: “naviInfo",
//            seq: 12313,  
//
//            data:{
//                  location:[x,y],
//                  orientation: “"
//                  navi:{
//                          hint : “前方;2”,  // $1$2米有障碍物，请注意避让
//                          action: “move|rotate” 
//                          angle: 30, //(-180~180)  ~30表示向左
//                          speed: 8, //option, only valid for pepper
//                          distance: 1,  
//                        confidence:0~1                
//                      }
//            }
//    }
    public static void handleNaviInfo(JSONObject navi){
        Log.e(TAG, "handleNaviInfo: "+navi );
        try {
        if(navi.has("hint")){
            String hint =  navi.getString("hint");
            if(!TextUtils.isEmpty(hint)){
                String[] split = hint.split(";");
                if(split.length==1)
                    handleHint(MetaApplication.mContext.getString(R.string.ahead),Double.valueOf(split[0]));
                else
                    handleHint(split[0],Double.valueOf(split[1]));
                MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,hint);
            }
        }
            if(navi.has("action")) {
                String action = navi.getString("action");
                if(!TextUtils.isEmpty(action))
                MetaManager.getInstance().handleMotionCommand(navi,MetaManager.NAVICOMMAND_AI);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void speak(String message){
        TTSSpeaker.speak(message, TTSSpeaker.NAVIINTO);
    }
    static Long lastTime=0l;
    static double lastDistance=-1;
    public static void handleHint(String direct,double distance){
        long timeMillis = System.currentTimeMillis();
        Log.e(TAG, "objectToString: "+(timeMillis-lastTime)+"  "+(timeMillis-lastTime>=(3000+distance*2000))+"   "+((3000+distance*2000)));
        Log.e(TAG, "objectToString: "+distance+"  "+lastDistance);
        if(lastDistance==-1||timeMillis-lastTime>=(3000+distance*2000)||(Math.floor(distance)!=Math.floor(lastDistance))) {
            speakNavi(direct, distance, timeMillis);
        }
        lastDistance=distance;
    }

    private static void speakNavi(String direct, double distance, long timeMillis) {
        //There are obstacles in front of 5 meters. Please take care
        TTSSpeaker.speak(String.format(MetaApplication.mContext.getString(R.string.obstacle_reminding),direct,distance) , TTSSpeaker.NAVIINTO);
        lastTime=timeMillis;
    }
}
