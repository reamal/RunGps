package com.bravo.rungps.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.bravo.rungps.R;
import com.bravo.rungps.adapter.LatAdapter;
import com.bravo.rungps.adapter.MLocationListener;
import com.bravo.rungps.bean.PositionBean;
import com.bravo.rungps.ui.base.BaseMVPActivity;
import com.bravo.rungps.utils.Logger;


public class MainActivity extends BaseMVPActivity<MainActView,MainActPersenter> implements OnClickListener, MainActView {

    protected static final int EMPTY_MSG = 0;
    private MapView mMapView;
    private ImageView mIvPosition;
    private ListView mLvLocationLats;
    private TextView mTvDis;
    private TextView mTvCalorie;
    private TextView mTvHeartRate;
    private TextView mTvHourV;
    private TextView mTvTimes;
    private TextView mTvV;
    private Button mBtnPause;
    private Button mBtnStart;
    private Button mBtnEnd;
    private BaiduMap mBaiduMap;
    private SDKReceiver mReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAttch = false;
        mMapView.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        isAttch = true;
        initView();
        // 判断有没有Gps,有Gps给LocationManager赋值。
        initGPSWithLocationManager();
        initListener();
        initMap(savedInstanceState);
        initEvent();
        startTimeThread();
    }

    @Override
    protected MainActPersenter createPersenter() {
        return new MainActPersenter(this);
    }

    long runTime;

    private void startTimeThread() {
        Thread timeThead = new Thread(new Runnable() {

            @Override
            public void run() {
                while (isAttch) {
                    SystemClock.sleep(1000);
                    if (!isPause) {
                        runTime += 1000;
                        mainHandler.sendEmptyMessage(EMPTY_MSG);
                    }
                }

            }
        });
        timeThead.start();

    }

    private void initListener() {

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
                locationGpsListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1,
                locationNetListener);
        // locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1,
        // locationListener);
    }

    /**
     * location的监听。
     */
    private MLocationListener locationNetListener = new MLocationListener() {

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location mlocation) {
            if (mlocation != null) {
                if (!isPause) {// 如果没有暂停，则记录数据。
                    if (mMoveNetDataList != null && mMoveNetDataList.size() >= 1) {
                        speed = mlocation.getSpeed();
                        double latitude = mlocation.getLatitude();
                        double longitude = mlocation.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        Logger.e(LTAG, "B_latLng : " + latLng.latitude + "," + latLng.longitude);
                        latLng = gpsToBaidu(latLng);

                        LatLng last = mPointListWithNet.getLast();
                        PositionBean lastPosition = mMoveNetDataList.getLast();
                        // 如果位置和上一次记录的位置点相同，则认为没有移动。
                        if (last.latitude != latLng.latitude
                                || last.longitude != latLng.longitude) {
                            // 计算移动距离,和速度。
                            PositionBean oncePosition = getMoveNetData(lastPosition, latLng);
                            if (oncePosition == null) {
                                showToast("点漂移了一次！");
                            } else {
                                mPointListWithNet.add(latLng);
                                mMoveNetDataList.add(oncePosition);
                                mListNetLocationAdapter.update(mMoveNetDataList);
//                                updateListenerView();
                                Logger.e(LTAG,
                                        "TLocationLatLng : " + mPointListWithNet.size());
                            }
                        } else {
                            Logger.e(LTAG, "未移动,LocationSpeed : " + speed);
                            if (onces % 10 == 0) {
                                showToast("未移动,LocationSpeed : " + speed);
                                onces = 0;
                            }
                            onces++;
                        }
                    } else {
                        firstGetLocation(mlocation);
                    }

                } else {// 如果暂停了，则toast提示。
                    showToast("位置改变了一次，暂停状态未记录！");
                }
            }
        }

    };
    /**
     * location的监听。
     */
    private MLocationListener locationGpsListener = new MLocationListener() {

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location mlocation) {
            if (mlocation != null) {
                if (!isPause) {// 如果没有暂停，则记录数据。
                    if (mMoveGpsDataList != null && mMoveGpsDataList.size() >= 1) {

                        speed = mlocation.getSpeed();
                        double latitude = mlocation.getLatitude();
                        double longitude = mlocation.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        Logger.e(LTAG, "B_latLng : " + latLng.latitude + "," + latLng.longitude);
                        latLng = gpsToBaidu(latLng);
                        
                        PositionBean lastPosition = mMoveGpsDataList.getLast();
                        // 如果位置和上一次记录的位置点相同，则认为没有移动。
                        if (lastPosition.latlng.latitude != latLng.latitude
                                || lastPosition.latlng.longitude != latLng.longitude) {
                            // 计算移动距离,和速度。
                            PositionBean oncePosition = getMoveGpsData(lastPosition, latLng);
                            if (oncePosition == null) {
                                showToast("点漂移了一次！");
                            } else {
                                mPointListWithGps.add(latLng);
                                mMoveGpsDataList.add(oncePosition);
                                mListGpsLocationAdapter.update(mMoveGpsDataList);
                                updateListenerView();
                            }
                        } else {
                            Logger.e(LTAG, "未移动,LocationSpeed : " + speed);
                            if (onces % 10 == 0) {
                                showToast("未移动,LocationSpeed : " + speed);
                                onces = 0;
                            }
                            onces++;
                        }
                    } else {
                        firstGetLocation(mlocation);
                    }

                } else {// 如果暂停了，则toast提示。
                    showToast("位置改变了一次，暂停状态未记录！");
                }
            }
        }

    };

    private PositionBean getMoveGpsData(PositionBean lastPosition, LatLng latLng) {
        // TODO 计算经纬度变化一次时，的距离
        long currentTime = System.currentTimeMillis();// 参数 当前时间：毫秒
        Logger.e(LTAG, currentTime + "");
        long pastTime = currentTime - lastPosition.currentTime;// 过去的时间：毫秒
        mTotalTimes += pastTime;

        float[] result = new float[3];

        LatLng start = lastPosition.latlng;
        LatLng end = latLng;
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude,
                result);
        double distance = result[0];// 参数 距离 单位：米
        mTotalDistance += distance;
        double velocity = (distance * 1000) / pastTime;
        // if (velocity > 10) {
        // return null;
        // } else {
        return new PositionBean().setCurrentTime(currentTime).setDistance(distance)
                .setLatlng(latLng).setVelocity(velocity).setPreGapTime(pastTime).setGpsSpeed(speed);
        // }

    }
    private PositionBean getMoveNetData(PositionBean lastPosition, LatLng latLng) {
        // TODO 计算经纬度变化一次时，的距离
        long currentTime = System.currentTimeMillis();// 参数 当前时间：毫秒
        Logger.e(LTAG, currentTime + "");
        long pastTime = currentTime - lastPosition.currentTime;// 过去的时间：毫秒
//        mTotalTimes += pastTime;
        
        float[] result = new float[3];
        
        LatLng start = lastPosition.latlng;
        LatLng end = latLng;
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude,
                result);
        double distance = result[0];// 参数 距离 单位：米
//        mTotalDistance += distance;
        double velocity = (distance * 1000) / pastTime;
        // if (velocity > 10) {
        // return null;
        // } else {
        return new PositionBean().setCurrentTime(currentTime).setDistance(distance)
                .setLatlng(latLng).setVelocity(velocity).setPreGapTime(pastTime).setGpsSpeed(speed);
        // }
        
    }

    private boolean isPause = true;

    private void initEvent() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        Location location = mActPersenter.getLatLng(locationManager);
        if (location != null) {
            firstGetLocation(location);
        }

        mIvPosition.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnEnd.setOnClickListener(this);
        mListGpsLocationAdapter = new LatAdapter(this, mMoveGpsDataList);
        mLvThreadLats.setAdapter(mListGpsLocationAdapter);
        mListNetLocationAdapter = new LatAdapter(this, mMoveNetDataList);
        mLvLocationLats.setAdapter(mListNetLocationAdapter);
        // updateThreadView();
        updateListenerView();
    }

    private void firstGetLocation(Location location) {
        speed = location.getSpeed();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        latLng = mActPersenter.gpsToBaidu(latLng);

        PositionBean positionBean =
                new PositionBean().setLatlng(latLng).setCurrentTime(System.currentTimeMillis())
                        .setDistance(0).setVelocity(0).setPreGapTime(0).setGpsSpeed(speed);

        mPointListWithNet.add(latLng);
        mPointListWithGps.add(latLng);
        mMoveNetDataList.add(positionBean);
        mMoveGpsDataList.add(positionBean);
        // 将地图移动到LatLng点。
        moveMap(mMoveGpsDataList);
    }

    // 将地图移动到list的最后一个点。
    private void moveMap(LinkedList<PositionBean> list) {
        MapStatusUpdate mMapStatusUpdate = mActPersenter.getMapStatusUpdate(list);
        if (mMapStatusUpdate != null) {
            mBaiduMap.setMapStatus(mMapStatusUpdate);
        } else {
            showToast("暂未拿到当前位置");
        }
    }

    private float mTotalDistance;
    private long mTotalTimes;
    private float speed;

    // private void updateThreadView() {
    //
    // mTvDis.setText("距离 ： " + distance);
    // mTvV.setText("配速 ： " + speed + "m/s");
    // mTvTimes.setText("时间 ： " + getFormatTime(times));
    //
    // mBaiduMap.clear();
    // // if (mPointListWithLocation != null && mPointListWithLocation.size() > 1) {
    // // OverlayOptions polylineOption =
    // // new PolylineOptions().color(R.color.red).points(mPointListWithLocation);
    // // mBaiduMap.addOverlay(polylineOption);
    // // }
    //
    // if (mPointListWithThread != null && mPointListWithThread.size() > 1) {
    // OverlayOptions polylineOption =
    // new PolylineOptions().color(Color.RED).points(mPointListWithThread);
    // mBaiduMap.addOverlay(polylineOption);
    // }
    //
    // // if (mPointListWithLocation != null && mPointListWithLocation.size() > 0) {
    // // LatLng latLng = mPointListWithLocation.get(0);
    // // if (latLng != null) {
    // // MyLocationData locData = new MyLocationData.Builder().accuracy(40)
    // // // 此处设置开发者获取到的方向信息，顺时针0-360
    // // .direction(100).latitude(latLng.latitude).longitude(latLng.longitude)
    // // .build();
    // // mBaiduMap.setMyLocationData(locData);
    // // moveMap(mPointListWithLocation);
    // // }
    // // }
    //
    // if (mPointListWithThread != null && mPointListWithThread.size() > 0) {
    // LatLng latLng = mPointListWithThread.getLast();
    // if (latLng != null) {
    // MyLocationData locData = new MyLocationData.Builder().accuracy(40)
    // // 此处设置开发者获取到的方向信息，顺时针0-360
    // .direction(100).latitude(latLng.latitude).longitude(latLng.longitude)
    // .build();
    // mBaiduMap.setMyLocationData(locData);
    // }
    // }
    // }

    private void updateListenerView() {
        if (mMoveGpsDataList != null && mMoveGpsDataList.size() >= 1) {
            PositionBean positionBean = mMoveGpsDataList.getLast();
            mTvDis.setText("距离 ： " + mTotalDistance);
            mTvV.setText("配速 ： " + positionBean.velocity + "m/s");
            mTvTimes.setText("时间 ： " + getFormatTime(mTotalTimes));
            if (mPointListWithGps != null && mPointListWithGps.size() > 1) {
                mBaiduMap.clear();// 先将地图上的点清除。
                // 在地图上画线。
                OverlayOptions polylineOption = new PolylineOptions().points(mPointListWithGps)
                        .width(10).color(Color.RED);
                Polyline mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOption);
                // 在地图上画起始点。
                OverlayOptions markerOptions;
                markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_positioning_big))
                        .position(mPointListWithGps.get(0));
                Marker mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
            }

            // 设置当前位置的蓝色的点。
            LatLng latLng = positionBean.latlng;
            if (latLng != null) {
                MyLocationData locData = new MyLocationData.Builder().accuracy(40)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(latLng.latitude).longitude(latLng.longitude)
                        .build();
                mBaiduMap.setMyLocationData(locData);
            }
        }

    }

    private void initMap(Bundle savedInstanceState) {

        mMapView.onCreate(this, savedInstanceState);
        // 初始化地图
        mMapView.showZoomControls(false);

        mBaiduMap = mMapView.getMap();

        // 设置缩放级别
        mBaiduMap.setMapStatus(
                MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(19).build()));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启交通图层
        mBaiduMap.setTrafficEnabled(false);
        // 开启热力图层
        mBaiduMap.setBaiduHeatMapEnabled(false);

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    private void initGPSWithLocationManager() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MainActivity.this, "请打开GPS", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("请打开GPS");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // 转到手机设置界面，用户设置GPS
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                }
            });
            dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            dialog.show();
        } else {
            locationManager = lm;
        }
    }

    private void initView() {

        mMapView = (MapView) findViewById(R.id.mapView);

        mIvPosition = (ImageView) findViewById(R.id.iv_position);
        mLvLocationLats = (ListView) findViewById(R.id.lv_location);
        mLvThreadLats = (ListView) findViewById(R.id.lv_thread);
        mTvDis = (TextView) findViewById(R.id.tv_distance);
        mTvCalorie = (TextView) findViewById(R.id.tv_calorie);
        mTvHeartRate = (TextView) findViewById(R.id.tv_heart_rate);
        mTvHourV = (TextView) findViewById(R.id.tv_hour_v);
        mTvTimes = (TextView) findViewById(R.id.tv_times);
        mTvV = (TextView) findViewById(R.id.tv_v);

        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnEnd = (Button) findViewById(R.id.btn_end);

    }

    private void setDistance() {
        if (mPointListWithGps == null) {
            mTotalDistance = 0;
            return;
        }

        if (mPointListWithGps.size() < 2) {
            mTotalDistance = 0;
            return;
        }

        float[] result = new float[3];
        int size = mPointListWithGps.size();
        LatLng start = mPointListWithGps.get(size - 2);
        LatLng end = mPointListWithGps.get(size - 1);
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude,
                result);
        mTotalDistance += result[0];

    }

    private LinkedList<LatLng> mPointListWithGps = new LinkedList<>();
    private LinkedList<LatLng> mPointListWithNet = new LinkedList<>();
    private LinkedList<PositionBean> mMoveNetDataList = new LinkedList<>();
    private LinkedList<PositionBean> mMoveGpsDataList = new LinkedList<>();
    // private LinkedList<LatLng> mPointListWithThread = new LinkedList<>();

    // private boolean isFirstStart = true;
    // private LatAdapter mListThreadAdapter;
    private LocationManager locationManager;

    @Override
    public void onClick(View v) {
        // 点击事件
        switch (v.getId()) {
            case R.id.iv_position:
                // moveMap(mPointListWithLocation.size() >= mPointListWithThread.size()
                // ? mPointListWithLocation : mPointListWithThread);
                moveMap(mMoveGpsDataList);
                break;
            case R.id.btn_start:
                isPause = !isPause;
                if (isPause) {
                    mBtnStart.setText("Start");
                } else {
                    mBtnStart.setText("Pause");
                }
                // if (isFirstStart) {
                // startThread();
                // isFirstStart = false;
                // } else {
                //
                // }
                break;
            case R.id.btn_pause:
                isPause = true;
                break;
            case R.id.btn_end:
                isPause = true;
                break;

            default:
                break;
        }

    }

    int onces = 0;
    private ListView mLvThreadLats;
    private LatAdapter mListNetLocationAdapter;
    private LatAdapter mListGpsLocationAdapter;
    private boolean isAttch;
    private Handler mainHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMPTY_MSG:
                    mTvTimes.setText("时间 ： " + getFormatTime(runTime));
                    break;

                default:
                    break;
            }
        };
    };
    // private void startThread() {
    // Thread timeThread = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // while (isAttch) {
    // SystemClock.sleep(1000);
    // if (!isPause) {
    // times += 1000;
    // Location location = mActPersenter.getLatLng(locationManager);
    // if (location == null) {
    // continue;
    // }
    // speed = location.getSpeed();
    // double latitude = location.getLatitude();
    // double longitude = location.getLongitude();
    // LatLng latLng = new LatLng(latitude, longitude);
    // latLng = mActPersenter.gpsToBaidu(latLng);
    //
    // LatLng last = mPointListWithThread.getLast();
    //
    // if (last.latitude != latLng.latitude
    // || last.longitude != latLng.longitude) {
    // mPointListWithThread.add(latLng);
    // mListThreadAdapter.update(mPointListWithThread);
    // updateThreadView();
    // Logger.e(LTAG, "ThreadLatLng : " + mPointListWithThread.size());
    // } else {
    // if (onces % 20 == 0) {
    // mainHandler.post(new Runnable() {
    //
    // @Override
    // public void run() {
    // Logger.e(LTAG, "未移动,speed : " + speed);
    // showToast("未移动,speed : " + speed);
    // }
    // });
    // onces = 0;
    // }
    // onces++;
    //
    // }
    // }
    // }
    //
    // }
    // });
    // timeThread.start();
    // }
}
