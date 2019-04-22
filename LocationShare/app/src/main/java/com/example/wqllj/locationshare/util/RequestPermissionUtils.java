package com.example.wqllj.locationshare.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by SX on 2017/3/10.
 */

public class RequestPermissionUtils {
    public static class Dangerous{
        public static final ArrayList<String> needrequest=new ArrayList<>();
        static {
            needrequest.add("android.permission.WRITE_CONTACTS");
            needrequest.add("android.permission.GET_ACCOUNTS");
            needrequest.add("android.permission.READ_CONTACTS");
            needrequest.add("android.permission-group.CONTACTS");

            needrequest.add("com.android.voicemail.permission.ADD_VOICEMAIL");
            needrequest.add("android.permission.PROCESS_OUTGOING_CALLS");
            needrequest.add("android.permission.USE_SIP");
            needrequest.add("android.permission.WRITE_CALL_LOG");
            needrequest.add("android.permission.CALL_PHONE");
            needrequest.add("android.permission.READ_PHONE_STATE");
            needrequest.add("android.permission.READ_CALL_LOG");
            needrequest.add("android.permission-group.PHONE");

            needrequest.add("android.permission.READ_CALENDAR");
            needrequest.add("android.permission.WRITE_CALENDAR");
            needrequest.add("android.permission-group.CALENDAR");

            needrequest.add("android.permission.BODY_SENSORS");
            needrequest.add("android.permission-group.SENSORS");

            needrequest.add("android.permission.CAMERA");
            needrequest.add("android.permission-group.CAMERA");

            needrequest.add("android.permission.ACCESS_COARSE_LOCATION");
            needrequest.add("android.permission.ACCESS_FINE_LOCATION");
            needrequest.add("android.permission-group.LOCATION");

            needrequest.add("android.permission.WRITE_EXTERNAL_STORAGE");
            needrequest.add("android.permission.READ_EXTERNAL_STORAGE");
            needrequest.add("android.permission-group.STORAGE");

            needrequest.add("android.permission.RECORD_AUDIO");
            needrequest.add("android.permission-group.MICROPHONE");

            needrequest.add("android.permission.READ_SMS");
            needrequest.add("android.permission.RECEIVE_WAP_PUSH");
            needrequest.add("android.permission.RECEIVE_MMS");
            needrequest.add("android.permission.RECEIVE_SMS");
            needrequest.add("android.permission.SEND_SMS");
            needrequest.add("android.permission.READ_CELL_BROADCASTS");
            needrequest.add("android.permission-group.SMS");
        }
    }
    private static OnCallBackListening listening;
    private static String[] permissions;
    public static void getPermissions(Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
            //应用装时间
            long firstInstallTime = packageInfo.firstInstallTime;
            //应用最后一次更新时间
            long lastUpdateTime = packageInfo.lastUpdateTime;
           Log.e("TEST","first install time : " + firstInstallTime + " last update time :" + lastUpdateTime);
            long lastTime = activity.getSharedPreferences("smartrobot", Context.MODE_PRIVATE).getLong("lastRunTime", 0l);
            if(lastTime<firstInstallTime||lastTime<lastUpdateTime){
                Log.e("TEST", "getPermissions: isFirstRun" +lastTime+"  "+firstInstallTime+"   "+lastUpdateTime);
                activity.getSharedPreferences("smartrobot", Context.MODE_PRIVATE).edit().putLong("lastRunTime", System.currentTimeMillis()).apply();
            }else{
                Log.e("TEST", "getPermissions: NOisFirstRun" );
                activity.getSharedPreferences("smartrobot", Context.MODE_PRIVATE).edit().putLong("lastRunTime", System.currentTimeMillis()).apply();
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            PackageInfo pack  = packageManager.getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = pack.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(permissions!=null&&permissions.length>0){
            RequestPermissionUtils.checkRequestPermission(permissions,activity,101);
        }
    }
    public synchronized static boolean checkRequestPermission(@NonNull String[] permissions, Activity activity, int requestCode){
        if (Build.VERSION.SDK_INT >= 23) {
            int index=0;
            ArrayList<String> temp=new ArrayList<>();
            PackageManager pm = activity.getPackageManager();
            while(index<permissions.length) {
                if(Dangerous.needrequest.contains(permissions[index])&&pm.checkPermission(permissions[index],activity.getPackageName())!= PackageManager.PERMISSION_GRANTED){
                    Log.e("TEST", "checkRequestPermission: "+permissions[index]+"   "+ ContextCompat.checkSelfPermission(activity, permissions[index]));
                    temp.add(permissions[index]);
                }
                index++;
            }
            if(temp.size()==0){
               // Toast.makeText(activity, "权限已授予", Toast.LENGTH_SHORT).show();
                Log.e("TEST", "checkRequestPermission: 权限已授予");
                return true;
            }
            permissions=new String[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                permissions[i]=temp.get(i);
            }
            Log.e("TEST", "checkRequestPermission: 权限申请"+ Arrays.toString(permissions));
                ActivityCompat.requestPermissions(activity,permissions,requestCode);
                return false;
        }
        return true;
    }
    public synchronized static boolean checkRequestPermission(@NonNull String[] permissions, Context context, OnCallBackListening listening){
        if (Build.VERSION.SDK_INT >= 23) {
            int index=0;
            ArrayList<String> temp=new ArrayList<>();
            PackageManager pm = context.getPackageManager();
            while(index<permissions.length) {
                if(pm.checkPermission(permissions[index],context.getPackageName())!= PackageManager.PERMISSION_GRANTED){
                    Log.e("TEST", "checkRequestPermission: "+permissions[index]+"   "+ ContextCompat.checkSelfPermission(context, permissions[index]));
                    temp.add(permissions[index]);
                }
                index++;
            }
            if(temp.size()==0){
                // Toast.makeText(activity, "权限已授予", Toast.LENGTH_SHORT).show();
                Log.e("TEST", "checkRequestPermission: 权限已授予");
                return true;
            }
            permissions=new String[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                permissions[i]=temp.get(i);
            }
            RequestPermissionUtils.permissions = permissions;
            Log.e("TEST", "checkRequestPermission: 权限申请"+ Arrays.toString(permissions));
            RequestPermissionUtils.listening = listening;
            context.startActivity(new Intent(context,PermissionActivity.class));
            return false;
        }
        return true;
    }
    public static class PermissionActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            RelativeLayout relativeLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1, 1);
            setContentView(relativeLayout,params);
            ActivityCompat.requestPermissions(this,permissions,100);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            ArrayList<String> temp=new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults.length>i&&grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    temp.add(permissions[i]);
                }
            }
            listening.dined(temp);
            finish();
        }
    }
     public interface OnCallBackListening{
        void dined(ArrayList<String> permissions);
    }
}
