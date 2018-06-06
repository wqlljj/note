package com.cloudminds.hc.metalib.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.features.network.checking.UpdateChecker;


/**
 * Created by willzhang on 22/06/17
 */

public final class NetworkUtil {

    private NetworkUtil() {}

    public static boolean isNetworkAvailable(Context context) {
        return getConnectionType(context) != -1;
    }

    public static int getConnectionType(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }
        return -1;
    }

    public static boolean isRoamingAnd3G(final Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connManager.getActiveNetworkInfo();
        boolean isRoaming3G = null != mNetworkInfo && mNetworkInfo.isRoaming() && mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        if (isRoaming3G) {
            Intent intent = new Intent(CMUpdaterActivity.ACTION_DOWNLOAD_DONE);
            intent.putExtra(CMUpdaterActivity.INTENT_EXTRA_RESPONSE_CODE, UpdateChecker.RESPONSE_CODE_CANCEL);
            context.sendBroadcast(intent);
            UIThreadDispatcher.dispatch(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(context, R.string.alert_roaming_messgae);
                }
            });
        }
        return isRoaming3G;
    }
}
