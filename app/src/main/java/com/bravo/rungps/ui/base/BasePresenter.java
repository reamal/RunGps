package com.bravo.rungps.ui.base;

import java.lang.ref.WeakReference;

/**
 * ClassName:BasePresenter <br/>
 * Function: Persenter层的基类，主要实现View层引用的attach和detach. <br/>
 * Date: 2016年12月23日 上午11:02:12 <br/>
 * 
 * @author Administrator
 * @version
 */
public abstract class BasePresenter<T extends BaseView> {

	// 弱引用的View
	public WeakReference<T> mActView;

	public BasePresenter() {
		super();
	}

	// 获取View中view引用。
	protected T getView() {
		return mActView.get();
	}

	// 判断view的引用是否仍持有，没有被GC回收。
	public boolean isViewAttached() {
		return mActView != null && mActView.get() != null;
	}

	// 将View对象以若引用的形式给mActView持有。
	public void attach(T view) {
		if (mActView == null) {
			mActView = new WeakReference<T>(view);
		}
	}

	// 将View在Activity调用onDestory时，释放。以便于Activiy销毁掉之后，内存可以被正常回收。
	public void detach() {
		if (mActView != null) {
			mActView.clear();
			mActView = null;
		}
	}


}
