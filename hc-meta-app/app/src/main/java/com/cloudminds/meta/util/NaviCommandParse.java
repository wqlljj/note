package com.cloudminds.meta.util;

import com.cloudminds.meta.activity.HubActivity;
import com.cloudminds.meta.bean.NaviBean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SX on 2017/5/23.
 */

public class NaviCommandParse {
    public static NaviBean CommandParse(JSONObject data){
        try {
            NaviBean naviBean = new NaviBean();
            naviBean.setType(data.getString("action"));
            if(naviBean.getType().equals("stop"))
                return naviBean;
            naviBean.setAngle(data.getInt("angle"));
            if(naviBean.getType().equals("move")) {
                if(data.has("distance")) {
                    naviBean.setDistance(data.getInt("distance"));
                }
            }
            return naviBean;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
