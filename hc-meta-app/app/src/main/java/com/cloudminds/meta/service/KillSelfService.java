package com.cloudminds.meta.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;

public class KillSelfService extends Service {

    private static  long stopDelayed=2000;
    private PowerManager.WakeLock wakeLock=null;
    private Handler handler;
    private String PackageName;
    private String TAG="META/KillSelfService";
    private boolean startCall;

    public KillSelfService() {
        handler=new Handler();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        ActivityManager.clear();
        startCall= SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_STARTCALL,false);
        PowerManager powerManager = (PowerManager)this.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Restart Lock");
        Log.e(TAG, "onStartCommand: wakeLock.acquire" );
        if(!wakeLock.isHeld())
        wakeLock.acquire();
        try {
            stopDelayed=intent.getLongExtra("Delayed",2000);
            PackageName=intent.getStringExtra("PackageName");
        }catch (Exception e){
            e.printStackTrace();
            if(wakeLock.isHeld())
            wakeLock.release();
            wakeLock=null;
            KillSelfService.this.stopSelf();
        }
        startMeta();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startMeta() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: startActivity" );
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
                startActivity(LaunchIntent);
            }
        },stopDelayed);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(check()){
                   close();
                }else{
                    Log.e(TAG, "run: check false" );
                    stopDelayed=0;
                    startMeta();
                }
            }
        },stopDelayed+2000);
    }
    private void close(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(startCall&&MetaApplication.state==HUB_CONN_IN_CONNECTION) {
                    Log.e(TAG, "run: check  true wakeLock.release");
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    wakeLock = null;
                    KillSelfService.this.stopSelf();
                }else{
                    close();
                }
            }
        },startCall?5000:0);
    }
    private boolean check(){
        return !MetaApplication.isApplicationBroughtToBackground(MetaApplication.mContext);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
