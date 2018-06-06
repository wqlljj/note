package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cloudminds.hc.metalib.bean.AlarmData;
import com.cloudminds.hc.metalib.manager.MetaSensorManager;
import com.cloudminds.meta.R;

import java.util.HashMap;
import java.util.Set;

public class BatteryReceiver extends BroadcastReceiver {

    private static String TAG="BatteryReceiver";
    public static int curlevel=0;
    private int status;
    private static final HashMap<String,MetaSensorManager.BatteryListener> listeners=new HashMap<>();
    private String alarmMsg;

    public static void addListener(MetaSensorManager.BatteryListener listener){
        if(listeners!=null) {
            listeners.put(listener.getClass().getSimpleName(),listener);
        }
        Log.e(TAG, "addListener: "+(listeners==null?"NULL":listener.getClass().getSimpleName() ));
    }
    public static void removeListener(MetaSensorManager.BatteryListener listener){
        if(listeners!=null)
            listeners.remove(listener.getClass().getSimpleName());
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: " );
        Message obtain = Message.obtain();
        obtain.obj=intent;
        obtain.what=HANDLEBATTERYRECEIVER;
        if(mHandler.hasMessages(HANDLEBATTERYRECEIVER)){
            mHandler.removeMessages(HANDLEBATTERYRECEIVER);
        }
        alarmMsg = context.getString(R.string.phone_power_low);
        mHandler.sendMessageDelayed(obtain,2000);
    }
    private final int HANDLEBATTERYRECEIVER=1000;
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = (Intent) msg.obj;
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                //电池状态
                status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
                //integer field containing the current battery level,
            curlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
//                if(curlevel==0)curlevel=20;
//                curlevel--;
                String statusString = "";
                switch (status){
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString="full";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString="charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString="discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString="unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString="not charging";
                        break;
                }
                Log.e(TAG, "onReceive: status = "+status );
                if(status!=BatteryManager.BATTERY_STATUS_CHARGING&&curlevel<=20){
                    AlarmData alarmData=null;
                    alarmData=new AlarmData();
                    alarmData.setCode(100);
                    alarmData.setMsg(alarmMsg);
                    alarmData.setData(curlevel);
                    Set<String> keys = listeners.keySet();
                    for (String key : keys) {
                        listeners.get(key).alarm(alarmData);
                    }
                }
                //
                boolean present= intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT,false);

                // integer containing the maximum battery level
                int maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
                //String describing the technology of the current battery
                String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                //integer containing the current health constant
                int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
                String healthStr = "";
                switch (health){
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        healthStr="cold";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthStr="dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthStr="good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthStr="voer voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        healthStr="unknown";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthStr="unspecified failure";
                        break;
                }

                //integer containing the resource ID of a small status bar icon indicating the current battery state
                int smallIcon = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL,0);
                //integer indicating whether the device is plugged in to a power source; 0 means it is on battery,
                // other constants are different types of power sources
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
                String pluggedStr="";
                switch (plugged){
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        pluggedStr="AC";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        pluggedStr="USB";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        pluggedStr="wireless";
                        break;
                }
                // integer containing the current battery temperature.
                int temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                // integer containing the current battery voltage level.
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
                StringBuilder sb = new StringBuilder("status:");
                sb.append(statusString);
                sb.append("\n");

                sb.append("present:").append(present);
                sb.append("\n");

                sb.append("level:").append(curlevel);
                sb.append("\n");
                sb.append("maxLevel:").append(maxLevel);
                sb.append("\n");
                sb.append("technology:").append(technology);
                sb.append("\n");
                sb.append("health:").append(healthStr);
                sb.append("\n");
                sb.append("icon:").append(smallIcon);
                sb.append("\n");
                sb.append("plugged:").append(pluggedStr);
                sb.append("\n");
                sb.append("temperature:").append(temperature);
                sb.append("\n");
                sb.append("voltage:").append(voltage);
                sb.append("\n");

                Log.e(TAG, "onReceive: "+sb.toString() );
            }
        }
    };


}
