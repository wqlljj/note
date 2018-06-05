package com.cloudminds.register.ui;

import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.cloudminds.register.repository.RegisterRepository;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.bean.MqttMessageInfo;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created
 */

class MainViewModel extends ViewModel {

    private final static String TAG = "MainViewModel";
    private final RegisterRepository mRepository;
    private Gson mGson;

    private MqttCallback mCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "connect connectionLost " + cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "connect messageArrived message = " + message.toString());
            try {
                MqttMessageInfo mqttMessageInfo = mGson.fromJson(message.toString(), MqttMessageInfo.class);
                EmployeeEntity employee = mqttMessageInfo.getList();
                switch (mqttMessageInfo.getStatus()) {
                    case MqttMessageInfo.ADD:
                        Log.i(TAG, "ADD");
                        insertEmployee(employee);
                        break;
                    case MqttMessageInfo.EDIT:
                        Log.i(TAG, "EDIT");
                        updateEmployee(employee);
                        break;
                    case MqttMessageInfo.DELETE:
                        Log.i(TAG, "DELETE");
                        deleteEmployee(employee.getId());
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "messageArrived = " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "connect deliveryComplete " + token);
        }
    };

    MainViewModel(RegisterRepository mRepository) {
        this.mRepository = mRepository;
        mGson = new Gson();
    }

    void initClient() {
        mRepository.initClient(mCallback);
    }

    void disconnect() {
        mRepository.disconnectClient();
    }

    private void updateEmployee(EmployeeEntity entity) {
        mRepository.updateEmployee(entity);
    }

    private void insertEmployee(EmployeeEntity entity) {
        mRepository.insertEmployee(entity);
    }

    private void deleteEmployee(int id) {
        mRepository.deleteEmployee(id);
    }

}
