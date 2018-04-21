  
package com.bravo.rungps.ui.base;

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
    private   static Application instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 百度地图
        SDKInitializer.initialize(getApplicationContext());
    }

}
  