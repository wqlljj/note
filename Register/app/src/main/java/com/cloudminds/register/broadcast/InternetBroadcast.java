package com.cloudminds.register.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.cloudminds.register.BasicApp;
import com.facebook.stetho.common.LogUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static com.cloudminds.register.broadcast.InternetBroadcast.NetType.*;

public class InternetBroadcast
        extends BroadcastReceiver
{
    private static String TAG = "InternetBroadcast";
    private static Context context;
    public static boolean hasConnectivity=false;
    static HashMap<String, InternetChangeListener> listeners = new HashMap();
    private static NetType netType =UNKOWN;
    public static boolean isNetworkAvailable()
    {
        if (context == null) {
            context = BasicApp.getContext();
        }
        NetworkInfo localNetworkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        boolean bool;
        if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable()))
        {
            hasConnectivity=true;
            LogUtil.d(TAG, new Object[] { "Current network state: netinfo is null, which means there is not active network" });
        }else{
            hasConnectivity = false;
            if(localNetworkInfo!=null) {
                LogUtil.d(TAG, new Object[]{"Current network state: netinfo != null and isAvailable=" + localNetworkInfo.isAvailable() + ", isConnected=" + localNetworkInfo.isConnected() + ", isConnectedOrConnecting=" + localNetworkInfo.isConnectedOrConnecting() + ", isFailover=" + localNetworkInfo.isFailover() + ", isRoaming=" + localNetworkInfo.isRoaming()});
            }else{
                LogUtil.d(TAG, new Object[]{"Current network state: netinfo != null "});
            }
        }
        return hasConnectivity;
    }

    public static void removeInternetChangeListener(InternetChangeListener paramInternetChangeListener)
    {
        listeners.remove(paramInternetChangeListener.getClass().getSimpleName());
    }

    public static void setContext(Context paramContext)
    {
        context = paramContext;
    }

    public static void setInternetChangeListener(InternetChangeListener paramInternetChangeListener)
    {
        listeners.put(paramInternetChangeListener.getClass().getSimpleName(), paramInternetChangeListener);
    }

    public void onReceive(Context paramContext, Intent paramIntent)
    {
        hasConnectivity = isNetworkAvailable();
        Set<String> keySet = listeners.keySet();
        for (String key : keySet) {
            listeners.get(key).changeInternet(hasConnectivity);
        }
    }

    public  interface InternetChangeListener
    {
        void changeInternet(boolean isAvailable);
    }

    public  enum NetType{UNKOWN,NONET,TWO_G,THREE_G,FOUR_G,WIFI}
}
