package com.cloudminds.hc.metalib.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.v4.app.NotificationManagerCompat;

import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloadService;
import com.cloudminds.hc.metalib.features.pojo.Newest;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.features.scheduler.NotificationFeedbackReceiver;


/**
 * Created by willzhang on 20/06/17
 */

public final class NotificationUtil {

    public static final int NOTIFICATION_ID_NEW_VERSION = 10001;
    public static final int NOTIFICATION_ID_DOWNLOADING = 10002;
    public static final int NOTIFICATION_ID_DOWNLOADED = 10003;
    public static final int NOTIFICATION_ID_INSTALL = 10004;
    private static final int MAX_PROGRESS = 100;

    /////////If action here modified, need make the same changes at manifest file also>>>>>>>>>
    public static final String ACTION_REMIND_ME_LATER_DOWNLOAD = "com.cloudminds.updater.remindmelater.download";
    public static final String ACTION_IGNORE_DOWNLOAD = "com.cloudminds.updater.ignore.download";
    public static final String ACTION_REMIND_ME_LATER_INSTALL = "com.cloudminds.updater.remindmelater.install";
    public static final String ACTION_INSTALL = "com.cloudminds.updater.install";
    /////////If action here modified, need make the same changes at manifest file also<<<<<<<<<<

    public static final String INTENT_KEY_PACKAGE_INFO = "cloudminds.package.info";

    private NotificationUtil() {}

    public static void showNewVersionDetectedNotification(Context mContext, VersionPojo versionPojo) {
        if(true)return;
        Newest newest = versionPojo.getNewest();

        boolean isForceDownload = newest.isForceUpgradeNeeded();
        if (!isForceDownload) { // 不是强制更新版本
            String ignoreVersionBuildTime = Config.getInstance().getIgnoreVersionBuildTime(mContext);
            if (ignoreVersionBuildTime.equals(newest.getTime())) { // ignore this version
                DLog.d("Ignore notification for:" + newest.getSimplename());
                return;
            }
        }

        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.stat_notify_update);
        builder.setContentText(mContext.getString(R.string.state_action_available));
        builder.setShowWhen(false);
        builder.setAutoCancel(true);

        // Open Updater
        Intent resultIntent = new Intent(mContext, CMUpdaterActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pIntent);

        if (!isForceDownload) {
            // Remind Me Later
            PendingIntent remindIntent = PendingIntent.getBroadcast(mContext, 1,
                    new Intent(ACTION_REMIND_ME_LATER_DOWNLOAD), PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Action remindMeLater = getAction(mContext, remindIntent, R.string.action_text_remind);
            builder.addAction(remindMeLater);

            // Ignore this version
            Intent iIntent = new Intent(ACTION_IGNORE_DOWNLOAD);
            iIntent.putExtra(NotificationFeedbackReceiver.EXTRA_KEY_IGNORE_BUILD_TIME, newest.getTime());
            PendingIntent ignoreIntent = PendingIntent.getBroadcast(mContext, 2, iIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Action ignoreThisVersion = getAction(mContext, ignoreIntent, R.string.action_text_ignore);
            builder.addAction(ignoreThisVersion);
        }

        // Start to download
        Intent dIntent = new Intent(mContext, PackageDownloadService.class);
        dIntent.putExtra(PackageDownloadService.INTENT_KEY_VERSION_INFO, newest);
        PendingIntent downloadIntent = PendingIntent.getService(mContext, 3, dIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action download = getAction(mContext, downloadIntent, R.string.action_text_download);
        builder.addAction(download);

        builder.setAutoCancel(true);
        Notification notification = builder.build();
        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID_NEW_VERSION, notification);
    }

    private static Notification.Action getAction(Context mContext, PendingIntent pendingIntent, int labelResId) {
        Notification.Action.Builder builder = new Notification.Action.Builder(
                Icon.createWithResource(mContext, R.drawable.icon_system_update_192),
                mContext.getString(labelResId),
                pendingIntent
        );
        return builder.build();
    }

    public static Notification getDownloadingNotification(Context mContext, int progress, boolean isForceDownload) {
        NotificationManagerCompat.from(mContext).cancel(NOTIFICATION_ID_NEW_VERSION);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.stat_notify_update);
        builder.setContentText(mContext.getString(R.string.state_action_downloading));
        builder.setShowWhen(false);
        builder.setAutoCancel(false);

        // Open Updater
        Intent resultIntent = new Intent(mContext, CMUpdaterActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pIntent);

        if (!isForceDownload) {
            Intent cIntent = new Intent(PackageDownloadService.ACTION_CANCEL_DOWNLOAD);
            PendingIntent cancelIntent = PendingIntent.getBroadcast(mContext, 0, cIntent, PendingIntent.FLAG_ONE_SHOT);
            Notification.Action cancelDownload = getAction(mContext, cancelIntent, android.R.string.cancel);
            builder.addAction(cancelDownload);
        }

        if (MAX_PROGRESS > progress) {
            String percentage = mContext.getString(R.string.notification_progress, progress);
            builder.setContentText(percentage);
            builder.setProgress(MAX_PROGRESS, progress, false);
        } else {
            builder.setContentText(mContext.getString(R.string.state_action_downloaded));
        }

        return builder.build();
    }

    public static Notification getInstallationNotification(Context mContext, VersionPojo versionPojo) {
        Newest newest = versionPojo.getNewest();

        boolean isForceDownload = newest.isForceUpgradeNeeded();
        if (isForceDownload) { // 不是强制更新版本
            String ignoreVersionBuildTime = Config.getInstance().getIgnoreVersionBuildTime(mContext);
            if (ignoreVersionBuildTime.equals(newest.getTime())) { // ignore this version
                DLog.d("Ignore notification for:" + newest.getSimplename());
                return null;
            }
        }

        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.stat_notify_update);
        builder.setContentText(mContext.getString(R.string.state_action_downloaded));
        builder.setShowWhen(false);
        builder.setAutoCancel(true);

        // Open Updater
        Intent resultIntent = new Intent(mContext, CMUpdaterActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 80000, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pIntent);

        if (!isForceDownload) {
            // Remind Me Later
            Intent rIntent = new Intent(ACTION_REMIND_ME_LATER_INSTALL);
            PendingIntent remindIntent = PendingIntent.getBroadcast(mContext, 80001, rIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Action remindMeLater = getAction(mContext, remindIntent, R.string.action_text_remind);
            builder.addAction(remindMeLater);
        }

        // Start to install
        Intent installIntent = new Intent(ACTION_INSTALL);
        installIntent.putExtra(INTENT_KEY_PACKAGE_INFO, newest);
        PendingIntent downloadedIntent = PendingIntent.getBroadcast(mContext, 80002, installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action install = getAction(mContext, downloadedIntent, R.string.button_install);
        builder.addAction(install);

        builder.setAutoCancel(true);
        return builder.build();
    }

    public static Notification getCheckMd5Notification(Context mContext) {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.stat_notify_update);
        builder.setContentText(mContext.getString(R.string.dialog_msg_md5_checked));
        builder.setShowWhen(false);
        builder.setAutoCancel(true);
        return builder.build();
    }
}
