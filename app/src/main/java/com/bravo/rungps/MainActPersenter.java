
package com.bravo.rungps;

import java.util.LinkedList;
import java.util.List;

import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bravo.rungps.bean.PositionBean;
import com.bravo.rungps.interfaces.MainActModelListener;
import com.bravo.rungps.interfaces.MainActView;

import android.location.Location;
import android.location.LocationManager;

/**
 * ClassName:MainActPersenter <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Date: 2017年3月17日 下午2:46:10 <br/>
 * 
 * @author Administrator
 * @version
 */
public class MainActPersenter implements MainActModelListener {

    private MainActView mActView;

    public MainActPersenter(MainActView mActView) {
        super();
        this.mActView = mActView;
    }

    public Location getLatLng(LocationManager locationManager) {
        double latitude = 0.0;
        double longitude = 0.0;
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

    /**
     * 标准的GPS经纬度坐标直接在地图上绘制会有偏移，这是测绘局和地图商设置的加密，要转换成百度地图坐标
     *
     * @return 百度地图坐标
     */
    public LatLng gpsToBaidu(LatLng data) {// data格式 nmea标准数据 ddmm.mmmmm,ddmm.mmmm
                                            // 如3030.90909,11449.1234

        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(data);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    public MapStatusUpdate getMapStatusUpdate(LinkedList<PositionBean> pointsList) {
        if (pointsList != null && pointsList.size() > 0) {
            LatLng lastLatLng = pointsList.get(pointsList.size() - 1).latlng;
            // 定义地图状态
            MapStatus mMapStatus = new MapStatus.Builder().target(lastLatLng).build();
            // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            // 改变地图状态
            return mMapStatusUpdate;
        } else {
            return null;
        }
    }

}
