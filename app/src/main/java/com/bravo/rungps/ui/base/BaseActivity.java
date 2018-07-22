  
package com.bravo.rungps.ui.base;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bravo.rungps.utils.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/** 
 * ClassName:BaseActivity <br/> 
 * Function: TODO ADD FUNCTION. <br/> 
 * Date:     2017年3月17日 下午2:44:43 <br/> 
 * @author   Administrator 
 * @version       
 */
public class BaseActivity extends FragmentActivity{
    public static final String LTAG = "MainActivity";

    /**
     * 得到一个格式化的时间
     * 
     * @param time
     * @return 时：分：秒：毫秒
     */
    public static String getFormatTime(long time) {
        time = time / 1000;
        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;
        // 毫秒秒显示两位
        // String strMillisecond = "" + (millisecond / 10);
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);
        return strHour + ":" + strMinute + ":" + strSecond;
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
    
    
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int toast) {
        Toast.makeText(this, getResources().getString(toast), Toast.LENGTH_SHORT).show();
    }
    
    
    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     *
     * 百度sdk key 校验
     */
    public class SDKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            Logger.d(LTAG, "action: " + s);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                showToast("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                showToast("网络出错");
            }
        }
    }
    
}
  