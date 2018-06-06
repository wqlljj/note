package com.cloudminds.hc.hariservice.command;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;
import android.widget.Toast;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPData;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPEntity;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.call.CallEvent;
import com.cloudminds.hc.hariservice.command.CmdEvent;
import com.cloudminds.hc.hariservice.command.listener.CmdEventListener;
import com.cloudminds.hc.hariservice.manager.CallManager;
import com.cloudminds.hc.hariservice.manager.SessionEvent;
import com.cloudminds.hc.hariservice.manager.SessionMonitor;
import com.cloudminds.hc.hariservice.push.PushClient;
import com.cloudminds.hc.hariservice.utils.Base64Util;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.HariUtils;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.hariservice.webrtc.PeerConnectionClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;
import org.webrtc.VideoCapturer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by zoey on 17/4/17.
 */

public class CommandEngine {

    public interface PictureCallback{
        public void onPictureTaken(byte[] data, int width, int height,
                                   int rotation);
    }

    private static final String TAG = "HS/CMDEngine";
    private CmdEventListener cmdEventListener;
    private Handler mHandler;
    private final int DEFAULT_UPLOAD_ROBOT_INFO_RATE  = 10*1000;    //10秒
    private boolean isSendDataThroughWS = true;   //数据走信令通道
    private final Handler uiThreadHandler;

    private ArrayDeque qaQueue = new ArrayDeque();    //qa
    private ArrayDeque infoQueue = new ArrayDeque();  //所有收到的指令
    private final int MAX_QA_QUEUE_SIZE = 10;
    private final int MAX_INFO_QUEUE_SIZE = 20;

    public CommandEngine(){
        mHandler = new Handler();
        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void setCmdEventListener(CmdEventListener eventListener){
        cmdEventListener = eventListener;
    }

    /*
     * 设置是否上传robot信息 （电量、连接状态、asr状态等）
     */
    public void setUploadRobotInfoEnable(boolean enable){
        PreferenceUtils.setPrefBoolean(BaseConstants.PRE_KEY_MONITOR_ROBOT_INFO_ENABLE,enable);
    }

    /*
     * 设置上传robot信息的频度，单位（秒）
     */
    public void setUploadRobotInfoRate(int second){
        PreferenceUtils.setPrefInt(BaseConstants.PRE_KEY_MONITOR_ROBOT_INFO_RATE,second);
    }

    /*
     设置指令走信令通道
     */
    public void setSendDataThroughWS(boolean flag){
        isSendDataThroughWS = flag;
    }

    public boolean isSendDataThroughWS(){
        return isSendDataThroughWS;
    }


    private Runnable uploadRobotInfoRunnable = new Runnable() {
        @Override
        public void run() {

            JSONObject info = cmdEventListener.robotInfo();
            LogUtils.i(TAG,"Upload Robot info :"+info);
            reportRobotInfo(info);

            int second = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_MONITOR_ROBOT_INFO_RATE,DEFAULT_UPLOAD_ROBOT_INFO_RATE);
            mHandler.postDelayed(uploadRobotInfoRunnable,second*1000);
        }
    };

    public void startGetRobotInfo(){

        boolean mEnable = PreferenceUtils.getPrefBoolean(BaseConstants.PRE_KEY_MONITOR_ROBOT_INFO_ENABLE,false);
        if (mEnable) {
            mHandler.removeCallbacks(uploadRobotInfoRunnable);
            int second = PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_MONITOR_ROBOT_INFO_RATE,DEFAULT_UPLOAD_ROBOT_INFO_RATE);
            mHandler.postDelayed(uploadRobotInfoRunnable,1000);
        }
    }

    public void stopGetRobotInfo(){
        mHandler.removeCallbacks(uploadRobotInfoRunnable);
    }

    public void sendMessage(final String msg){
        LogUtils.i(TAG,"Ready to send message:"+msg);
        try {
            CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();
            if (isSendDataThroughWS){
                JSONObject object = CDGenerator.createSpeakJSON(msg);
                manager.sendInfo(object);
            }

        }catch (Exception e){
            LogUtils.i(TAG,"Send message: "+msg+" error: "+e.getLocalizedMessage());
        }finally {

        }
    }

    public void sendData(final JSONObject object){
        try {

            CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();

            if (isSendDataThroughWS){
                String type = object.getString("type");
                if (!type.equals("reportSensor") && !type.equals("reportIMU")){
                    JSONObject dataJSON = CDGenerator.generateJSON(object);
                    manager.sendInfo(dataJSON);
                }
            }

        }catch (Exception e){

        }finally {

        }
    }


    public void sendNaviInfo(JSONObject infoJson, String type){

        try {

            CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();

            if (isSendDataThroughWS){
                JSONObject dataJSON = null;
                if (type.equalsIgnoreCase("startNavi")){

                    dataJSON = CDGenerator.createStartNaviJSON(infoJson);

                } else if(type.equalsIgnoreCase("stopNavi")){

                    dataJSON = CDGenerator.createStopNaviJSON(infoJson);

                } else if(type.equalsIgnoreCase("naviInfo")){

                    dataJSON = CDGenerator.createNaviInfoJSON(infoJson);

                } else if (type.equalsIgnoreCase("updateNavi")){

                    dataJSON = CDGenerator.generateJSON(infoJson,"updateNavi");

                } else {
                    return;
                }

                manager.sendInfo(dataJSON);
            }

        }catch (Exception e){

        }finally {

        }
    }

    //pepper摄像头拍的照 分割上传，每一块大小
    private int PICTURE_PER_SECTION_LEN = 20*1024;   //20KB

    /**
     * 上传pepper摄像头抓拍的图片
     * @param picBytes
     */
    public void sendPicture(final  byte[] picBytes){
        sendOnePicture(picBytes,-1,"webCamStream");
    }

    /**
     * 发送一组图片  用于pepper抠图
     * @param pics 图片二进制数据list
     */
    public void sendPictureList(List<byte[]> pics){

        for (int i=0; i < pics.size(); i++){
            byte[] picData = pics.get(i);
            if (i==(pics.size()-1)){
                sendOnePicture(picData,2,"faceStream");
            } else if (i == 0){
                sendOnePicture(picData,0,"faceStream");
            } else {
                sendOnePicture(picData,1,"faceStream");
            }
        }
    }

    /**
     *
     * @param picBytes
     * @param picIndex  图片在数组中的index， 第一张0 最后一张2 中间张 1
     * @param cmdType
     */
    private void sendOnePicture(final byte[] picBytes, int picIndex, String cmdType){
        try {
            LogUtils.i(TAG,"SendPicture entry! cmd:"+cmdType);
            if (null == picBytes){
                LogUtils.i(TAG,"Pic is null ,return");
                return;
            }
            LogUtils.i(TAG,"Pic size:"+ picBytes.length + " picIndex:"+picIndex);
            CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();
            PeerConnectionClient pcc = manager.getPeerConnection();
            if (null==pcc || !pcc.isConnected()){
                //LogUtils.i(TAG,"PeerConnection is disconnected,can not send pic");
                //return;
            }

            String picString  = Base64Util.encodeToString(picBytes);
            LogUtils.i(TAG,"Encoded pic string length:"+picString.length());

            long resLen = picString.length()%PICTURE_PER_SECTION_LEN;
            int blockNum = (int) (picString.length()/PICTURE_PER_SECTION_LEN) + (picString.length()%PICTURE_PER_SECTION_LEN>0?1:0);

            for (int i=0;i<blockNum;i++){

                JSONObject json = new JSONObject();
                json.put("type",cmdType);
                json.put("seq", CDGenerator.generateSeqNumber());
                json.put("sid", SessionMonitor.mSessionId);
                if (picIndex>=0){
                    json.put("streamFlag",picIndex);
                }

                JSONObject dataJson = new JSONObject();
                int index = (int) (i*PICTURE_PER_SECTION_LEN);

                if (i == blockNum-1){

                    dataJson.put("flag",2);  //段尾
                    dataJson.put("image",picString.substring(index,picString.length()));

                } else if(i==0) {

                    dataJson.put("flag",0); //段首
                    dataJson.put("image",picString.substring(index,PICTURE_PER_SECTION_LEN));

                } else {

                    dataJson.put("flag",1); //段中
                    dataJson.put("image",picString.substring(index,index+PICTURE_PER_SECTION_LEN));
                }

                LogUtils.d(TAG,"send picture with StreamFlag:"+picIndex+" flag:"+dataJson.getString("flag") + " seq:"+json.getString("seq"));

                json.put("data",dataJson);
                String jsonString = json.toString();


                pushMessage(jsonString);
//                byte[] string = jsonString.getBytes();
//                ByteBuffer buffer = ByteBuffer.allocate(string.length);
//                buffer.put(string);
//                buffer.flip();
//
//
//                if (null!=pcc && pcc.isConnected()){
//
//                    pcc.sendData(buffer);
//                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void reportRobotInfo(JSONObject info){

        try {
            CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();
            if (isSendDataThroughWS){
                JSONObject dataJSON = CDGenerator.createReportJSON(info);
                manager.sendInfo(dataJSON);
                return;
            }

        }catch (Exception e){

        }finally {

        }
    }


    static  long lastCmdTime = -1;
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onHariEvent(CmdEvent event) {
        //Log.d("VoiceReg",(String) event.getInfo());

        if (null == cmdEventListener){
            return;
        }

        CmdEvent.Event eventType = event.getEvent();

        switch (eventType) {
            case SPEAK_RECEIVED: {    //信令通道

                String content = (String) event.getInfo();
                cmdEventListener.onSpeak("SPEAK_RECEIVED",content,"");
                break;
            }
            case INFO_RECEIVED: { //信令通道

//                String info = (String) event.getInfo();
//                cmdEventListener.onInfo(info);
                CDPData  data = (CDPData) event.getInfo();
                handleReceivedChannelData(data);
                break;
            }
            case PUSH_RECEIVED:{
                String message = (String)event.getInfo();
                handlePushMsg(message);
                LogUtils.d(TAG,"push msg:"+message);
            }

        }
    }

    private void  handleReceivedChannelData(CDPData data){

        CDPEntity body = data.getBodyList().get(0);
        String info = body.getBody();
        String type = body.getType();
        //先判断是否为重发
        //若为重发 client已经接到了 直接返回response
        //client若未接到过，继续往上层抛，返回response
        try {
            JSONObject infoObj = new JSONObject(info);
            if (infoObj.has("infoId")){
                String infoId = infoObj.getString("infoId");
                boolean hasReceived = false;
                if (infoObj.has("isRetry")){
                    if (infoQueue.contains(infoId)){
                        hasReceived = true;
                    }
                }

                sendInfoResponse(infoId,type);

                if (hasReceived) return;

                if (infoQueue.size() == MAX_INFO_QUEUE_SIZE){
                    infoQueue.removeFirst();
                }
                infoQueue.addLast(infoId);
            }
        } catch (JSONException e){
            LogUtils.i(TAG,e.getLocalizedMessage());
        }

        if (type.equalsIgnoreCase("qa")){
            String string = body.getBody();
            try {
                JSONObject jsonObject = new JSONObject(string);
                JSONObject dataObj = jsonObject.getJSONObject("data");
                JSONArray answerArray = dataObj.getJSONArray("answer");
                JSONObject answerObj = answerArray.getJSONObject(0);

                String text = answerObj.getString("text");
                String operation = "";
                if (!answerObj.isNull("operation")){
                    operation = answerObj.getString("operation");
                }

                //读取questionId
                String questionId = "";
                if (dataObj.has("question")){
                    JSONObject questionObj = dataObj.getJSONObject("question");
                    questionId = questionObj.getString("questionId");
                }


                boolean isNeedSpeak = true;
                if (dataObj.has("isRetry")){   //坐席侧未收到response，尝试再次发送
                    if (qaQueue.contains(questionId+text)){
                        isNeedSpeak = false;
                    }
                }

                if (isNeedSpeak){
                    cmdEventListener.onSpeak("qa",text,operation);

                    if (qaQueue.size() == MAX_QA_QUEUE_SIZE){
                        qaQueue.removeFirst();
                    }
                    qaQueue.addLast(questionId+text);
                }

                //cmd response
                if (null != dataObj.get("uuid")){
                    String  uuid = dataObj.getString("uuid");
                    sendCmdResponse(uuid);
                }

            }catch (Exception e){
                LogUtils.i(TAG,e.getLocalizedMessage());
            }
        } else if (type.equalsIgnoreCase("takePicture")){
            LogUtils.i("Ready to take picture");
            takePicture(null);

        } else if (type.equalsIgnoreCase("speak")||type.equalsIgnoreCase("speakOr")){
            String string = body.getBody();
            try {
                JSONObject jsonObject = new JSONObject(string);
                JSONObject dataObj = jsonObject.getJSONObject("data");
                String text = dataObj.getString("text");
                //String operation = dataObj.getString("operation");
                cmdEventListener.onSpeak(type,text,"");

            }catch (Exception e){
                LogUtils.i(TAG,e.getLocalizedMessage());
            }
        } else if (type.equalsIgnoreCase("openVideo")){

            HariServiceClient.getCallEngine().startVideoSource();

        } else if (type.equalsIgnoreCase("pauseVideo")){

            HariServiceClient.getCallEngine().stopVideoSource();

        } else if (type.equalsIgnoreCase("mediaReconnect")){
            HariServiceClient.getCallEngine().setCallMode(CallEngine.CALL_MODE_MEDIA);
            HariServiceClient.getCallEngine().restartCall();

        } else {

            String string = body.getBody();
            cmdEventListener.onInfo(string);

            //cmd response
            try {
                JSONObject jsonObject = new JSONObject(string);
                JSONObject dataObj = jsonObject.getJSONObject("data");
                if (null != dataObj.get("uuid")){
                    String  uuid = dataObj.getString("uuid");
                    sendCmdResponse(uuid);
                }
            }catch (Exception e){
                LogUtils.i(TAG,e.getLocalizedMessage());
            }

        }
    }

    /**
     * 解析push message
     * @param message
     */
    public void handlePushMsg(String message){
        try {

            JSONObject object = new JSONObject(message);
            String sid = object.getString("sid");
            if (!sid.equalsIgnoreCase(SessionMonitor.mSessionId)){
                LogUtils.i(TAG,"Push msg sid "+sid+" is not match current session id:"+SessionMonitor.mSessionId);
                return;
            }

            JSONObject msgObjecct = object.getJSONObject("message");
            cmdEventListener.onInfo(msgObjecct.toString());

        } catch (JSONException e){
            LogUtils.i(TAG,"Parse push msg error:"+e.getMessage());
        }
    }

    public void pushMessage(String message){
        try {
            if (message.isEmpty()){
                LogUtils.i(TAG,"Message is null");
                return;
            }
            PushClient pushClient = PushClient.getInstance();
            if (null != pushClient){
                pushClient.publish("faceStream",message);
            }
        }catch (Exception e){
            LogUtils.i(TAG,"Push message error:"+e.getMessage());
        }
    }


    /**
     * 测试使用
     * @param callback
     */
    public void takePicture(final PictureCallback callback){
        CallManager manager = HariUtils.getInstance().getHariServiceConnector().getHariService().getCallManager();
        PeerConnectionClient pcc = manager.getPeerConnection();
        if (null != pcc ){
            pcc.takePicture(new VideoCapturer.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, final int width, final int height, final int rotation, long timeStamp) {
                    LogUtils.i(TAG,"Picture taken length:"+data.length);
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != callback){
                                callback.onPictureTaken(data,width,height,rotation);
                            }
                            //savePicture(data,width,height,rotation);
                        }
                    });

                }
            });
        }
    }

    /**
     * QA Response
     * @param uuid
     */
    private void sendCmdResponse(String uuid){

        JSONObject json = new JSONObject();
        try {
            json.put("type","cmdResponse");
            json.put("seq", CDGenerator.generateSeqNumber());
            JSONObject jsonData = new JSONObject();
            jsonData.put("uuid",uuid);
            json.put("data",jsonData);
        }catch (Exception e){
            e.printStackTrace();
        }

        sendData(json);
    }

    /**
     * info response
     * @param infoId
     */
    private void sendInfoResponse(String infoId,String type){
        JSONObject json = new JSONObject();
        try {
            json.put("id","infoResponse");
            json.put("seq", CDGenerator.generateSeqNumber());
            json.put("infoId",infoId);
            json.put("type",type);
        }catch (Exception e){
            e.printStackTrace();
        }

        sendData(json);
    }

    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.PostThread)
    public void onSessionEvent(SessionEvent event) {
        SessionEvent.Event eventType = event.getEvent();
        switch (eventType){

            case SESSION_CONNECTED:{
//                startGetRobotInfo();
//                LogUtils.i(TAG,"On session_connected event reveived, Start report Robot status");
                break;
            }

            case SESSION_SERVER_CONNECTED:{
                startGetRobotInfo();
                LogUtils.i(TAG,"On WS connected event reveived, Start report Robot status");
                break;
            }

            case SESSION_CHANNEL_CONNECTION_ERROR:{
                EventBus.getDefault().post(new CallEvent(CallEvent.Event.SIGNAL_CONNECTION_ERR,CallEvent.CODE_SIGNAL_ELSE_ERROR, event.getInfo()));
                break;
            }

            case SESSION_CHANNEL_CLOSE:{
                break;
            }

            case SESSION_WS_CLOSED:{
                stopGetRobotInfo();
                LogUtils.i(TAG,"On session_closed event reveived, Stop Report Robot status");
                break;
            }
        }
    }

    /**  test
     * int32转换为二进制（4个字节）
     * @param i 待转换的整数
     * @return 返回4字节二进制数
     */
    public byte[] int2byte(int i){
        byte[] res = new byte[4];
        res[3] = (byte)i;
        res[2] = (byte)(i>>>8);
        res[1] = (byte)(i>>>16);
        res[0] = (byte)(i>>>24);
        return res;
    }

    /**
     * 用来测试从webrtc中获取图片的功能，图片存储本地
     * @param data
     * @param width
     * @param height
     * @param rotation
     */
    private void savePicture(byte[] data, int width, int height, int rotation){
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0,width, height), 80, baos);

        Bitmap bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);

        String fileName = Environment.getExternalStorageDirectory().toString()
                + File.separator
                +"AppTest"
                +File.separator
                +"PicTest_"+System.currentTimeMillis()+".jpg";
        File file = new File(fileName);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdir();//创建文件夹
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);//向缓冲区压缩图片
            bos.flush();
            bos.close();

            LogUtils.i(TAG,"Save photo successful  path:"+fileName);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
    }
}
