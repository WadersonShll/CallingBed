package com.wdkl.callingbed.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.wdkl.callingbed.MyApplication;

public class ReceiverUtil {

    /**
     * 发送广播
     */
    public static void sendBroadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        MyApplication.getAppContext().sendBroadcast(intent);

    }

    public static void sendBroadcast(String action, Intent intent) {
        intent.setAction(action);
        MyApplication.getAppContext().sendBroadcast(intent);

    }

    public static void sendBroadcast(Activity activity, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        activity.sendBroadcast(intent);

    }

    public static void sendBroadcast(Activity activity, String action, Intent intent) {
        intent.setAction(action);
        activity.sendBroadcast(intent);

    }

    /**
     * 动态注册一个广播
     */
    public static void registReceiver(Activity activity,
                                      BroadcastReceiver receiver, String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        activity.registerReceiver(receiver, intentFilter);
    }

    /**
     * 动态注册一个广播
     */
    public static void registReceiver(Activity activity,
                                      BroadcastReceiver receiver, String action, int pro) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        intentFilter.setPriority(pro);
        activity.registerReceiver(receiver, intentFilter);
    }

    public static void registReceiver(BroadcastReceiver receiver, String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        MyApplication.getAppContext().registerReceiver(receiver, intentFilter);
    }

}
