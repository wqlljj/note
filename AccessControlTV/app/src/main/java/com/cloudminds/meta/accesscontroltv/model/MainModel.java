package com.cloudminds.meta.accesscontroltv.model;

import android.content.Intent;

import com.cloudminds.meta.accesscontroltv.mqtt.MQTTService;
import com.cloudminds.meta.accesscontroltv.persenter.InterFacePersenter;
import com.cloudminds.meta.accesscontroltv.persenter.MainPersenter;
import com.cloudminds.meta.accesscontroltv.view.MainActivity;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by WQ on 2018/4/10.
 */

public class MainModel implements InterFaceModel {
    InterFacePersenter persenter;
    private Intent intentService;

    public MainModel(InterFacePersenter persenter) {
        this.persenter = persenter;
        init();
    }

    private void init() {
        EventBus.getDefault().register(this);
        MainActivity view = (MainActivity) ((MainPersenter) persenter).getView();
        intentService = new Intent(view, MQTTService.class);
        view.startService(intentService);
    }
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void getMqttMessage(MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());
        persenter.onResponseSuccess(message);
    }

    @Override
    public void destory() {
        EventBus.getDefault().unregister(this);
        MainActivity view = (MainActivity) ((MainPersenter) persenter).getView();
        view.stopService(intentService);
        persenter=null;
    }
}
