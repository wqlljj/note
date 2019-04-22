package com.example.wqllj.locationshare.model.baidumap.navi_bike_wake;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.CoordinatePointBean;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.bean.RouteLineBean;
import com.example.wqllj.locationshare.db.operator.EventOperator;
import com.example.wqllj.locationshare.db.operator.PersonOperator;
import com.example.wqllj.locationshare.db.operator.RouteLineOperator;
import com.example.wqllj.locationshare.model.baidumap.BaiDuClient;

import java.util.ArrayList;
import java.util.List;

public class BNaviMainActivity extends Activity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Marker mMarkerA;
    private Marker mMarkerB;
    private Marker mMarkerNow;

    private LatLng startPt,endPt;

    private BikeNavigateHelper mNaviHelper;
    private WalkNavigateHelper mWNaviHelper;
    BikeNaviLaunchParam param;
    WalkNaviLaunchParam walkParam;
    BDLocation bdLocation;
    private static boolean isPermissionRequested = false;

    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdB = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markb);
    BitmapDescriptor bdNow = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_openmap_focuse_mark);
    BitmapDescriptor bdMark = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_openmap_mark);
    private BDLocationListener locationListener;
    private String TAG="BNaviMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_main);
        requestPermission();
        mMapView = (MapView) findViewById(R.id.mapview);

        Button bikeBtn = (Button) findViewById(R.id.button);
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!resetNaviData()) return;
                startBikeNavi();
            }
        });
        Button carBtn = (Button) findViewById(R.id.button2);
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!resetNaviData()) return;
                BaiDuClient.getInstance().startPlan(new BNRoutePlanNode(startPt.longitude,startPt.latitude,"","",BNRoutePlanNode.CoordinateType.BD09LL),
                        new BNRoutePlanNode(endPt.longitude,endPt.latitude,"","",BNRoutePlanNode.CoordinateType.BD09LL));
            }
        });

        Button walkBtn = (Button) findViewById(R.id.button1);
        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!resetNaviData()) return;
                startWalkNavi();
            }
        });

        try {
            mNaviHelper = BikeNavigateHelper.getInstance();
            mWNaviHelper = WalkNavigateHelper.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DbManager.getInstance().getOperator(RouteLineOperator.class).insertOrReplace(routeLineBean);
        locationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
//                Log.e(TAG, "onReceiveLocation:" );
                if(BNaviMainActivity.this.bdLocation==null){
                    Log.e(TAG, "onReceiveLocation: initMapStatus" );
                    BNaviMainActivity.this.bdLocation = bdLocation;
                    initMapStatus();
//                    initOverlay();
                }else{
                    BNaviMainActivity.this.bdLocation = bdLocation;
                }
            }
        };
        BaiDuClient.getInstance().startLocation(locationListener);
        param = new BikeNaviLaunchParam();
        walkParam = new WalkNaviLaunchParam();
    }

    private boolean resetNaviData() {
        startPt=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
        if(endPt==null){
            Toast.makeText(BNaviMainActivity.this, "请选择目的地！", Toast.LENGTH_SHORT).show();
            return false;
        }
        walkParam.stPt(startPt).endPt(endPt);
        param.stPt(startPt).endPt(endPt);
        return true;
    }
    Handler mHandler=new Handler();
    private Polyline mPolyline;
    private List<CoordinatePointBean> allLatLng=new ArrayList<>();
    private List<CoordinatePointBean> latLngs=new ArrayList<>();
    boolean isNeedTrajectory=true;
    private final int tra_time_interval = 5000;
    PersonBean personBean = DbManager.getInstance().getOperator(PersonOperator.class).queryByKey(1l);
    RouteLineBean routeLineBean = new RouteLineBean(allLatLng, System.currentTimeMillis(),personBean.getId(), personBean);
    Runnable trajectoryTask=new Runnable() {
        @Override
        public void run() {
            if(latLngs.size()>1) {
                List<LatLng> lngs=new ArrayList<>();
                for (CoordinatePointBean latLng : latLngs) {
                    lngs.add(latLng.getLatLng());
                }
                OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(lngs);
                //在地图上画出线条图层，mPolyline：线条图层
                mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
                mPolyline.setZIndex(3);
                CoordinatePointBean pointBean = latLngs.get(latLngs.size()-1);
                if(allLatLng.size()!=0) {
                    latLngs.remove(0);
                }
                allLatLng.addAll(latLngs);
                DbManager.getInstance().getOperator(RouteLineOperator.class).insertOrReplace(routeLineBean);
                List<RouteLineBean> routeLineBeen = DbManager.getInstance().getOperator(RouteLineOperator.class).queryAll();
                for (RouteLineBean lineBean : routeLineBeen) {
                    Log.e(TAG, "run: "+ lineBean);
                }
                latLngs.clear();
                EventBean eventBean = new EventBean(personBean.getName(), 1, "购物", personBean.getId(), pointBean.getId(),pointBean);
                DbManager.getInstance().getOperator(EventOperator.class).insertOrReplace(eventBean);
                latLngs.add(0,pointBean);
                List<CoordinatePointBean> pointBeen = DbManager.getInstance().getOperator(RouteLineOperator.class).queryByTime(routeLineBean.getDate()).get(0).getLatLngs();
                Log.e(TAG, "run: "+pointBeen.size() );
                for (CoordinatePointBean coordinatePointBean : pointBeen) {
                    Log.e(TAG, "run: "+coordinatePointBean );
                }
                List<EventBean> eventBeen = DbManager.getInstance().getOperator(EventOperator.class).queryAll();
                for (EventBean bean : eventBeen) {
                    Log.e(TAG, "run: "+bean );
                }
                MarkerOptions ooA = new MarkerOptions().position(pointBean.getLatLng()).icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markb))
                        .zIndex(10).draggable(true).title(eventBean.getData());
                mBaiduMap.addOverlay(ooA);


//                //创建InfoWindow展示的view
//                Button button = new Button(getApplicationContext());
////                button.setBackgroundResource(R.drawable.icon_message);
//                button.setText(eventBean.getData());
////定义用于显示该InfoWindow的坐标点
//                LatLng pt = eventBean.getPointBean().getLatLng();
//
////创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
//                InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);
//
////显示InfoWindow
//                mBaiduMap.showInfoWindow(mInfoWindow);

//                //定义文字所显示的坐标点
//                LatLng llText = eventBean.getPointBean().getLatLng();
//
////构建文字Option对象，用于在地图上添加文字
//                OverlayOptions textOption = new TextOptions()
//                        .bgColor(0xAAFFFF00)
//                        .fontSize(60)
//                        .fontColor(0xFFFF00FF)
//                        .text(eventBean.getData())
//                        .rotate(0)
//                        .position(llText);
//
////在地图上添加该文字对象并显示
//                mBaiduMap.addOverlay(textOption);
            }
            if(isNeedTrajectory) {
                mHandler.postDelayed(trajectoryTask, tra_time_interval);
            }
        }
    };
    private void initMapStatus(){
        mBaiduMap = mMapView.getMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        latLngs.add(new CoordinatePointBean(latLng,routeLineBean.getId(),System.currentTimeMillis()));
        mHandler.postDelayed(trajectoryTask, tra_time_interval);
        builder.target(latLng).zoom(19);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        MarkerOptions ooA = new MarkerOptions().position(latLng).icon(bdNow)
                .zIndex(9).draggable(true);
        mMarkerNow = (Marker) (mBaiduMap.addOverlay(ooA));
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!TextUtils.isEmpty(marker.getTitle())) {
//                创建InfoWindow展示的view
                    Button button = new Button(getApplicationContext());
//                button.setBackgroundResource(R.drawable.icon_message);

                    button.setText(marker.getTitle());
//定义用于显示该InfoWindow的坐标点
                    LatLng pt = marker.getPosition();

//创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                    InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);

//显示InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }else{
                    mBaiduMap.hideInfoWindow();
                }
                Toast.makeText(BNaviMainActivity.this, "点击"+marker.getId(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                Toast.makeText(BNaviMainActivity.this, "拖动"+marker.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(BNaviMainActivity.this, "点击"+marker.getId(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                endPt=latLng;
                latLngs.add(new CoordinatePointBean(endPt,routeLineBean.getId(),System.currentTimeMillis()));
                if(mMarkerB!=null&&mMarkerB.isDraggable()){
                    mMarkerB.remove();
                }
                MarkerOptions oo = new MarkerOptions().position(latLng).icon(bdMark)
                        .zIndex(9).draggable(true);
                mMarkerB = (Marker) (mBaiduMap.addOverlay(oo));
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                endPt=mapPoi.getPosition();
                latLngs.add(new CoordinatePointBean(endPt,routeLineBean.getId(),System.currentTimeMillis()));
                if(mMarkerB!=null&&mMarkerB.isDraggable()){
                    mMarkerB.remove();
                }
                MarkerOptions oo = new MarkerOptions().position(mapPoi.getPosition()).icon(bdMark)
                        .zIndex(9).draggable(true);
                mMarkerB = (Marker) (mBaiduMap.addOverlay(oo));
//                Toast.makeText(BNaviMainActivity.this, mapPoi.getUid()+"\n"+
//                        mapPoi.getName()+"\n"+mapPoi.getPosition(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }


    private void startBikeNavi() {
        Log.d("View", "startBikeNavi");
        try {
            mNaviHelper.initNaviEngine(this, new IBEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d("View", "engineInitSuccess");
                    routePlanWithParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d("View", "engineInitFail");
                }
            });
        } catch (Exception e) {
            Log.d("Exception", "startBikeNavi");
            e.printStackTrace();
        }
    }

    private void startWalkNavi() {
        Log.d("View", "startBikeNavi");
        try {
            mWNaviHelper.initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d("View", "engineInitSuccess");
                    routePlanWithWalkParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d("View", "engineInitFail");
                }
            });
        } catch (Exception e) {
            Log.d("Exception", "startBikeNavi");
            e.printStackTrace();
        }
    }

    private void routePlanWithParam() {
        mNaviHelper.routePlanWithParams(param, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("View", "onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d("View", "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(BNaviMainActivity.this, BNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Log.d("View", "onRoutePlanFail");
            }

        });
    }
    private void routePlanWithWalkParam() {
        mWNaviHelper.routePlanWithParams(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("View", "onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d("View", "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(BNaviMainActivity.this, WNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d("View", "onRoutePlanFail");
            }

        });
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {

            isPermissionRequested = true;

            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (permissions.size() == 0) {
                return;
            } else {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isNeedTrajectory=false;
        mMapView.onDestroy();
        bdA.recycle();
        bdB.recycle();
    }
}
