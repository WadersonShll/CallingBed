package com.wdkl.callingbed.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by fengxiangqian on 2017/7/17.
 */

public class NetUtil {

    public static final String WD_FRISTTIME = "WD_FRISTTIME";        //
    public static final String WD_INFO = "WD_INFO";
    private static NetUtil mNetUtil;

    public static NetUtil getInstance() {
        if (mNetUtil == null)
            mNetUtil = new NetUtil();
        return mNetUtil;
    }


    /**
     * SharedPreferences数据的清除
     *
     * @param context
     * @param spname
     */
    public static void deleSp(Context context, String spname) {
        SharedPreferences sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
//                                Inet4Address
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        System.out.println(inetAddress.getHostAddress());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return null;
    }

    /**
     * wifi的网关
     *
     * @param context
     * @return
     */
    public static String IPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
//        CONNECTIVITY_SERVICE
        DhcpInfo di = wm.getDhcpInfo();
//		WifiInfo wifiInfo = wm.getConnectionInfo();
        InetAddress ip = intToInetAddress(di.gateway);
//		String ip1 = ip+"".substring(0, 1);
        LogUtil.d("网关地址:", ip + "");

        return ip.toString();
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


    // 获取wifiIP
    public static String getWIFIIPAddress(Context ctx) {
        WifiManager wifi_service = (WifiManager) ctx
                .getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        // DhcpInfo中的ipAddress是一个int型的变量，通过Formatter将其转化为字符串IP地址
        return Formatter.formatIpAddress(dhcpInfo.ipAddress);
    }

    // 根据Wifi信息获取本地Mac
    public static String getLocalMacAddressFromWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    private static NetworkInfo.State ethernetState;

    public static boolean isethernetConnected(ConnectivityManager cm) {
        boolean result = false;
        ethernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
        if (ethernetState == NetworkInfo.State.CONNECTED) {
            result = true;
        }
        return result;
    }

    /**
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    @SuppressLint("NewApi")
    public static String getLocalMacAddressFromIp() {
        String strMacAddr = null;
        try {
            // 获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();

        } catch (Exception e) {

        }

        return strMacAddr;
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    public static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            // 列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface
                    .getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {// 是否还有元素
                NetworkInterface ni = en_netInterface
                        .nextElement();// 得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();// 得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }


        return ip;
    }

    /**
     * 得到网关
     *
     * @param ip
     * @return
     */
    public static String getLocalElement(String ip) {
        String[] temp = null;
        String nip = ip.substring(1, ip.length());
        //截取ip
        temp = nip.split("\\.");
        //裁剪ip
        ip = nip.substring(0, nip.length() - temp[3].length());
        ip = ip + 1;
        return ip;
    }

    /**
     * 修改系统的日期
     *
     * @param context
     * @param year
     * @param month
     * @param day
     */
    public void setSysDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * 修改系统的小时和分钟
     *
     * @param context
     * @param hour
     * @param minute
     */
    public void setSysTime(Context context, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * ping 网络
     *
     * @param host
     * @param pingCount
     * @param stringBuffer
     * @return
     */
    public static boolean ping(String host, int pingCount, StringBuffer stringBuffer) {
        String line = null;
        Process process = null;
        BufferedReader successReader = null;
        String command = "ping -c " + pingCount + " " + host;
        boolean isSuccess = false;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                append(stringBuffer, "ping fail:process is null.");
                return false;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                append(stringBuffer, line);
            }
            int status = process.waitFor();
            if (status == 0) {
                append(stringBuffer, "exec cmd success:" + command);
                isSuccess = true;
            } else {
                append(stringBuffer, "exec cmd fail.");
                isSuccess = false;
            }
            append(stringBuffer, "exec finished.");
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                }
            }
        }
        if (getIsEthernetConnects() != null) {
            isEthernetConnects.checkEthernetConnect(isSuccess);
        }

        return isSuccess;
    }

    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text + "\n");
        }
    }

    /**
     * wifi信号强度
     *
     * @param context
     * @return
     */
    public static int obtainWifiInfo(Context context) {
        // Wifi的连接速度及信号强度：
        int strength = 0;
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            // 链接信号强度，5为获取的信号强度值在5以内
            strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            // 链接速度
            int speed = info.getLinkSpeed();
            // 链接速度单位
            String units = WifiInfo.LINK_SPEED_UNITS;
            // Wifi源名称
            String ssid = info.getSSID();
        }
        /**
         * 其中0到50表示信号最好，50到70表示信号偏差，大于70表示最差，有可能连接不上或者掉线。
         */
        int level = Math.abs(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getRssi());

//        if ((level - 50) <= 0) {// 0-50
//            currentWifiSignal = NetWorkSignalDesc.WIFI_SIGNAL_INTENSITY_GOOD;
//        } else if ((level - 70) <= 0) {// 50 -70
//            currentWifiSignal = NetWorkSignalDesc.WIFI_SIGNAL_INTENSITY_BETTER;
//        } else {
//            currentWifiSignal = NetWorkSignalDesc.WIFI_SIGNAL_INTENSITY_BAD;
//        }
        return strength;
    }

    /**
     * 判断以太网是否连接成功
     *
     * @param context
     * @return
     */
    public static boolean isIntenetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            boolean hasInternet = mInternetNetWorkInfo != null && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            return hasInternet;
        }
        return false;
    }

    /**
     * 判断wifi是否连接成功
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean hasInternet = mInternetNetWorkInfo != null && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            return hasInternet;
        }
        return false;
    }


    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 本地网络
     */
    public static final int NETWORK_ETHERNET = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_ETHERNET)) {
                return NETWORK_ETHERNET;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    private static IsEthernetConnect isEthernetConnects = null;

    public static IsEthernetConnect getIsEthernetConnects() {
        return isEthernetConnects;
    }

    /**
     * 网络是否pingping通（处理1分钟后重调通知接口重启APP的问题）
     */
    public interface IsEthernetConnect {
        void checkEthernetConnect(boolean isConnect);
    }

    public static void setIsEthernetConnect(IsEthernetConnect isEthernetConnect) {
        isEthernetConnects = isEthernetConnect;
    }
}
