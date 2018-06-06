package com.cloudminds.hc.hariservice.manager.http;

import org.json.JSONObject;

import java.util.Dictionary;

/**
 * Created by zoey on 2017/10/25.
 */

public interface FetchServerCallBack {

    public void onSuccess(JSONObject result);
    public void onFailure(String error);
}
