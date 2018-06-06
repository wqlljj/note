package com.cloudminds.hc.metalib.features.network.downloading;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.installation.InstallationActivity;
import com.cloudminds.hc.metalib.features.installation.VersionInfoDialog;
import com.cloudminds.hc.metalib.features.network.BaseNetworkTask;
import com.cloudminds.hc.metalib.features.network.checking.CheckingParams;
import com.cloudminds.hc.metalib.features.network.checking.UpdateChecker;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.Newest;
import com.cloudminds.hc.metalib.features.pojo.Packages;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.features.scheduler.UpdaterScheduler;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.cloudminds.hc.metalib.utils.NotificationUtil;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.hc.metalib.utils.UIThreadDispatcher;
import com.google.gson.Gson;

import java.io.File;
import java.text.NumberFormat;

/**
 * Created by willzhang on 16/06/17
 */

public class PackageDownloadService extends Service {

    public static final String INTENT_KEY_VERSION_INFO = "cloudminds.package.version.info";
    public static final String ACTION_CANCEL_DOWNLOAD = "cloudminds.action.cancel_download";
    public static final String ACTION_CONTINUE_DOWNLOAD = "cloudminds.action.continue_download";

    public static boolean isDownloading = false;
    public static boolean isServiceRunning = false;

    private PackageDownloader downloader;
    private DownloadStatusReceiver downloadStatusReceiver;
    private int initConnectionType;
    private long packageSize;
    private ActualVersionInfo mActualVersionInfo;

    public PackageDownloadService() {
        File packageFolder = new File(Environment.getExternalStorageDirectory(), BaseNetworkTask.FOLDER_NAME);
        if (!packageFolder.exists()) {
            boolean created = packageFolder.mkdirs();
            if (!created) {
                DLog.e("Cannot create package folder:" + packageFolder);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 开始下载更新包（下载之前会在检查一次更新，确保token是最新的）
     */
    public static void checkAndStart(Context mContext, ActualVersionInfo versionInfo) {
        start(mContext, versionInfo);
    }

    public static void forceDownloadIfNeeded(Context mContext, VersionPojo versionPojo) {
        Newest newest;
        if (null != versionPojo
                && versionPojo.hasNewVersion()
                && null != (newest = versionPojo.getNewest())
                && newest.isForceUpgradeNeeded()) { // 如果是强制更新就直接下载
//            if (!Config.isGongan3() && NetworkUtil.getConnectionType(mContext) != ConnectivityManager.TYPE_WIFI) {
            if (!true && NetworkUtil.getConnectionType(mContext) != ConnectivityManager.TYPE_WIFI) {
                // 如果不是gongan3，且不处于WIFI状态, 就不立即下载
                DLog.d("Force upgrade detected, but not on WIFI state, ignore");
                return;
            }
            start(mContext, newest);
        }
    }

    public static boolean isDownloaderServiceAvailable(final Context mContext) {
        if (PackageDownloadService.isDownloading) { // 正在下载
            UIThreadDispatcher.dispatch(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(mContext, R.string.toast_wait_downloading);
                }
            });
            return false;
        }
        if (-1 == NetworkUtil.getConnectionType(mContext)) { // 没有网络
            UIThreadDispatcher.dispatch(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(mContext, R.string.toast_text_no_available_connection);
                }
            });
            return false;
        }
        return true;
    }

    private static void start(Context mContext, ActualVersionInfo versionInfo) {
        Intent downloadIntent = new Intent(mContext, PackageDownloadService.class);
        downloadIntent.putExtra(INTENT_KEY_VERSION_INFO, versionInfo);
        mContext.startService(downloadIntent);
    }

    @Override
    public int onStartCommand(final @Nullable Intent intent, int flags, int startId) {
        if (isDownloading) {
            DLog.d("Downloading, ignore");
            return START_STICKY;
        }
        isDownloading = true;
        isServiceRunning = true;
        if (null == downloadStatusReceiver) {
            downloadStatusReceiver = new DownloadStatusReceiver();
            IntentFilter filter = new IntentFilter(ACTION_CONTINUE_DOWNLOAD);
            filter.addAction(ACTION_CANCEL_DOWNLOAD);
            registerReceiver(downloadStatusReceiver, filter);
        }

        HandlerThread thread = new HandlerThread("DownloadPackage");
        thread.start();
        Handler downloadHandler;
        downloadHandler = new Handler(thread.getLooper());
        downloadHandler.post(new Runnable() {
            @Override
            public void run() {

                if (null != intent && intent.hasExtra(INTENT_KEY_VERSION_INFO)) {
                    if (-1 == NetworkUtil.getConnectionType(PackageDownloadService.this)) {
                        isDownloading = false;
                        DLog.d("Network not available, ignore download");
                        return;
                    }
                    mActualVersionInfo = (ActualVersionInfo) intent.getSerializableExtra(INTENT_KEY_VERSION_INFO);
                    if (null == mActualVersionInfo) {
                        isDownloading = false;
                        DLog.e("Invalid params");
                        return;
                    }
                    String versionName = mActualVersionInfo.getName();
                    if (Build.DISPLAY.equals(versionName)) {
                        isDownloading = false;
                        DLog.d("Ignore current version");
                        return;
                    }

                    Packages downloadPackage = mActualVersionInfo.getPackages()[0];
                    final String packageName = downloadPackage.getName();
                    boolean forceDownload = mActualVersionInfo.isForceUpgradeNeeded();
                    // gongan3是土豪，忽略网络状态
                    if (true) {
                        // 忽略漫游状态下的下载行为
                        if (NetworkUtil.isRoamingAnd3G(PackageDownloadService.this)) {
                            DLog.d("Roaming, ignore download");
                            isDownloading = false;
                            return;
                        }
                    }
                    if (forceDownload) {
                        String packageUrl = mActualVersionInfo.getPackages()[0].getToken();
                        download(packageUrl, packageName);
                    } else {
                        CheckingParams params = new CheckingParams();
                        Gson gson = new Gson();
                        String postData = gson.toJson(params);
                        UpdateChecker checker = new UpdateChecker(PackageDownloadService.this) {
                            // 这里重写是为了忽略强制下载，以免无限递归
                            @Override
                            protected boolean needHandleForceUpgrade() {
                                return false;
                            }
                        };
                        // Check updates to get the latest token
                        checker.check(postData, new UpdateChecker.CheckUpdateCallback() {
                            @Override
                            public void onCheckDone(int responseCode, VersionPojo versionPojo) {
                                // 下载之前先检查一次更新，以便获取最新token(服务器返回的token是一个包含token的url)
                                if (responseCode == PackageDownloader.RESPONSE_CODE_SUCCESS) {
                                    initConnectionType = NetworkUtil.getConnectionType(PackageDownloadService.this);
                                    ActualVersionInfo.DownloadStatus status = ActualVersionInfo.DownloadStatus.NOT_STARTED;
                                    ActualVersionInfo downloadingVersionInfo = Config.getInstance().getDownloadingPackageInfo(PackageDownloadService.this);
                                    if (null != downloadingVersionInfo) {
                                        if (null != packageName && packageName.equals(downloadingVersionInfo.getPackages()[0].getName())) {
                                            // 记录下载状态
                                            status = downloadingVersionInfo.getDownloadStatus();
                                        }
                                    }
                                    mActualVersionInfo = versionPojo.getVersionInfo(packageName);
                                    mActualVersionInfo.setDownloadStatus(status);
                                    if (null != mActualVersionInfo) {
                                        Packages packages = mActualVersionInfo.getPackages()[0];
                                        download(packages.getToken(), packages.getName());
                                        packageSize = Long.valueOf(packages.getSize());
                                    }
                                }
                            }
                        });
                    }
                } else { // Service 重启，继续下载
                    mActualVersionInfo = Config.getInstance().getDownloadingPackageInfo(PackageDownloadService.this);
                    if (null != mActualVersionInfo) {
                        Packages downloadingPackage = mActualVersionInfo.getPackages()[0];
                        if (null != downloadingPackage) {
                            DLog.d("Continue to download package:" + mActualVersionInfo.getSimplename());
                            download(downloadingPackage.getToken(), downloadingPackage.getName());
                        }
                    }
                }
            }
        });
        return START_STICKY;
    }

    private void download(String packageUrl, String packageName) {
        if (!TextUtils.isEmpty(packageUrl) && !TextUtils.isEmpty(packageName)) {
            File downloadFolder = PackageDownloader.getPackageFolder();
            if (null != downloadFolder && downloadFolder.exists() && downloadFolder.isDirectory()) {
                long freeSpace = new StatFs(downloadFolder.getPath()).getAvailableBytes();
                long packageSize = Long.parseLong(mActualVersionInfo.getPackages()[0].getSize());
                if (freeSpace - packageSize < packageSize + packageSize * 0.1) { // 剩余空间比安装包大10%才开始下载
                    UIThreadDispatcher.dispatch(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(PackageDownloadService.this, R.string.state_error_disk_space);
                        }
                    });
                    isDownloading = false;
                    return;
                }
            } else {
                UIThreadDispatcher.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show(PackageDownloadService.this, R.string.state_error_disk_space_check);
                    }
                });
                isDownloading = false;
                return;
            }

            Config.getInstance().setDownloadingPackageInfo(this, mActualVersionInfo);
            downloader = new PackageDownloader();
            final boolean isForceDownload = mActualVersionInfo.isForceUpgradeNeeded();
            Notification notification = NotificationUtil.getDownloadingNotification(this, 0, isForceDownload);

            startForeground(NotificationUtil.NOTIFICATION_ID_DOWNLOADING, notification);
            downloader.download(packageUrl, packageName, new PackageDownloader.OnProgressListener() {

                private String lastPercentage;

                @Override
                public void onProgressUpdated(long total, long progressed) {
                    try {
                        isDownloading = true;
                        double percent = (double) progressed / total;
                        NumberFormat numberFormat = NumberFormat.getPercentInstance();
                        numberFormat.setMinimumFractionDigits(1); // 保留小数点后1位
                        String percentage = numberFormat.format(percent);
                        // 和上一次的数据不一样才发通知
                        if (null != percentage && !percentage.equals(lastPercentage)) {
                            lastPercentage = percentage;
                            Intent intent = new Intent(CMUpdaterActivity.ACTION_DOWNLOAD_PROGRESS);
                            intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOAD_PROGRESS, percentage);
                            intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOADING_VERSION, mActualVersionInfo);
                            intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOADED_SIZE, progressed);
                            sendBroadcast(intent);
                            Notification notification = NotificationUtil.getDownloadingNotification(
                                    PackageDownloadService.this, (int) (percent * 100), isForceDownload);
                            NotificationManagerCompat.from(PackageDownloadService.this)
                                    .notify(NotificationUtil.NOTIFICATION_ID_DOWNLOADING, notification);
                        }
                    } catch (Exception e) {
                        DLog.e(e);
                        Intent intent = new Intent(CMUpdaterActivity.ACTION_DOWNLOAD_PROGRESS);
                        intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOAD_PROGRESS, "N.A");
                        sendBroadcast(intent);
                    }
                }
            }, new PackageDownloader.DownloadCallback() {
                @Override
                public void onDownloadFinished(int responseCode) {

                    Intent intent = new Intent(CMUpdaterActivity.ACTION_DOWNLOAD_DONE);
                    intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_RESPONSE_CODE, responseCode);
                    intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOADING_VERSION, mActualVersionInfo);
                    if (responseCode == PackageDownloader.RESPONSE_CODE_SUCCESS) { // 下载成功
                        DLog.d("Download finished");
                        // Force upgrade
                        if (null != mActualVersionInfo && mActualVersionInfo.isForceUpgradeNeeded()) {
                            //// TODO: 2017/9/8 固件安装
                            Intent activityIntent = new Intent(PackageDownloadService.this, InstallationActivity.class);
                            activityIntent.putExtra(NotificationUtil.INTENT_KEY_PACKAGE_INFO, mActualVersionInfo);
                            startActivity(activityIntent);
                        } else {
                            UpdaterScheduler.scheduleInstallation(PackageDownloadService.this);
                        }
                    } else if (responseCode == PackageDownloader.RESPONSE_CODE_CANCEL) { // 取消下载
                        DLog.d("Download canceled");
                    } else { // 下载失败
                        DLog.d("Download failed");
                    }
                    sendBroadcast(intent);
                    isDownloading = false;
                    if (responseCode == PackageDownloader.RESPONSE_CODE_SUCCESS || responseCode == PackageDownloader.RESPONSE_CODE_CANCEL) {
                        stopSelf(); // 下载成功/取消，关闭service, 下载失败时，不stop service
                    } else {
                        stopForeground(true);
                    }
                }
            } );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != downloadStatusReceiver) {
            unregisterReceiver(downloadStatusReceiver);
            downloadStatusReceiver = null;
        }
        isDownloading = false;
        isServiceRunning = false;
    }

    private class DownloadStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_CONTINUE_DOWNLOAD.equals(action)) {
                int connectionType = NetworkUtil.getConnectionType(context);
                // 1.安装包大小大于100M
                // 2.网络连接类型和初始的不一样（初始的时候不是wifi的话仍继续下载）
                // 3.网络连接不是wifi(处于流量下载状态)
                // 满足以上条件就cancel下载
                if (packageSize > VersionInfoDialog.SIZE_MOBILE_DATA_LIMIT &&
                        initConnectionType != connectionType &&
                        connectionType != ConnectivityManager.TYPE_WIFI) {
                    if (null != downloader) {
                        downloader.setNeedCancel(true);
                    }
                    // 1. 网络链接是WIFI
                    // 2. 已经开启了WIFI自动下载
                    // 满足以上条件自动继续下载
                } else if (connectionType == ConnectivityManager.TYPE_WIFI
                        && Config.getInstance().isAutoDownloadOnWifi(context)) {
                    mActualVersionInfo = (ActualVersionInfo) intent.getSerializableExtra(ACTION_CONTINUE_DOWNLOAD);
                    if (null != mActualVersionInfo && ActualVersionInfo.DownloadStatus.UserCanceled != mActualVersionInfo.getDownloadStatus()) {
                        Packages packages = mActualVersionInfo.getPackages()[0];
                        start(context, mActualVersionInfo);
                        packageSize = Long.valueOf(packages.getSize());
                    }
                }
            } else if (ACTION_CANCEL_DOWNLOAD.equals(action)) {
                // 默认是用户cancel的，也有可能是断网触发的强制cancel
                boolean isCancelByUser = intent.getBooleanExtra(ACTION_CANCEL_DOWNLOAD, true);
                if (null != mActualVersionInfo) {
                    if (isCancelByUser) {
                        mActualVersionInfo.setDownloadStatus(ActualVersionInfo.DownloadStatus.UserCanceled);
                    } else {
                        mActualVersionInfo.setDownloadStatus(ActualVersionInfo.DownloadStatus.NOT_STARTED);
                    }
                    // Update downloading package info
                    Config.getInstance().setDownloadingPackageInfo(context, mActualVersionInfo);
                }
                if (null != downloader) {
                    downloader.setNeedCancel(true, isCancelByUser);
                }
            }
        }
    }
}
