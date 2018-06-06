package com.cloudminds.meta.service.navigation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.navi.model.NaviLatLng;
import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.meta.util.DeviceUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zoey on 17/5/15.
 */

public class LocationMonitor implements AMapLocationListener {

    private final String TAG = "NAVI/LocationMonitor";
    private static LocationMonitor locationMonitor;
    private Context mContext;
    private AMapLocationClient mLocationClient;
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption;

    public static String cityCode = "";
    public static double curLatitude = 0.0;  //当前位置纬度
    public static double curLongitude = 0.0; //当前位置经度
    private LocationObserver locationObserver;

    private boolean enableReportLocation =  false;    //上传位置到hari 后台

    //英文环境使用framwork提供的api
    private LocationManager locationManager = null;

    public static LocationMonitor instance(Context context){
        if (null == locationMonitor){
            locationMonitor = new LocationMonitor();
            locationMonitor.init(context);
        }

        return locationMonitor;
    }

    public static LocationMonitor getInstance(){
        return locationMonitor;
    }

    public void setEnableReportLocation(boolean enable){
        enableReportLocation = enable;
    }

    public void init(Context context){
        mContext = context;
        if ("EN".equalsIgnoreCase(DeviceUtils.getSysLanguage())){
            //initEn();
        } else {
            initCH();
        }


    }

    private void initEn(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String serviceString = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) mContext.getSystemService(serviceString);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 0, locationListener);
    }

    private void initCH(){

        mLocationClient = new AMapLocationClient(mContext);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mLocationClient.setLocationListener(this);

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //启用手机传感器 用来获取方向角
        mLocationOption.setSensorEnable(true);
//设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
// 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
// 在定位结束后，在合适的生命周期调用onDestroy()方法
    }


    public NaviLatLng getCurPoint(){
        NaviLatLng latLng = new NaviLatLng();
        latLng.setLatitude(curLatitude);
        latLng.setLongitude(curLongitude);

        return latLng;
    }

    public void setLocationObserver(LocationObserver observer){
        locationObserver = observer;
    }


    public void startLocation(LocationObserver observer){

        if ("EN".equalsIgnoreCase(DeviceUtils.getSysLanguage())){
            initEn();
        } else {
            boolean gpsEnable = PreferenceUtils.getPrefBoolean("GPSEnable",true);
            if (gpsEnable){
                locationObserver = observer;
                mLocationClient.startLocation();
            }
        }

    }

    public void stopLocation(){

        if (null != locationManager){
            locationManager.removeUpdates(locationListener);
        }

        if (null != mLocationClient){
            mLocationClient.stopLocation();
        }

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                curLatitude = aMapLocation.getLatitude();//获取纬度
                curLongitude = aMapLocation.getLongitude();//获取经度
                Log.d(TAG,"定位精度 : "+aMapLocation.getAccuracy());

                if (cityCode.isEmpty()){
                    cityCode = aMapLocation.getCityCode();//城市编码
                }

                if (enableReportLocation){
                    reportLocation();
                    enableReportLocation = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.d(TAG,"location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }

        if (locationObserver != null){
            locationObserver.onLocationChanged(aMapLocation);
        }
    }

    public  interface LocationObserver{
        public void onLocationChanged(AMapLocation aMapLocation);
    }

    //英文环境下 位置更新回调
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            curLongitude = location.getLongitude();
            curLatitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public boolean isGpsEnabled(){
        boolean enable = true;

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
            enable = false;
        }

        return enable;
    }


    private void reportLocation(){
        Log.d(TAG, "reportRobotLocation"+"latitude:"+ curLatitude + " longitude:" + curLongitude);
        JSONObject object = new JSONObject();
        try {
            object.put("type","reportLocation");
            JSONObject locationInfo = new JSONObject();
            locationInfo.put("lng",curLongitude);
            locationInfo.put("lat",curLatitude);
            object.put("data",locationInfo);
        }catch (JSONException e){
            e.printStackTrace();
        }

        HariServiceClient.getCommandEngine().sendData(object);
    }

}
