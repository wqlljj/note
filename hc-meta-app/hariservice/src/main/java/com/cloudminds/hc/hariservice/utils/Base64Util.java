package com.cloudminds.hc.hariservice.utils;

import android.util.Base64;

/**
 * Created by zoey on 17/5/13.
 */

public class Base64Util {

    public static String encodeToString(byte[] data){
        return Base64.encodeToString(data,Base64.NO_PADDING);
    }
}
