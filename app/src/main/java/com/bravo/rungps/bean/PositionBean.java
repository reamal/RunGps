
package com.bravo.rungps.bean;

import com.baidu.mapapi.model.LatLng;

/**
 * ClassName:PositionBean <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Date: 2017年3月18日 下午3:00:25 <br/>
 * 
 * @author Administrator
 * @version
 */
public class PositionBean {
    public LatLng latlng;
    public double distance;
    public long currentTime;
    public double velocity;
    public long preGapTime;
    public float gpsSpeed;

    public PositionBean setLatlng(LatLng latlng) {
        this.latlng = latlng;
        return this;
    }

    public PositionBean setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    public PositionBean setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
        return this;
    }

    public PositionBean setVelocity(double velocity) {
        this.velocity = velocity;
        return this;
    }

    public PositionBean setPreGapTime(long preGapTime) {
        this.preGapTime = preGapTime;
        return this;
    }

    public PositionBean setGpsSpeed(float gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
        return this;
    }

}
