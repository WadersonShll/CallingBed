package com.wdkl.callingbed.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * UdpHelper帮助类
 *
 * @author 陈喆榕
 */
public class UdpHelper implements Runnable {
    public Boolean IsThreadDisable = false;//指示监听线程是否终止
    private static WifiManager.MulticastLock lock;
    private Context context;

    InetAddress mInetAddress;
//    private static List<String> receiveList;
//    private final int SEND=1000;
//    private final int RECIVE=2000;
//    private MyHandler handler = new MyHandler(this);
//    //广播播放器
//    class MyHandler extends Handler {
//        // 弱引用 ，防止内存泄露
//        private WeakReference<UdpHelper> weakReference;
//
//        public MyHandler(UdpHelper callingBedActivity) {
//            weakReference = new WeakReference<UdpHelper>(callingBedActivity);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            // 通过  软引用  看能否得到activity示例
//            UdpHelper helperUtil = weakReference.get();
//            // 防止内存泄露
//            if (helperUtil != null) {
//                // 如果当前Activity，进行UI的更新
////                if (msg.what == TIME_WHAT) {
////                }
//            } else {
//                // 没有实例不进行操作
//            }
//        }
//    }

    public UdpHelper(WifiManager manager, Context context) {
        lock = manager.createMulticastLock("UDPwifi");
        this.context = context;
    }

    public void StartListen() {
        // UDP服务器监听的端口
        Integer port = 10010;
        // 接收的字节大小，客户端发送的数据不能超过这个大小
        byte[] message = new byte[1400];
        try {
            // 建立Socket连接
            DatagramSocket datagramSocket = new DatagramSocket(port);
            datagramSocket.setBroadcast(true);
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
            try {
                while (!IsThreadDisable) {
                    // 准备接收数据

                    lock.acquire();
                    datagramSocket.receive(datagramPacket);

                    String strMsg = new String(datagramPacket.getData()).trim();
                    LogUtil.d("UDP_Helper", "UDP接收:" + strMsg);

                    UdpSendUtil.receiveUdp(strMsg, context);

                    for (int i = 0; i < message.length; i++) {
                        message[i] = 0;
                    }
                    lock.release();
                }
            } catch (IOException e) {//IOException
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    private int SEND_COUNT = 20;

    public static void send(String message) {
        message = (message == null ? "Hello IdeasAndroid!" : message);
        //如果list中包含 message 就不停发送
        int server_port = 10010;
        LogUtil.d("UDP_Helper", "UDP发送:" + message);
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress local = null;
        try {
            local = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int msg_length = message.getBytes().length;
        byte[] messageByte = message.getBytes();
        DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,
                server_port);
        try {
            s.send(p);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        new Thread() {
            @Override
            public void run() {
                //把网络访问的代码放在这里
                StartListen();
            }
        }.start();
    }
}