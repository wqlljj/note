package com.cloudminds.hc.metalib.features.installation;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.UpdaterApplication;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloader;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.Packages;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.FileUtils;
import com.cloudminds.hc.metalib.utils.MD5;
import com.cloudminds.hc.metalib.utils.NotificationUtil;

import java.io.File;


public class PackageInstallService extends IntentService {

    public static final int WAITING_TIME_BEFORE_FORCE_UPGRADE = 10;
    public static final String INTENT_KEY_PACKAGE_INFO = "cloudminds.updater.package.info";

    private File packageFile;
    private boolean needWipeData;
    private boolean isForceUpgrade;
    private String packageMd5;

    public PackageInstallService() {
        super("PackageInstallService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Notification notification = NotificationUtil.getCheckMd5Notification(this);
            startForeground(NotificationUtil.NOTIFICATION_ID_INSTALL, notification);
            ActualVersionInfo versionInfo = (ActualVersionInfo) intent.getSerializableExtra(INTENT_KEY_PACKAGE_INFO);
            if (null != versionInfo) {
                File packageFolder = PackageDownloader.getPackageFolder();
                Packages packages = versionInfo.getPackages()[0];
                packageFile = new File(packageFolder, packages.getName());
                needWipeData = versionInfo.needRemoveData();
                packageMd5 = versionInfo.getPackages()[0].getMd5();
                isForceUpgrade = versionInfo.isForceUpgradeNeeded();

                boolean isPackageValid = checkPackageMd5();
                Intent checkDoneIntent = new Intent(InstallationActivity.ACTION_MD5_CHECK_DONE);
                checkDoneIntent.putExtra(InstallationActivity.INTENT_KEY_MD5_CHECK_RESULT, isPackageValid);
                checkDoneIntent.setPackage(getPackageName());
                sendBroadcast(checkDoneIntent);
                if (isPackageValid) {
                    installPackage();
                } else {
                    boolean deleted = packageFile.delete();
                    DLog.d("MD5 not matched, delete package file:" + deleted);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setIntentRedelivery(true);
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean checkPackageMd5() {
        Intent intent;
        if (isForceUpgrade) { // 如果是强制更新, 就显示一个倒计时的对话框
            for (int i = WAITING_TIME_BEFORE_FORCE_UPGRADE; i > 0; i--) {
                String msg = getResources().getQuantityString(R.plurals.dialog_msg_pre_install, i, i);
                intent = new Intent(InstallationActivity.ACTION_UPDATE_DIALOG);
                intent.putExtra(InstallationActivity.INTENT_KEY_DIALOG_MSG, msg);
                intent.setPackage(getPackageName());
                sendBroadcast(intent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        intent = new Intent(InstallationActivity.ACTION_UPDATE_DIALOG);
        intent.putExtra(InstallationActivity.INTENT_KEY_DIALOG_MSG, getString(R.string.dialog_title_check_md5));
        intent.setPackage(getPackageName());
        sendBroadcast(intent);

        if (TextUtils.isEmpty(packageMd5)) {
            DLog.e("!!!Invalid md5!!!");
            return false;
        }
        try {
            return MD5.check(packageFile, packageMd5);
        } catch (Exception e) {
            e.printStackTrace();
            DLog.e("!!!Check md5 failed!!!");
        }
        return false;
    }

    @SuppressLint("SdCardPath")
    private void installPackage() {
        try {
            Config.getInstance().setDownloadingPackageInfo(this, null);
            Config.getInstance().setCachedUpdatingInfo(this, null);

            if (needWipeData) {
                Config.getInstance().setWipeDataOnNextReboot(this, true);
            }
            String newPath = packageFile.getAbsolutePath();
            //TODO 固件安装
            Log.e("PackageInstallService", "installPackage: 安装固件："+newPath );
            byte[] bytes = FileUtils.unzip_MetaRom(newPath);
            if(bytes!=null){
                HCMetaUtils.updateFirmwareAsync(bytes);
            }else{
                UpdaterApplication.error("固件解压失败");
            }
//            if (newPath.contains("/storage/sdcard1/")) {
//                newPath = newPath.replace("/storage/sdcard1/", "/sdcard/");
//            } else {
//                newPath = newPath.replace("/storage/emulated/", "/data/media/");
//                newPath = newPath.replace("/mnt/sdcard/", "/data/media/");
//            }
//            DLog.d("===========Install=============");
//            RecoverySystem.installPackage(this, new File(newPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
