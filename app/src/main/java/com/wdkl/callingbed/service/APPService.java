package com.wdkl.callingbed.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wdkl.callingbed.MyApplication;

import java.util.Timer;
import java.util.TimerTask;

import static com.wdkl.callingbed.MyApplication.serialPortUtil;

/**
 * Created by Waderson on 2018/01/23.
 */

public class APPService extends Service {

    ServiceBinder myBinder = new ServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        sendHeartbeat();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public Timer timer ;
    public TimerTask timerTask ;

    /**
     * 发送心跳信号
     */
    private void sendHeartbeat() {
        if (timer != null) timer.purge();
        if (timerTask != null) timerTask.cancel();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (MyApplication.HEARTBEAT) {
                    serialPortUtil.startHeart();
                } else {
                    serialPortUtil.closeHeart();
                }
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    public class ServiceBinder extends Binder {
        public void doThings() {
        }
    }

}