package com.cloudminds.hc.hariservice.command.listener;

import org.json.JSONObject;

/**
 * Created by zoey on 17/4/17.
 */

public interface CmdEventListener {

    public void onSpeak(String type,final String content,final String operation);

    public void onInfo(final String info);

    public JSONObject robotInfo();
}
