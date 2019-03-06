package com.wdkl.callingbed.util;

/**
 * Created by 胡博文 on 2017/9/19.
 */

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by asus on 2016/12/8.
 * 屏幕亮度调节器
 */
public class ScreenManagerUtil {
    /**
     * 屏幕亮度的最大值
     */
    public static int maxBrightness = 255;

    /**
     * 获得当前屏幕亮度的模式
     *
     * @return 1 为自动调节屏幕亮度,0 为手动调节屏幕亮度,-1 获取失败
     */
    public static int getScreenMode(Context context) {
        int mode = -1;
        try {
            mode = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode;
    }

    /**
     * @return 0--255
     */
    public static int getScreenBrightness(Context context) {
        int screenBrightness = -1;
        try {
            screenBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }

    /**
     * 设置当前屏幕亮度的模式
     *
     * @param mode 1 为自动调节屏幕亮度,0 为手动调节屏幕亮度
     */
    public static void setScreenMode(Context context, int mode) {
        try {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            Uri uri = Settings.System
                    .getUriFor("screen_brightness_mode");
            context.getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int screenBrightness_Now;

    /**
     * 保存当前的屏幕亮度值，并使之生效
     *
     * @param paramInt 0-255
     */
    public static void setScreenBrightness(Context context, int paramInt) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
        Uri uri = Settings.System.getUriFor("screen_brightness");
        LogUtil.w("当前亮度", "当前亮度======" + getScreenBrightness(context));
        context.getContentResolver().notifyChange(uri, null);
    }
}
