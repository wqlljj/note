package com.cloudminds.hc.metalib.features.installation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.BatteryManager;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by willzhang on 20/06/17
 */

class InstallationDialog {

    private final ActualVersionInfo versionInfo;
    private AlertDialog confirmDialog;
    private TaskFinishCallback callback;

    interface TaskFinishCallback {
        void onTaskFinished();
    }

    InstallationDialog(Context mContext, ActualVersionInfo versionInfo) {
        this.versionInfo = versionInfo;
        initConfirmDialog(mContext);
    }

    private void initConfirmDialog(final Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog);
        builder.setCancelable(true);
        builder.setTitle(R.string.dialog_title_install);
        builder.setMessage(R.string.dialog_msg_install);
        builder.setPositiveButton(R.string.button_install, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BatteryManager bm = (BatteryManager) mContext.getSystemService(BATTERY_SERVICE);
                int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//            if (batLevel < 30) {
//                dialog.dismiss();
//                ToastUtil.show(mContext, R.string.toast_text_battery);
//                if (null != callback) {
//                    callback.onTaskFinished();
//                }
//                return;
//            }
                mContext.stopService(new Intent(mContext, InstallationSchedulerService.class));
                install(mContext);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (null != callback) {
                    callback.onTaskFinished();
                }
            }
        });
        confirmDialog = builder.create();
        confirmDialog.setCanceledOnTouchOutside(false);
    }

    private void install(final Context mContext) {
        Intent serviceIntent = new Intent(mContext, PackageInstallService.class);
        serviceIntent.putExtra(PackageInstallService.INTENT_KEY_PACKAGE_INFO, versionInfo);
        mContext.startService(serviceIntent);
    }

    public void show() {
        confirmDialog.show();
    }

    void setOnTaskFinishCallback(TaskFinishCallback callback) {
        if (null != callback) {
            this.callback = callback;
        }
    }
}
