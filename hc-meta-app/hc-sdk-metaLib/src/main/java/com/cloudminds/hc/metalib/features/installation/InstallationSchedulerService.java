package com.cloudminds.hc.metalib.features.installation;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.utils.NotificationUtil;


public class InstallationSchedulerService extends JobService {

    public static final String ACTION_SHOW_NOTIFICATION = "com.cloudminds.updater.show.notification";

    public InstallationSchedulerService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        showInstallationNotification();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            boolean showNotification = intent.getBooleanExtra(ACTION_SHOW_NOTIFICATION, false);
            if (showNotification) {
                showInstallationNotification();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showInstallationNotification() {
        VersionPojo versionPojo = Config.getInstance().getCachedUpdatingInfo(this);
        if (null != versionPojo && versionPojo.hasNewVersion() && null != versionPojo.getNewest()) {
            versionPojo.getNewest().setDownloadStatus(ActualVersionInfo.DownloadStatus.DOWNLOADED);
            Config.getInstance().setCachedUpdatingInfo(this, versionPojo);
            Notification notification = NotificationUtil.getInstallationNotification(this, versionPojo);
            startForeground(NotificationUtil.NOTIFICATION_ID_DOWNLOADED, notification);
        }
    }
}
