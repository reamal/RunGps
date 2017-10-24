  
package com.bravo.rungps;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

/** 
 * ClassName:App <br/> 
 * Function: TODO ADD FUNCTION. <br/> 
 * Date:     2017年3月16日 下午8:10:15 <br/> 
 * @author   Administrator 
 * @version       
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        // 百度地图
        SDKInitializer.initialize(getApplicationContext());
    }

}
  