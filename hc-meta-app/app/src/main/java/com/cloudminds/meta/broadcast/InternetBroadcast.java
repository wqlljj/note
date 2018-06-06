package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.service.navigation.IndoorNavigator;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.LogUtils;
import com.cloudminds.meta.util.TTSSpeaker;

import java.util.HashMap;
import java.util.Set;

public class InternetBroadcast extends BroadcastReceiver {
    static HashMap<String,InternetChangeListener> listeners=new HashMap<>();
    public static boolean hasConnectivity=false;
    public static int netType=NetType.UNKOWN;
    private static Context context;
    private static String TAG="InternetBroadcast";
    public static class NetType{
        public static final int NONET=0;
        public static final int UNKOWN=1;
        public static final int TWO_G=2;
        public static final int THREE_G=3;
        public static final int FOUR_G=4;
        public static final int WIFI=5;
    }
    public InternetBroadcast() {
    }
    public static void setContext(Context context){
        InternetBroadcast.context=context;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int lastNetType = netType;
        boolean isNetAvailable = isNetworkAvailable();
            if (isNetAvailable) {
                LogUtils.d(TAG,"Connectivity changed: available1=" + isNetAvailable);
                String type="";
                switch (netType){
                    case NetType.TWO_G:
                        if(lastNetType>=NetType.FOUR_G)
                        type="2G";
                        break;
                    case NetType.THREE_G:
                        if(lastNetType!=NetType.THREE_G)
                        type="3G";
                        break;
                    case NetType.FOUR_G:
                        if(lastNetType<NetType.FOUR_G)
                        type="4G";
                        break;
                    case NetType.WIFI:
                        if(lastNetType<NetType.FOUR_G)
                        type="wifi";
                        break;
                    case NetType.UNKOWN:
                    case NetType.NONET:
                        if(lastNetType>=NetType.THREE_G)
                        type=context.getString(R.string.unavailable);
                        break;
                }
                if(!type.equals(""))
                TTSSpeaker.speak(String.format(context.getString(R.string.net_type),type),TTSSpeaker.HIGH);
                hasConnectivity = true;
            } else {
                hasConnectivity = false;
                LogUtils.d(TAG,"Connectivity changed: available2=" + (IndoorNavigator.type== IndoorNavigator.Type.NAVIING)+"  "+OutdoorNavigator.isStartNavi);
                if(IndoorNavigator.type== IndoorNavigator.Type.NAVIING|| OutdoorNavigator.isStartNavi){
                    TTSSpeaker.speak(context.getString(R.string.no_internet_navi), TTSSpeaker.HIGH);
                }else{
                    TTSSpeaker.speak(context.getString(R.string.no_internet), TTSSpeaker.HIGH);
                }
            }
        Set<String> keySet = listeners.keySet();
        for (String key : keySet) {
            listeners.get(key).changeInternet(netType);
        }
    }
    public static void setInternetChangeListener(InternetChangeListener listener){
        listeners.put(listener.getClass().getSimpleName(),listener);
    }
    public static void removeInternetChangeListener(InternetChangeListener listener){
        listeners.remove(listener.getClass().getSimpleName());
    }
    public static boolean isNetworkAvailable() {
        if(context==null)context= MetaApplication.mContext;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo;
        netInfo = cm.getActiveNetworkInfo();

        boolean available = netInfo != null && netInfo.isAvailable() ;//&& netInfo.isConnected();
        netType=getNetworkType();
        if(netInfo==null){
            LogUtils.d(TAG,"Current network state: netinfo is null, which means there is not active network");
        }else{
            LogUtils.d(TAG,"Current network state: netinfo != null and isAvailable="+netInfo.isAvailable()
                    +", isConnected="+netInfo.isConnected()+", isConnectedOrConnecting="+netInfo.isConnectedOrConnecting()
                    +", isFailover="+netInfo.isFailover()+", isRoaming="+netInfo.isRoaming());
        }
        return  hasConnectivity=available;

    }
    public static int getNetworkType()
    {
        int netType = NetType.NONET;

        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        Log.e(TAG, "getNetworkType: "+(networkInfo==null?"networkInfo=null":""+networkInfo.isConnected()) );
        if (networkInfo != null && networkInfo.isAvailable())
        {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                netType = NetType.WIFI;
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                String _strSubTypeName = networkInfo.getSubtypeName();

                Log.e(TAG, "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                Log.e(TAG, "getNetworkType: "+ networkType);
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        netType = NetType.TWO_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        netType = NetType.THREE_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                    case 19://api<11 : replace by 13
                        netType = NetType.FOUR_G;
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetType.THREE_G;
                        } else {
                            Log.e(TAG, "getNetworkType: _strSubTypeName = "+_strSubTypeName );
                            netType = NetType.UNKOWN;
                        }
                        break;
                }
            }
        }
        Log.e(TAG, "Network Type : " + netType);

        return netType;
    }
    public interface InternetChangeListener{
        void changeInternet(int netType);
    }
}
