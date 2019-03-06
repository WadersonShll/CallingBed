package com.wdkl.callingbed.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.wdkl.callingbed.MyApplication;
import com.wdkl.callingbed.R;
import com.wdkl.callingbed.base.BaseActivity;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.InitDataEntity;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.util.AutoRebootUtil;
import com.wdkl.callingbed.util.DateUtil;
import com.wdkl.callingbed.util.DownloadUtil;
import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.util.NetUtil;
import com.wdkl.callingbed.util.StringUtils;
import com.wdkl.callingbed.util.ToastUtil;
import com.wdkl.callingbed.util.UdpSendUtil;
import com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.OnClick;
import okhttp3.Call;
import serialporttest.utils.SerialPortUtil;

import static com.wdkl.callingbed.MyApplication.mScreenExtinguishUtil;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.closeHeart;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setNurseBrightness;

/**
 * 类名称：InitActivity <br>
 * 类描述：APP初始化页面<br>
 * 创建人：Waderson <br>
 * 创建时间：2018-02-01 <br>
 *
 * @version V1.0
 */

public class InitActivity extends BaseActivity implements SerialPortUtil.ISerialPortBedOnclickEvent {
    @Bind(R.id.activity_init_layout_rl)
    View initView;
    @Bind(R.id.activity_init_layout_iv_loading)
    ImageView ivLoading;
    AnimationDrawable animationDrawable;

    ConnectivityManager cm;
    /**
     * mac地址
     */
    String macAddress = "";

    SerialPortUtil serialPortUtil;
    private int countsReboot;//重启计数

    @Override
    public int getLayoutId() {
        return R.layout.activity_init_layout;
    }


    @Override
    protected void initView() {
        animationDrawable = (AnimationDrawable) ivLoading.getBackground();
        animationDrawable.start();
    }

    @Override
    protected void initUtil() {
        serialPortUtil = ((MyApplication) this.getApplication()).serialPortUtil;
        serialPortUtil.setOnDataReceiveListener(this);
    }

    @Override
    protected void initData() {
        //刚进来的时候将所有的护理灯全部灭掉
        dismissNurseBrightness();

        dateRefresh();

    }

    @Override
    public View getLoadingTargetView() {
        return initView;
    }

    @OnClick(R.id.activity_init_layout_rl)
    public void init(View view) {
        isNewWork();
    }

    /**
     * 获取网络请求地址头
     */
    private void getLocalWayAddress() {
        try {
            Constants.URL = "http://" + NetUtil.getLocalElement(NetUtil.getLocalInetAddress().toString());
        } catch (Exception e) {
            LogUtil.e("getLocalWayAddress", "Exception==" + e.toString());
            CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
            showNetErrorView("MAC地址: " + macAddress + "\nError：请检查网络连接是否正常"
                    + "\n" + AutoRebootUtil.getTextTip());
            e.printStackTrace();
        }
    }

    /**
     * 获取MAC地址
     */
    private void getMacAddress() {
        if (NetUtil.isethernetConnected(cm)) {
            macAddress = NetUtil.getLocalMacAddressFromIp();
            if (null != macAddress) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.MSG_SP, Context.MODE_PRIVATE); //私有数据
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("macAddress", macAddress);
                editor.commit();//提交修改
            }
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.MSG_SP, Context.MODE_PRIVATE); //私有数据
            macAddress = sharedPreferences.getString("macAddress", null);
        }
        LogUtil.d("getMacAddress", "macAddress==" + macAddress);
        Constants.MAC_ADDRESS = macAddress;
    }


    /**
     * 判断网络连接
     * 无网络显示错误页面
     */
    public void isNewWork() {
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    CallingBedSendCommand.setSipStatus(serialPortUtil, "2");
                    getInitData();
                } else {
                    updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
                    showNetErrorView("MAC地址: " + macAddress + "\nError：本地网络或wifi连接错误");
                    ToastUtil.showToast("网络连接错误");
                    CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
                }
            } else {
                updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
                CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
//                showNetErrorView("MAC地址: " + macAddress + "\nError：网络信息初始化错误");
                autoReboot();

            }
        } else {
            updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
            showNetErrorView("MAC地址: " + macAddress + "\nError：ConnectivityManager初始化失败");
            CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
            ToastUtil.showToast("ConnectivityManager初始化失败");
        }
    }

    /**
     * =================================================（网络不通）15次以上自动重启系统=====================================================
     */
    private void autoReboot() {
        if (countsReboot == 16) {
            AutoRebootUtil.calculate(this);
            countsReboot = 0;
        }
        countsReboot++;
    }

    /**
     * 检查APP更新版本
     */
    private void appCheckUpdate() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        try {
            OkHttpUtils.post().url(Constants.URL + Constants.URL_END + Constants.APP_CHECK_UPDATE).build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Constants.UPDATE_APP_FLAG = false;
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            String data = response.substring(0, response.length() - 4);
                            LogUtil.d("appCheckUpdate", "appCheckUpdate==" + data);
                            try {
                                JSONObject object = new JSONObject(data);
                                if (object.getString("Code").equals("OK!")) {
                                    float APPVersion = StringUtils.parseFloat(StringUtils.deleteCharAt(object.getString("APPVersion"), 0));
                                    float APPVersion_Now = StringUtils.parseFloat(StringUtils.getAppVersionName(InitActivity.this));
                                    String downloadURL = object.getString("downloadURL");
                                    if (APPVersion_Now != APPVersion) {//本来是“<”的；但有个别机器老是不升级成功
                                        if (StringUtils.notEmpty(downloadURL)) {
                                            closeHeart();//关闭心跳
                                            DownloadUtil.APP_VERSION = APPVersion;
                                            Intent intent = new Intent(InitActivity.this, APPUpdateActivity.class);
                                            intent.putExtra("downLoadURL", Constants.URL + Constants.URL_END + "/" + downloadURL);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Constants.UPDATE_APP_FLAG = false;
                            }
                        }
                    });
        } catch (Exception e) {
            Constants.UPDATE_APP_FLAG = false;
            e.printStackTrace();
        }

    }

    /**
     * 获取初始化信息
     */
    private void getInitData() {
        LogUtil.d("getInitData", "URL==" + Constants.URL + Constants.URL_END + Constants.CALLINGBED_INIT);
        if (Constants.URL.length() > 7) {
            OkHttpUtils
                    .post()
                    .url(Constants.URL + Constants.URL_END + Constants.CALLINGBED_INIT)
                    .addParams("deviceMAC", macAddress)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            dismissNurseBrightness();
                            showNetErrorView("MAC地址: " + macAddress + "\nError：" + "数据请求错误[MAC地址是否注册？]");
                            updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
                            ToastUtil.showToast("数据请求错误");
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            String data = response.substring(0, response.length() - 4);
                            LogUtil.d("getInitData", "getInitData==" + data);
                            try {
                                showContent();
                                JSONObject object = new JSONObject(data);
                                if (object.getString("Code").equals("ERROR!")) {
                                    dismissNurseBrightness();
                                    showNetErrorView("MAC地址: " + macAddress + "\nError：" + "数据请求失败");
                                    updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
                                    ToastUtil.showToast("数据请求失败");
                                } else {
                                    if (object.getString("deviceStatus").equals("1")) {
                                        Gson gson = new Gson();
                                        InitDataEntity initDataEntity = gson.fromJson(data, InitDataEntity.class);
                                        saveData(initDataEntity);
                                        Intent intent = new Intent();
                                        intent.putExtra(Constants.INITENTITY, initDataEntity);
                                        intent.setClass(InitActivity.this, CallingBedActivity.class);
                                        startActivity(intent);
                                        InitActivity.this.finish();
                                    } else {
                                        dismissNurseBrightness();
                                        showNetErrorView("MAC地址: " + macAddress + "\nError: " + "设备未启用");
                                        updateHandler.sendEmptyMessageDelayed(404, DATEREFRESH_DELAYMILLIS);
                                        ToastUtil.showToast("设备未启用");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void saveData(InitDataEntity initDataEntity) {
        if (null != initDataEntity) {
            Constants.SIP_ID = initDataEntity.getDeviceSipId();
            Constants.SIP_PASS_WORD = initDataEntity.getDeviceSipPassWord();
            Constants.SIP_IP = initDataEntity.getDeviceSipIp();
            Constants.DEVICE_WIFI_HOST_NAME = initDataEntity.getDeviceWifiHostName();
            Constants.BED_ID = initDataEntity.getId();
            Constants.CALLMAIN_ID = initDataEntity.getDeviceHostingID();
            Constants.MYSELF_ID = initDataEntity.getId();
            Constants.ROOM_ID = initDataEntity.getDeviceRoomId();
            Constants.DEVICE_SCREEN_SLEEP = initDataEntity.getDeviceScreamSleep();
            Constants.DEVICE_HUMAN_ID = initDataEntity.getDeviceHumanId();
            LogUtil.d("saveData", "deviceHostingID==" + initDataEntity.getDeviceHostingID());
            if (StringUtils.notEmpty(initDataEntity.getDeviceHostingID())) {
                String n = StringUtils.substringByLengh(initDataEntity.getDeviceHostingID(), 0, 1);
                if ("#".equals(n)) {//服务器托管状态：处于托管中
                    String nDis = StringUtils.deleteCharAt(initDataEntity.getDeviceHostingID(), 0);
                    Constants.DEVICE_HOSTING_ID = nDis.split(",")[0];
                    Constants.TRUST_NEW_MAIN_ID = nDis.split(",")[1];
                } else {//服务器托管状态：处于未托管
                    Constants.DEVICE_HOSTING_ID = initDataEntity.getDeviceHostingID();
                    Constants.TRUST_NEW_MAIN_ID = "";
                }
            }
            setSystemTime(initDataEntity);
        }
    }

    /**
     * 设置系统时间 (邓喆)
     *
     * @param initDataEntity
     */
    private void setSystemTime(InitDataEntity initDataEntity) {
        LogUtil.d("setSystemTime", initDataEntity.getCurTime());
        String[] time = initDataEntity.getCurTime().trim().split(" ");
        String[] day = time[0].split("/");
        String[] hour = time[1].split(":");
        //=================设置时间（可用）
        DateUtil.setSystemTime(this, Integer.valueOf(day[0]), Integer.valueOf(day[1]) - 1, Integer.valueOf(day[2])
                , Integer.valueOf(hour[0]), Integer.valueOf(hour[1]), Integer.valueOf(hour[2]));

//        if (!TimeUtil.isRoot()) {
//            String t = initDataEntity.getCurTime().trim().replaceAll("/", "").replace(":", "").replace(" ", ".");
//            if (t != null && t != "") {
//                TimeUtil.setSysDate(t);
//            }
//        }
    }

    @Override
    public void onReload() {
        super.onReload();
        dateRefresh();
    }

    public void dateRefresh() {
        cm = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) getMacAddress();
        getLocalWayAddress();
        isNewWork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        animationDrawable.stop();
        updateHandler = null;
        serialPortUtil = null;//2018-01-10 Waderson
    }

    /**
     * 灭掉护理灯
     */
    public void dismissNurseBrightness() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setNurseBrightness(serialPortUtil, 1, "000000", "000000", "000000", "000000", "000000");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 监听呼叫按钮 buffer[5]   如果按下则 放松注册信息给后台
     */
    @Override
    public void serialPortBedOnclick(byte[] buffer) {

        mScreenExtinguishUtil.touchScreen();//===============================息屏

        //呼叫护士键短按松开
        if (buffer[5] == 1) {
            if (null != Constants.MAC_ADDRESS) {
                //有线mac地址，设备出场信息，无线mac地址
                String str = "MGR_REG_A" + Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "4" + Constants.DELIMITER + android.os.Build.DISPLAY +
                        Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "FF:FF:FF:FF:FF:FF" + Constants.DELIMITER + SerialPortUtil.KEY_ID;
                UdpSendUtil.sendManualReboot(str);
            }
        }
        //呼叫护士键长按松开
        if (buffer[5] == 2) {
            if (null != Constants.MAC_ADDRESS) {
                //有线mac地址，设备出场信息，无线mac地址
                String str = "MGR_REG_A" + Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "4" + Constants.DELIMITER + android.os.Build.DISPLAY +
                        Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "FF:FF:FF:FF:FF:FF" + Constants.DELIMITER + SerialPortUtil.KEY_ID;
                UdpSendUtil.sendManualReboot(str);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 404:
                    dateRefresh();
                    break;
            }
        }
    };

    public static final long DATEREFRESH_DELAYMILLIS = 3000;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case Constants.EVENT_MGR_APP_UPDATE://APP更新
                appCheckUpdate();
                break;
        }
    }
}
