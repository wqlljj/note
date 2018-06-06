package com.cloudminds.hc.hariservice.push;

import android.content.Context;

import com.cloudminds.hc.hariservice.command.CmdEvent;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.webrtc.LooperExecutor;
import com.getui.logful.util.LogUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.greenrobot.event.EventBus;

/**
 * Created by zoey on 2018/4/19.
 */

public class PushClient {
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions options;
    private static final String MQTT_SERVER = "tcp://10.11.35.179:1883";
    public static final String TOPIC = "testhari";
    private String clientId = "18610441497";
    private static PushClient instance;
    private static final String USERNAME = "hari";
    private static final String PASSWORD = "123456";
    private static final String TAG = "PushClient";
    private static LooperExecutor executor;

    private PushClient(Context context){
        executor = new LooperExecutor();
        executor.requestStart();
        options = new MqttConnectOptions();
        options.setCleanSession(false);
//            // 设置连接的用户名
        options.setUserName(USERNAME);
        // 设置连接的密码
        options.setPassword(PASSWORD.toCharArray());
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(10);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(20);
        options.setAutomaticReconnect(true);
        try {
            mqttAndroidClient = new MqttAndroidClient(context, MQTT_SERVER, clientId, new MemoryPersistence());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i(TAG, "create mqtt client error");
        }
        mqttAndroidClient.setCallback(mqttCallback);
    }

    private final MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            LogUtils.i(TAG, "lost:" + cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LogUtils.i(TAG, "receiveMsg:topic" + topic + ",msg:" + message.toString());

            try {
                String msg = new String(message.getPayload());
                CmdEvent event = new CmdEvent(CmdEvent.Event.PUSH_RECEIVED,msg);
                EventBus.getDefault().post(event);
            }catch (Exception e){
                LogUtil.d(TAG,e.getMessage());
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LogUtils.i(TAG, "msg published");
        }
    };

    public static PushClient instance(Context context){
        if (null == instance){
            synchronized (PushClient.class){
                instance = new PushClient(context);
            }
        }
        return instance;
    }

    public static PushClient getInstance(){
        return instance;
    }
    /**
     * 连接服务器
     * MqttService有自己的重连机制，在断线情况下会重连，但是首次连接失败后，需要再调用connect方法
     */
    public void connect() {

        if (mqttAndroidClient.isConnected()){
            return;
        }

        try {
            mqttAndroidClient.connect(options, this, iMqttActionListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private final IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            LogUtils.i(TAG, "Push server connected");
            try {
                mqttAndroidClient.subscribe(new String[]{TOPIC}, new int[]{2});
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            LogUtils.i(TAG, "Push server connect error:" + exception);
            connect();//当发生连接失败的情况时继续连接。通常只发生在服务器未在线情况，一旦服务器上线，将立刻连接。
        }
    };

    /**
     * 断开服务器链接
     */
    public void disConnect() {
        if (null == mqttAndroidClient) {
            return;
        }
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        LogUtils.i(TAG, "Push server disconnected");
    }

    public void destory(){
        disConnect();
        executor.requestStop();
    }
    /**
     * 发布消息
     *
     * @param topic topic
     * @param msg   消息内容
     */
    public void publish(final String topic, final String msg) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG,"publish message:"+msg);
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(msg.getBytes());
                mqttMessage.setRetained(true);
                mqttMessage.setQos(2);  //* @param qos   0：最多一次的传输；1：至少一次的传输；2： 只有一次的传输
                try {
                    IMqttDeliveryToken token = mqttAndroidClient.publish(topic, mqttMessage);
                    token.waitForCompletion();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
