package com.wdkl.callingbed;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.wdkl.callingbed.service.APPService;
import com.wdkl.callingbed.util.ScreenExtinguishUtil;
import com.wdkl.callingbed.util.UdpHelper;
import com.wdkl.callingbed.util.anrfcutil.AnrFcExceptionUtil;
import com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus.WifiBindSipStatusConnector;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import serialporttest.utils.SerialPortUtil;

/**
 * Created by 胡博文 on 2017/8/17.
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG              #
 * #                                                   #
 */

public class MyApplication extends Application {
    private static Context sAppContext;
    //是否心跳（20180104）
    public volatile static boolean HEARTBEAT = true;
    //串口工具类
    public static SerialPortUtil serialPortUtil;

    public static UdpHelper helper;

    private WifiManager wifiManager;
    private WifiBindSipStatusConnector mWifiBindSipStatusConnector;
    public static ScreenExtinguishUtil mScreenExtinguishUtil;
//    private static RefWatcher refWatcher;
//    public static RefWatcher getRefWatcher(Context context) {
//        return refWatcher;
//    }


    public MyApplication() {
        sAppContext = getAppContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //refWatcher = LeakCanary.install(this);
        //ANR奔溃异常处理
        AnrFcExceptionUtil.getInstance(this).initFCException();
        //屏幕息屏处理
        mScreenExtinguishUtil = ScreenExtinguishUtil.getInstance(this);
        mScreenExtinguishUtil.controlScreenLight();

        sAppContext = getApplicationContext();
        serialPortUtil = new SerialPortUtil();
        serialPortUtil.openSerialPort();

        serialPortUtil.getKeyId();

        initClient();
        initUdp();
        //初始化关闭连接wifi热点，优先连接以太网
        closeConnectWifiHost();

        Intent bindIntent = new Intent(this, APPService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

    }

    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //APPService.ServiceBinder myBinder = (APPService.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void closeConnectWifiHost() {
        mWifiBindSipStatusConnector = WifiBindSipStatusConnector.getInstance(this);
        if (mWifiBindSipStatusConnector.isWifiOpened())//wifi开启
        {
            mWifiBindSipStatusConnector.setWifi(false, MyApplication.getAppContext());//=====================wifi关闭
        }

    }

    private void initUdp() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        helper = new UdpHelper(wifiManager, sAppContext);
        helper.run();
    }

    /**
     * 配置OkhttpClient
     */
    public void initClient() {
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
////                .addInterceptor(new LoggerInterceptor("TAG"))
////                .addInterceptor(new LoggerInterceptor("TAG"))
//                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
//                .readTimeout(10000L, TimeUnit.MILLISECONDS)
//                //其他配置
//                .build();
//        MyOkHttpRetryInterceptor myOkHttpRetryInterceptor = new MyOkHttpRetryInterceptor.Builder()
//                .executionCount(6)
//                .retryInterval(1000)
//                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
//                .addInterceptor(myOkHttpRetryInterceptor)
                .connectionPool(new ConnectionPool())
                .connectTimeout(8000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    @Override
    public void onTerminate() { // 程序终止 (Waderson 20180123)
        super.onTerminate();
        //unbindService(connection);
    }

}
