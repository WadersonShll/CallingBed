package com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.wdkl.callingbed.R;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.util.NetUtil;
import com.wdkl.callingbed.util.SipUtil.SipHelperUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import static com.wdkl.callingbed.common.Constants.ETHERNETSTATUS;

/**
 * Created by dengzhe on 2017/11/29.
 * wifi\Sip自动连接工具类
 */
public class WifiBindSipStatusConnector {
    private Handler mHandler;
    //以太网连接状态
    public static boolean ethernetStatus = false;
    private int ethernetCounts = 0;
    //wifi连接状态(暂时注掉)
    // public static boolean wifiStatus;

    /**
     * 向UI发送消息
     *
     * @param
     *///(暂时注掉)info 消息
    /*public void sendMsg(String info) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage();
            msg.obj = info;
            mHandler.sendMessage(msg);// 向Handler发送消息
        } else {
            LogUtil.e("wifi", info);
        }
    }*/

    //WIFICIPHER_WEP是WEP ，WIFICIPHER_WPA是WPA，WIFICIPHER_NOPASS没有密码  //(暂时注掉)
    /*public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }*/
    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public WifiBindSipStatusConnector(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    // 提供一个外部接口，传入要连接的无线网 //(暂时注掉)
    /*public void connect(String ssid, String password, WifiCipherType type) {
        Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
        thread.start();
    }*/


    // 查看以前是否也配置过这个网络 //(暂时注掉)
    /*private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }*/

    //(暂时注掉)
    /*private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(KeyMgmt.NONE);
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }*/

    // 打开wifi功能 //(暂时注掉)
    /*private boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }*/
    //(暂时注掉)
    /*class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                // 打开wifi--------------硬件不支持，暂时不用--------------------//
                // openWifi();
                sendMsg("opened");
                Thread.sleep(200);
                while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                WifiConfiguration wifiConfig = createWifiInfo(ssid, password,
                        type);
                if (wifiConfig == null) {
                    sendMsg("wifiConfig is null!");
                    return;
                }

                WifiConfiguration tempConfig = isExsits(ssid);

                if (tempConfig != null) {
                    wifiManager.removeNetwork(tempConfig.networkId);
                }

                int netID = wifiManager.addNetwork(wifiConfig);
                boolean enabled = wifiManager.enableNetwork(netID, true);
                sendMsg("enableNetwork status enable=" + enabled);
                boolean connected = wifiManager.reconnect();
                sendMsg("enableNetwork connected=" + connected);
                sendMsg("连接成功!");
            } catch (Exception e) {
                sendMsg(e.getMessage());
                e.printStackTrace();
            }
        }
    }*/

    //(暂时注掉)
    /*private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f')) {
                return false;
            }
        }

        return true;
    }*/

    //原设定：1分钟去检测网络：不通则互相循环切换以太网和wifi去ping网，
    // 发现检测状态虽及时更新到ui上显示，但ping网切换wifi和以太网会延时，需要等待一段时间
    // 现换成：30 秒发送一次检测网络切换信号
    private int mTimeNetStatus = 30000;
    public Timer timerNetStatus = null;
    public TimerTask timerNetStatusTask = null;
    private WifiBindSipStatusConnector wac;

    public void sendNetStatus(final Context context) {
        mWifiMiddle = context.getResources().getDrawable(R.drawable.wifi_middle);//wifi中
        mWifiLow = context.getResources().getDrawable(R.drawable.wifi_low);//wifi弱

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiBindSipStatusConnector = getInstance(context);
        if (timerNetStatus != null) {
            timerNetStatus.purge();
        }
        timerNetStatus = new Timer();

        if (timerNetStatusTask != null) {
            timerNetStatusTask.cancel();
        }
        timerNetStatusTask = new TimerTask() {
            @Override
            public void run() {
                ethernetIsSuccess(context, Constants.DEVICE_WIFI_HOST_NAME);//检测以太网连接
            }
        };
        timerNetStatus.schedule(timerNetStatusTask, 10, mTimeNetStatus);
    }

    /*
  * 以太网或者wifi是否ping成功
  * */
    private String ipEthernet;//以太网ip
    private String ipWifi;//wifiip

    public void ethernetIsSuccess(Context context, String DEVICE_WIFI_HOSTNAME) {
        if (NetUtil.getInstance().getLocalInetAddress() != null)
            ipEthernet = NetUtil.getInstance().getLocalInetAddress().toString();
        if (NetUtil.getInstance().getWIFIIPAddress(context) != null)
            ipWifi = "/" + NetUtil.getInstance().getWIFIIPAddress(context).toString();
        if (ipEthernet != null) {
            if (SipHelperUtil.getInstance(context) != null) {
                ethernetStatus = SipHelperUtil.getInstance(context)
                        .getmEthernetWifiCallBackI()
                        .ethernetStatus(NetUtil.ping(NetUtil.getLocalElement(ipEthernet), 2, null));//以太网连接状态
            }
            if (ethernetStatus) {
                ethernetCounts = 0;
            }
            if (!ethernetStatus && ethernetCounts <= 3) {
                ethernetStatus = true;
                ethernetCounts++;
            }
            if (ethernetCounts > 3 && !ethernetStatus) {
                ethernetCounts = 5;
            }
        } else {
            ethernetStatus = false;
        }

        // (暂时注掉)
        /*if (ipWifi != null) {
            wifiStatus = SipHelperUtil.getInstance(context)
                    .getmEthernetWifiCallBackI()
                    .wifiStatus(NetUtil.ping(NetUtil.getLocalElement(ipWifi), 2, null));//wifi连接状态
        } else {
            wifiStatus = false;
        }*/
        EventBus.getDefault().post(new MessageEvent(ETHERNETSTATUS, Constants.EVENT_SIP_INTERNETPING));//循环检测SIP，以太网ping状态


        //以太网连接未开启  以太网 ping不通(暂时注掉)
        /*if (!NetUtil.getInstance().isIntenetConnected(context) || !ethernetStatus) {
            if (!isWifiOpened())//wifi未开启
            {
                //(暂时注掉)
                *//*if (!closeWifiAp()) {
                    return;
                }*//*
                //暂时注掉
                *//*                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ---------------------wifi硬件暂时不支持------------------------//
                setWifi(true, MyApplication.getAppContext());//=====================wifi开启*//*
                setWifi(false, MyApplication.getAppContext());//=====================wifi开启
            }
            if (!isWifiOpened()) {
                return;
            }
        }*/
        //            LogUtil.d("WifiHot", "子机：以太网连接不成功，wifi开启连接"); (暂时注掉)
        /*if (NetUtil.getInstance().getWIFIIPAddress(context) != null)
            ipWifi = "/" + NetUtil.getInstance().getWIFIIPAddress(context).toString();
        if (ipWifi != null) {
            wifiStatus = NetUtil.ping(NetUtil.getLocalElement(ipWifi), 5, null);//wifi连接状态
        } else {
            wifiStatus = false;
        }*/
        //wifi ping不成功(暂时注掉)
        /*if (!wifiStatus && !NetUtil.getInstance().isWifiConnected(context)) {

            //子机去连门口机的wifi热点(暂时注掉)
            //....................
            *//*            LogUtil.d("WifiHot", "子机去连门口机的wifi热点");
            wac.connect("WD_Host1", "hnwd8888",
                    "hnwd8888" == null ? WifiBindSipStatusConnector.WifiCipherType.WIFICIPHER_NOPASS : WifiBindSipStatusConnector.WifiCipherType.WIFICIPHER_WPA);
            =======================暂时注掉======================//
            if (wifiManager.getWifiState() == WIFI_STATE_ENABLED) {//wifi没有成功连上
                if ("" != DEVICE_WIFI_HOSTNAME) {
                    wac.connect(DEVICE_WIFI_HOSTNAME, "12345678",
                            "12345678" == null ? WifiBindSipStatusConnector.WifiCipherType.WIFICIPHER_NOPASS : WifiBindSipStatusConnector.WifiCipherType.WIFICIPHER_WPA);
                }
            return;
            }*//*

        }*/

        //(暂时注掉)
        /*if (NetUtil.getInstance().getWIFIIPAddress(context) != null)
            ipWifi = "/" + NetUtil.getInstance().getWIFIIPAddress(context).toString();
        if (ipWifi != null)
            wifiStatus = NetUtil.ping(NetUtil.getLocalElement(ipWifi), 5, null);//wifi连接状态
        else
            wifiStatus = false;
        if (!wifiStatus && !NetUtil.getInstance().isWifiConnected(context)) {//wifi ping不成功
            setWifi(false, MyApplication.getAppContext());//=====================wifi关闭  以太网自动连接
            //            LogUtil.d("WifiHot", "wifi IP：" + ipWifi + " ping不成功, wifi关闭  以太网自动连接");
            if (isWifiOpened()) {
                return;
            }
            if (NetUtil.getInstance().isIntenetConnected(context) && ethernetStatus) {//以太网 ping成功 连接上
                //子机去连以太网
                //....................
                //                LogUtil.d("WifiHot", "以太网IP" + ipEthernet + "ping成功");
            } else {
                //                LogUtil.d("WifiHot", "以太网IP" + ipEthernet + "ping不成功" + "网络连接异常");
            }
        }*/
    }

    //暂时注掉
    /*private void autoConnectWifi() {
        wac = WifiBindSipStatusConnector.getInstance(context);
        wac.setmHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 操作界面
                LogUtil.d("AutoConnectWifi", msg.obj + "");
                super.handleMessage(msg);
            }
        });
    }*/


    /**
     * 显示以太网连接状态 (暂时注掉)
     */
    /*private void showNetStatus(TextView textview, Drawable drawable) {
        if (textview != null) {//已连接
            textview.setText("");
            textview.setCompoundDrawables(drawable, null, null, null);
        }
    }*/

    /**
     * wifi信号强弱图标
     */
    private Drawable mWifiHigh, mWifiMiddle, mWifiLow, mWifiNor;

    /**
     * 显示wifi连接状态
     *
     * @param //textview
     * @param //b        是否显示wifi断开
     * @param drawable   wifi强度图标
     * @param //s        wifi强度数值 (暂时注掉)
     */
    /*public void showWifiPower(TextView textview, boolean b, Drawable drawable, int s) {
        if (textview != null)
            textview.setVisibility(View.VISIBLE);
        if (b) {//wifi断开
            textview.setCompoundDrawables(mWifiNor, null, null, null);
            textview.setText("");
        } else {//wifi连接
            if (s != -1) {
                if (s > 0 && s < 30)
                    textview.setCompoundDrawables(mWifiLow, null, null, null);
                else if (s >= 30 && s < 70) {
                    textview.setCompoundDrawables(mWifiMiddle, null, null, null);
                } else if (s >= 70 && s <= 100) {
                    textview.setCompoundDrawables(mWifiHigh, null, null, null);
                } else {
                    textview.setCompoundDrawables(mWifiNor, null, null, null);
                }
                //                textview.setText(s + "%");
                //                textview.setCompoundDrawablePadding(2);
            }
        }

    }*/


    /**
     * @param text（托管文字）
     * @param visible（托管图标是否显示）
     * @param b
     */

    private static final int WIFI_AP_STATE_DISABLING = 10;
    private static final int WIFI_AP_STATE_DISABLED = 11;
    private static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    private static final int WIFI_AP_STATE_FAILED = 14;
    private WifiManager wifiManager;
    private static WifiBindSipStatusConnector mWifiBindSipStatusConnector;

    public static WifiBindSipStatusConnector getInstance(Context contexts) {
        if (mWifiBindSipStatusConnector == null) {
            context = contexts;
            mWifiBindSipStatusConnector = new WifiBindSipStatusConnector((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
            ;
        }
        return mWifiBindSipStatusConnector;
    }

    /**
     * @param //context
     * @param //wifiName（热点名字）
     * @param //wifiSecret（热点密码）//(暂时注掉)
     */
    /*private void stratWifiAp(Context context, String wifiName, String wifiSecret) {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method method1 = null;
        try {
            method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = wifiName;
            netConfig.allowedAuthAlgorithms
                    .set(AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement
                    .set(KeyMgmt.WPA_PSK);
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            netConfig.preSharedKey = wifiSecret;
            method1.invoke(wifiManager, netConfig, true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }*/
    //(暂时注掉)
    /*public int getWifiApState(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            LogUtil.i("wifi", "wifi state:  " + i);
            return i;
        } catch (Exception e) {
            LogUtil.i("wifi", "Cannot get WiFi AP state" + e);
            return WIFI_AP_STATE_FAILED;
        }
    }*/

    private Handler handler;
    private Handler closeWifiHandler;
    // private CloseWifiThread cwt;
    private static Context context;
    private String wifiName;
    private String wifiSecret;

    /**
     * @param context
     * @param wifiName(设置wifi名字)
     * @param wifiSecret(设置wifi密码)//(暂时注掉)
     */
    /*public void startWifiAp(Context context, String wifiName, String wifiSecret) {
        context = context;
        this.wifiSecret = wifiSecret;
        this.wifiName = wifiName;
        handler = new Handler(Looper.getMainLooper());
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {// 判断是否处理开启状态
            //开启热点需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        if (wifiManager.isWifiEnabled()) {
            closeWifiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    startWifiApTh();
                    super.handleMessage(msg);
                }
            };
            cwt = new CloseWifiThread();
            Thread thread = new Thread(cwt);
            thread.start();

        } else {
            startWifiApTh();
        }


    }*/

    //(暂时注掉)
    /*class CloseWifiThread implements Runnable {
        public CloseWifiThread() {
            super();
        }

        @Override
        public void run() {
            int state = wifiManager.getWifiState();
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                wifiManager.setWifiEnabled(false);
                closeWifiHandler.postDelayed(cwt, 1000);
            } else if (state == WifiManager.WIFI_STATE_DISABLING) {
                closeWifiHandler.postDelayed(cwt, 1000);
            } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                closeWifiHandler.sendEmptyMessage(0);
            }

        }
    }*/

    //(暂时注掉)
    //private StratWifiApThread swat;

    //(暂时注掉)
    /*private void startWifiApTh() {
        swat = new StratWifiApThread();
        Thread thread = new Thread(swat);
        thread.start();
    }*/

    //(暂时注掉)
    /*class StratWifiApThread implements Runnable {
        public StratWifiApThread() {
            super();
        }

        private boolean isFirst = true;

        @Override
        public void run() {
            WifiBindSipStatusConnector mWifiUtil = getInstance(context);
            int state = getWifiApState(wifiManager);
            if (state == WIFI_AP_STATE_DISABLED) {
                mWifiUtil.stratWifiAp(context, wifiName, wifiSecret);
                handler.postDelayed(swat, 1000);
            } else if (state == WIFI_AP_STATE_ENABLING
                    || state == WIFI_AP_STATE_FAILED) {
                handler.postDelayed(swat, 1000);
            } else if (state == WIFI_AP_STATE_ENABLED) {
                LogUtil.i("wifi", "已开启wifi热点");
//                if (isFirst) {
//                    mWifiUtil.stratWifiAp(context, wifiName, wifiSecret);
//                    handler.postDelayed(swat, 1000);
//                    isFirst = false;
//                }
            }
        }

    }*/

    /**
     * 判断wifi是否已经打开
     *
     * @return true：已打开、false:未打开
     */
    public boolean isWifiOpened() {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int status = wifiManager.getWifiState();
        return status == WifiManager.WIFI_STATE_ENABLED;
    }

    /**
     * wifi开关
     *
     * @param isEnable
     * @param context
     */
    public void setWifi(boolean isEnable, Context context) {
        WifiManager mWm = null;
        //
        if (mWm == null) {
            mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
//        LogUtil.d("WifiHot", "wifi开关是否开启：" + mWm.isWifiEnabled());
        if (isEnable) {// 开启wifi
            if (!mWm.isWifiEnabled()) {

                mWm.setWifiEnabled(true);

            }
        } else {
            // 关闭 wifi
            if (mWm.isWifiEnabled()) {
                mWm.setWifiEnabled(false);
            }
        }

    }

    /**
     * 热点开关是否打开
     *(暂时注掉)
     */
    /*public boolean isWifiApEnabled() {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }*/

    /**
     * 关闭WiFi热点//(暂时注掉)
     */
    /*public boolean closeWifiAp() {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiApEnabled()) {
            try {
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }*/

}