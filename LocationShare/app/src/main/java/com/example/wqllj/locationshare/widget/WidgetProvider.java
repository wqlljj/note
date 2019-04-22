package com.example.wqllj.locationshare.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.model.MainModel;
import com.example.wqllj.locationshare.model.baidumap.BaiDuClient;
import com.example.wqllj.locationshare.viewModel.WidgetViewModel;

/**
 * Created by cloud on 2018/9/19.
 */

public class WidgetProvider extends AppWidgetProvider {
    public static final String CLICK_ACTION = "com.example.wqllj.locationshare.widget.CLICK"; // 点击事件的广播ACTION
    private int requestCode = 1001;
    private String TAG = "WidgetProvider1";
    private int length;
    private WidgetViewModel widgetViewModel;
    private static boolean firstRun=false;
    private static PendingIntent pendIntent;

    public WidgetProvider() {
        super();
    }

    /**
     * 接收窗口小部件点击时发送的广播
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, "onReceive: " + intent.getAction());
        if (CLICK_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "hello dog!", Toast.LENGTH_SHORT).show();
        }
        switch (intent.getAction()) {
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                repeatUpdate(context);
                break;
        }
    }

    /**
     * 每次窗口小部件被更新都调用一次该方法
     */

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.e(TAG, "onUpdate: " + (length != appWidgetIds.length));
//        if(length!=appWidgetIds.length) {
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
//            Intent intent = new Intent(CLICK_ACTION);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.tv, pendingIntent);
//            length = appWidgetIds.length;
//            for (int appWidgetId : appWidgetIds) {
//                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//            }
//        }
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra("appWidgetIds", appWidgetIds);
        context.startService(intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.e(TAG, "onDeleted: ");
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.e(TAG, "onDisabled: ");
        if (widgetViewModel != null) {
            widgetViewModel.onDestory();
        }
        widgetViewModel = null;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendIntent);
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        firstRun=true;
        Log.e(TAG, "onEnabled: ");
        widgetViewModel = new WidgetViewModel(context);
        repeatUpdate(context);
    }

    private void repeatUpdate(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode = 0;
        if(pendIntent==null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                intent.setComponent(new ComponentName(context, WidgetProvider.class));
            }
            pendIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        // 5秒后发送广播，然后每个10秒重复发广播。广播都是直接发到AlarmReceiver的

        // pendingIntent 为发送广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10000, pendIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10000, pendIntent);
        } else if(firstRun){
            firstRun=false;
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10000, pendIntent);
        }

    }

    /**
     * 当小部件大小改变时
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.e(TAG, "onAppWidgetOptionsChanged: ");
    }


    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        Log.e(TAG, "onRestored: ");
    }

}
