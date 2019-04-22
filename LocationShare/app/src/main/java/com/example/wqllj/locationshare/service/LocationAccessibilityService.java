package com.example.wqllj.locationshare.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by cloud on 2018/9/25.
 */

public class LocationAccessibilityService extends AccessibilityService {

    private String TAG ="LocationAccessibility";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "无障碍已启动", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: "+event.getEventType() );

    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: " );
    }
}
