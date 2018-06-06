package com.cloudminds.hc.metalib.features;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by willzhang on 15/06/17
 */

public class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean isAllPermissionGranted() {
        // PERMISSION_ACCESS_CACHE_FILESYSTEM
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED/* ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_CACHE_FILESYSTEM)
                        != PackageManager.PERMISSION_GRANTED*/) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.ACCESS_CACHE_FILESYSTEM,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    REQUEST_CODE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_PERMISSION == requestCode) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    finish();
                    return;
                }
            }
            onPermissionGranted();
        }
    }

    protected void onPermissionGranted() {
    }


    @SuppressWarnings("unchecked")
    public <T extends View> T v(int resId) {
        return (T) findViewById(resId);
    }
}
