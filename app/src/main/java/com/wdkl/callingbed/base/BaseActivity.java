package com.wdkl.callingbed.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.widget.dialog.LoadingActivityDialog;
import com.wdkl.callingbed.widget.loading.VaryViewHelperController;
import com.zhy.autolayout.AutoLayoutActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

/**
 * Created by 胡博文 on 2017/8/14.
 * BaseActivity父类
 */

public abstract class BaseActivity<T extends BasePresenterI, V> extends AutoLayoutActivity implements BaseViewI<V> {

    private String TAG;
//    public T mPresenter;

    private LoadingActivityDialog loadDialogView;
    protected VaryViewHelperController mVaryViewHelperController;
    public Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());
        context = this;

        ButterKnife.bind(this);
        TAG = this.getClass().getSimpleName();

        if (null != getLoadingTargetView()) {
            mVaryViewHelperController = new VaryViewHelperController(getLoadingTargetView());
        }

        initView();
        initUtil();
        initData();
    }


    /**
     * Base基本类
     */
    public abstract int getLayoutId();

    /**
     * 设置toolbar
     */
    protected abstract void initUtil();

    /**
     * 设置initView
     */
    protected abstract void initView();

    protected abstract void initData();

    /**
     * 显示加载弹框
     */
    @Override
    public void showProgress() {
        if (loadDialogView == null) {
            loadDialogView = LoadingActivityDialog.createDialog(BaseActivity.this);
        }

        loadDialogView.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if (loadDialogView != null) {
            loadDialogView.dismiss();
        }
//        MyApplication.getRefWatcher(this).watch(this);
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 隐藏加载弹框
     */
    @Override
    public void hideProgress() {
        if (loadDialogView != null) {
            loadDialogView.dismiss();
        }
    }

    @Override
    public void onReload() {

    }

    @Override
    public void showDataError(String errorMessage, int tag) {
        hideProgress();
    }

    @Override
    public void showDataSuccess(V datas) {
        hideProgress();
    }

    /**
     * 加载中的的View
     */
    @Override
    public void showLoadingView() {
        if (mVaryViewHelperController == null) {
            throw new IllegalStateException("no ViewHelperController");
        }
        mVaryViewHelperController.showLoading();
    }

    /**
     * 初始化失败的View
     */
    @Override
    public void showInitError() {
        if (mVaryViewHelperController == null) {
            throw new IllegalStateException("no ViewHelperController");
        }
        mVaryViewHelperController.showInitError(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReload();
            }
        });
    }

    /**
     * 加载失败的View
     */
    @Override
    public void showNetErrorView(String str) {
        if (mVaryViewHelperController == null) {
            throw new IllegalStateException("no ViewHelperController");
        }
        mVaryViewHelperController.showNetworkError(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReload();
            }
        },str);
    }

    /**
     * 加载不到数据的View
     */
    @Override
    public void showEmptyView(String msg) {
        if (mVaryViewHelperController == null) {
            throw new IllegalStateException("no ViewHelperController");
        }
        mVaryViewHelperController.showEmpty(msg);
    }

    @Override
    public void showContent() {
        if (mVaryViewHelperController == null) {
            throw new IllegalStateException("no ViewHelperController");
        }
        LogUtil.v(TAG, "调用");
        mVaryViewHelperController.restore();
    }

    public View getLoadingTargetView() {
        return null;
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * 添加自定义颜色
     * */
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

}
