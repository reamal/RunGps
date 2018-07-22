package com.bravo.rungps.ui;

import java.util.LinkedList;
import java.util.List;

import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bravo.rungps.bean.PositionBean;
import com.bravo.rungps.ui.base.BasePresenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

/**
 * ClassName:MainActPresenter <br/>
 * Function: Persenter. <br/>
 * Date: 2017年3月17日 下午2:46:10 <br/>
 *
 * @author Administrator
 * @version
 */
public class MainActPresenter extends BasePresenter<MainActView> implements MainActModelListener {

    private MainActView mActView;

    public MainActPresenter(MainActView mActView) {
        super();
        this.mActView = mActView;
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
