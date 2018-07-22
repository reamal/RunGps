package com.bravo.rungps.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import com.baidu.mapapi.model.LatLng;
import com.bravo.rungps.MsgObservable;
import com.bravo.rungps.RunConstants;
import com.bravo.rungps.adapter.MLocationListener;
import com.bravo.rungps.bean.Information;
import com.bravo.rungps.bean.PositionBean;
import com.bravo.rungps.ui.base.App;
import com.bravo.rungps.ui.base.BaseService;
import com.bravo.rungps.utils.Logger;
import com.bravo.rungps.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static com.bravo.rungps.RunConstants.FIRST_LOCATION_MSG;
import static com.bravo.rungps.RunConstants.RUN_FIRST_LOCATION_MSG;
import static com.bravo.rungps.RunConstants.RUN_LOCATION_MSG;

public class WorkService extends BaseService<WorkSerView, WorkSerPresenter> implements WorkSerView {

    static final int HASH_CODE = 1;

    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService = false;
    public static boolean isRunStop = true;

    public static Disposable sSubscription;
    private LocationManager locationManager;

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.启动守护服务;
     * 5.守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用.
     */
    int onStart(Intent intent, int flags, int startId) {
        startForeground(HASH_CODE, new Notification());
        //----------业务逻辑----------

        //实际使用时，根据需求，将这里更改为自定义的条件，判定服务应当启动还是停止 (任务是否需要运行)
        if (sShouldStopService) stopService();
        else startService();

        //----------业务逻辑----------
        return START_STICKY;
    }

    // 设置室外跑的监听。从startCommand调用。
    @SuppressLint("MissingPermission")
    private void setOutDoorRunListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /**
         * 获取一次位置信息。
         */

        Location location = mSerPersenter.getLatLng(locationManager);
        if (location != null) {
            Message msg = Message.obtain();
            msg.what = RunConstants.FIRST_LOCATION_MSG;
            msg.obj = location;
            MsgObservable.getInstance().sendMsgs(msg);
            Logger.d(LTAG, "bestProvider : get the location success!");
        } else {
            Logger.d(LTAG, "bestProvider : get the location failed!");
        }

        // GPS位置变化监听。
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            //期望间隔两秒或者两米更新一次。
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000, 2, locationGpsListener);
            locationManager.addGpsStatusListener(statusListener);
        } else {

        }
    }


    void startService() {
        //检查服务是否不需要运行
        if (sShouldStopService) return;
        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接 return
        if (sSubscription != null && !sSubscription.isDisposed()) return;

        //----------业务逻辑----------

        // 如果需要加载室外跑数据，注册地理位置的监听。
        setOutDoorRunListener();

        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
                    System.out.println("保存数据到磁盘。");
                }).subscribe(count -> {
//                    System.out.println("每 3 秒采集一次数据... count = " + count);
                });

        //----------业务逻辑----------
    }

    /**
     * 停止服务并取消定时唤醒
     * <p>
     * 停止服务使用取消订阅的方式实现，而不是调用 Context.stopService(Intent name)。因为：
     * 1.stopService 会调用 Service.onDestroy()，而 WorkService 做了保活处理，会把 Service 再拉起来；
     * 2.我们希望 WorkService 起到一个类似于控制台的角色，即 WorkService 始终运行 (无论任务是否需要运行)，
     * 而是通过 onStart() 里自定义的条件，来决定服务是否应当启动或停止。
     */
    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sSubscription != null) sSubscription.dispose();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    void onEnd(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
        startService(new Intent(App.getInstance(), WorkService.class));
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    @Override
    protected WorkSerPresenter createPresenter() {
        return new WorkSerPresenter();
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd(null);
    }

    @Override
    public void onRespondError(String message) {

    }


    public static int mGpsSatelliteNumber = 0;

    // 状态监听
    @SuppressLint("MissingPermission")
    private GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Logger.i(LTAG, "第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    mGpsSatelliteNumber = count;
//                    Logger.w(LTAG, "mGpsSatelliteNumber : " + mGpsSatelliteNumber);
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Logger.i(LTAG, "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    mGpsSatelliteNumber = 0;
                    Logger.i(LTAG, "定位结束");
                    break;
            }
        }

        ;
    };


    public static Location preLocation;
    private static PositionBean oncePosition;
    private static ArrayList<LatLng> mLocationList = new ArrayList<>();

    /**
     * location的监听。
     */
    private MLocationListener locationGpsListener = new MLocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Logger.w(LTAG, "locationListener : onStatusChanged ,  status : " + status);
            if (status != LocationProvider.AVAILABLE) {
                // 修改一个Gps状态为不可用。
            }
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(final Location mlocation) {
            if (mlocation == null) {
                return;
            }
            if (!isRunStop) {
                ThreadPoolUtils.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        if (preLocation != null) {
                            // 将google的坐标转为Baidu的坐标,修改mLocation坐标。
                            double latitude = mlocation.getLatitude();
                            double longitude = mlocation.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            latLng = mSerPersenter.gpsToBaidu(latLng);
                            mlocation.setLatitude(latLng.latitude);
                            mlocation.setLongitude(latLng.longitude);

                            // 如果位置和上一次记录的位置点相同，则认为没有移动。
                            if (preLocation.getLatitude() != latLng.latitude
                                    || preLocation.getLongitude() != latLng.longitude) {

                                oncePosition = mSerPersenter.getMoveGpsData(preLocation,
                                        mlocation);
                                double totalDistance = mSerPersenter
                                        .getDoubleToDouble(oncePosition.distance);

                                // 将变动的点存入集合中

                                // 如果locationList
                                // 集合的数量大于startTimes，则说明这段开始已经有记录的点。
                                mLocationList.add(latLng);

                                Information info =
                                        new Information(Information.GET_ONCE_LOCATION,
                                                mLocationList, oncePosition, mlocation);
                                Message msg = Message.obtain();
                                msg.what = RUN_LOCATION_MSG;
                                msg.obj = info;
                                MsgObservable.getInstance().sendMsgs(msg);
                            }

                        } else {

                            double latitude = mlocation.getLatitude();
                            double longitude = mlocation.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            latLng = mSerPersenter.gpsToBaidu(latLng);
                            mlocation.setLatitude(latLng.latitude);
                            mlocation.setLongitude(latLng.longitude);

                            mLocationList.add(latLng);

                            Message msg = Message.obtain();
                            msg.what = RUN_FIRST_LOCATION_MSG;
                            msg.obj = mlocation;
                            MsgObservable.getInstance().sendMsgs(msg);
                        }

                        preLocation = mlocation;
                    }

                });

            } else {
                // showToast("位置改变了一次，暂停状态未记录！");
                preLocation = null;
                if (mLocationList.size() > 0) {
                    mLocationList.clear();
                }
                Message msg = Message.obtain();
                msg.what = FIRST_LOCATION_MSG;
                msg.obj = mlocation;
                MsgObservable.getInstance().sendMsgs(msg);
            }
        }
    };
}
