package com.cloudminds.meta.accesscontroltv.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cloudminds.meta.accesscontroltv.constant.Constant;
import com.cloudminds.meta.accesscontroltv.view.MainActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.greenrobot.event.EventBus;

import static com.cloudminds.meta.accesscontroltv.mqtt.MQTTService.MqttType.CONNECTED;
import static com.cloudminds.meta.accesscontroltv.mqtt.MQTTService.MqttType.CONNECTING;
import static com.cloudminds.meta.accesscontroltv.mqtt.MQTTService.MqttType.DISCONNECTED;
import static com.cloudminds.meta.accesscontroltv.mqtt.MQTTService.MqttType.EXCEPTION;
import static com.cloudminds.meta.accesscontroltv.mqtt.MQTTService.MqttType.INIT;

public class MQTTService extends Service {
    public static final String TAG = "MQTT/MQTTService";

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    MqttType type=EXCEPTION;
    enum MqttType{EXCEPTION,INIT,CONNECTING,CONNECTED,DISCONNECTED}



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    public static void publish(String msg){
        String topic = Constant.TOPIC;
        Integer qos = 0;
        Boolean retained = false;
        try {
            if(client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue())==null){
                Log.e(TAG, "publish: 发送失败" );
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    int index=0;
    private void init() {
        index=index%26;
        Constant.CLIENTID=(char)('a'+index++)+Constant.CLIENTID.substring(1);
        // 服务器地址（协议+地址+端口号）
        client = new MqttAndroidClient(this, Constant.getHOST(), Constant.CLIENTID);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(5);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(10);
        // 用户名
        conOpt.setUserName(Constant.USERNAME);
        // 密码
        conOpt.setPassword(Constant.PASSWORD.toCharArray());

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + Constant.CLIENTID + "\"}";
        Log.e(TAG, "init: "+Constant.CLIENTID );
        String topic = Constant.TOPIC;
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
                Log.e(TAG, "init: setWill" );
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        type=INIT;
        if (doConnect) {
            doClientConnection();
        }

    }
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1000:
                    if(type!=DISCONNECTED) {
                        Toast.makeText(MQTTService.this, "尝试重连", Toast.LENGTH_SHORT).show();
                        init();
//                        doClientConnection();
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        try {
            type=DISCONNECTED;
            if(mHandler.hasMessages(1000)){
                mHandler.removeMessages(1000);
            }
            if(client.isConnected()) {
                client.disconnect();
            }
            client=null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /** 连接MQTT服务器 */
    private void doClientConnection() {
        Log.e(TAG, "doClientConnection: " );
        if (client!=null&&!client.isConnected() ) {
            if(isConnectIsNomarl()) {
                type=CONNECTING;
                try {
                    IMqttToken connect = client.connect(conOpt, null, iMqttActionListener);
                    Log.e(TAG, "doClientConnection: " + connect);
                } catch (MqttException e) {
                    e.printStackTrace();
                    if(type!=DISCONNECTED) {
                        type=EXCEPTION;
                        Toast.makeText(MQTTService.this, "连接失败", Toast.LENGTH_SHORT).show();
                        mHandler.sendEmptyMessageDelayed(1000, 5000);
                    }
                }
            }else{
                if(type!=DISCONNECTED) {
                    type = EXCEPTION;
                    Toast.makeText(MQTTService.this, "网络不可用", Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(1000, 20000);
                }
            }
        }else{
            if(!client.isConnected()) {
                type = EXCEPTION;
            }
        }

    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅myTopic话题
                IMqttToken subscribe = client.subscribe(Constant.TOPIC, 1);
                Log.e(TAG, "onSuccess: "+subscribe );
                type=CONNECTED;
                Toast.makeText(MQTTService.this, "连接成功", Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                e.printStackTrace();
                try {
                    client.disconnect();
                } catch (MqttException e1) {
                    e1.printStackTrace();
                }
                if(type!=DISCONNECTED) {
                    type = EXCEPTION;
                    Toast.makeText(MQTTService.this, "连接失败", Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(1000, 5000);
                }
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            if(type!=DISCONNECTED) {
                type = EXCEPTION;
                // 连接失败，重连
                Toast.makeText(MQTTService.this, "连接失败", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(1000, 5000);
            }
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            MqttMessage msg = new MqttMessage();
            msg.setPayload(message.getPayload());
            EventBus.getDefault().post(msg);
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "messageArrived:" + str1);
            Log.i(TAG, str2);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.e(TAG, "connectionLost: " );
            if(type!=DISCONNECTED) {
                type=EXCEPTION;
                Toast.makeText(MQTTService.this, "连接断开", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessage(1000);
                // 失去连接，重连
            }
        }
    };

    /** 判断网络是否连接 */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
