package com.wdkl.callingbed.util.anrfcutil;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.wdkl.callingbed.MyApplication;
import com.wdkl.callingbed.common.Constants;

/**
 * Created by dengzhe on 2018/4/2.
 * //=========================FC&ANR异常处理类=========================//
 */

public class AnrFcExceptionUtil implements Thread.UncaughtExceptionHandler {

    private static ANRWatchDog mANRWatchDog;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "MyApplication";
    private static MyApplication application;

    private static AnrFcExceptionUtil mAnrFcExceptionUtil;

    public static AnrFcExceptionUtil getInstance(MyApplication application) {
        if (mAnrFcExceptionUtil == null) {
            mAnrFcExceptionUtil = new AnrFcExceptionUtil(application);
        }
        return mAnrFcExceptionUtil;
    }

    private AnrFcExceptionUtil(MyApplication application) {
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            Intent intent = new Intent(application.getApplicationContext(), Constants.ANR_FC);
            @SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
                    application.getApplicationContext(), 0, intent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            //退出程序
            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent); // 1秒钟后重启应用
            //杀死该应用进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
//                Toast.makeText(application.getApplicationContext(), "很抱歉,程序出现异常,即将重新启动.",
//                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        return true;
    }


    /**
     * ===================================================崩溃异常处理===================================================
     */
    public static void initFCException() {
        //设置该CrashHandler为程序的默认处理器
        AnrFcExceptionUtil catchExcep = AnrFcExceptionUtil.getInstance(application);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
        mANRWatchDog = new ANRWatchDog(5000);
        mANRWatchDog.setInterruptionListener(new ANRWatchDog.InterruptionListener() {
            @Override
            public void onInterrupted(InterruptedException exception) {
            }
        }).setIgnoreDebugger(true).setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Intent mStartActivity = new Intent(application.getApplicationContext(), Constants.ANR_FC);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(application.getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) application.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, mPendingIntent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }).start();

    }
}
