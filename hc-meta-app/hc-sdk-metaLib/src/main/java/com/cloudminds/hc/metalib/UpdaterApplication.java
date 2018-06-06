package com.cloudminds.hc.metalib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloadService;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.utils.NetworkUtil;


/**
 * Created by willzhang on 12/07/17
 */

public class UpdaterApplication  {
    private static UpdaterApplication updaterApplication;
    private static Activity activity;

    public UpdaterApplication(Context context) {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        NetworkStatusReceiver receiver = new NetworkStatusReceiver();
        context.registerReceiver(receiver, filter);
    }
    public static void setShower(Activity activity){
        UpdaterApplication.activity=activity;
    }
    public static void error(String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public static void init(Context context) {
        if(updaterApplication==null)
        updaterApplication = new UpdaterApplication( context);
    }

    private class NetworkStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)&& PackageDownloadService.isDownloading) {
                int connectionType = NetworkUtil.getConnectionType(context);

                // GONGAN3是土豪, 有网就直接下载Config.isGongan3()
                if ( true&& connectionType != -1) {
                    tryContinueDownloading(context);
                    return;
                }

                // 网络连接不是wifi就cancel下载
                if (connectionType != ConnectivityManager.TYPE_WIFI) {
                    Intent cancelIntent = new Intent(PackageDownloadService.ACTION_CANCEL_DOWNLOAD);
                    cancelIntent.putExtra(PackageDownloadService.ACTION_CANCEL_DOWNLOAD, false);
                    context.sendBroadcast(cancelIntent);
                } else if (Config.getInstance().isAutoDownloadOnWifi(context)) {
                    tryContinueDownloading(context);
                }
            }
        }

        private void tryContinueDownloading(Context context) {
            ActualVersionInfo versionInfo = Config.getInstance().getDownloadingPackageInfo(context);
            if (null != versionInfo) {
                if (PackageDownloadService.isServiceRunning) { // service正在运行
                    Intent continueIntent = new Intent(PackageDownloadService.ACTION_CONTINUE_DOWNLOAD);
                    continueIntent.putExtra(PackageDownloadService.ACTION_CONTINUE_DOWNLOAD, versionInfo);
                    context.sendBroadcast(continueIntent);
                } else {
                    // 当有正在下载的包且不处于被用户暂停的状态
                    if (ActualVersionInfo.DownloadStatus.UserCanceled != versionInfo.getDownloadStatus()) {
                        PackageDownloadService.checkAndStart(context, versionInfo);
                    }
                }
            }
        }
    }
    public interface Installer{
        void install(byte[] data);
    }
}
