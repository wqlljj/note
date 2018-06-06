package com.cloudminds.hc.metalib.features.network.checking;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.features.pojo.Newest;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.cloudminds.hc.metalib.utils.NotificationUtil;
import com.google.gson.Gson;

/**
 * Created by willzhang on 15/06/17
 */

public class UpdateCheckingSchedulerService extends JobService {

    private Handler handler;
    private String TAG="UpdateCheckingSchedulerService";

    @Override
    public boolean onStartJob(final JobParameters params) {
        if (NetworkUtil.isRoamingAnd3G(this)) {
            DLog.d("Roaming, ignore update checking");
            jobFinished(params, true);
            return true;
        }
        // Check for updates
        HandlerThread thread = new HandlerThread("UpdateCheckingSchedulerService");
        thread.start();
        handler = new Handler(thread.getLooper(), new Handler.Callback() {
            int i=0;
            @Override
            public boolean handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: i = "+(i++) );
                int responseCode = msg.arg1;
                VersionPojo versionPojo = (VersionPojo) msg.obj;
                if (responseCode == UpdateChecker.RESPONSE_CODE_SUCCESS) {
                    Newest newest = versionPojo.getNewest();
                    boolean hasNewerVersion = versionPojo.hasNewVersion();
                    if (hasNewerVersion) {
                        DLog.d("Newer version detected:" + newest.getSimplename());
                        CMUpdaterActivity.notifyCheckDone(UpdateCheckingSchedulerService.this, responseCode, versionPojo);
                        NotificationUtil.showNewVersionDetectedNotification(UpdateCheckingSchedulerService.this, versionPojo);
                    }
                } else {
                    DLog.e("Auto check failed:" + responseCode);
                }
                jobFinished(params, true);
                return true;
            }
        });
        handler.post(new CheckUpdatesTask());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class CheckUpdatesTask implements Runnable {

        @Override
        public void run() {
            CheckingParams params = new CheckingParams();
            Gson gson = new Gson();
            String postData = gson.toJson(params);
            UpdateChecker checker = new UpdateChecker(UpdateCheckingSchedulerService.this);
            checker.check(postData, new UpdateChecker.CheckUpdateCallback() {
                @Override
                public void onCheckDone(int responseCode, VersionPojo versionPojo) {
                    Message msg = new Message();
                    msg.arg1 = responseCode;
                    msg.obj = versionPojo;
                    handler.sendMessage(msg);
                }
            });
        }
    }
}
