package com.cloudminds.hc.metalib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SimpleCursorTreeAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static com.cloudminds.hc.metalib.BaseData.ACTION_USB_DEVICE_ATTACHED;
import static com.cloudminds.hc.metalib.BaseData.ACTION_USB_DEVICE_DETACHED;
import static com.cloudminds.hc.metalib.BaseData.PRODUCT_NAME;

/**
 * Created by SX on 2017/2/6.
 */

public class USBUtils {
    private static Context context;
    static UsbManager mUsbManager;
    private static boolean init=false;
    public static boolean isConnect=false;
    public static boolean isChangeState=false;
    private static final HashMap<String,MetaHotSwapListener> listeners=new HashMap<>();
    private static BroadcastReceiver mUsbReceiver=new UsbReceiver();

    public static void addListener(MetaHotSwapListener listener){
        Log.e(TAG, "addListener: " );
        if(listeners!=null) {
            Log.e(TAG, "addListener: "+listener.toString() );
            listeners.put(listener.getClass().getSimpleName(),listener);
        }else{
            throw new IllegalStateException("请先初始化USBUtils");
        }
    }
    public static void removeListener(MetaHotSwapListener listener){
        if(listeners!=null)
            listeners.remove(listener.getClass().getSimpleName());
        else{
                throw new IllegalStateException("请先初始化USBUtils");
        }
    }
    public static void init(Context context){
        if(init)return;
        init=true;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(mUsbReceiver, filter);
        USBUtils.context = context;

    }
    public static void destory(){
        if(!init)return;
        init=false;
        if(listeners!=null) {
            listeners.clear();
        }
        context.unregisterReceiver(mUsbReceiver);
    }

    public static HashMap<String, MetaHotSwapListener> getListeners() {
        return listeners;
    }

    public static int i=0;
    private static String TAG="METAlib/USBUtils";
    static Handler handler=new Handler(){
        int i=0;
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            super.handleMessage(msg);
            synchronized (this.getClass()) {
                boolean hasMeta = hasMeta();
                if (!isConnect&&hasMeta) {
                    Set<String> keys = listeners.keySet();
                    isConnect = true;
                    HCMetaUtils.isConnectMeta=true;
                    for (String key : keys) {
                        listeners.get(key).cutIn();
                        Log.e("TEST", "onReceive: "+key );
                    }
                    Log.e("TEST", "onReceive: isConnect="+isConnect+"   "+USBUtils.isConnect );
                }else if(isConnect&&!hasMeta){
                    isConnect=false;
                    HCMetaUtils.isConnectMeta=false;
                    Set<String> keys = listeners.keySet();
                    for (String key : keys) {
                        listeners.get(key).cutOut();
                    }
                    Log.e("TEST", "onReceive: isConnect="+isConnect+"   "+USBUtils.isConnect );
                }
            }
        }
    };
    private static final int HANDLEUSBRECEIVER=100;
    public static  class UsbReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean connected = intent.getExtras().getBoolean("connected");
            Log.e("TEST", "onReceive: "+connected+"  "+i );
            if (ACTION_USB_DEVICE_DETACHED.equals(action)||ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if(handler.hasMessages(HANDLEUSBRECEIVER))handler.removeMessages(HANDLEUSBRECEIVER);
                handler.sendEmptyMessageDelayed(HANDLEUSBRECEIVER,700);
            }
        }


    }
    public static boolean hasMeta() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.e("TEST", "hasMeta: "+deviceList.size());
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e("TEST", "hasMeta: "+device.getProductName() +"  | "+PRODUCT_NAME);
                String productName = device.getProductName();
                if ((!TextUtils.isEmpty(productName))&&productName.equals(PRODUCT_NAME)) {
                    Log.e("TEST", "hasMeta: "+device.getDeviceId()+"   "+device.getProductId() );
                    return true;
                }
            }
        }
        return false;
    }
    public interface MetaHotSwapListener{
        void cutIn();
        void cutOut();
    }


}
