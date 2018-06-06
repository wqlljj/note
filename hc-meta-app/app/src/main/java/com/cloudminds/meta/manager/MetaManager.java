package com.cloudminds.meta.manager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.LooperExecutor;
import com.cloudminds.hc.metalib.bean.AxisData;
import com.cloudminds.hc.metalib.bean.BatteryData;
import com.cloudminds.hc.metalib.bean.IoData;
import com.cloudminds.hc.metalib.bean.UltrasonicData;
import com.cloudminds.hc.metalib.manager.MetaSensorManager;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.bean.NaviBean;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.service.HubServiceConnector;
import com.cloudminds.meta.util.DateUtil;
import com.cloudminds.meta.util.NaviCommandParse;
import com.cloudminds.meta.util.TTSSpeaker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SX on 2017/5/24.
 */

public class MetaManager {
    private static MetaManager metaManager;
    private static String TAG="MetaManager";
    private static LooperExecutor executor = new LooperExecutor();
    private static ExecutorService singleThread = Executors.newSingleThreadExecutor();
    private static Handler metaHandler=null;
    private HandlerThread metaThread;
    private boolean isInited=false;
    private static Context mContext;
    private static HubServiceConnector hubServiceConnector;
    private static final String NAVI_CMD_ANGLE="rotate";
    public final String NAVI_CMD_FORWARD = "F";
    public final String NAVI_CMD_BACKWARD = "stop";
    public final String NAVI_CMD_PAD_LEFT = "L";
    public final String NAVI_CMD_PAD_RIGHT = "R";
    public final String NAVI_CMD_TERMINATE = "E";

    public static final int NAVICOMMAND_HI=0;
    public static final int NAVICOMMAND_AI=1;


    public  String NAVI_CMD_FORWARD_TIPS ;
    public  String NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX ;
    public  String NAVI_CMD_TERMINATE_TIPS ;
    public  String NAVI_CMD_BACKWARD_TIPS ;
    public  String NAVI_CMD_PAD_LEFT_TIPS ;
    public  String NAVI_CMD_PAD_RIGHT_TIPS ;
    public  String NAVI_CMD_TURN_RIGHT_TIPS ;
    public  String NAVI_CMD_TURN_LEFT_TIPS ;
    public  String NAVI_CMD_DEGREE_TIPS ;
    public  String NAVI_CMD_TURN_BACK_TIPS ;
    private String NAVI_CMD_TURN_LEFT_FORWARD_TIPS;
    private String NAVI_CMD_TURN_LEFT_BACK_TIPS;
    private String NAVI_CMD_TURN_RIGHT_FORWARD_TIPS;
    private String NAVI_CMD_TURN_RIGHT_BACK_TIPS;
    final int INVALID_ANGLE = 360;
    final String NAV_CMD_SEPERATOR = ":";

    private int SENSOR_LIB_GUIDE_CMD_STOP = 0;
    public final int SENSOR_LIB_GUIDE_CMD_START = 1;
    public final int SENSOR_LIB_GUIDE_CMD_LEFT = 2;
    public final int SENSOR_LIB_GUIDE_CMD_RIGHT = 3;
    public final int SENSOR_LIB_GUIDE_CMD_KEEPALIVE = 4;
    public final int SENSOR_LIB_GUIDE_CMD_UNKNOWN = 5;
    private boolean flag;
    private int command_time=1000;

    public MetaManager() {
    }
    public static MetaManager getInstance(){
        if(metaManager==null)
        metaManager = new MetaManager();
        return metaManager;
    }
    public void initWithContext(Context context){
        if(!isInited ) {
            isInited = true;
            mContext=context;
            initRes();
            hubServiceConnector = HubServiceConnector.getIntance(mContext);
            metaThread = new HandlerThread(TAG);
            metaThread.setPriority(Thread.MAX_PRIORITY);
            metaThread.start();
            metaHandler=new Handler(metaThread.getLooper());
            executor.requestStart();
        }
    }

    public void initRes() {
        NAVI_CMD_FORWARD_TIPS = mContext.getString(R.string.NAVI_CMD_FORWARD_TIPS);//前进
        NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX = mContext.getString(R.string.NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX);//前方
        NAVI_CMD_TERMINATE_TIPS = mContext.getString(R.string.NAVI_CMD_TERMINATE_TIPS);//停止导航
        NAVI_CMD_BACKWARD_TIPS = mContext.getString(R.string.NAVI_CMD_BACKWARD_TIPS);//停
        NAVI_CMD_PAD_LEFT_TIPS = mContext.getString(R.string.NAVI_CMD_PAD_LEFT_TIPS);//左跨一步
        NAVI_CMD_PAD_RIGHT_TIPS = mContext.getString(R.string.NAVI_CMD_PAD_RIGHT_TIPS);//右跨一步
        NAVI_CMD_TURN_RIGHT_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_TIPS);//右转
        NAVI_CMD_TURN_LEFT_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_LEFT_TIPS);//左转
        NAVI_CMD_DEGREE_TIPS = mContext.getString(R.string.NAVI_CMD_DEGREE_TIPS);//度
        NAVI_CMD_TURN_BACK_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_BACK_TIPS);//向后转
        NAVI_CMD_TURN_LEFT_FORWARD_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_LEFT_FORWARD_TIPS);//向左前方转
        NAVI_CMD_TURN_LEFT_BACK_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_LEFT_BACK_TIPS);//向左后方转
        NAVI_CMD_TURN_RIGHT_FORWARD_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_FORWARD_TIPS);//向右前方转
        NAVI_CMD_TURN_RIGHT_BACK_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_BACK_TIPS);//向右后方转
    }

    static Thread thread=new Thread(new Runnable() {

        @Override
        public void run() {
            while(MetaManager.metaManager.flag){
                String poll = metaDatas.poll();
                if(metaDatas.size()>0&& InternetBroadcast.hasConnectivity&&!TextUtils.isEmpty(poll)){
                    hubServiceConnector.sendData(poll);
                }
            }
        }
    });
   static Queue<String> metaDatas=new LinkedList<>();
    private static long DELAY_INTERVAL=1000;

    private static final Runnable reportSessorDataCb =new Runnable() {
        @Override
        public void run() {
//            String ret = MetaManager.metaManager.getSensorInfo();
//            Log.e(TAG, "run: reportSessorDataCb"+(TextUtils.isEmpty(ret)?"null":ret));
//            //TODO: to sync between metaHandler sender thread and libsensor capture thread? 建议将Sensor 数据发送线程分开
//            if(!TextUtils.isEmpty(ret)) {
//                if(metaDatas.size()>900)metaDatas.poll();
//                metaDatas.offer(ret);
//            }

            if (metaHandler != null)
                metaHandler.postDelayed(reportSessorDataCb, DELAY_INTERVAL);

            String ret = MetaManager.metaManager.getSensorInfo();
                    Log.e(TAG, "run: reportSessorDataCb"+(TextUtils.isEmpty(ret)?"null":ret));
                    //TODO: to sync between metaHandler sender thread and libsensor capture thread? 建议将Sensor 数据发送线程分开
                    if(!TextUtils.isEmpty(ret))
                        hubServiceConnector.sendData(ret);
        }

    };
    private  FileOutputStream fileOutputStream;
    private synchronized void writeIMU(final String ret){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    fileOutputStream.write(ret.getBytes());
                    fileOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void sendUltrasound(boolean flag){
        this.flag = flag;
        if(this.flag) {
            try {
                fileOutputStream=new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"IMU"+DateUtil.dateToFormatString(System.currentTimeMillis())+".txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //thread.start();
            startSendIMU();
            metaHandler.postDelayed(reportSessorDataCb, DELAY_INTERVAL);
        }else{
            stopSendIMU();
            metaHandler.removeCallbacks(reportSessorDataCb);
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int guideCmd = SENSOR_LIB_GUIDE_CMD_STOP;
            HCMetaUtils.sendLibSensorGuideCommand(guideCmd);
            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,mContext.getString(R.string.navigation_end));
        }
    }
    public void handleMotionCommand(JSONObject data,int type){
        NaviBean naviBean = NaviCommandParse.CommandParse(data);
        if(naviBean==null){
           MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,data.toString());
        }else {
            metaManager.handleMetaCommand(naviBean,type);
        }
    }
    public void handleMetaCommand(final NaviBean naviBean,final int type) {
        LogUtils.e("handleMetaCommand: "+naviBean.getType()+" "+naviBean.getAngle()+"   "+naviBean.getDistance() );
        metaHandler.post(new Runnable() {
            @Override
            public void run() {
                metaManager.handleNaviCommand(naviBean,type);
            }
        });
    }
    Long lastTime=0l;
    NaviBean lastNavi=new NaviBean("",-1,-1);
    private   void handleNaviCommand(NaviBean naviBean, int navi_type){
        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,naviBean.getType()+":"+naviBean.getAngle()+"  "+naviBean.getDistance());
        String type = naviBean.getType();
        int distance = naviBean.getDistance();
        int angle = naviBean.getAngle();
        int direct=-1;
        if(lastNavi.getType().equals(naviBean.getType())&&navi_type==NAVICOMMAND_AI){
            switch (naviBean.getType()){
                case "rotate":
                     direct = parseDirect(angle);
                    if(lastNavi.getAngle()==direct&&System.currentTimeMillis()-lastTime< command_time) {
                        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"指令频率高，已取消  "+(System.currentTimeMillis()-lastTime));
                        return;
                    }
                    lastNavi=naviBean;
                    lastNavi.setAngle(direct);
                    break;
                case "stop":
                    if(System.currentTimeMillis()-lastTime<command_time/2){
                        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"指令频率高，已取消  "+(System.currentTimeMillis()-lastTime));
                        return;
                    }
                    lastNavi=naviBean;
                    break;
                default://"move"
                    if(lastNavi.getAngle()==naviBean.getAngle()) {
                        switch (naviBean.getAngle()) {
                            case 0:
                                if (Math.floor(lastNavi.getDistance()) == Math.floor(naviBean.getDistance()) && System.currentTimeMillis() - lastTime < command_time){
                                    MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"指令频率高，已取消  "+(System.currentTimeMillis()-lastTime));
                                    return;
                                }
                                lastNavi = naviBean;
                                break;
                            case 90:
                            case -90:
                                if (System.currentTimeMillis() - lastTime < command_time){
                                    MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,"指令频率高，已取消  "+(System.currentTimeMillis()-lastTime));
                                    return;
                                }
                                lastNavi = naviBean;
                                break;
                        }
                    }else{
                        lastNavi = naviBean;
                    }
                    break;
            }
        }else{
            lastNavi=naviBean;
            if(naviBean.getType().equals("rotate")) {
                 direct = parseDirect(angle);
                lastNavi.setAngle(direct);
            }
        }
        lastTime=System.currentTimeMillis();
        Log.e(TAG, "handleNaviCommand in metaHandler: "+type+ "   "+distance);
        if(NAVI_CMD_ANGLE.equals(type)){
                    if(direct==0){
                        offlineSpeak(NAVI_CMD_FORWARD_TIPS,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_FORWARD_TIPS);
                    }else if(direct==1){
                        String tips = NAVI_CMD_TURN_RIGHT_FORWARD_TIPS;
                        if(angle>40)
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_FORWARD_TIPS);
                    }else if(direct==2){
                        String tips = NAVI_CMD_TURN_RIGHT_TIPS;
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_TIPS);
                    }else if(direct==3){
                        String tips = NAVI_CMD_TURN_RIGHT_BACK_TIPS;
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_BACK_TIPS);
                    }else if(direct==4){
                        String tips = NAVI_CMD_TURN_LEFT_FORWARD_TIPS;
                        if(angle<-40)
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_BACK_TIPS);
                    }else if(direct==5){
                        String tips = NAVI_CMD_TURN_LEFT_TIPS;
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_TIPS);
                    }else if(direct==6){
                        String tips = NAVI_CMD_TURN_LEFT_BACK_TIPS;
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_BACK_TIPS);
                    }else if(direct==7){
                        String tips = NAVI_CMD_TURN_BACK_TIPS;
                        offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                        Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_BACK_TIPS);
                    }
                    if(direct!=-1&&angle!=0) {
                        HCMetaUtils.setLibSensorNaviExtend(angle);
                    }
        }else if(NAVI_CMD_BACKWARD.equals(type)){//action=stop
            int guideCmd = SENSOR_LIB_GUIDE_CMD_STOP;
            offlineSpeak(NAVI_CMD_BACKWARD_TIPS,MetaSensorManager.TYPE_HIGH);
            HCMetaUtils.sendLibSensorGuideCommand(guideCmd);
        }else{//action="move"
            int guideCmd = SENSOR_LIB_GUIDE_CMD_UNKNOWN;
            if(angle==0){
                guideCmd = SENSOR_LIB_GUIDE_CMD_START;
                if(distance<1 || distance == INVALID_ANGLE)
                    offlineSpeak(NAVI_CMD_FORWARD_TIPS,MetaSensorManager.TYPE_NAVIGATION);
                else{
                    String num="";
                    if(isZh()){
                        num+=distance;
                    }else{
                        switch (distance){
                            case 1:
                                num+="one";
                                break;
                            case 2:
                                num+="two";
                                break;
                            case 3:
                                num+="three";
                                break;
                            case 4:
                                num+="four";
                                break;
                            case 5:
                                num+="five";
                                break;
                        }
                    }
                    String tips = mContext.getResources().getString(R.string.NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX, num);
                    offlineSpeak(tips,MetaSensorManager.TYPE_NAVIGATION);
                }
            }else  if(angle==-90){
                guideCmd = SENSOR_LIB_GUIDE_CMD_LEFT;
                offlineSpeak(NAVI_CMD_PAD_LEFT_TIPS,MetaSensorManager.TYPE_NAVIGATION);
            }else if(angle==90){
                guideCmd = SENSOR_LIB_GUIDE_CMD_RIGHT;
                offlineSpeak(NAVI_CMD_PAD_RIGHT_TIPS,MetaSensorManager.TYPE_NAVIGATION);
            }
            HCMetaUtils.sendLibSensorGuideCommand(guideCmd);
        }
    }

    private int parseDirect(int angle) {
        int direct=-1;
        if(angle>=-180 && angle<=180){
            if(angle>=-10 && angle<=10){
                direct=0;
            }else if(angle>10 && angle<80){
                direct=1;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_FORWARD_TIPS);
            }else if(angle>=80 && angle<=100){
                direct=2;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_TIPS);
            }else if(angle>100 && angle<170){
                direct=3;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_RIGHT_BACK_TIPS);
            }else if(angle>-70 && angle<-10){
                direct=4;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_BACK_TIPS);
            }else if(angle>=-100 && angle<=-70){
                direct=5;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_TIPS);
            }else if(angle>-170 && angle<-100){
              direct=6;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_LEFT_BACK_TIPS);
            }else{
                direct=7;
                Log.e(TAG, "handleNaviCommand: "+NAVI_CMD_TURN_BACK_TIPS);
            }
        }
        return direct;
    }

    public static boolean isZh() {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
    private void offlineSpeak(String content, int typeNavigation) {
        switch (typeNavigation){
            case MetaSensorManager.TYPE_NAVIGATION:
                TTSSpeaker.speak(content, TTSSpeaker.NAVIINTO);
                break;
            case  MetaSensorManager.TYPE_HIGH:
                TTSSpeaker.speak(content, TTSSpeaker.HIGH);
                break;
        }
    }



    private boolean readImuEnable = true;
    private int lastSeq = -1;
    private Thread readImuThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (readImuEnable){
                final AxisData axisData = new AxisData();
                // Log.d("IMU","Before read IMU");
                long beginTime = System.currentTimeMillis();
                HCMetaUtils.getLibSensorAxisData(axisData);
                long endTime = System.currentTimeMillis();
                if (lastSeq != axisData.getSeq()){
                    if (lastSeq>=0 && (axisData.getSeq()-lastSeq != 1)){
                        Log.e("IMU-LOST","--- --- ---- 丢包");
                    }
                    lastSeq = axisData.getSeq();
                    sendIMU(axisData);
                }
                Log.d("IMU-1","IMU data SEQ:"+lastSeq + "    耗时:"+(endTime-beginTime));
                try {
                    Thread.sleep(8);
                }catch (InterruptedException e){

                }
            }

        }
    });

    private void  sendIMU(final AxisData axisData){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "reportIMU");
                    JSONObject data = new JSONObject();
                    JSONObject axisJson = new JSONObject();
                    int seq = axisData.getSeq();

                    axisJson.put("SeqNumber", seq);
                    axisJson.put("AcceleratorX", axisData.getAcc_x());
                    axisJson.put("AcceleratorY", axisData.getAcc_y());
                    axisJson.put("AcceleratorZ", axisData.getAcc_z());
                    axisJson.put("GyroscopeX", axisData.getGyro_x());
                    axisJson.put("GyroscopeY", axisData.getGyro_y());
                    axisJson.put("GyroscopeZ", axisData.getGyro_z());
                    axisJson.put("CompassX", axisData.getComp_x());
                    axisJson.put("CompassY", axisData.getComp_y());
                    axisJson.put("CompassZ", axisData.getComp_z());
                    data.put("Axes", axisJson);

                    json.put("data",data);

                }catch (JSONException e){
                    e.printStackTrace();
                }

                HariServiceClient.getCommandEngine().sendData(json);

                Log.d("IMU-2","send imu data:"+lastSeq );
            }
        });
    }

    public void startSendIMU(){
        readImuEnable = true;
        readImuThread.start();
    }

    public void stopSendIMU(){
        readImuEnable = false;
    }

    public float getBattery(){
        final BatteryData batteryData = new BatteryData();
        HCMetaUtils.getLibSensorBatteryData(batteryData);
        Log.e(TAG, "getBattery: "+batteryData.toString() );
        return batteryData.getResidue_capacity() / batteryData.getCapacity()*100;
    }
    /*
     * 获取超声数据
     */
    private int SeqNumber=-1;
    public   String getSensorInfo(){

        String ret = null;
        try {
            //final AxisData axisData = new AxisData();
            final BatteryData batteryData = new BatteryData();
            final IoData ioData = new IoData();
            ArrayList<UltrasonicData> ultrasonicDatas=new ArrayList<>();
            ultrasonicDatas.add(new UltrasonicData());
            ultrasonicDatas.add(new UltrasonicData());
            ultrasonicDatas.add(new UltrasonicData());
           // HCMetaUtils.getLibSensorAxisData(axisData);
           // Log.i(TAG, "axisData="+axisData.toString());
            HCMetaUtils.getLibSensorBatteryData(batteryData);
            Log.i(TAG, "batteryData="+batteryData.toString());
            for (int i = 0; i < 3; i++) {
                HCMetaUtils.getLibSensorUltrasonicData(ultrasonicDatas.get(i));
                Log.i(TAG, i+"   ultrasonicData="+ultrasonicDatas.get(i).toString());
                Thread.sleep(100);
            }

            JSONObject json = new JSONObject();
            try {
                json.put("type", "reportSensor");
                JSONObject data = new JSONObject();
//                JSONObject axisJson = new JSONObject();
//                int seq = axisData.getSeq();
//                synchronized (MetaManager.class){
//                    if (seq == SeqNumber) return null;
//                    SeqNumber = seq;
//                }
//                axisJson.put("SeqNumber", seq);
//                axisJson.put("AcceleratorX", axisData.getAcc_x());
//                axisJson.put("AcceleratorY", axisData.getAcc_y());
//                axisJson.put("AcceleratorZ", axisData.getAcc_z());
//                axisJson.put("GyroscopeX", axisData.getGyro_x());
//                axisJson.put("GyroscopeY", axisData.getGyro_y());
//                axisJson.put("GyroscopeZ", axisData.getGyro_z());
//                axisJson.put("CompassX", axisData.getComp_x());
//                axisJson.put("CompassY", axisData.getComp_y());
//                axisJson.put("CompassZ", axisData.getComp_z());
//                data.put("Axes", axisJson);

                JSONObject ultraJson = new JSONObject();
                ultraJson.put("SeqNumber", ultrasonicDatas.get(0).getSeq());
                JSONArray distanceArray = new JSONArray();

                if (ultrasonicDatas.get(0).getDistances().length < 6) {
                    int sumd = 0;
                    for (Float distance : ultrasonicDatas.get(0).getDistances()) {
                        sumd += distance.intValue();
                        distanceArray.put(distance);
                    }
                    int gap = 6 - ultrasonicDatas.get(0).getDistances().length;
                    for (int i = 0; i < gap; i++) {
                        if (sumd == 0) {
                            distanceArray.put(0);
                        } else {
                            distanceArray.put(50);
                        }
                    }
                } else if (ultrasonicDatas.get(0).getDistances().length == 6) {
                    int index = ultrasonicDatas.get(0).getDistances().length / 2;
                    for (; index < ultrasonicDatas.get(0).getDistances().length; index++) {
                        float distance = ultrasonicDatas.get(0).getDistances()[index];
                        distanceArray.put(distance);
                    }
                    for (index = 0; index < ultrasonicDatas.get(0).getDistances().length / 2; index++) {
                        float distance = ultrasonicDatas.get(0).getDistances()[index];
                        if(index==0){
                            distance=-1;
                            ArrayList<Float> distances=new ArrayList<>();
                            for (int i = 0; i < 3; i++) {
                                float temp = ultrasonicDatas.get(i).getDistances()[index];
                                if(temp<0.01)continue;
                                distances.add(temp);
                            }
                            distance=average(distances);
                            if(distance==-1)return null;
                        }
                        distanceArray.put(distance);
                    }
                }
                ultraJson.put("Distance", distanceArray);
                data.put("Ultrasonic", ultraJson);

                JSONObject batteJson = new JSONObject();
                batteJson.put("SeqNumber", batteryData.getSeq());
                batteJson.put("Capacity", batteryData.getCapacity());
                batteJson.put("ResidueCapacity", batteryData.getResidue_capacity());
                batteJson.put("Voltage", batteryData.getVoltage());
                batteJson.put("Current", batteryData.getCurrent());
                batteJson.put("Status", batteryData.getStatus());
                data.put("Battery", batteJson);

                JSONObject ioJson = new JSONObject();
                ioJson.put("SeqNumber", ioData.getSeq());
                ioJson.put("IOStatus", ioData.getIo_status());
                data.put("IO", ioJson);

                SimpleDateFormat formatter = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate =  new Date(System.currentTimeMillis());
                String  timeString = formatter.format(curDate);
                data.put("CollectTime", timeString);
                json.put("data",data);
                ret = json.toString();
//                writeIMU(System.currentTimeMillis()+","+axisData.getSeq()+","+axisData.getAcc_x()+","+axisData.getAcc_y()+","+axisData.getAcc_z()+","+"\r\n");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG,e.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG,e.getLocalizedMessage());
        }
        Log.e(TAG, "sensorInfo:   ret = "+(TextUtils.isEmpty(ret)?"null":ret) +"   "+executor.checkOnLooperThread());
        return ret;
    }

    private float average(ArrayList<Float> distances) {
        switch (distances.size()){
            case 1:
                return distances.get(0);
            case 2:
//                if(Math.abs(distances.get(0)-distances.get(1))<0.5)
                return (distances.get(0)+distances.get(1))/2;
            case 3:
                float[] floats=new float[3];
                for (int i = 0; i < distances.size(); i++) {
                    floats[i]=distances.get(i);
                }
                Arrays.sort(floats);
                float a = floats[1] - floats[0];
                float b = floats[2] - floats[1];
                if(a<0.5&&b<0.5)return (floats[0]+floats[1]+floats[2])/3;
                else if(a<0.5) return (floats[1] + floats[0])/2;
                else if(b<0.5) return (floats[2] + floats[1])/2;
                else return floats[1];
        }
        return -1;
    }

    public static class LogUtils {
        public static void e(String msg){
                Log.e(TAG, msg );
        }
    }
}
