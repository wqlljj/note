package com.cloudminds.meta.service.navigation;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.bean.AxisData;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.util.TTSSpeaker;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Handler;

/**
 * Created by zoey on 17/5/15.
 */

public class OutdoorNavigator implements GeocodeSearch.OnGeocodeSearchListener,AMapNaviListener {

    private final String TAG = "NAVI/OutdoorNavigator";
    private static OutdoorNavigator outdoorNavigator;
    private GeocodeSearch geocodeSearch;
    private Context mContext;
    protected AMapNavi mMapNavi;
    protected LocationMonitor locMonitor;
    private boolean isNaving = false;  //是否正在导航过程 包含起初定位、地理解析过程
    public static boolean isStartNavi = false; //是否正在导航
    private boolean isDestChanged = false; //导航过程中改变目的地 重新发起导航
    private NaviLatLng startPoint;  //起点经纬度
    private NaviLatLng endPoint;   //终点经纬度
    private boolean isHiOnline = false;
    public static final int STOP_REASON_ARRIVE_DEST = 1;
    public static final int STOP_REASON_USER_STOP = 0;
    public static final int STOP_REASON_INTERRUPT = -1;
    private static final int NAVI_INFO_TIME_OUT = 4000;
    private static final int START_NAVI_TIME_OUT = 5000;
    private static final int MAX_START_NAVI_RETRY = 3;

    public static OutdoorNavigator instance(Context context){
        if (null == outdoorNavigator){
            outdoorNavigator = new OutdoorNavigator();
            outdoorNavigator.init(context);
        }

        return outdoorNavigator;
    }

    public static OutdoorNavigator getInstance(){ return  outdoorNavigator;}


    private void init(Context context){
        mContext = context;
        geocodeSearch = new GeocodeSearch(mContext);
        geocodeSearch.setOnGeocodeSearchListener(this);
        initMapNavi();

        locMonitor = LocationMonitor.getInstance();
    }

    private void initMapNavi(){
        mMapNavi = AMapNavi.getInstance(mContext);
        mMapNavi.addAMapNaviListener(this);
    }


    public void getLatLon(final String address){
        Log.d(TAG,"准备目的地地理解析");
        GeocodeQuery query = new GeocodeQuery(address, LocationMonitor.cityCode);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocodeSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    public AMapNavi getMapNavi(){ return  mMapNavi;}


    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {

        if (rCode == AMapException.CODE_AMAP_SUCCESS) {

            if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null
                    && geocodeResult.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);

                LatLonPoint point = address.getLatLonPoint();
                endPoint = new NaviLatLng(point.getLatitude(),point.getLongitude());

                Log.d(TAG,"查询地点对应的经纬度："+endPoint);

                //开始规划路线
                calculateRoute(locMonitor.getCurPoint(),endPoint);

            } else {

                Log.d(TAG,"未查询到地点");
                TTSSpeaker.speak(mContext.getString(R.string.navi_end_2), TTSSpeaker.HIGH);

                if (isDestChanged){       //导航中途切换路线 查询失败  向坐席发送停止导航
                    stopNavi(STOP_REASON_USER_STOP);
                } else {
                    isNaving = false;
                    isStartNavi = false;
                    startPoint = null;
                    locMonitor.setLocationObserver(null);
                }
            }

        } else {

            Log.d(TAG,"未查询到地点");
            TTSSpeaker.speak(mContext.getString(R.string.navi_end_2), TTSSpeaker.HIGH);

            if (isDestChanged){       //导航中途切换路线 查询失败  向坐席发送停止导航
                stopNavi(STOP_REASON_USER_STOP);
            } else {
                isNaving = false;
                isStartNavi = false;
                startPoint = null;
                locMonitor.setLocationObserver(null);
            }
        }
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    private void calculateRoute(NaviLatLng startPoint, NaviLatLng endPoint){
        mMapNavi.calculateWalkRoute(startPoint, endPoint);
    }

    private long  startWaitingTime = -1;
    /**
     * 导航到address
     * 1.先获取当前位置
     * 2.获取目的经纬度
     */

    public void startNaviTo(final String address){
        Log.d(TAG,"Enter startNaviTo :"+address);
        if (!LocationMonitor.getInstance().isGpsEnabled()){
            TTSSpeaker.speak(mContext.getString(R.string.navi_gps_not_open), TTSSpeaker.HIGH);
            return;
        }

        //导航功能关闭
        if (!PreferenceUtils.getPrefBoolean("GPSEnable",true)){
            TTSSpeaker.speak(mContext.getString(R.string.navi_outdoor_disabled), TTSSpeaker.HIGH);
            return;
        }

        if (isNaving) {
            Log.d(TAG,"Navigating now ... ready to change the destination");
            mMapNavi.stopNavi();
            locMonitor.setLocationObserver(null);
            isStartNavi = false;
            isDestChanged = true;
            startPoint = null;

            TTSSpeaker.speak(mContext.getString(R.string.destination_switch), TTSSpeaker.HIGH);
        } else {
            TTSSpeaker.speak(mContext.getString(R.string.navi_ready), TTSSpeaker.HIGH);
        }

        Log.d(TAG,"Start navi to :"+address);

        isNaving = true;
        TTSSpeaker.speak(mContext.getString(R.string.route_plan), TTSSpeaker.HIGH);

        Log.d(TAG,"定位当前位置...");
        locMonitor.startLocation(new LocationMonitor.LocationObserver() {

            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {

                        //定位成功回调信息，设置相关消息
                        if (startPoint == null){

                            float accuracy = aMapLocation.getAccuracy();

                            if (accuracy > 200.0){

                                if (startWaitingTime > 0){
                                    if ((System.currentTimeMillis()-startWaitingTime) > 30*1000){
                                        stopNavi(STOP_REASON_INTERRUPT);
                                        TTSSpeaker.speak(mContext.getString(R.string.navi_end_3), TTSSpeaker.HIGH);
                                        startWaitingTime = -1;
                                    }

                                } else {
                                    startWaitingTime = System.currentTimeMillis();
                                    TTSSpeaker.speak(mContext.getString(R.string.gps_weak), TTSSpeaker.HIGH);
                                }

                            } else {

                                startPoint = locMonitor.getCurPoint();
                                Log.d(TAG,"当前位置定位成功");
                                //目的地地理解析
                                getLatLon(address);

                            }

                        }

                        double curLatitude = aMapLocation.getLatitude();//获取纬度
                        double curLongitude = aMapLocation.getLongitude();//获取经度
                        float bearing = aMapLocation.getBearing();  //获取当前方位

                        //获取meta的转向角度
                        AxisData axisData = new AxisData();
                        HCMetaUtils.getLibSensorAxisData(axisData);
                        bearing = axisData.getGyro_x();
                        Log.d(TAG,"meta current gyro_x :" + bearing);
                        //上报当前位置
                        sendLocation(curLongitude,curLatitude,bearing);


                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        if (startPoint == null){
                            Log.d(TAG,"当前位置定位失败");
                            isNaving = false;
                            TTSSpeaker.speak(mContext.getString(R.string.navi_end_4), TTSSpeaker.HIGH);
                            locMonitor.setLocationObserver(null);
                        }
                      Log.d(TAG,"Location  failure");

                    }
                } else {
                    if (startPoint == null){
                        Log.d(TAG,"当前位置定位失败");
                        isNaving = false;
                        TTSSpeaker.speak(mContext.getString(R.string.navi_end_4), TTSSpeaker.HIGH);
                        locMonitor.setLocationObserver(null);
                    }
                    Log.d(TAG,"Location  failure");
                }
            }
        });
    }

    public void startNaviTo(double longtitude, double latitude) {
        Log.d(TAG,"Enter startNaviTo :"+longtitude+","+latitude);
        //系统gps未开启
        if (!LocationMonitor.getInstance().isGpsEnabled()){
            TTSSpeaker.speak(mContext.getString(R.string.navi_gps_not_open), TTSSpeaker.HIGH);
            return;
        }

        //导航功能关闭
        if (!PreferenceUtils.getPrefBoolean("GPSEnable",true)){
            TTSSpeaker.speak(mContext.getString(R.string.navi_outdoor_disabled), TTSSpeaker.HIGH);
            return;
        }

        if (isNaving) {
            Log.d(TAG,"Navigating now ... ready to change the destination");
            mMapNavi.stopNavi();
            locMonitor.setLocationObserver(null);
            isStartNavi = false;
            isDestChanged = true;
            startPoint = null;

            TTSSpeaker.speak(mContext.getString(R.string.destination_switch), TTSSpeaker.HIGH);
        } else {
            TTSSpeaker.speak(mContext.getString(R.string.navi_ready), TTSSpeaker.HIGH);
        }

        endPoint = new NaviLatLng(latitude,longtitude);
        Log.d(TAG,"Start navi to :"+longtitude+","+latitude);

        isNaving = true;
        TTSSpeaker.speak(mContext.getString(R.string.route_plan), TTSSpeaker.HIGH);

        Log.d(TAG,"定位当前位置...");
        locMonitor.startLocation(new LocationMonitor.LocationObserver() {

            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {

                        //定位成功回调信息，设置相关消息
                        if (startPoint == null){

                            float accuracy = aMapLocation.getAccuracy();

                            if (accuracy > 200.0){

                                if (startWaitingTime > 0){
                                    if ((System.currentTimeMillis()-startWaitingTime) > 30*1000){
                                        stopNavi(STOP_REASON_USER_STOP);
                                        TTSSpeaker.speak(mContext.getString(R.string.navi_end_3), TTSSpeaker.HIGH);
                                        startWaitingTime = -1;
                                    }

                                } else {
                                    startWaitingTime = System.currentTimeMillis();
                                    TTSSpeaker.speak(mContext.getString(R.string.gps_weak), TTSSpeaker.HIGH);
                                }

                            } else {

                                startPoint = locMonitor.getCurPoint();
                                Log.d(TAG,"当前位置定位成功");
                                //规划路线
                                calculateRoute(locMonitor.getCurPoint(),endPoint);

                            }

                        }

                        double curLatitude = aMapLocation.getLatitude();//获取纬度
                        double curLongitude = aMapLocation.getLongitude();//获取经度
                        float bearing = aMapLocation.getBearing();  //获取当前方位

                        //获取meta的转向角度
                        AxisData axisData = new AxisData();
                        HCMetaUtils.getLibSensorAxisData(axisData);
                        bearing = axisData.getGyro_x();
                        Log.d(TAG,"meta current gyro_x :" + bearing);
                        //上报当前位置
                        sendLocation(curLongitude,curLatitude,bearing);


                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        if (startPoint == null){
                            Log.d(TAG,"当前位置定位失败");
                            isNaving = false;
                            TTSSpeaker.speak(mContext.getString(R.string.navi_end_4), TTSSpeaker.HIGH);
                            locMonitor.setLocationObserver(null);
                        }
                        Log.d(TAG,"Location  failure");

                    }
                } else {
                    if (startPoint == null){
                        Log.d(TAG,"当前位置定位失败");
                        isNaving = false;
                        TTSSpeaker.speak(mContext.getString(R.string.navi_end_4), TTSSpeaker.HIGH);
                        locMonitor.setLocationObserver(null);
                    }
                    Log.d(TAG,"Location  failure");
                }
            }
        });
    }

    public void restartNavi(){
        isNaving = false;
        startPoint = null;
        startNaviTo(endPoint.getLongitude(),endPoint.getLatitude());
    }

    public void hariConnected(){
        if (null != endPoint){
            restartNavi();
        }
    }

    public void stopNavi(int reason){
        if (isNaving){
            locMonitor.setLocationObserver(null);
            mMapNavi.stopNavi();
            isHiOnline = false;
            mMapNavi.destroy();
            initMapNavi();
            isNaving = false;
            isStartNavi = false;
            startPoint = null;
            if (reason != STOP_REASON_INTERRUPT){
                endPoint = null;
                isDestChanged = false;
            }

            TTSSpeaker.speak(mContext.getString(R.string.navi_end), TTSSpeaker.HIGH);
            sendStopNavi(reason);
        }

    }

    private android.os.Handler timeHandler = new android.os.Handler();

    private Runnable navInfoTimeoutCb = new Runnable() {
        @Override
        public void run() {

            isHiOnline = false;
            timeHandler.removeCallbacks(navInfoTimeoutCb);
            Log.d(TAG,"Navinfo time out...");
            TTSSpeaker.speak(mContext.getString(R.string.hari_warn_hi_error),TTSSpeaker.HIGH);
        }
    };

    private int startNaviSendedNum = 0;
    private Runnable startNaviTimeoutCb = new Runnable() {
        @Override
        public void run() {
            startNaviSendedNum ++;
            if (startNaviSendedNum < MAX_START_NAVI_RETRY){
                sendStartNavi();
            }
        }
    };

    /*
     * naviinfo response 当前hi在线，发送navInfo会收到response
     */
    public void onNaviInfoResponse(String type){
        if (type.equalsIgnoreCase("naviInfoResponse")){
            if (isStartNavi){
                isHiOnline = true;
            }
            timeHandler.removeCallbacks(navInfoTimeoutCb);
        } else if (type.equalsIgnoreCase("startNaviResponse") || type.equalsIgnoreCase("updateNaviResponse")){
            timeHandler.removeCallbacks(startNaviTimeoutCb);
        }

    }

    private void sendLocation(double longtitude, double latitude, float bearing){

        if (isStartNavi){
            CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
            if (cmdEngine!=null){
                JSONObject object = new JSONObject();
                try {

                    JSONArray curPoint = new JSONArray();
                    curPoint.put(locMonitor.getCurPoint().getLongitude()); //经度
                    curPoint.put(locMonitor.getCurPoint().getLatitude());
                    object.put("location",curPoint);
                    object.put("orientation",bearing);

                } catch (Exception e){

                }

                Log.d(TAG,"上传位置信息 ："+object.toString());
                cmdEngine.sendNaviInfo(object,"naviInfo");
                if (isHiOnline){
                    timeHandler.postDelayed(navInfoTimeoutCb,NAVI_INFO_TIME_OUT);
                }
            }
        }

    }

    private void sendStartNavi(){
        //路线计算成功
        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        if (cmdEngine!=null){
            JSONObject object = new JSONObject();
            try {
                object.put("naviType","outdoor");
                object.put("orientation","0");
                JSONArray fromPoint = new JSONArray();
                fromPoint.put(locMonitor.getCurPoint().getLongitude()); //经度
                fromPoint.put(locMonitor.getCurPoint().getLatitude());
                object.put("from",fromPoint);
                JSONArray toPoint = new JSONArray();
                toPoint.put(endPoint.getLongitude());
                toPoint.put(endPoint.getLatitude());
                object.put("to",toPoint);
                cmdEngine.sendNaviInfo(object,"startNavi");

                timeHandler.postDelayed(startNaviTimeoutCb,START_NAVI_TIME_OUT);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
      偏航 路线重新规划 通知坐席
     */
    private void sendUpdateNavi(){

        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        if (cmdEngine!=null){
            JSONObject object = new JSONObject();
            try {
                object.put("naviType","outdoor");
                object.put("orientation","0");
                JSONArray fromPoint = new JSONArray();
                fromPoint.put(locMonitor.getCurPoint().getLongitude()); //经度
                fromPoint.put(locMonitor.getCurPoint().getLatitude());
                object.put("from",fromPoint);
                JSONArray toPoint = new JSONArray();
                toPoint.put(endPoint.getLongitude());
                toPoint.put(endPoint.getLatitude());
                object.put("to",toPoint);
                cmdEngine.sendNaviInfo(object,"updateNavi");

                timeHandler.postDelayed(startNaviTimeoutCb,START_NAVI_TIME_OUT);

            } catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private void sendStopNavi(int reason){
        CommandEngine cmdEngine = HariServiceClient.getCommandEngine();
        if (cmdEngine!=null){
            JSONObject object = new JSONObject();
            try {
                object.put("reason",reason==STOP_REASON_ARRIVE_DEST?"已到达目的地，结束导航！":"用户结束导航");
                object.put("naviType","outdoor");
            }catch (Exception e){
                e.printStackTrace();
            }
            cmdEngine.sendNaviInfo(object,"stopNavi");
        }
    }


    /**************************          导航相关回调         *********************************/

    @Override
    public void onInitNaviFailure() {
        Log.d(TAG,"onInitNaviFailure");
        TTSSpeaker.speak(mContext.getString(R.string.navi_end_5), TTSSpeaker.HIGH);
    }

    @Override
    public void onInitNaviSuccess() {
        //初始化成功
        Log.d(TAG,"onInitNaviSuccess");
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onStartNavi(int type) {
        //开始导航回调
    }

    @Override
    public void onTrafficStatusUpdate() {
        //
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {
        //当前位置回调

        Log.d(TAG,"onLocationChange :"+location.getCoord().toString());

    }

    @Override
    public void onGetNavigationText(int type, String text) {
        //播报类型和播报文字回调
        Log.d(TAG,text);
        TTSSpeaker.speak(text, TTSSpeaker.NAVIINTO);
        MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,text);
    }

    @Override
    public void onEndEmulatorNavi() {
        //结束模拟导航
    }

    @Override
    public void onArriveDestination() {
        //到达目的地
        TTSSpeaker.speak(mContext.getString(R.string.destination_arrived), TTSSpeaker.HIGH);
        stopNavi(STOP_REASON_ARRIVE_DEST);
    }

    @Override
    public void onCalculateRouteSuccess() {
        //路线计算成功
        TTSSpeaker.speak(mContext.getString(R.string.route_plan_s), TTSSpeaker.HIGH);
        startNaviSendedNum = 0;
        if (isDestChanged){
            sendUpdateNavi();
        } else {
            sendStartNavi();
        }

        mMapNavi.startNavi(NaviType.GPS);   //开始导航
        isStartNavi = true;
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
        //路线计算失败
        Log.d(TAG,"路径规划失败");
        TTSSpeaker.speak(mContext.getString(R.string.navi_end_6), TTSSpeaker.HIGH);
        locMonitor.setLocationObserver(null);
        isNaving = false;
        isStartNavi = false;
        startPoint = null;

    }

    @Override
    public void onReCalculateRouteForYaw() {
        //偏航后重新计算路线回调
        Log.d(TAG,"您已偏航，重新规划路径");
        TTSSpeaker.speak(mContext.getString(R.string.route_replan), TTSSpeaker.HIGH);
        sendUpdateNavi();
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        //拥堵后重新计算路线回调
    }

    @Override
    public void onArrivedWayPoint(int wayID) {
        //到达途径点
    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
        //GPS开关状态回调
        Log.d(TAG,"GpsOpenStatus:"+enabled);
    }


    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
        //导航过程中的信息更新，请看NaviInfo的具体说明
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        //已过时
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        //已过时
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        //显示转弯回调
    }

    @Override
    public void hideCross() {
        //隐藏转弯回调
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        //显示车道信息

    }

    @Override
    public void hideLaneInfo() {
        //隐藏车道信息
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        //多路径算路成功回调
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        //更新交通设施信息
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        //更新巡航模式的统计信息
    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        //更新巡航模式的拥堵信息
    }

    @Override
    public void onPlayRing(int i) {

    }

}
