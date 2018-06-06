package com.cloudminds.meta.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.util.Locale;

/**
 * Created by tiger on 17-4-1.
 */

public class DeviceUtils {

    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getSysLanguage(){
        String devLang = Locale.getDefault().getLanguage();
        if (devLang.equalsIgnoreCase("zh")){
            return "CH";
        } else if(devLang.equalsIgnoreCase("ja")){
            return "EN";
        } else {
            return "EN";
        }
    }


}
