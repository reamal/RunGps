  
package com.bravo.rungps.adapter;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/** 
 * ClassName:MLocationListener <br/> 
 * Function: TODO ADD FUNCTION. <br/> 
 * Date:     2017年3月17日 下午3:07:05 <br/> 
 * @author   Administrator 
 * @version       
 */
public class MLocationListener implements LocationListener{


    // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // Provider被enable时触发此函数，比如GPS被打开
    @Override
    public void onProviderEnabled(String provider) {

    }

    // Provider被disable时触发此函数，比如GPS被关闭
    @Override
    public void onProviderDisabled(String provider) {

    }

    // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        
    }

}
  