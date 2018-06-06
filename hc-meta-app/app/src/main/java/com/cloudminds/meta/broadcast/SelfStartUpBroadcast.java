package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cloudminds.meta.activity.ActivateActivity;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.util.SharePreferenceUtils;

/**
 * Created by SX on 2017/5/27.
 */

public class SelfStartUpBroadcast extends BroadcastReceiver {
    public static final String ACTION1 = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION2 = "android.intent.action.DOUBLE_TAP_HOME";
    private String TAG="META:StartUpBroadcast";
    public static boolean isStartUp=false;
    public static DoubleClickHomeListener listener;
    public static void setDoubleClickHomeListener(DoubleClickHomeListener listener){
        SelfStartUpBroadcast.listener=listener;
    }
    public static void removeDoubleClickHomeListener(){
        SelfStartUpBroadcast.listener=null;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: "+action);
        openFromOnDestory(context);
    }
    public void openFromOnDestory(Context mContext){
//        Intent intent = new Intent(mContext,ActivateActivity.class);
        if(isStartUp&&listener!=null) {
//            Intent intent = new Intent();
//            ComponentName cmp = new ComponentName("com.cloudminds.meta", "com.cloudminds.meta.activity.HubActivity");
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setComponent(cmp);
//            mContext.startActivity(intent);
            listener.doubleClick();
        }else{
            SharePreferenceUtils.setPrefInt(Constant.PRE_KEY_CALLCALLEE,0);
            Intent intent = new Intent();
            ComponentName cmp = new ComponentName("com.cloudminds.meta", "com.cloudminds.meta.activity.HubActivity");
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("startCall", true);
            intent.setComponent(cmp);
            mContext.startActivity(intent);
        }
    }
    public interface  DoubleClickHomeListener{
        void doubleClick();
    }
}
