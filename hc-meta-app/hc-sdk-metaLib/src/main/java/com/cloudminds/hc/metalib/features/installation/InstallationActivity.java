package com.cloudminds.hc.metalib.features.installation;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.features.BaseActivity;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.utils.NotificationUtil;
import com.cloudminds.hc.metalib.utils.ToastUtil;


public class InstallationActivity extends BaseActivity implements InstallationDialog.TaskFinishCallback {

    public static final String ACTION_UPDATE_DIALOG = "ACTION_UPDATE_DIALOG";
    public static final String ACTION_MD5_CHECK_DONE = "ACTION_MD5_CHECK_DONE";
    public static final String INTENT_KEY_DIALOG_MSG = "INTENT_KEY_DIALOG_MSG";
    public static final String INTENT_KEY_MD5_CHECK_RESULT = "INTENT_KEY_MD5_CHECK_RESULT";

    private InstallServiceReceiver receiver;
    private ProgressDialog preInstallDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ActualVersionInfo versionInfo = (ActualVersionInfo) intent.getSerializableExtra(NotificationUtil.INTENT_KEY_PACKAGE_INFO);
        if (null == versionInfo) {
            return;
        }
        InstallationDialog.TaskFinishCallback callback = this;

        if (null == receiver) {
            receiver = new InstallServiceReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_MD5_CHECK_DONE);
            filter.addAction(ACTION_UPDATE_DIALOG);
            registerReceiver(receiver, filter);
        }
        if (versionInfo.isForceUpgradeNeeded()) {
            Intent serviceIntent = new Intent(this, PackageInstallService.class);
            serviceIntent.putExtra(PackageInstallService.INTENT_KEY_PACKAGE_INFO, versionInfo);
            startService(serviceIntent);
        } else {
            InstallationDialog dialog = new InstallationDialog(this, versionInfo);
            dialog.setOnTaskFinishCallback(callback);
            dialog.show();
        }

        preInstallDialog = new ProgressDialog(this);
        preInstallDialog.setIndeterminate(true);
//        preInstallDialog.setTitle(R.string.dialog_title_pre_install);
        preInstallDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        preInstallDialog.setCancelable(false);
        preInstallDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != receiver) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onTaskFinished() {
        finish();
    }

    private class InstallServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroyed()) {
                return;
            }
            String action = intent.getAction();
            if (ACTION_MD5_CHECK_DONE.equals(action)) {
                if (null != preInstallDialog && preInstallDialog.isShowing()) {
                    preInstallDialog.dismiss();
                }
                if (!intent.getBooleanExtra(INTENT_KEY_MD5_CHECK_RESULT, false)) { // md5不匹配
                    ToastUtil.show(InstallationActivity.this.getApplicationContext(), R.string.toast_text_package_verify_failed);
                }
                finish();
            } else if (ACTION_UPDATE_DIALOG.equals(action)) {
                if (null != preInstallDialog) {
                    if (!preInstallDialog.isShowing()) {
                        preInstallDialog.show();
                    }
                    String msg = intent.getStringExtra(INTENT_KEY_DIALOG_MSG);
                    preInstallDialog.setMessage(msg);
                }
            }
        }
    }
}
