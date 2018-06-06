package com.cloudminds.meta.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cloudminds.meta.service.asr.BusEvent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class StandardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }
    @Subscribe(sticky = false,priority = 0,threadMode = ThreadMode.MainThread)
    public void onEventBus(BusEvent ttsEvent){
        switch (ttsEvent.getEvent()) {
            case MOVETASKTOBACK:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
