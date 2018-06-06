package com.cloudminds.hc.metalib.features.version;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.BaseActivity;
import com.cloudminds.hc.metalib.features.installation.InstallationActivity;
import com.cloudminds.hc.metalib.features.installation.VersionInfoDialog;
import com.cloudminds.hc.metalib.features.network.checking.CheckingParams;
import com.cloudminds.hc.metalib.features.network.checking.UpdateChecker;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloadService;
import com.cloudminds.hc.metalib.features.network.downloading.PackageDownloader;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.Newest;
import com.cloudminds.hc.metalib.features.pojo.Packages;
import com.cloudminds.hc.metalib.features.pojo.Software;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.features.pojo.Versions;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.cloudminds.hc.metalib.utils.NotificationUtil;
import com.cloudminds.hc.metalib.utils.TimeUtil;
import com.cloudminds.hc.metalib.utils.UIThreadDispatcher;
import com.google.gson.Gson;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionListActivity extends BaseActivity {

    private List<ActualVersionInfo> versionData = new ArrayList<>();
    private RecyclerView versionList;
    private ProgressReceiver mProgressReceiver;
    private VersionListAdapter mAdapter;
    private boolean hasNewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_list);
        versionList = v(R.id.versionList);
        DataLoader mDataLoader = new DataLoader();
        mDataLoader.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CMUpdaterActivity.ACTION_DOWNLOAD_DONE);
        filter.addAction(CMUpdaterActivity.ACTION_DOWNLOAD_PROGRESS);
        mProgressReceiver = new ProgressReceiver();
        registerReceiver(mProgressReceiver, filter);

        TextView versionName = v(R.id.versionName);
        TextView buildTime = v(R.id.buildTime);
        TextView versionType = v(R.id.versionType);
        CheckingParams checkingParams = new CheckingParams();
        versionName.setText(checkingParams.getDisplay_version());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentBuildTime = null;
        try {
            long time = simpleDateFormat.parse(checkingParams.getBuild_time()).getTime();
            currentBuildTime = TimeUtil.readableTime(this, time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        buildTime.setText(currentBuildTime==null?"null":currentBuildTime);
        int versionId = checkingParams.getFormal().equals("0")?
                R.string.item_version_test :
                R.string.item_version_formal;
        String versionText = getString(versionId);
        versionType.setText(versionText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mProgressReceiver) {
            unregisterReceiver(mProgressReceiver);
        }
    }

    private class DataLoader extends Thread {

        @Override
        public void run() {
            loadOfflineData(); // load offline data before online data deliver
            boolean isNetworkAvailable = NetworkUtil.isNetworkAvailable(VersionListActivity.this);
            if (isNetworkAvailable) {
                loadOnlineData();
            }
        }

        private void loadOnlineData() {
            CheckingParams params = new CheckingParams();
            Gson gson = new Gson();
            String postData = gson.toJson(params);
            UpdateChecker checker = new UpdateChecker(VersionListActivity.this);
            checker.check(postData, new UpdateChecker.CheckUpdateCallback() {
                @Override
                public void onCheckDone(int responseCode, VersionPojo versionPojo) {
                    if (responseCode == UpdateChecker.RESPONSE_CODE_SUCCESS) {
                        processData(versionPojo);
                    }
                }
            });
        }

        private void loadOfflineData() {
            VersionPojo cachedVersionPojo = Config.getInstance().getCachedUpdatingInfo(VersionListActivity.this);
            processData(cachedVersionPojo);
        }

        private void processData(VersionPojo versionPojo) {
            Software[] software;
            Versions[] versions;
            if (null == versionPojo) {
                DLog.d("No cached version data found");
            } else {
                versionData.clear();
                Newest newest = versionPojo.getNewest();
                hasNewVersion = versionPojo.hasNewVersion();
                if (hasNewVersion && null != newest) {
                    versionData.add(newest);
                }
                software = versionPojo.getSoftware();
                if (null != software && software.length > 0) {
                    versions = software[0].getVersions(); // json格式如此，永远只有一个item，所以直接取第0个
                    if (null != versions && versions.length > 0 ) {
                        versionData.addAll(Arrays.asList(versions));
                    }
                }

                ActualVersionInfo dummyInfo = new ActualVersionInfo();
                dummyInfo.setTime(TimeUtil.toServerFormat(Build.TIME));
                versionData.remove(dummyInfo); // 移除当前版本

                for (ActualVersionInfo versionInfo : versionData) {
                    Packages packageInfo = versionInfo.getPackages()[0];
                    File file = PackageDownloader.getPackageFile(packageInfo.getName());
                    if (file.exists()) {
                        long fileLength = file.length();
                        if (String.valueOf(fileLength).equals(packageInfo.getSize())) {
                            versionInfo.setDownloadStatus(ActualVersionInfo.DownloadStatus.DOWNLOADED);
                        } else {
                            versionInfo.setDownloadStatus(ActualVersionInfo.DownloadStatus.PAUSED);
                        }
                        versionInfo.setDownloadedSize(fileLength);
                    } else {
                        versionInfo.setDownloadStatus(ActualVersionInfo.DownloadStatus.NOT_STARTED);
                        versionInfo.setDownloadedSize(0);
                    }
                }
                UIThreadDispatcher.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(VersionListActivity.this);
                        versionList.setLayoutManager(layoutManager);
                        DividerItemDecoration dividerItemDecoration =
                                new DividerItemDecoration(VersionListActivity.this, layoutManager.getOrientation());
                        versionList.addItemDecoration(dividerItemDecoration);
                        mAdapter = new VersionListAdapter();
                        versionList.setAdapter(mAdapter);
                        RecyclerView.ItemAnimator animator = versionList.getItemAnimator();
                        if (animator instanceof SimpleItemAnimator) {
                            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                        }
                    }
                });
            }
        }
    }

    private void initViewHolder(VersionHolder holder, final ActualVersionInfo version, int position) {
        holder.versionName.setText(version.getSimplename());
        Packages packages = version.getPackages()[0];

        String formal = version.getFormal();
        String versionType = packages.getType();
        versionType = Packages.PACKAGE_TYPE_1.equals(versionType) ?
                getString(R.string.download_package_type1) : getString(R.string.download_package_type2);
        if (CheckingParams.FORMAL_MAIN.equals(formal)) {
            String fullType = getString(R.string.item_version_formal) + ", " + versionType;
            holder.versionType.setText(fullType);
        } else if (CheckingParams.FORMAL_TEST.equals(formal)) {
            String fullType = getString(R.string.item_version_test) + ", " + versionType;
            holder.versionType.setText(fullType);
        } else {
            holder.versionType.setText(R.string.version_unknown);
        }

        long time = TimeUtil.fromFormattedTimeString(version.getTime(), TimeUtil.SERVER_TIME_FORMAT);
        String currentBuildTime = TimeUtil.readableTime(this, time);
        holder.buildTime.setText(currentBuildTime);

        String readableSize;
        try {
            long packageSize = Long.valueOf(packages.getSize());
            readableSize = Formatter.formatFileSize(VersionListActivity.this, packageSize);
            holder.packageSize.setText(readableSize);

            long downloadedSize = version.getDownloadedSize();
            if (downloadedSize > 0) {
                holder.progressLayout.setVisibility(View.VISIBLE);
                double percent = (double) downloadedSize / packageSize;
                NumberFormat numberFormat = NumberFormat.getPercentInstance();
                numberFormat.setMinimumFractionDigits(1); // 保留小数点后1位
                String percentage = numberFormat.format(percent);
                holder.percentage.setText(percentage);

                holder.downloadProgress.setProgress((int) (percent * 100));
            } else {
                holder.progressLayout.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        final ActualVersionInfo.DownloadStatus downloadStatus = version.getDownloadStatus();
        switch (downloadStatus) {
            case DOWNLOADED:
                holder.actionButton.setText(R.string.button_install);
                holder.progressLayout.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                holder.progressLayout.setVisibility(View.VISIBLE);
                holder.actionButton.setText(R.string.button_cancel);
                break;
            case NOT_STARTED:
                holder.actionButton.setText(R.string.button_download);
                holder.progressLayout.setVisibility(View.GONE);
                break;
            case PAUSED:
                holder.actionButton.setText(R.string.button_download);
                holder.progressLayout.setVisibility(View.VISIBLE);
                break;
        }
        int newVersionItem = hasNewVersion ? 0 : -1; // 无新版本时，不显示最新版本
        if (position == newVersionItem) {
            holder.versionStatus.setVisibility(View.VISIBLE);
            holder.versionStatus.setText(R.string.text_version_status_latest);
        } else if (position == newVersionItem + 1) {
            holder.versionStatus.setVisibility(View.VISIBLE);
            holder.versionStatus.setText(R.string.text_version_stetus_recent);
        } else {
            holder.versionStatus.setVisibility(View.GONE);
        }
        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (downloadStatus) {
                    case DOWNLOADED: // 安装
                        //// TODO: 2017/9/8 固件安装
                        Intent activityIntent = new Intent(VersionListActivity.this, InstallationActivity.class);
                        activityIntent.putExtra(NotificationUtil.INTENT_KEY_PACKAGE_INFO, version);
                        startActivity(activityIntent);
                        break;
                    case DOWNLOADING: // 暂停、取消
                        if (!version.isForceUpgradeNeeded()) {
                            Intent intent = new Intent(PackageDownloadService.ACTION_CANCEL_DOWNLOAD);
                            sendBroadcast(intent);
                        }
                        break;
                    case NOT_STARTED:
                    case PAUSED: // 下载
                        if (!PackageDownloadService.isDownloaderServiceAvailable(VersionListActivity.this)) {
                            return;
                        }
                        VersionInfoDialog dialog = new VersionInfoDialog(VersionListActivity.this, version, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PackageDownloadService.checkAndStart(VersionListActivity.this, version);

                            }
                        });
                        dialog.show();
                        break;

                }
            }
        });
//        holder.holderView.setOnLongClickListener(v -> {
//            showDeleteDialog(version);
//            return true;
//        });
    }

//    private void showDeleteDialog(ActualVersionInfo version) {
//        Packages packageInfo = version.getPackages()[0];
//        File file = PackageDownloader.getPackageFile(packageInfo.getName());
//        if (file.exists()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog);
//            builder.setCancelable(true);
//            builder.setTitle(version.getSimplename());
//            builder.setMessage(R.string.dialog_text_delete_pkg);
//            builder.setPositiveButton(R.string.dialog_btn_delete, (dialog, which) -> {
//                boolean deleted = file.delete();
//                if (deleted) {
//                    version.setDownloadStatus(ActualVersionInfo.DownloadStatus.NOT_STARTED);
//                    int index = versionData.indexOf(version);
//                    mAdapter.notifyItemChanged(index);
//                    DLog.d(version.getName() + " deleted");
//                }
//            });
//            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
//            builder.show();
//        }
//    }

    private class VersionListAdapter extends RecyclerView.Adapter<VersionHolder> {

        @Override
        public VersionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View holderView = LayoutInflater.from(VersionListActivity.this).inflate(R.layout.item_version_list, parent, false);
            return new VersionHolder(holderView);
        }

        @Override
        public void onBindViewHolder(VersionHolder holder, int position) {
            final ActualVersionInfo version = versionData.get(position);
            initViewHolder(holder, version, position);
        }

        @Override
        public int getItemCount() {
            return versionData.size();
        }
    }

    class VersionHolder extends RecyclerView.ViewHolder {

        TextView versionName;
        TextView buildTime;
        TextView versionType;
        TextView packageSize;
        ProgressBar downloadProgress;
        TextView percentage;
        Button actionButton;
        View progressLayout;
        TextView versionStatus;
        View holderView;

        VersionHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            versionName = (TextView) itemView.findViewById(R.id.versionName);
            buildTime = (TextView) itemView.findViewById(R.id.buildTime);
            versionType = (TextView) itemView.findViewById(R.id.versionType);
            packageSize = (TextView) itemView.findViewById(R.id.packageSize);
            downloadProgress = (ProgressBar) itemView.findViewById(R.id.downloadProgress);
            percentage = (TextView) itemView.findViewById(R.id.percentage);
            actionButton = (Button) itemView.findViewById(R.id.actionButton);
            progressLayout = itemView.findViewById(R.id.progressLayout);
            versionStatus = (TextView) itemView.findViewById(R.id.versionStatus);
        }
    }

    private class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ActualVersionInfo mActualVersionInfo = (ActualVersionInfo) intent.getSerializableExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOADING_VERSION);
            int index = getIndex(mActualVersionInfo);
            if (index < 0) {
                DLog.e("Invalid index");
                return;
            }
            ActualVersionInfo info = versionData.get(index);
            switch (action) {
                case CMUpdaterActivity.ACTION_DOWNLOAD_PROGRESS:
                    long downloadedSize = intent.getLongExtra(CMUpdaterActivity.INTENT_EXTRA_DOWNLOADED_SIZE, 0);
                        info.setDownloadStatus(ActualVersionInfo.DownloadStatus.DOWNLOADING);
                        info.setDownloadedSize(downloadedSize);
                    break;
                case CMUpdaterActivity.ACTION_DOWNLOAD_DONE:
                    int responseCode = intent.getIntExtra(CMUpdaterActivity.INTENT_EXTRA_RESPONSE_CODE, PackageDownloader.RESPONSE_CODE_REQUEST_FAILED);
                    if (null != mActualVersionInfo) {
                        if (responseCode == PackageDownloader.RESPONSE_CODE_SUCCESS) {
                            info.setDownloadStatus(ActualVersionInfo.DownloadStatus.DOWNLOADED);
                        } else {
                            info.setDownloadStatus(ActualVersionInfo.DownloadStatus.PAUSED);
                        }
                        mAdapter.notifyItemChanged(index);
                    }
                    break;
                default:
                    return;
            }
            versionData.set(index, info);
            mAdapter.notifyItemChanged(index);
        }

        private int getIndex(ActualVersionInfo mActualVersionInfo) {
            if (null != mAdapter && null != mActualVersionInfo && null != versionData) {
                return versionData.indexOf(mActualVersionInfo);
            }
            return -1;
        }
    }
}
