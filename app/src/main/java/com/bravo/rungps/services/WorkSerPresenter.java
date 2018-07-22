package com.bravo.rungps.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bravo.rungps.bean.PositionBean;
import com.bravo.rungps.ui.MainActivity;
import com.bravo.rungps.ui.base.BasePresenter;

import java.util.List;

/**
 * Create on 2018/7/22 on 14:37
 * Description:
 * Coder by lilee
 */
public class WorkSerPresenter extends BasePresenter<WorkSerView>{

    public LatLng gpsToBaidu(LatLng latLng) {
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(latLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    public PositionBean getMoveGpsData(Location preLocation, Location mlocation) {
        // TODO 计算经纬度变化一次时，的距离
        long pastTime = mlocation.getTime() - preLocation.getTime();// 过去的时间：毫秒

        float[] result = new float[3];

        LatLng start = new LatLng(preLocation.getLatitude(), preLocation.getLongitude());
        LatLng end = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude,
                result);
        double distance = result[0];// 参数 距离 单位：米
        double velocity = pastTime / (distance * 60);
        return new PositionBean().setCurrentTime(mlocation.getTime()).setDistance(distance)
                .setLatlng(end).setVelocity(velocity).setPreGapTime(pastTime)
                .setGpsSpeed(mlocation.getSpeed());

    }

    public double getDoubleToDouble(double distance) {
        try {
            String str = String.valueOf(distance);
            str = str.substring(0,
                    str.length() < str.indexOf(".") + 3 ? str.length() : str.indexOf(".") + 3);

            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 返回查询条件
     * @return
     */
    public Criteria getCriteria(){
        Criteria criteria=new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        //设置是否需要方位信息
        criteria.setBearingRequired(false);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }

    /**
     * 获取gps最后一个点的位置。
     * @param mainActivity
     * @param locationManager
     * @return
     */
    @SuppressLint("MissingPermission")
    public Location getLatLng(LocationManager locationManager) {
        String provider;

        List<String> providerList = locationManager.getProviders(true);
        boolean isNet = providerList.contains(LocationManager.NETWORK_PROVIDER);
        boolean isGps = providerList.contains(LocationManager.GPS_PROVIDER);
        if (isNet) {// 网络提供器
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (isGps) { // GPS提供器
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = null;
        }

        if (provider != null) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                return null;
            } else {
                return location;
            }
        } else {
            return null;
        }
    }
}
