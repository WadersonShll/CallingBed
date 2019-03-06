package com.wdkl.callingbed.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 胡博文 on 2017/9/13.
 */

public class TimeUtil {
    private Calendar cal;
    private static TimeUtil mTimeUtil;
    //返回当前时间 年月日 时间 星期
    private StringBuffer str = new StringBuffer();

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int length;
    private Integer a;
    private int b;
    private String s;
    private SimpleDateFormat sdfInput;
    private Date date1;
    private int dayOfWeek;
    private int i;
    private static long mills;


    //返回当前秒钟
    public String getSecond() {
        cal = Calendar.getInstance();//使用日历类
        second = cal.get(Calendar.SECOND);//得到秒
        return second + "";
    }


    public static TimeUtil getInstance() {
        if (mTimeUtil == null)
            mTimeUtil = new TimeUtil();

        return mTimeUtil;
    }

    public TimeUtil() {
        sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
        mills = System.currentTimeMillis();
        date1 = new Date(mills);
    }


    public String getDataTime() {
        mills = System.currentTimeMillis();
        date1 = new Date(mills);
        return sdfInput.format(date1).toString();
    }

    public static long getMills() {
        return mills;
    }

    //返回当前时间 年月日 时间 星期
//    public String getClockTime() {
//        String str = "";
//        int year = cal.get(Calendar.YEAR);//得到年
//        int month = cal.get(Calendar.MONTH) + 1;//得到月，因为从0开始的，所以要加1
//        int day = cal.get(Calendar.DAY_OF_MONTH);//得到天
//        int hour = cal.get(Calendar.HOUR_OF_DAY);//得到小时
//        int minute = cal.get(Calendar.MINUTE);//得到分钟
//        int second = cal.get(Calendar.SECOND);//得到秒
//        String date = year + "/" + month + "/" + day;
//        str += date;
//
//        Integer aInteger = minute;
//        int length = aInteger.toString().length();
//        Integer a = second;
//        int b = a.toString().length();
//        if (length == 1 && b == 1) {
//            str += " " + hour + ":" + "0" + minute + ":" + "0" + second;
//        } else if (length != 1 && b != 1) {
//            str += " " + hour + ":" + minute + ":" + second;
//        } else if (length == 1 && b != 1) {
//            str += " " + hour + ":" + "0" + minute + ":" + second;
//        } else {
//            str += " " + hour + ":" + minute + ":" + "0" + second;
//        }
//
//        String s = year + "/" + month + "/" + day;
//        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy/MM/dd");
//        Date date1 = new Date();
//        try {
//            date1 = sdfInput.parse(s);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        calendar.setTime(date1);
//        return str;
//    }

    /*获取星期几*/

    public String getWeek() {
        i = cal.get(Calendar.DAY_OF_WEEK);
        switch (i) {
            case 1:
                return "星期日";
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
            default:
                return "";
        }
    }

    /**
     * 设置系统时间
     *
     * @param date
     */
    public static void setSysDate(String date) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            if (null != process) {
                String datetime = date; //测试的设置的时间【时间格式 yyyyMMdd.HHmmss】"20131023.112800"
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("setprop persist.sys.timezone GMT\n");
                os.writeBytes("/system/bin/date -s " + datetime + "\n");
                os.writeBytes("clock -w\n");
                os.writeBytes("exit\n");
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean isRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if (0 == i) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
        return false;

    }
}
