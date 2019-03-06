package com.wdkl.callingbed.util;

import android.content.Context;

import com.wdkl.callingbed.common.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by dengzhe on 2018/5/7.
 * //===============息屏工具类===============//
 */

public class ScreenExtinguishUtil {

    private static ScreenExtinguishUtil mScreenExtinguishUtil;

    private static Context context;

    private ScreenExtinguishThread mScreenExtinguishThread;

    /**
     * 亮屏亮度值
     */
    private static int screenLight = 0;//
    /**
     * 息屏亮度值（晚上：20；白天：90）
     */
    private static int screenExtinguishLight = 0;//
    private static long screenExtinguishTime = 30000;//息屏时间默认30秒
    private static long upTouchTimes;//手指按下或抬起屏幕的时间
    private static long someTimes;//手指停止操作后的某个时间


    public static ScreenExtinguishUtil getInstance(Context contexts) {
        context = contexts;
        if (mScreenExtinguishUtil == null) {
            mScreenExtinguishUtil = new ScreenExtinguishUtil();
        }
        return mScreenExtinguishUtil;
    }

    public void ScreenExtinguishUtil() {

    }

    /**
     * 息屏30秒处理. true:息屏；false:亮屏
     */
    public static boolean SCREEN_EXTINGUISH = false;

    /**
     * //===============================================================Application初始化息屏控制===============================================================//
     */
    public void controlScreenLight() {
        synchronized (context) {
            mScreenExtinguishThread = new ScreenExtinguishThread(context);
            screenLightChange(true);
            mScreenExtinguishThread.start();
            return;
        }

    }

    /**
     * //===============================================================onTouch()按下触发倒计时===============================================================//
     */
    public void touchScreen() {
        //-------------------------------------------屏幕点亮-------------------------------------------
        screenLightChange(false);
        //-------------------------------------------屏幕定时变暗-------------------------------------------
        screenLightChange(true);
    }

    /**
     * @param isTouch true：倒计时开始;false:倒计时停止
     */
    public void screenLightChange(boolean isTouch) {
        upTouchTimes = System.currentTimeMillis();
        if (!isTouch) {
            SCREEN_EXTINGUISH = false;
        } else {
            SCREEN_EXTINGUISH = true;
        }
    }

    private static class ScreenExtinguishThread extends Thread {
        WeakReference<Context> mThreadMyApplication;

        public ScreenExtinguishThread(Context context) {
            mThreadMyApplication = new WeakReference<Context>(context);
        }

        @Override
        public void run() {
            super.run();
            if (mThreadMyApplication == null)
                return;
            if (mThreadMyApplication.get() != null) {
                while (true) {

                    if (ScreenManagerUtil.getScreenBrightness(context) == 205) {

                    } else {
                        ScreenManagerUtil.setScreenBrightness(context, 205);
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    someTimes = System.currentTimeMillis();
//                    //息屏延时息屏时间_(从后台获取)
//                    screenExtinguishTime = (StringUtils.parseInt(Constants.SCREENEXTINGUISHTIME) == 0
//                            ? 30
//                            : Integer.valueOf(Constants.SCREENEXTINGUISHTIME)) * 1000;
//
//                    if (SCREEN_EXTINGUISH && (someTimes - upTouchTimes >= screenExtinguishTime)) {
//                        if (ScreenManagerUtil.getScreenBrightness(context) == screenExtinguishLight) {
//
//                        } else {
//                            ScreenManagerUtil.setScreenBrightness(context, getScreenExtinguishLight());
//                            LogUtil.d("屏幕亮度", "息屏亮度" + screenExtinguishLight);
//                        }
//                    } else {
//                        if (ScreenManagerUtil.getScreenBrightness(context) == screenLight) {
//
//                        } else {
//                            ScreenManagerUtil.setScreenBrightness(context, getScreenLight());
//                            LogUtil.d("屏幕亮度", "亮屏亮度" + screenLight);
//                        }
//                    }

                }
            }
        }
    }

    public static void setScreenLight(int screenLight) {
        ScreenExtinguishUtil.screenLight = (int) (ScreenManagerUtil.maxBrightness * ((float) screenLight / 100.0f));
    }

    //===============================================================获取亮屏亮度===============================================================//
    public static int getScreenLight() {
        if (Constants.MORNING_NIGTH.equals("0")) {//--------白天
            setScreenLight(Integer.valueOf(Constants.SCREENLIGHT));
        } else {//--------晚上
            setScreenLight((Integer.valueOf(Constants.SCREENLIGHT) >= 50 && Integer.valueOf(Constants.SCREENLIGHT) <= 100)//(50-100:设默认亮度60(避免过亮))
                    ? 60
                    : ((Integer.valueOf(Constants.SCREENLIGHT) < 50 && Integer.valueOf(Constants.SCREENLIGHT) > 28) //（28-50：可以调节亮度区间；28以下：不设入亮度）
                    ? Integer.valueOf(Constants.SCREENLIGHT) : 28));
        }
        return screenLight;
    }


    public static void setScreenExtinguishLight(int screenExtinguishLight) {
        ScreenExtinguishUtil.screenExtinguishLight = (int) (ScreenManagerUtil.maxBrightness * ((float) screenExtinguishLight / 100.0f));
    }

    //===============================================================获取息屏亮度===============================================================//
    public static int getScreenExtinguishLight() {
        if (Constants.MORNING_NIGTH.equals("0")) {//--------白天约90亮度
            setScreenExtinguishLight(36);
        } else {
            setScreenExtinguishLight(8);

        }
        return screenExtinguishLight;
    }
}


