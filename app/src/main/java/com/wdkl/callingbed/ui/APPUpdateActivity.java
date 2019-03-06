package com.wdkl.callingbed.ui;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.wdkl.callingbed.R;
import com.wdkl.callingbed.base.BaseActivity;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.util.DownloadUtil;
import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.util.StringUtils;
import com.wdkl.callingbed.util.ToastUtil;
import com.wdkl.callingbed.util.silentupdate.SilentUpdateUtil;
import com.wdkl.callingbed.widget.view.ProgressView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;

/**
 * Created by Waderson on 2017/8/23.
 */

public class APPUpdateActivity extends BaseActivity {

    @Bind(R.id.activity_appupdate_dialog_progressview)
    ProgressView progressview;

    String downLoadURL = "";

    @Override
    public int getLayoutId() {
        return R.layout.activity_appupdate_layout;
    }

    @Override
    protected void initUtil() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        downLoadURL = getIntent().getStringExtra("downLoadURL");
        if (StringUtils.notEmpty(downLoadURL)) {
            downLoadAPK(downLoadURL);
        }
    }

    /**
     * 下载APK包
     */
    public void downLoadAPK(String url) {
        LogUtil.d(APPUpdateActivity.class, "downLoadAPK  url==" + url);
        progressview.setCurProgress(0);
        DownloadUtil.getInstance().download(url, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                LogUtil.d("SDERDF", "onDownloadSuccess==" + "成功");
                updateAPPHandler.sendEmptyMessage(APP_UPDATE_SUCSSED);
            }

            @Override
            public void onDownloading(int progress) {
                LogUtil.d("SDERDF", "onDownloading==" + progress);
                Message message = updateAPPHandler.obtainMessage();
                message.obj = progress;
                message.what = APP_UPDATE_ING;
                updateAPPHandler.sendMessage(message);
            }

            @Override
            public void onDownloadFailed() {
                LogUtil.d("SDERDF", "onDownloadFailed==" + "失败");
                updateAPPHandler.sendEmptyMessage(APP_UPDATE_FILED);
            }
        });
    }

    public static final int APP_UPDATE_SUCSSED = 1200;
    public static final int APP_UPDATE_ING = 1300;
    public static final int APP_UPDATE_FILED = 1400;
    @SuppressLint("HandlerLeak")
    Handler updateAPPHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APP_UPDATE_SUCSSED:
                    ToastUtil.showToast("正在安装中...");
                    LogUtil.d(APPUpdateActivity.class, "下载==APP_UPDATE_SUCSSED");
                    //升级
                    SilentUpdateUtil.updateApk(APPUpdateActivity.this);
                    break;
                case APP_UPDATE_ING:
                    int progress = (int) msg.obj;
                    progressview.setCurProgress(progress);
                    break;
                case APP_UPDATE_FILED:
                    if (StringUtils.notEmpty(downLoadURL)) downLoadAPK(downLoadURL);
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
    }
}

