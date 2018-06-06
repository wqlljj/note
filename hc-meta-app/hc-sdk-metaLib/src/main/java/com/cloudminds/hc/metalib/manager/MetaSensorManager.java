package com.cloudminds.hc.metalib.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.LooperExecutor;
import com.cloudminds.hc.metalib.MetaBaseContants;
import com.cloudminds.hc.metalib.PreferenceUtils;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.ThreadUtils;
import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.hc.metalib.bean.AlarmData;
import com.cloudminds.hc.metalib.bean.AxisData;
import com.cloudminds.hc.metalib.bean.BatteryData;
import com.cloudminds.hc.metalib.bean.DeviceInfo;
import com.cloudminds.hc.metalib.bean.IoData;
import com.cloudminds.hc.metalib.bean.UltrasonicData;
import com.cloudminds.hc.metalib.bean.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import static com.cloudminds.hc.metalib.BaseData.IMEI_NO;

/**
 * Created by SX on 2017/1/9.
 */
public class MetaSensorManager implements USBUtils.MetaHotSwapListener {
    private static final String TAG = "smartSensorManager";
    private static MetaSensorManager metaSensorManager;
    private static boolean need=true;
    private static boolean maybe=false;
    // private HandlerThread sensorThread = null;
    private Context mContext = null;
   // private static Handler sensorHandler = null;
    private static Handler metaHandler=null;
    public static final int DELAY_INTERVAL = 500;
    final int INVALID_ANGLE = 360;
    private static LooperExecutor executor = new LooperExecutor();
    private static final HashMap<String,BatteryListener> listeners=new HashMap<>();

    private  static boolean isInited = false;
    private HandlerThread metaThread;

    public static final int TYPE_NAVIGATION=0;
    public static final int TYPE_HIGH=1;

    public  native String getversion(Version version);
    public  native String getAxisData(AxisData axisData);
    public  native String getUltrasonicData(UltrasonicData ultrasonicData);
//    public  native String getIoData(IoData ioData);
    public  native String getBatteryData(BatteryData batteryData);
    public  native int setAxesHz(float hz);
    public  native int setUltrasonicHz(float hz);

    public  native float getAxesHz();
    public  native float getUltrasonicHz();
    public  native String sensorsInit();
    public  native String sensorsExit();

    public  native int getDeviceInfo(DeviceInfo info);
    public  native int getUpdateStatus();
    public  native int updateFirmwareAsync(byte[] buf);

//	错误值是小于0的，比如-RET_NO_DEV
    public  native int setNaviExtend(int ori);
    public  native int sendGuideCommand(int cmd);
    public void keyEventCallback(char code , char status)
    {
        Log.e(TAG, "keyEventCallback: "+code+"   "+status );
        Set<String> keys = listeners.keySet();
        for (String key : keys) {
            listeners.get(key).keyEventCallback((""+code).getBytes()[0],(""+status).getBytes()[0]);
        }
    }

    //左转30 A:-30， 右转30 A:30  前进1米 F:1 ，停止： B  左跨一步: L 右跨一步: R
    public final String NAVI_CMD_ANGLE = "";
    public final String NAVI_CMD_FORWARD = "F";
    public final String NAVI_CMD_BACKWARD = "B";
    public final String NAVI_CMD_PAD_LEFT = "L";
    public final String NAVI_CMD_PAD_RIGHT = "R";
    public final String NAVI_CMD_TERMINATE = "E";

    public  String NAVI_CMD_FORWARD_TIPS ;
    public  String NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX ;
    public  String NAVI_CMD_FORWARD_DISTANCE_TIPS_SUFFIX ;
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

    final String NAV_CMD_SEPERATOR = ":";

    public final int SENSOR_LIB_SUCCESS = 0;
    public final int SENSOR_LIB_NO_DEV = 1;
    public final int SENSOR_LIB_DEV_BUSY = 2;
    public final int SENSOR_LIB_NOT_INIT = 3;
    public final int SENSOR_LIB_INV_ARGS = 4;
    public final int SENSOR_LIB_UNKNOW_ERR = 5;

    private int SENSOR_LIB_GUIDE_CMD_STOP = 0;
    public final int SENSOR_LIB_GUIDE_CMD_START = 1;
    public final int SENSOR_LIB_GUIDE_CMD_LEFT = 2;
    public final int SENSOR_LIB_GUIDE_CMD_RIGHT = 3;
    public final int SENSOR_LIB_GUIDE_CMD_KEEPALIVE = 4;
    public final int SENSOR_LIB_GUIDE_CMD_UNKNOWN = 5;
    public static boolean isLoad=false;

    static {
        //A1-901
        if(android.os.Build.MODEL.startsWith("A1")) {
            System.loadLibrary("senser");
            isLoad=true;
        }
    }

    public static MetaSensorManager getIntance() {
        if (metaSensorManager == null) {
            metaSensorManager = new MetaSensorManager();
            LogUtils.e(TAG, "getIntance: "+isLoad,need );
        }
        return metaSensorManager;
    }
    // int ret = setLibSensorNaviExtend(0);//clear beep
    //ret = setLibSensorNaviExtend(angle);蜂鸣声角度

    //int ret = setLibSensorNaviExtend(0);//clear beep
    // ret = sendLibSensorGuideCommand(guide);导航指令
    public void initWithContext(Context context){
            isInited = true;
            mContext=context;
//            NAVI_CMD_FORWARD_TIPS = mContext.getString(R.string.NAVI_CMD_FORWARD_TIPS);//前进
//            NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX = mContext.getString(R.string.NAVI_CMD_FORWARD_DISTANCE_TIPS_PREFIX);//前方
//            NAVI_CMD_TERMINATE_TIPS = mContext.getString(R.string.NAVI_CMD_TERMINATE_TIPS);//停止导航
//           NAVI_CMD_BACKWARD_TIPS = mContext.getString(R.string.NAVI_CMD_BACKWARD_TIPS);//停
//            NAVI_CMD_PAD_LEFT_TIPS = mContext.getString(R.string.NAVI_CMD_PAD_LEFT_TIPS);//左跨一步
//             NAVI_CMD_PAD_RIGHT_TIPS = mContext.getString(R.string.NAVI_CMD_PAD_RIGHT_TIPS);//右跨一步
//             NAVI_CMD_TURN_RIGHT_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_TIPS);//右转
//             NAVI_CMD_TURN_LEFT_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_LEFT_TIPS);//左转
//            NAVI_CMD_DEGREE_TIPS = mContext.getString(R.string.NAVI_CMD_DEGREE_TIPS);//度
//             NAVI_CMD_TURN_BACK_TIPS = mContext.getString(R.string.NAVI_CMD_TURN_BACK_TIPS);//向后转
//            NAVI_CMD_TURN_LEFT_FORWARD_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_LEFT_FORWARD_TIPS);//向左前方转
//            NAVI_CMD_TURN_LEFT_BACK_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_LEFT_BACK_TIPS);//向左后方转
//            NAVI_CMD_TURN_RIGHT_FORWARD_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_FORWARD_TIPS);//向右前方转
//            NAVI_CMD_TURN_RIGHT_BACK_TIPS=mContext.getString(R.string.NAVI_CMD_TURN_RIGHT_BACK_TIPS);//向右后方转
            metaThread = new HandlerThread(TAG);
            metaThread.setPriority(Thread.MAX_PRIORITY);
            metaThread.start();
            metaHandler=new Handler(metaThread.getLooper());
            executor.requestStart();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    libSensorInit();
                }
            });
            metaHandler.removeCallbacks(scanAlarm);
            metaHandler.post(scanAlarm);
            HCMetaUtils.addMetaConnectListener(this);
        if(isLoad) {
            setUltrasonicHz(60);
            setAxesHz(40);
        }
    }

    public MetaSensorManager() {

    }

    public String getLibSensorVervion(Version version){
        String ver = "";
        if(isLoad) {
            ver = getversion(version);
        }
        return ver;
    }
    public   String getLibSensorAxisData(AxisData axisData){
        String ret = "";
        LogUtils.e(TAG, "getLibSensorAxisData: 0" ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "getLibSensorAxisData: 1",maybe );
            ret = getAxisData(axisData);
            LogUtils.e(TAG, "getLibSensorAxisData: 2",maybe );
        }
        return ret;
    }
    public    String getLibSensorUltrasonicData(UltrasonicData ultrasonicData){
        String ret = "";
        LogUtils.e(TAG, "getLibSensorUltrasonicData: 0" ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "getLibSensorUltrasonicData: 1",maybe );
            ret = getUltrasonicData(ultrasonicData);
            LogUtils.e(TAG, "getLibSensorUltrasonicData: 2" ,maybe);
        }
        return ret;
    }
//    public    String getLibSensorIoData(IoData ioData){
//        String ret = "";
//        LogUtils.e(TAG, "getLibSensorIoData: 0",maybe );
//        if(isLoad) {
//            LogUtils.e(TAG, "getLibSensorIoData: 1",maybe );
//            ret = getIoData(ioData);
//            LogUtils.e(TAG, "getLibSensorIoData: 2",maybe );
//        }
//        return ret;
//    }
    public  String getIMEI(final Context context) {
        String IMEI = PreferenceUtils.getPrefString(context, "imei", "");
        if("".equals(IMEI)||IMEI==null){
            IMEI = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if("".equals(IMEI)||IMEI==null){
                LogUtils.e(TAG,"没有Imei",need);
                PreferenceUtils.setPrefBoolean(context, IMEI_NO, true);
            }
        }
        if("".equals(IMEI)||IMEI==null){
            do{
                Random random = new Random();
                IMEI = String.valueOf(random.nextLong());
            }while(IMEI.contains("-"));

            PreferenceUtils.setPrefString(context, "imei", IMEI);
            LogUtils.e(TAG, "IMEI="+IMEI,need);
        }
        return IMEI;
//		IMEI = testImei;
    }
    public    String getLibSensorBatteryData(BatteryData batteryData){
        String ret = "";
        LogUtils.e(TAG, "getLibSensorBatteryData: 0"  ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "getLibSensorBatteryData: 1"  ,maybe);
            ret =  getBatteryData(batteryData);
            LogUtils.e(TAG, "getLibSensorBatteryData: 2" ,maybe );
        }
        return ret;
    }
    public int getMetaDeviceInfo(DeviceInfo info){
        int ret=-1;
        if(isLoad){
            ret=getDeviceInfo(info);
        }
        return ret;
    }
    public int getMetaUpdateStatus(){
        int ret=-1;
        if(isLoad){
            ret=getUpdateStatus();
        }
        return ret;
    }
    public int updateMetaFirmwareAsync(byte[] data){
        int ret=-1;
        if(isLoad){
            ret=updateFirmwareAsync(data);
        }
        return ret;
    }
    private   String libSensorInit(){
        String ret = "";
        LogUtils.e(TAG, "libSensorInit: 0" ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "libSensorInit: 1",maybe );
            ret =  sensorsInit();
            LogUtils.e(TAG, "libSensorInit: 2"+ret,maybe );
        }
        return ret;
    }
    private   String libSensorExit(){
        String ret = "";
        LogUtils.e(TAG, "libSensorExit: 0",maybe);
        if(isLoad) {
            LogUtils.e(TAG, "libSensorExit: 1",maybe);
            ret =  sensorsExit();
            LogUtils.e(TAG, "libSensorExit: 2"+ret,maybe);
        }
        return ret;
    }

    //	/* Return -ERR_NUMBER */
//	enum ret_val {
//		RET_OK = 0,
//		RET_NO_DEV = 1,
//		RET_DEV_BUSY = 2,
//		RET_NOT_INIT = 3,
//		RET_INV_ARGS = 4,
//		RET_ERR = 5,
//	};
//	错误值是小于0的，比如-RET_NO_DEV
    public    int setLibSensorNaviExtend(int ori){
        int ret = 0;
        LogUtils.e(TAG, "setLibSensorNaviExtend:0   "+ori ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "setLibSensorNaviExtend:1 "+ori,maybe );
            ret =  setNaviExtend(ori);
            LogUtils.e(TAG, "setLibSensorNaviExtend:ret = "+ret ,maybe);
        }
        return ret;
    }
    public    int sendLibSensorGuideCommand(int cmd){
        int ret = 0;
        LogUtils.e(TAG, "sendLibSensorGuideCommand: 0"+cmd ,maybe);
        if(isLoad) {
            LogUtils.e(TAG, "sendLibSensorGuideCommand: 1"+cmd ,maybe);
            ret =  sendGuideCommand(cmd);
            LogUtils.e(TAG, "sendLibSensorGuideCommand: 2"+ret ,maybe);
        }
        return ret;
    }
    public void close() {
        metaHandler.removeCallbacks(scanAlarm);
        if(metaThread!=null){
            metaThread.quitSafely();
            ThreadUtils.joinUninterruptibly(metaThread);
            metaThread = null;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendLibSensorGuideCommand(SENSOR_LIB_GUIDE_CMD_STOP);
                libSensorExit();
            }
        });

        executor.requestStop();
        listeners.clear();
    }
    static int i=0,invalidDataNum=0;
    private static float capacity=0;
    private static final Runnable  scanAlarm =new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run: 扫描故障" );
            if(!HCMetaUtils.isConnectMeta&&!USBUtils.hasMeta()) {
                Log.e(TAG, "oncreate scanAlarm: meta未插入" );
                return;
            }
            boolean flag=false;
            BatteryData batteryData = new BatteryData();
            String ret = metaSensorManager.getBatteryData(batteryData);
            LogUtils.e(TAG, "run:batteryData= "+batteryData, need);
            if(i==3)batteryData.setResidue_capacity(0f);
            if(batteryData.getCapacity() <=0||batteryData.getResidue_capacity()<0){
                LogUtils.e(TAG,"无效数据",need);
                metaHandler.postDelayed(scanAlarm,1000*15);
                return;
            }
            if((batteryData.getCapacity()!=capacity||batteryData.getResidue_capacity()==0||batteryData.getHealth()<=0)&&invalidDataNum<3){
                capacity=batteryData.getCapacity();
                invalidDataNum++;
                metaHandler.postDelayed(scanAlarm,15000);
                return;
            }else if(batteryData.getResidue_capacity()>0){
                invalidDataNum=0;
            }
            float dump = batteryData.getResidue_capacity() / batteryData.getCapacity();
//            float dump = 0.25f-(i<25?i++:i)/100.0f;
            AlarmData alarmData=null;
            LogUtils.e(TAG, "run:dump= "+dump*100+"   "+MetaBaseContants.lowPawer+"  "+i, need);
            if(dump*100<=MetaBaseContants.lowPawer&&batteryData.getStatus()==0){
              alarmData=new AlarmData();
                alarmData.setCode(100);
                alarmData.setMsg(MetaSensorManager.getIntance().mContext.getString(R.string.helmet_power_low));
                alarmData.setData(Math.round(dump*100));
                Set<String> keys = listeners.keySet();
                for (String key : keys) {
                    listeners.get(key).alarm(alarmData);
                }
                flag=true;
            }
            if(batteryData.getHealth()< MetaBaseContants.lowHealth){
                alarmData=new AlarmData();
                alarmData.setCode(101);
                alarmData.setMsg(MetaSensorManager.getIntance().mContext.getString(R.string.battery_serious));
                alarmData.setData((int)batteryData.getHealth());
                Set<String> keys = listeners.keySet();
                for (String key : keys) {
                    listeners.get(key).alarm(alarmData);
                }
                flag=true;
            }
            if(flag)
                metaHandler.postDelayed(scanAlarm,1000*15);
            else
            metaHandler.postDelayed(scanAlarm,1000*15);
        }

    };



    public  void addListener(BatteryListener listener){
        if(listeners!=null) {
            listeners.put(listener.getClass().getSimpleName(),listener);
        }
        Log.e(TAG, "addListener: "+(listeners==null?"NULL":listener.getClass().getSimpleName() ));
    }
    public  void removeListener(BatteryListener listener){
        if(listeners!=null)
            listeners.remove(listener.getClass().getSimpleName());
    }
    private String naviMode="0";
    //imei为空
    private  String objectToString(){
        String ret = null;
        //checkRunOnExcutorThread
        if(executor.checkOnLooperThread()) {
//		Version version = new Version();
//		getversion(version);
//		Log.i("version", "version="+version.toString());

            try {
                final AxisData axisData = new AxisData();
                //		Log.i("axisData", "axisData="+axisData.toString());

                final BatteryData batteryData = new BatteryData();
                //		Log.i("batteryData", "batteryData="+batteryData.toString());

                final IoData ioData = new IoData();
                //		Log.i("ioData", "ioData="+ioData.toString());
                final UltrasonicData ultrasonicData = new UltrasonicData();

                        getLibSensorAxisData(axisData);
                        getLibSensorBatteryData(batteryData);
//                        getLibSensorIoData(ioData);
                        getLibSensorUltrasonicData(ultrasonicData);

                //		Log.i("ultrasonicData", "ultrasonicData="+ultrasonicData.toString());
                JSONObject json = new JSONObject();
                try {
//                    json.put("UserName", IMEI);
                    JSONObject axisJson = new JSONObject();
                    axisJson.put("SeqNumber", axisData.getSeq());
                    axisJson.put("AcceleratorX", axisData.getAcc_x());
                    axisJson.put("AcceleratorY", axisData.getAcc_y());
                    axisJson.put("AcceleratorZ", axisData.getAcc_z());
                    axisJson.put("GyroscopeX", axisData.getGyro_x());
                    axisJson.put("GyroscopeY", axisData.getGyro_y());
                    axisJson.put("GyroscopeZ", axisData.getGyro_z());
                    axisJson.put("CompassX", axisData.getComp_x());
                    axisJson.put("CompassY", axisData.getComp_y());
                    axisJson.put("CompassZ", axisData.getComp_z());
                    json.put("Axes", axisJson);

                    JSONObject ultraJson = new JSONObject();
                    ultraJson.put("SeqNumber", ultrasonicData.getSeq());
                    JSONArray distanceArray = new JSONArray();

                    if (ultrasonicData.getDistances().length < 6) {
                        int sumd = 0;
                        for (Float distance : ultrasonicData.getDistances()) {
                            sumd += distance.intValue();
                            distanceArray.put(distance);
                        }
                        int gap = 6 - ultrasonicData.getDistances().length;
                        for (int i = 0; i < gap; i++) {
                            if (sumd == 0) {
                                distanceArray.put(0);
                            } else {
                                distanceArray.put(50);
                            }
                        }
                    } else if (ultrasonicData.getDistances().length == 6) {
                        int index = ultrasonicData.getDistances().length / 2;
                        for (; index < ultrasonicData.getDistances().length; index++) {
                            Float distance = ultrasonicData.getDistances()[index];
                            distanceArray.put(distance);
                        }
                        for (index = 0; index < ultrasonicData.getDistances().length / 2; index++) {
                            Float distance = ultrasonicData.getDistances()[index];
                            distanceArray.put(distance);
                        }
                    }

                    ultraJson.put("Distance", distanceArray);
                    json.put("Ultrasonic", ultraJson);

                    JSONObject batteJson = new JSONObject();
                    batteJson.put("SeqNumber", batteryData.getSeq());
                    batteJson.put("Capacity", batteryData.getCapacity());
                    batteJson.put("ResidueCapacity", batteryData.getResidue_capacity());
                    batteJson.put("Voltage", batteryData.getVoltage());
                    batteJson.put("Current", batteryData.getCurrent());
                    batteJson.put("Status", batteryData.getStatus());
                    json.put("Battery", batteJson);

                    JSONObject ioJson = new JSONObject();
                    ioJson.put("SeqNumber", ioData.getSeq());
                    ioJson.put("IOStatus", ioData.getIo_status());
                    json.put("IO", ioJson);

                    ret = json.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,e.getLocalizedMessage());
                }
            } catch (Exception e) {
               Log.e(TAG,e.getLocalizedMessage());
            }
        }
        return ret;
    }

    @Override
    public void cutIn() {
        Log.e(TAG, "oncreate cutIn: ");
        metaHandler.removeCallbacks(scanAlarm);
        metaHandler.post(scanAlarm);
    }

    @Override
    public void cutOut() {
        Log.e(TAG, "oncreate cutOut: ");
        metaHandler.removeCallbacks(scanAlarm);
    }

    public static class LogUtils {
        public static void e(String TAG,String msg,boolean flag){
            if (flag)
                Log.e(TAG, msg );
        }
    }
    public  interface BatteryListener {
        void alarm(AlarmData alarm);
        void keyEventCallback(int code , int status);
    }
}
