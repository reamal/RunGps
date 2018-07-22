package com.bravo.rungps.bean;
import java.util.ArrayList;
import java.util.LinkedList;

import com.baidu.mapapi.model.LatLng;

import android.location.Location;

public class Information {
    public int mInfoType;
    public ArrayList<LatLng> mLocationList;
    public PositionBean mPositionBean;
    public Location location;

    public static final int FIRST_GET_LOCATION = 1;
    public static final int GET_ONCE_LOCATION = 2;

    public Information(int infoType, ArrayList<LatLng> mLocationList,
                       PositionBean mPositionBean,Location location) {
        super();
        mInfoType = infoType;
        this.mLocationList = mLocationList;
        this.mPositionBean = mPositionBean;
        this.location = location;
    }

}