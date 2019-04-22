package com.baidumap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangqi on 2018/6/7.
 */

public class BaiDuClient {
    private static BaiDuClient baiDuClient;
//    public LocationClient mLocationClient = null;
//    LocationClientOption option = new LocationClientOption();
//    private MyLocationListener myListener = new MyLocationListener();
    private AppCompatActivity mainActivity;
    private String TAG="BaiDuClient";

    public static BaiDuClient getInstance() {
        if (baiDuClient == null) {
            baiDuClient = new BaiDuClient();
        }
        return baiDuClient;
    }

    public void init(Context context) {
//        mLocationClient = new LocationClient(context);
//        //声明LocationClient类
//        mLocationClient.registerLocationListener(myListener);
//        //注册监听函数
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
////可选，设置定位模式，默认高精度
////LocationMode.Hight_Accuracy：高精度；
////LocationMode. Battery_Saving：低功耗；
////LocationMode. Device_Sensors：仅使用设备；
//
//        option.setCoorType("bd09ll");
////可选，设置返回经纬度坐标类型，默认gcj02
////gcj02：国测局坐标；
////bd09ll：百度经纬度坐标；
////bd09：百度墨卡托坐标；
////海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
//
//        option.setScanSpan(1000);
////可选，设置发起定位请求的间隔，int类型，单位ms
////如果设置为0，则代表单次定位，即仅定位一次，默认为0
////如果设置非0，需设置1000ms以上才有效
//
//        option.setOpenGps(true);
////可选，设置是否使用gps，默认false
////使用高精度和仅用设备两种定位模式的，参数必须设置为true
//
//        option.setLocationNotify(true);
////可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
//
//        option.setIgnoreKillProcess(false);
////可选，定位SDK内部是一个service，并放到了独立进程。
////设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
//
//        option.SetIgnoreCacheException(false);
////可选，设置是否收集Crash信息，默认收集，即参数为false
//
//        option.setWifiCacheTimeOut(5 * 60 * 1000);
////可选，7.2版本新增能力
////如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
//
//        option.setEnableSimulateGps(false);
////可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
//
//        option.setIsNeedAddress(true);
////可选，是否需要地址信息，默认为不需要，即参数为false
////如果开发者需要获得当前点的地址信息，此处必须为true
//
//        option.setIsNeedLocationPoiList(true);
////可选，是否需要周边POI信息，默认为不需要，即参数为false
////如果开发者需要获得周边POI信息，此处必须为true
//
//        option.setIsNeedLocationDescribe(true);
////可选，是否需要位置描述信息，默认为不需要，即参数为false
////如果开发者需要获得当前点的位置信息，此处必须为true
//        mLocationClient.setLocOption(option);
////mLocationClient为第二步初始化过的LocationClient对象
////需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
////更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        //车载导航
        mainActivity = (AppCompatActivity) context;
        BaiduNaviManagerFactory.getBaiduNaviManager().init(mainActivity,
                Environment.getExternalStorageDirectory().getAbsolutePath(),"LocationSharing", new IBaiduNaviManager.INaviInitListener() {
                    private boolean hasInitSuccess=false;
                    String authinfo="";
                    @Override
                    public void onAuthResult(int status, String msg) {
                        if (0 == status) {
                            authinfo = "key校验成功!";
                        } else {
                            authinfo = "key校验失败, " + msg;
                        }
                        mainActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Log.e(TAG, "run: " +authinfo);
                                Toast.makeText(mainActivity, authinfo, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void initStart() {
                        Log.e(TAG, "initStart: 百度导航引擎初始化开始" );
                        Toast.makeText(mainActivity, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Log.e(TAG, "initSuccess: 百度导航引擎初始化成功" );
                        Toast.makeText(mainActivity, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        hasInitSuccess = true;

                        // 初始化tts
                        initTTS(mainActivity);
                        startPlan();
                    }

                    @Override
                    public void initFailed() {
                        Log.e(TAG, "initFailed: 百度导航引擎初始化失败" );
                        Toast.makeText(mainActivity, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private void initTTS(Context context) {
        BaiduNaviManagerFactory.getTTSManager().initTTS(context,
                Environment.getExternalStorageDirectory().getAbsolutePath(),"LocationSharing", "11369172");

        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayError");
                    }
                }
        );

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.e("BNSDKDemo", "ttsHandler.msg.what=" + msg.what);
                    }
                }
        );
    }

    static final String ROUTE_PLAN_NODE = "routePlanNode";
    public void startPlan(){
        int coType =BNRoutePlanNode.CoordinateType.BD09LL;//百度经纬度
        BNRoutePlanNode sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", "百度大厦", coType);
        BNRoutePlanNode eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", "北京天安门", coType);
        switch (coType) {
            case BNRoutePlanNode.CoordinateType.GCJ02: {//国际经纬度
                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", "百度大厦", coType);
                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", "北京天安门", coType);
                break;
            }
            case BNRoutePlanNode.CoordinateType.WGS84: {//国家局坐标
                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", "百度大厦", coType);
                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", "北京天安门", coType);
                break;
            }
            case BNRoutePlanNode.CoordinateType.BD09_MC: {//百度墨卡托
                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", "百度大厦", coType);
                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", "北京天安门", coType);
                break;
            }
            case BNRoutePlanNode.CoordinateType.BD09LL: {//百度经纬度
                sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", "百度大厦", coType);
                eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", "北京天安门", coType);
                break;
            }
            default:
                ;
        }

        final BNRoutePlanNode mStartNode = sNode;

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                    list,
                    IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                    null,
                    new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                    Toast.makeText(mainActivity, "算路开始", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                    Toast.makeText(mainActivity, "算路成功", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                    Toast.makeText(mainActivity, "算路失败", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                    Toast.makeText(mainActivity, "算路成功准备进入导航", Toast.LENGTH_SHORT)
                                            .show();
                                    Intent intent = new Intent(mainActivity,
                                            DemoGuideActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(ROUTE_PLAN_NODE, mStartNode);
                                    intent.putExtras(bundle);
                                    mainActivity.startActivity(intent);
                                    break;
                                default:
                                    // nothing
                                    break;
                            }
                        }
                    });
        }
    }
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public void start() {
        //调用LocationClient的start()方法，便可发起定位请求
//        start()：启动定位SDK；stop()：关闭定位SDK。调用start()之后只需要等待定位结果自动回调即可。
//        开发者定位场景如果是单次定位的场景，在收到定位结果之后直接调用stop()函数即可。
//        如果stop()之后仍然想进行定位，可以再次start()等待定位结果回调即可。
//        自v7.2版本起，新增LocationClient.reStart()方法，用于在某些特定的异常环境下重启定位。
//        mLocationClient.start();
    }

    public void stop() {
//        mLocationClient.stop();
    }
}
