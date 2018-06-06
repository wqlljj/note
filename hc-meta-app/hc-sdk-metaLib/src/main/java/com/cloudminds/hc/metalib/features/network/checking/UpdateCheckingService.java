package com.cloudminds.hc.metalib.features.network.checking;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.features.pojo.VersionPojo;
import com.cloudminds.hc.metalib.utils.DLog;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.google.gson.Gson;

/**
 * Created by willzhang on 16/06/17
 */

public class UpdateCheckingService extends IntentService {

    public UpdateCheckingService() {
        super("UpdateCheckingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (NetworkUtil.isRoamingAnd3G(this)) {
            DLog.d("Roaming, ignore update checking 1");
            return;
        }
        DLog.d("start checking for updates");
        CheckingParams params = new CheckingParams();
        Gson gson = new Gson();
        String postData = gson.toJson(params);
        UpdateChecker checker = new UpdateChecker(this);
        checker.check(postData, new UpdateChecker.CheckUpdateCallback() {
                    @Override
                    public void onCheckDone(int responseCode, VersionPojo versionPojo) {
                        CMUpdaterActivity.notifyCheckDone(UpdateCheckingService.this, responseCode, versionPojo);
                    }
                }
        );
    }
}
