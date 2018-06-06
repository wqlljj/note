package com.cloudminds.hc.metalib.features.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.installation.InstallationActivity;
import com.cloudminds.hc.metalib.features.installation.InstallationSchedulerService;
import com.cloudminds.hc.metalib.utils.NotificationUtil;


public class NotificationFeedbackReceiver extends BroadcastReceiver {

    public static final String EXTRA_KEY_IGNORE_BUILD_TIME = "EXTRA_KEY_IGNORE_BUILD_TIME";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case NotificationUtil.ACTION_REMIND_ME_LATER_INSTALL: // install later
                context.stopService(new Intent(context, InstallationSchedulerService.class));
                break;
            case NotificationUtil.ACTION_REMIND_ME_LATER_DOWNLOAD: // download later
                NotificationManagerCompat.from(context).cancel(NotificationUtil.NOTIFICATION_ID_NEW_VERSION);
                break;
            case NotificationUtil.ACTION_IGNORE_DOWNLOAD: // ignore this version
                String buildTime = intent.getStringExtra(EXTRA_KEY_IGNORE_BUILD_TIME);
                if (!TextUtils.isEmpty(buildTime)) {
                    Config.getInstance().setIgnoreVersionBuildTime(context, buildTime);
                }
                NotificationManagerCompat.from(context).cancel(NotificationUtil.NOTIFICATION_ID_NEW_VERSION);
                break;
            case NotificationUtil.ACTION_INSTALL: // install
                if (intent.hasExtra(NotificationUtil.INTENT_KEY_PACKAGE_INFO)) {
                    //// TODO: 2017/9/8 固件安装
                    Intent activityIntent = new Intent(intent); // copy data from intent
                    activityIntent.setClass(context, InstallationActivity.class);
                    context.startActivity(activityIntent);
                }
                break;
        }
    }
}
