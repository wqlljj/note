package com.cloudminds.meta.broadcast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cloudminds.meta.application.MetaApplication;

import static android.content.Context.TELEPHONY_SERVICE;

public class PhoneReceiver extends BroadcastReceiver {

    private String TAG="META/PhoneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: "+intent.getAction() );
        //如果是去电
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            String phoneNumber = intent
            .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "call OUT:" + phoneNumber);
            }else{
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            //设置一个监听器
            }
    }
    PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.e(TAG, "onCallStateChanged:挂断 " );
                 break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.e(TAG, "onCallStateChanged: 接听" );
                 break;
                case TelephonyManager.CALL_STATE_RINGING:
                    AudioManager audioManager = ((AudioManager) MetaApplication.mContext.getSystemService(
                            Context.AUDIO_SERVICE));
                    Log.e(TAG, "onCallStateChanged: mode = "+audioManager.getMode());
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    Log.e(TAG, "onCallStateChanged: mode = "+audioManager.getMode());
                    Log.e(TAG, "onCallStateChanged: 来电号码"+incomingNumber );
                 //输出来电号码
                 break;
                }
            }
    };

}
