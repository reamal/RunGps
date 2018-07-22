package com.bravo.rungps.ui.base;

import android.os.Bundle;


/**
 * ClassName:BaseActActivity <br/>
 * Function: MVP模式 Activity基类，继承BaseActivity. T ：为View层的实现类。 P为Persenter的具体实现类<br/>
 * Date: 2016年12月23日 下午1:58:53 <br/>
 * 
 * @author Administrator
 * @version
 */
public abstract class BaseMVPActivity<V extends BaseView, P extends BasePresenter<V>>
		extends BaseActivity implements BaseView {

	// Persenter类的实例。
	public P mActPersenter;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActPersenter = createPresenter();
		if (mActPersenter != null) {
			// BasePersenter类的方法。主要用于将View用弱引用赋值给P层的View对象
			mActPersenter.attach((V) this);
		}
	}

	// 子类实现，具体类型创建具体P层对象。
	protected abstract P createPresenter();

	@Override
	protected void onDestroy() {
		if (mActPersenter != null) {
			// BasePersenter类的方法。主要用于将View的引用清除。
			mActPersenter.detach();
		}
		super.onDestroy();
	}

	/**
	 * 网络请求错误，返回message时，如果想直接弹提示，调用这里。
	 * @param message
	 */
	@Override
	public void onRespondError(String message) {
		showToast(message);
	}

//	private void showToast(String message) {
//		Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
//	}

}
