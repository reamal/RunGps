
package com.bravo.rungps.ui.base;

import android.content.Context;
import android.support.v4.app.Fragment;


/**
 * ClassName:BaseMVPFragment <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Date: 2017年1月10日 下午4:13:18 <br/>
 * 
 * @author Administrator
 * @version
 */
public abstract class BaseMVPFragment<V extends BaseView, P extends BasePresenter<V>>
        extends Fragment implements BaseView{
    // Persenter类的实例。
    public P mFragPersenter;

    @Override
    public void onAttach(Context context) {
        mFragPersenter = createPersenter();
        if (mFragPersenter != null) {
            // BasePersenter类的方法。主要用于将View用弱引用赋值给P层的View对象
            mFragPersenter.attach((V) this);
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        if (mFragPersenter != null) {
            // BasePersenter类的方法。主要用于将View的引用清除。
            mFragPersenter.detach();
        }
        super.onDetach();
    }

    // 子类实现，具体类型创建具体P层对象。
    protected abstract P createPersenter();

}
