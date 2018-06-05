package com.cloudminds.register.repository.network;


import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * ClassName: MQTTManager
 */

public class MQTTManager {
    public static final String TAG = "MQTTManager";
    private volatile static MQTTManager INSTANCE;
    private MqttClient mClient;

    public static MQTTManager getInstance() {
        if (INSTANCE == null) {
            synchronized (MQTTManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MQTTManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * MQTT init.
     */
    public void init(MqttCallback callback) {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mClient = new MqttClient(Constant.Connection.BROKER, Constant.Connection.CLIENTID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setPassword(Constant.Connection.PASSWORD.toCharArray());
            connOpts.setUserName(Constant.Connection.USERNAME);
            connOpts.setAutomaticReconnect(true);
            mClient.setCallback(callback);
            Log.i(TAG, "init : " + mClient.isConnected());
            if (!mClient.isConnected()) {
                mClient.connect(connOpts);
                mClient.subscribe(Constant.Connection.TOPIC);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "init MqttException " + e.getMessage());
        }
    }

    /**
     * MQTT disconnect.
     */
    public void disconnect() {
        Log.d(TAG, "disconnect : " + mClient.isConnected());
        if (!mClient.isConnected()) {
            return;
        }
        try {
            mClient.unsubscribe(Constant.Connection.TOPIC);
            mClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "disconnect MqttException " + e.getMessage());
        }
    }

}
