package com.example.wqllj.locationshare.broadcast;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.wqllj.locationshare.service.JobSchedulerService;
import com.example.wqllj.locationshare.view.MainActivity;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class KeepAliveReceiver extends BroadcastReceiver {

    private String TAG = "KeepAliveReceiver";
    public static String ACTION_KEEPALIVE="com.example.wqllj.locationshare.broadcast.ACTION_KEEPALIVE";
    //
//    Intent.ACTION_SHUTDOWN
    //Intent.ACTION_BOOT_COMPLETED
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: " + action);
        switch (action){
            case Intent.ACTION_BOOT_COMPLETED:
                intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
        }
        if(System.currentTimeMillis()-JobSchedulerService.startTime>80*1000) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            startJobScheduler(context,jobScheduler);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startJobScheduler(Context context,JobScheduler mJobScheduler) {
        int id = 1;
        mJobScheduler.cancel(id);
        JobInfo.Builder builder = new JobInfo.Builder(id, new ComponentName(context, JobSchedulerService.class));
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setMinimumLatency(5000); //执行的最小延迟时间
            builder.setOverrideDeadline(5000);  //执行的最长延时时间
            builder.setMinimumLatency(5000);
            builder.setBackoffCriteria(0, JobInfo.BACKOFF_POLICY_LINEAR);//线性重试方案
        } else {
            builder.setPeriodic(5000);
        }
        builder.setPersisted(true);  // 设置设备重启时，执行该任务
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(true); // 当插入充电器，执行该任务
        JobInfo info = builder.build();
        mJobScheduler.schedule(info); //开始定时执行该系统任务
    }
}
