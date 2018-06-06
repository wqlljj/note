package com.cloudminds.meta.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.utils.ThreadPoolUtils;
import com.cloudminds.hc.hariservice.webrtc.AppRTCAudioManager;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.hc.metalib.UpdaterApplication;
import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.HubActivity;
import com.cloudminds.meta.activity.IHubView;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.broadcast.LocaleChangeReceiver;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.service.AIBridge;
import com.cloudminds.meta.service.HubServiceConnector;
import com.cloudminds.meta.manager.MetaManager;
import com.cloudminds.meta.util.DangerousPermissions;
import com.cloudminds.meta.util.DateUtil;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;
import com.getui.logful.LoggerConfigurator;
import com.getui.logful.LoggerFactory;
import com.kongqw.OpenCVApi;
import com.tencent.bugly.crashreport.CrashReport;

import org.webrtc.UsbCameraEnumerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ai.kitt.snowboy.SnowboyClient;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cloudminds.meta.broadcast.InternetBroadcast.NetType.FOUR_G;
import static com.cloudminds.meta.broadcast.InternetBroadcast.NetType.THREE_G;
import static com.cloudminds.meta.broadcast.InternetBroadcast.NetType.WIFI;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_NOMAL;


/**
 * Created by zoey on 17/5/16.
 */

public class MetaApplication extends Application implements UpdaterApplication.Installer, USBUtils.MetaHotSwapListener, LocaleChangeReceiver.LocaleChangeListener, InternetBroadcast.InternetChangeListener{
    static HashMap<String,NotifyItemListener> listeners=new HashMap<>();
    public static Context mContext ;
    public static Application gApplication;
    public static int state=HUB_CONN_NOMAL;
    public static boolean isReConnect=false;

    private static boolean isRecognition=true;
    private static boolean isAutoRecognition=false;

    private static IHubView state_listenr;
    private static String TAG="META/MetaApplication";
    public static boolean isCheckRom=true;
    public static  boolean isStartHubActivity=false;
    public static boolean hariServiceInitialized=false;

    public static void setState_listenr(IHubView state_listenr) {
        MetaApplication.state_listenr = state_listenr;
    }

    public static void setState(int state) {
        MetaApplication.state = state;
        if(state_listenr!=null){
            state_listenr.setUIState(state,null);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
        gApplication = this;
        // 分别为MainThread和VM设置Strict Mode
//        if (BuildConfig.DEBUG) {
        if (false) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectResourceMismatches()
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build());
        }
        ThreadPoolUtils.init();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
//        CustomAudioManager.getInstance(MetaApplication.mContext).changeToReceiver();
        SharePreferenceUtils.setContext(this.getApplicationContext());

        PackageInfo pack  = null;
        try {
            pack = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String[] pers = pack.requestedPermissions;
        ArrayList<String> temp=new ArrayList<>();
        for (String per : pers) {
            if(DangerousPermissions.needrequest.contains(per))
                temp.add(per);
        }
        pers =  temp.toArray(new String[temp.size()]);
        HCMetaUtils.init(this);
        if(EasyPermissions.hasPermissions(this,pers))
        init();
    }

    public void init() {
        TTSSpeaker.restartAPP(false);
        AppRTCAudioManager mAudioManager = AppRTCAudioManager.create(getApplicationContext(), new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: " );
            }
        });
        Log.d(TAG,"Initializing the WebRTC audio manager...");
        mAudioManager.init();
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                //        CrashReport.initCrashReport(getApplicationContext(), "1b0be65288", false);
                Context context = getApplicationContext();
// 获取当前包名
                String packageName = context.getPackageName();
// 获取当前进程名
                String processName = getProcessName(android.os.Process.myPid());
// 设置是否为上报进程
                CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
                strategy.setUploadProcess(processName == null || processName.equals(packageName));
// 初始化Bugly

//		第三个参数为SDK调试模式开关，调试模式的行为特性如下：
//		•输出详细的Bugly SDK的Log；
//		•每一条Crash都会被立即上报；
//		•自定义日志将会在Logcat中输出。
                //建议在测试阶段建议设置成true，发布时设置为false
                CrashReport.initCrashReport(context, "2186a4fb3d", true, strategy);
            }
        });
        HubServiceConnector hubServiceConnector = HubServiceConnector.getIntance(mContext);
        hubServiceConnector.startHubService(mContext);
        AIBridge.instance(this.getApplicationContext());
        HCApiClient.init(this);
        MetaManager.getInstance().initWithContext(this);
        HCMetaUtils.addMetaConnectListener(this);
        LocaleChangeReceiver.language=LocaleChangeReceiver.getLanguage(this);
        LocaleChangeReceiver.addListener(this);
        handler.sendEmptyMessageDelayed(STARTHUBACTIVITY,4000);
        InternetBroadcast.setInternetChangeListener(this);
        OpenCVApi.init(this);
    }

    //	其中获取进程名的方法“getProcessName”有多种实现方法，推荐方法如下：
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
    public static boolean isRecognition() {
        return isRecognition;
    }

    public static void setIsRecognition(boolean isRecognition) {
        MetaApplication.isRecognition = isRecognition;
        if(!isRecognition){
            setIsAutoRecognition(false);
        }
    }

    public static boolean isAutoRecognition() {
        return isAutoRecognition;
    }

    public static void setIsAutoRecognition(boolean isAutoRecognition) {
        MetaApplication.isAutoRecognition = isAutoRecognition;
    }
    public static void addMessage(int type,String data){//ChatMessage.Type.CHAT_LEFT
        Log.e(TAG, "addMessage: "+data );
        Constant.msg.add(new ChatMessage(type, DateUtil.getCurrentTimeStr(),data));
        Set<String> keySet = listeners.keySet();
        for (String key : keySet) {
            listeners.get(key).notifyItem();
        }
    }
    public static void addNotifyItemListener(NotifyItemListener listener){
        listeners.put(listener.getClass().getSimpleName(),listener);
        Log.e(TAG, "addNotifyItemListener: "+ listeners.toString() );
    }
    public static void removeNotifyItemListener(NotifyItemListener listener){
        if(listeners.containsValue(listener))
            listeners.remove(listener.getClass().getSimpleName());
        Log.e(TAG, "removeNotifyItemListener: "+ listeners.toString() );
    }

    @Override
    public void install(byte[] data) {
        HCMetaUtils.updateFirmwareAsync(data);
    }
    private final int STARTVIDEO=100;
    private final int STOPVIDEO=101;
    private final int STARTHUBACTIVITY=102;
    Handler handler=new Handler(){
        int i=0;
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            super.handleMessage(msg);
            switch (msg.what){
                case STARTVIDEO:
                    if(state==HUB_CONN_IN_CONNECTION){
                        HariServiceClient.getCallEngine().startVideoSource();
                    }
                    break;
                case STOPVIDEO:
                    if(state==HUB_CONN_IN_CONNECTION){
                        HariServiceClient.getCallEngine().stopVideoSource();
                    }
                    break;
                case STARTHUBACTIVITY://start HubActivity
                    if(!isStartHubActivity){
                        Log.e(TAG, "handleMessage: isAppRunning isStartHubActivity = "+isStartHubActivity );
                        startActivity(new Intent(getApplicationContext(), HubActivity.class));
                        handler.sendEmptyMessageDelayed(STARTHUBACTIVITY,4000);
                    }
                    break;
            }
        }
    };

    public static boolean isApplicationBroughtToBackground(final Context context) {
        if(context==null){
            Log.e(TAG, "isApplicationBroughtToBackground: context==null" );
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            Log.e(TAG, "isApplicationBroughtToBackground: "+topActivity.getPackageName());
            if (!topActivity.getPackageName().equals("com.cloudminds.meta")) {
                return true;
            }
        }
        return false;
    }
    //头盔拔出
    @Override
    public void cutIn() {
        Toast.makeText(this, R.string.meta_connected, Toast.LENGTH_SHORT).show();
        if(handler.hasMessages(STARTVIDEO)){
            handler.removeMessages(STARTVIDEO);
        }
        if(handler.hasMessages(STOPVIDEO)){
            handler.removeMessages(STOPVIDEO);
        }
        TTSSpeaker.speak(getString(R.string.meta_conn), TTSSpeaker.HIGH);
        if(!UsbCameraEnumerator.isSupported()){
            TTSSpeaker.speak(getString(R.string.camera_wrong), TTSSpeaker.HIGH);
            return;
        }
        handler.sendEmptyMessageDelayed(STARTVIDEO,1000);
    }
    //头盔插入
    @Override
    public void cutOut() {
        Toast.makeText(this, R.string.meta_disconnected, Toast.LENGTH_SHORT).show();
        if(handler.hasMessages(STARTVIDEO)){
            handler.removeMessages(STARTVIDEO);
        }
        if(handler.hasMessages(STOPVIDEO)){
            handler.removeMessages(STOPVIDEO);
        }
        TTSSpeaker.speak(getString(R.string.meta_unconn), TTSSpeaker.HIGH);
        handler.sendEmptyMessageDelayed(STOPVIDEO,1000);
    }
    //语言环境改变
    @Override
    public void onChange(String language) {
        MetaManager.getInstance().initRes();
    }

    @Override
    public void changeInternet(int netType) {
        if(state==HUB_CONN_IN_CONNECTION&&(netType==THREE_G||netType==FOUR_G||netType==WIFI)&&HariServiceClient.getCallEngine().isVideoStopped()){
            if(handler.hasMessages(STARTVIDEO)){
                handler.removeMessages(STARTVIDEO);
            }
            if(handler.hasMessages(STOPVIDEO)){
                handler.removeMessages(STOPVIDEO);
            }
            handler.sendEmptyMessageDelayed(STARTVIDEO,1000);
        }
    }

    //告知历史界面历史信息改变
    public  interface NotifyItemListener{
        void notifyItem();
    }
}
