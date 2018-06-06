package com.cloudminds.hc.metalib.features.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.cloudminds.hc.metalib.features.installation.InstallationSchedulerService;
import com.cloudminds.hc.metalib.features.network.checking.UpdateCheckingSchedulerService;
import com.cloudminds.hc.metalib.utils.DLog;


/**
 * Created by willzhang on 15/06/17
 */

public final class UpdaterScheduler {

    private static final int JOB_ID_CHECK = 682682682; // ota ota ota
    private static final int JOB_ID_INSTALL = 4678255; // install
    private static final int CHECK_INTERVAL =  1000 * 60 * 60 * 24; // 1 day
    private static final int INSTALL_INTERVAL =  1000 * 60 * 60 * 12; // half day

    public static void scheduleDailyChecking(Context mContext) {
        JobScheduler mJobScheduler = mContext.getSystemService(JobScheduler.class);
        JobInfo jobInfo = getJobInfo(mContext, JOB_ID_CHECK, UpdateCheckingSchedulerService.class, CHECK_INTERVAL);
        int result = mJobScheduler.schedule(jobInfo);
        DLog.d("result:" + result);
        if (JobScheduler.RESULT_SUCCESS != result) {
            DLog.e("!!!Cannot schedule daily checking!!!");
        }
    }

    public static void scheduleInstallation(Context mContext) {
        Intent intent = new Intent(mContext, InstallationSchedulerService.class);
        intent.putExtra(InstallationSchedulerService.ACTION_SHOW_NOTIFICATION, true);
        mContext.startService(intent);

        JobScheduler mJobScheduler = mContext.getSystemService(JobScheduler.class);
        JobInfo jobInfo = getJobInfo(mContext, JOB_ID_INSTALL, InstallationSchedulerService.class, INSTALL_INTERVAL);
        mJobScheduler.cancel(JOB_ID_INSTALL);
        int result = mJobScheduler.schedule(jobInfo);
        DLog.d("result:" + result);
        if (JobScheduler.RESULT_SUCCESS != result) {
            DLog.e("!!!Cannot schedule installation!!!");
        }
    }

    private static JobInfo getJobInfo(Context mContext, int jobId, Class clazz, int interval) {
        ComponentName mComponentName = new ComponentName(mContext, clazz);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, mComponentName);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // 有网络才执行(任意网络)
        builder.setPeriodic(interval);
        return builder.build();
    }
}
