package com.wdkl.callingbed.util;

import android.util.Log;

/**
 * 类名称：LogUtil <br>
 * 类描述：打印日志工具类 <br>
 * 创建人：Waderson Shll<br>
 */
public class LogUtil {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static final int LEVEL = VERBOSE;

    public static String getTag(Class<?> clazz) {
        if (null != clazz) {
            return clazz.getSimpleName();
        }
        return "WADERSON";
    }

    public static void v(String tag, String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LEVEL <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg);
        }
    }

    public static void v(Class<?> clazz, String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(getTag(clazz), msg);
        }
    }

    public static void d(Class<?> clazz, String msg) {
        if (LEVEL <= DEBUG) {
            Log.d(getTag(clazz), msg);
        }
    }

    public static void i(Class<?> clazz, String msg) {
        if (LEVEL <= INFO) {
            Log.i(getTag(clazz), msg);
        }
    }

    public static void w(Class<?> clazz, String msg) {
        if (LEVEL <= WARN) {
            Log.w(getTag(clazz), msg);
        }
    }

    public static void e(Class<?> clazz, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(getTag(clazz), msg);
        }
    }
}
