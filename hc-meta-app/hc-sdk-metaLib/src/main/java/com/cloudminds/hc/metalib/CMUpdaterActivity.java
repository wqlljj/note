package com.cloudminds.hc.metalib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;


import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.BaseActivity;
import com.cloudminds.hc.metalib.features.installation.InstallationActivity;
import com.cloudminds.hc.metalib.features.installation.VersionInfoDialog;
import com.cloudminds.hc.metalib.features.network.checking.CheckingParams;
import com.cloudminds.hc.metalib.features.network.checking.UpdateChecker;
import com.cloudminds.hc.metalib.features.network.checking.UpdateCheckingService;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloadService;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloader;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.Newest;
import com.cloudminds.hc.metalib.features.pojo.Packages;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.features.settings.SettingDialog;
import com.cloudminds.hc.metalib.features.version.VersionDetailActivity;
import com.cloudminds.hc.metalib.features.version.VersionListActivity;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.cloudminds.hc.metalib.utils.NotificationUtil;
import com.cloudminds.hc.metalib.utils.TimeUtil;
import com.cloudminds.hc.metalib.utils.ToastUtil;

import java.io.File;
import java.text.NumberFormat;

public class CMUpdaterActivity extends BaseActivity {

    public static final String ACTION_CHECK_DONE = "cloudminds.action.check.done";
    public static final String ACTION_DOWNLOAD_PROGRESS = "cloudminds.action.download.progress";
    public static final String ACTION_DOWNLOAD_DONE = "cloudminds.action.download.done";

    public static final String INTENT_EXTRA_RESPONSE_CODE = "cloudminds.intent.extra.response";
    public static final String INTENT_EXTRA_VERSION_INFO = "cloudminds.intent.extra.version.info";
    public static final String INTENT_EXTRA_DOWNLOAD_PROGRESS = "cloudminds.intent.extra.progress";
    public static final String INTENT_EXTRA_DOWNLOADING_VERSION = "cloudminds.intent.extra.downloading_version";
    public static final String INTENT_EXTRA_DOWNLOADED_SIZE = "cloudminds.intent.extra.processed";

    public static final int VIEW_STATUS_NONE = 1;
    public static final int VIEW_STATUS_HAS_UPDATE = 2;
    public static final int VIEW_STATUS_CHECK_FAILED = 3;
    public static final int VIEW_STATUS_CHECKING = 4;
    public static final int VIEW_STATUS_DOWNLOADING = 5;
    public static final int VIEW_STATUS_DOWNLOADED = 6;
    public static final int VIEW_STATUS_DOWNLOAD_FAILED = 7;
    public static final int VIEW_STATUS_DOWNLOAD_CANCELED = 8;

    private TextView versionStatus;
    private TextView latestVersion;
    private TextView lastCheckTime;
    private Button checkButton;
    private View progressDot;
    private View progress;
    private CMAnimationListener mCMAnimationListener;
    private Config config;
    private UpdateAndDownloadInfoReceiver infoReceiver;
    private VersionPojo currentVersionPojo;
    private int currentStatus = VIEW_STATUS_NONE;
    private TextView welcome_tv;
    private TextView use_tv;
    private TextView meta_tv;
    private int width;
    private int height;
    private View welcome_layout;
    private String start_type="self";
    private String TAG="META/CMUpdaterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        UpdaterApplication.setShower(this);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
         width = wm.getDefaultDisplay().getWidth();
         height = wm.getDefaultDisplay().getHeight();
        Intent intent = getIntent();
        if(intent.hasExtra("start_type")){
            start_type = intent.getStringExtra("start_type");
        }
        if (isAllPermissionGranted()) {
            initView();
        }

    }

    private void initView() {
        welcome_layout = findViewById(R.id.welcome_layout);
        welcome_tv = ((TextView) findViewById(R.id.welcome));
        use_tv = ((TextView) findViewById(R.id.use));
        meta_tv = ((TextView) findViewById(R.id.meta));
        welcome_tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean hasDraw=false;
                    @Override
            public boolean onPreDraw() {
              if (!hasDraw) {
                    Log.e("onPreDraw",welcome_tv.getMeasuredWidth() + "=="+ welcome_tv.getMeasuredWidth());
                  hasDraw = true;
                  startAnim(welcome_tv,0,0);
              }
              return true;
            }
        });
        use_tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean hasDraw=false;
            @Override
            public boolean onPreDraw() {
                if (!hasDraw) {
                    Log.e("onPreDraw",use_tv.getMeasuredWidth() + "=="+ use_tv.getMeasuredWidth());
                    hasDraw = true;
                    startAnim(use_tv,width+use_tv.getX(),0);
                }
                return true;
            }
        });
        meta_tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean hasDraw=false;
            @Override
            public boolean onPreDraw() {
                if (!hasDraw) {
                    Log.e("onPreDraw",meta_tv.getMeasuredWidth() + "=="+ meta_tv.getMeasuredWidth());
                    hasDraw = true;
                    startAnim(meta_tv,meta_tv.getX(),height+meta_tv.getY());
                }
                return true;
            }
        });

        Typeface typeface = Typeface.createFromAsset(getAssets(),"t2.ttf");
        welcome_tv.setTypeface(typeface);
        use_tv.setTypeface(typeface);
        meta_tv.setTypeface(typeface);
        config = Config.getInstance();
        currentVersionPojo = config.getCachedUpdatingInfo(this);
        versionStatus = v(R.id.version_status);
        latestVersion = v(R.id.newVersionId);
        progress = v(R.id.progress);
        progressDot = v(R.id.progressDot);
        lastCheckTime = v(R.id.lastCheckTime);
        TextView currentVersion = v(R.id.current_version);
        checkButton = v(R.id.button_check_now);
        View menu = v(R.id.menu);
//        Config.isGongan3()
        if (true) {
            menu.setVisibility(View.GONE);
        }
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingDialog dialog = new SettingDialog(CMUpdaterActivity.this);
                dialog.setAnchorView(v);
                dialog.show();
            }
        });

        notifyLastCheckTime();

        infoReceiver = new UpdateAndDownloadInfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHECK_DONE);
        filter.addAction(ACTION_DOWNLOAD_DONE);
        filter.addAction(ACTION_DOWNLOAD_PROGRESS);
        registerReceiver(infoReceiver, filter);

        checkButton.setOnClickListener(new CMClickListener());
        CheckingParams checkingParams = new CheckingParams();
        currentVersion.setText(checkingParams.getDisplay_version());
        latestVersion.setText(checkingParams.getDisplay_version());
//        currentVersion.setText(Build.DISPLAY);
//        latestVersion.setText(Build.DISPLAY);
        checkButton.setText(R.string.button_check_now);
        if (PackageDownloadService.isDownloading) {
            ActualVersionInfo versionInfo = Config.getInstance().getDownloadingPackageInfo(this);
            if (null == versionInfo) {
                startChecking();
            } else {
                Packages packageInfo = versionInfo.getPackages()[0];
                File file = PackageDownloader.getPackageFile(packageInfo.getName());
                if (file.exists()) {
                    long fileLength = file.length();
                    try {
                        double percent = (double) fileLength / Long.valueOf(packageInfo.getSize());
                        Message message = new Message();
                        if (percent == 1) {
                            message.arg1 = VIEW_STATUS_DOWNLOADED;
                        } else {
                            NumberFormat numberFormat = NumberFormat.getPercentInstance();
                            numberFormat.setMinimumFractionDigits(1); // 保留小数点后1位
                            String percentage = numberFormat.format(percent);
                            if (versionInfo.getDownloadStatus() == ActualVersionInfo.DownloadStatus.UserCanceled) {
                                message.arg1 = VIEW_STATUS_DOWNLOAD_CANCELED;
                            } else {
                                message.arg1 = VIEW_STATUS_DOWNLOADING;
                            }
                            message.obj = percentage;
                        }
                        handler.sendMessage(message);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    startChecking();
                }
            }
        } else {
            if (checkNetworkAndToastIfNotAvailable()) {
                startChecking();
            }
        }

        View versionListLayout = v(R.id.versionListLayout);
        if (Config.getInstance().isVersionTest(this)) {
            versionListLayout.setVisibility(View.VISIBLE);
        } else {
            versionListLayout.setVisibility(View.INVISIBLE);
        }
        versionListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CMUpdaterActivity.this, VersionListActivity.class);
                startActivity(intent);
            }
        });
        View currentVersionLayout = v(R.id.currentVersionLayout);
        currentVersionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CMUpdaterActivity.this, VersionDetailActivity.class);
                String description = getString(R.string.text_empty_description); // 目前还无法获取当前版本的release note, 所以直接使用固定字符串
                intent.putExtra(VersionDetailActivity.INTENT_EXTRA_DETAIL, description);
                startActivity(intent);
            }
        });
    }
    int playNum=0;
    private void startAnim(View v,float startX,float startY) {
        if(start_type.equals("use")){
            init_status=2;
            if(status!=0){
                welcome_layout.setVisibility(View.GONE);
            }
            return;
        }
        ObjectAnimator alpha_animator = ObjectAnimator.ofFloat(v, "alpha", 0f,1f);

    //沿x轴放大
    ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(v, "scaleX", 0f, 1f);
        //沿y轴放大
         ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(v, "scaleY", 0f, 1f);
        //移动
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(v, "translationY", startY-v.getY(),0f );
         ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(v, "translationX",startX- v.getX(), 0f);

        AnimatorSet set = new AnimatorSet(); //同时沿X,Y轴放大，且改变透明度，然后移动
         set.play(scaleXAnimator).with(scaleYAnimator).with(alpha_animator).with(translationXAnimator).with(translationYAnimator); //都设置3s，也可以为每个单独设置
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                playNum--;
                if(playNum==0){
                    init_status=2;
                    if(status==1){
                        CMUpdaterActivity.this.setResult(200);
                        CMUpdaterActivity.this.finish();
                    }else{
                        welcome_layout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                playNum++;
                if(init_status==0)init_status++;
            }
        });
        set.setDuration(3000);
        set.start();

    }

    @Override
    protected void onPermissionGranted() {
        initView();
    }

    private boolean checkNetworkAndToastIfNotAvailable() {
        if (NetworkUtil.isNetworkAvailable(CMUpdaterActivity.this)) {
            return true;
        } else {
            ToastUtil.show(this.getApplicationContext(), R.string.toast_text_no_available_connection);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != infoReceiver) {
            unregisterReceiver(infoReceiver);
        }
    }

    private void startChecking() {
        Message message = new Message();
        message.arg1 = VIEW_STATUS_CHECKING;
        handler.sendMessage(message);
        Intent intent = new Intent(CMUpdaterActivity.this, UpdateCheckingService.class);
        startService(intent);
    }
    int init_status=0;//0未初始化，1播放中，2播放完成，3初始化完成
    int status=0;//0初始值，1finsh，2VISIBLE
    private Handler handler = new Handler(new Handler.Callback() {
        int i=0;
        @Override
        public boolean handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: i = "+(i++) );
            int status = msg.arg1;
            boolean checkButtonEnabled = true;
            currentStatus = status;
            switch (status) {
                case VIEW_STATUS_CHECK_FAILED:
                    versionStatus.setText(R.string.state_action_checked_failure);
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_CHECKING:
                    progressDot.setVisibility(View.VISIBLE);
                    RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(1500);
                    animation.setRepeatCount(-1);
                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
                    mCMAnimationListener = new CMAnimationListener();
                    animation.setAnimationListener(mCMAnimationListener);
                    progress.startAnimation(animation);
                    checkButtonEnabled = false;
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_DOWNLOADING:
                    versionStatus.setText(R.string.state_action_downloading);
                    latestVersion.setText(msg.obj.toString());
                    checkButton.setText(R.string.button_cancel);
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_NONE:
                    notifyLastCheckTime();
                    versionStatus.setText(R.string.state_action_none);
                    CMUpdaterActivity.this.status=1;
                    break;
                case VIEW_STATUS_HAS_UPDATE:
                    notifyLastCheckTime();
                    versionStatus.setText(R.string.state_action_available);
                    Newest newest = (Newest) msg.obj;
                    String version = newest.getSimplename();
                    latestVersion.setText(version);
                    checkButton.setText(R.string.button_download);
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_DOWNLOADED:
                    versionStatus.setText(R.string.state_action_downloaded);
                    checkButton.setText(R.string.button_install_now);
                    Newest _newest = (Newest) msg.obj;
                    if (null != _newest) {
                        String _version = _newest.getSimplename();
                        latestVersion.setText(_version);
                    }
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_DOWNLOAD_FAILED:
                    versionStatus.setText(R.string.state_action_download_failed);
                    checkButton.setText(R.string.button_download);
                    CMUpdaterActivity.this.status=2;
                    break;
                case VIEW_STATUS_DOWNLOAD_CANCELED:
                    versionStatus.setText(R.string.state_action_download_canceled);
                    checkButton.setText(R.string.button_download);
                    CMUpdaterActivity.this.status=2;
                    break;
                default:
                    CMUpdaterActivity.this.status=1;
                    break;
            }
            if(init_status==2){
                if(CMUpdaterActivity.this.status==1&&start_type.equals("self")){
                    CMUpdaterActivity.this.setResult(200);
                    CMUpdaterActivity.this.finish();
                }else{
                    welcome_layout.setVisibility(View.GONE);
                }
            }
            checkButton.setEnabled(checkButtonEnabled);
            return true;
        }
    });

    private void notifyLastCheckTime() {
        long lastTime = config.getLastCheckTime(this);
        if (0 != lastTime) {
            String lastTimeText = TimeUtil.readableTime(this, lastTime);
            lastCheckTime.setText(lastTimeText);
        }
    }

    private void setCheckingDone(int responseCode, VersionPojo versionPojo) {
        if (null != mCMAnimationListener) {
            mCMAnimationListener.cancelAfterRepeatComplete();
        }
        Message message = new Message();
        if (responseCode == UpdateChecker.RESPONSE_CODE_SUCCESS) {
            // update last check time
            config.setLastCheckTime(CMUpdaterActivity.this, System.currentTimeMillis());
            Newest newest = versionPojo.getNewest();
            String name = newest.getName();
            String display_version = new CheckingParams().getDisplay_version();
            if (null != newest&&(!name.equals(display_version)) && (newest.getDownloadStatus() == ActualVersionInfo.DownloadStatus.DOWNLOADED||PackageDownloader.getPackageFile(newest.getPackages()[0].getName()).exists())) {
                message.arg1 = VIEW_STATUS_DOWNLOADED;
                message.obj = newest;
            } else {
                boolean hasNewerVersion = versionPojo.hasNewVersion();
                if (hasNewerVersion&&(!name.equals(display_version))) {
                    message.arg1 = VIEW_STATUS_HAS_UPDATE;
                    message.obj = newest;
                    // auto download on wifi
                    if (NetworkUtil.getConnectionType(this) == ConnectivityManager.TYPE_WIFI &&
                            Config.getInstance().isAutoDownloadOnWifi(this)) {
                        if (newest != null) {
                            startDownload(newest);
                        }
                    } else {
                        NotificationUtil.showNewVersionDetectedNotification(this, versionPojo);
                    }
                } else {

                    message.arg1 = VIEW_STATUS_NONE;
                }
            }
            currentVersionPojo = versionPojo;
        } else {
            message.arg1 = VIEW_STATUS_CHECK_FAILED;
        }
        handler.sendMessage(message);
    }

    private class CMAnimationListener implements Animation.AnimationListener {

        private boolean needCancelAnim;

        @Override
        public void onAnimationStart(Animation animation) {
            needCancelAnim = false;
            if (null != versionStatus) {
                versionStatus.setText(R.string.state_action_checking);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (null != progressDot) {
                progressDot.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            if (needCancelAnim) {
                try {
                    animation.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void cancelAfterRepeatComplete() {
            needCancelAnim = true;
        }
    }

    private class UpdateAndDownloadInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroyed() || null == intent) {
                return;
            }
            String action = intent.getAction();
            Message message = new Message();
            int responseCode = intent.getIntExtra(INTENT_EXTRA_RESPONSE_CODE, UpdateChecker.RESPONSE_CODE_REQUEST_FAILED);
            switch (action) {
                case ACTION_CHECK_DONE:
                    VersionPojo versionPojo = (VersionPojo) intent.getSerializableExtra(INTENT_EXTRA_VERSION_INFO);
                    setCheckingDone(responseCode, versionPojo);
                    break;
                case ACTION_DOWNLOAD_PROGRESS:
                    String percentage = intent.getStringExtra(INTENT_EXTRA_DOWNLOAD_PROGRESS);
                    message.arg1 = VIEW_STATUS_DOWNLOADING;
                    message.obj = percentage;
                    handler.sendMessage(message);
                    break;
                case ACTION_DOWNLOAD_DONE:
                    currentVersionPojo = Config.getInstance().getCachedUpdatingInfo(CMUpdaterActivity.this);
                    if (responseCode == UpdateChecker.RESPONSE_CODE_SUCCESS) {
                        message.arg1 = VIEW_STATUS_DOWNLOADED;
                        if (null != currentVersionPojo) {
                            message.obj = currentVersionPojo.getNewest();
                        }
                    } else if (responseCode == UpdateChecker.RESPONSE_CODE_CANCEL) {
                        message.arg1 = VIEW_STATUS_DOWNLOAD_CANCELED;
                    } else {
                        message.arg1 = VIEW_STATUS_DOWNLOAD_FAILED;
                    }
                    handler.sendMessage(message);
                    break;
            }
        }
    }

    private void startDownload(ActualVersionInfo versionInfo) {
        if (!PackageDownloadService.isDownloaderServiceAvailable(this)) {
            return;
        }
        PackageDownloadService.checkAndStart(this, versionInfo);
    }

    public static void notifyCheckDone(Context mContext, int responseCode, VersionPojo versionPojo) {
        Intent intent = new Intent(CMUpdaterActivity.ACTION_CHECK_DONE);
        intent.setPackage(mContext.getPackageName());
        intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_RESPONSE_CODE, responseCode);
        intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_VERSION_INFO, versionPojo);
        mContext.sendBroadcast(intent);
    }

    private class CMClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ActualVersionInfo downloadingVersionInfo = Config.getInstance().getDownloadingPackageInfo(CMUpdaterActivity.this);
            switch (currentStatus) {
                case VIEW_STATUS_DOWNLOADED:
                case VIEW_STATUS_HAS_UPDATE:
                case VIEW_STATUS_DOWNLOAD_CANCELED:
                    if (currentStatus == VIEW_STATUS_DOWNLOADED) { // install
                        //// TODO: 2017/9/8 固件安装
                        Intent activityIntent = new Intent(CMUpdaterActivity.this, InstallationActivity.class);
                        activityIntent.putExtra(NotificationUtil.INTENT_KEY_PACKAGE_INFO, downloadingVersionInfo);
                        startActivity(activityIntent);
                    } else { // Download
                         final ActualVersionInfo versionInfo;
                        if (currentVersionPojo.hasNewVersion()) { // 有最新版本, 开始下载最新版本
                            versionInfo = currentVersionPojo.getNewest();
                        } else if (null != downloadingVersionInfo) { // 有正在下载的(暂停状态), 继续下载
                            versionInfo = downloadingVersionInfo;
                        } else { // 没有最新版本, 也没有最新的版本
                            DLog.e("WTF, 没有最新版本, 也没有最新的版本");
                            return;
                        }
                        if (checkNetworkAndToastIfNotAvailable()) {
                            VersionInfoDialog dialog = new VersionInfoDialog(CMUpdaterActivity.this, versionInfo, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startDownload(versionInfo);
                                    checkButton.setText(R.string.button_cancel);
                                }
                            });
                            dialog.show();
                        }
                    }
                    break;
                case VIEW_STATUS_DOWNLOAD_FAILED:
                case VIEW_STATUS_CHECK_FAILED:
                case VIEW_STATUS_NONE: // check
                    if (checkNetworkAndToastIfNotAvailable()) {
                        if (null == downloadingVersionInfo || Build.DISPLAY.equals(downloadingVersionInfo.getName())) {
                            long currentTime = System.currentTimeMillis();
                            long lastTime = config.getLastCheckTime(CMUpdaterActivity.this);
                            if (currentTime - lastTime < 1000 * 60) {
                                ToastUtil.show(CMUpdaterActivity.this.getApplicationContext(), R.string.toast_1mins_check);
                            } else {
                                startChecking();
                            }
                        } else {
                            startDownload(downloadingVersionInfo);
                        }
                    }
                    break;
                case VIEW_STATUS_DOWNLOADING: // cancel
                    if (null != downloadingVersionInfo && !downloadingVersionInfo.isForceUpgradeNeeded()) {
                        Intent intent = new Intent(PackageDownloadService.ACTION_CANCEL_DOWNLOAD);
                        sendBroadcast(intent);
                        checkButton.setText(R.string.button_download);
                    }
                default:
                    break;
            }
        }
    }
}
