package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static com.cloudminds.meta.broadcast.VPNStateReceiver.VPNState.*;

public class VPNStateReceiver extends BroadcastReceiver {
    private String TAG="META:VPNStateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: "+intent.getAction() );
        if ("android.intent.action.VPN_STATE".equals(intent.getAction())) {
            int VPNState = intent.getIntExtra("vpn_state", 0);
//            Log.d(TAG,_METHOD_()+ "VPNState:" + VPNState);
            if ( (VPNState == 0) || (VPNState == 2)) {
                Intent vpnintent = new Intent();
                vpnintent.setAction("android.intent.action.ROBOT_ACTION");
                vpnintent.putExtra("action_name","start_vpn");
                context.sendBroadcast(vpnintent);
            }
            Log.e(TAG, "onReceive: VPNState = "+VPNState+"  "+ mVPNState.ordinal()+"  "+mVPNState);
            if(mVPNState.ordinal()!=VPNState){
                switch (VPNState){
                    case 0:
                        mVPNState=VPN_STATE_INIT;
                        break;
                    case 1:
                        mVPNState=VPN_STATE_CONNECTED;
                        break;
                    case 2:
                        mVPNState=VPN_STATE_DISCONNECTED;
                        break;
                    case 3:
                        mVPNState=VPN_STATE_OTHER;
                        break;
                }
                Log.e(TAG, "onReceive: "+mVPNState );
//                if(listener!=null)
//                listener.vpnState(mVPNState);
            }
        }
    }
    private  Context mContext;
    public  VPNState mVPNState=VPN_STATE_OTHER;
    private VPNStateChangeListener listener;

    public void setListener(VPNStateChangeListener listener) {
        this.listener = listener;
    }

    public enum VPNState{
        VPN_STATE_INIT,VPN_STATE_CONNECTED,VPN_STATE_DISCONNECTED,VPN_STATE_OTHER
    }

    public  void init(Context context){
        if(mContext!=null){
            return;
        }
        mContext = context;
        if ( !isVPNConnected()) {
//            listener.vpnState(VPN_STATE_DISCONNECTED);
            Log.e(TAG, "VPNDisconnected: ");
            Intent vpnintent = new Intent();
            vpnintent.setAction("android.intent.action.ROBOT_ACTION");
            vpnintent.putExtra("action_name","start_vpn");
            mContext.sendBroadcast(vpnintent);
        } else {
            Log.e(TAG, "VPNConnected: ");
            mVPNState = VPN_STATE_CONNECTED;
//            listener.vpnState(VPN_STATE_CONNECTED);
        }
    }
    public void destory(){
        mContext=null;
        mVPNState=null;
        listener=null;
    }
    public  boolean isVPNConnected() {
        ConnectivityManager mConnectivityMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        //新版本调用方法获取网络状态

        if (mConnectivityMgr != null) {
            NetworkInfo[] networkInfo = mConnectivityMgr.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    Log.i(TAG,  i + " type:" + networkInfo[i].getTypeName() + ", state:" + networkInfo[i].getState());
                    if ((networkInfo[i].getTypeName().equals("VPN")) && (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public  interface VPNStateChangeListener{
        void vpnState(VPNState state);
    }
}
