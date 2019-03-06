package com.wdkl.callingbed.util;

import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static com.wdkl.callingbed.common.Constants.NET_ERROR_FIVE_AFTER_TOAST;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.closeHeart;

/**
 * Created by dengzhe on 2018/4/18.
 * //========自动重启工具类========//
 */

public class AutoRebootUtil {
    private static Context context;

    private static Calendar calendar;
    private static int countDownMinute;
    private static String currentTime = "0";
    private static int timeFirst = 11;
    private static int timeSecond = 17;
    private static int mt = 50;
    private static int lt = 55;
    private static int hour;
    private static int minute;


    public static void reboot() {
        closeHeart();//关闭心跳
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    /**
     * =======================================(网络信息初始化错误15次以上)并且连续重启次数3次，不再重启系统，需更换机子=======================================
     */
    public static void rebootContinueCounts(int counts) {
        SharedPreferencesUtil.putStringSp(context, NET_ERROR_FIVE_AFTER_TOAST, NET_ERROR_FIVE_AFTER_TOAST, String.valueOf(counts));
    }

    public static int getRepeatRebootCounts() {
        String rebootCounts = SharedPreferencesUtil
                .getStringSp(context, NET_ERROR_FIVE_AFTER_TOAST, NET_ERROR_FIVE_AFTER_TOAST);
        return rebootCounts.equals("") ? 0 : Integer.valueOf(rebootCounts);
    }

    public static void calculate(Context contexts) {
        context = contexts;
        calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
//        LogUtil.d("hour+minute", hour + "\t" + minute + "");
        countDownMinute = mt - minute;
        if ((hour == timeFirst || hour == timeSecond) && ((minute > mt) && (minute < lt))) {//12点和18点开启重启，检测网卡（每天）
            rebootContinueCounts(0);
        }
//        LogUtil.d("getRepeatRebootCounts()", getRepeatRebootCounts() + "");
        if (getRepeatRebootCounts() < 3) {
            LogUtil.d("getRepeatRebootCounts()", "is coming");
            rebootContinueCounts(getRepeatRebootCounts() + 1);
            reboot();
        } else {
        }
    }

    public static int getCountDownMinute() {
        return countDownMinute;
    }

    /**
     * 重启倒计时信息
     *
     * @return
     */
    public static String getTextTip() {
        if (countDownMinute <= 0) {
            return "";
        } else if (countDownMinute <= 5 && countDownMinute > 0 && (hour == timeFirst || hour == timeSecond)) {
            return "网络自检启动：系统将在" + countDownMinute + "分钟后重新启动";
        } else return "";
    }
}
