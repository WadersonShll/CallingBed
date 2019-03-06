package com.wdkl.callingbed.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类描述：字符串操作工具包
 * 创建人：Waderson Shll
 * 创建时间：2017-10-31
 *
 * @version 1.0
 */
public class StringUtils {
    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private final static Pattern backCard = Pattern
            .compile(" /^(\\[0-9]{16}|\\[0-9]{18}|\\[0-9]{19})$/;");
    // private final static SimpleDateFormat dateFormater = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // private final static SimpleDateFormat dateFormater2 = new
    // SimpleDateFormat("yyyy-MM-dd");

    private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return info.versionName;
            // return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1.0 + "";
    }

    /**
     * 将字符串转为日期类型
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.get().parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 以友好的方式显示时间
     *
     * @param sdate
     * @return
     */
    public static String friendly_time(String sdate) {
        Date time = toDate(sdate);
        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days <= 10) {
            ftime = days + "天前";
        } else if (days > 10) {
            ftime = dateFormater2.get().format(time);
        }
        return ftime;
    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = dateFormater2.get().format(today);
            String timeDate = dateFormater2.get().format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }
        return b;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean notEmpty(String input) {

        return input != null && input.length() > 0
                && !input.equals("null");
    }

    public static boolean listNotEmpty(List list) {
        return list != null && list.size() > 0;
    }

    /**
     * 判断是不是一个合法的银行卡
     *
     * @param cardNum
     * @return
     */
    public static boolean isCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().length() == 0)
            return false;
        return backCard.matcher(cardNum).matches();
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }


    /**
     * 检查是否是金额（12.00）
     *
     * @param phone
     * @return
     */
    public static boolean checkNumEx(String phone) {
        Pattern pattern = Pattern.compile("^([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?$");
        Matcher match = pattern.matcher(phone);
        return match.matches();
    }


    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 是否是英文字
     *
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0;
    }

    /**
     * 获取英文字长度
     *
     * @param s
     * @return
     */
    public static int length(String s) {
        if (s == null)
            return 0;
        char[] c = s.toCharArray();
        int len = 0;
        int length = c.length;
        for (int i = 0; i < length; i++) {
            len++;
            if (!isLetter(c[i])) {
                len++;
            }
        }
        return len;
    }

    /**
     * 去掉html标签
     *
     * @param htmlStr
     * @return
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script,
                Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern
                .compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        htmlStr = htmlStr.replaceAll("&nbsp;", "");

        return htmlStr.trim(); // 返回文本字符串
    }

    /**
     * 去掉换行符
     *
     * @param str
     * @return
     */
    public static String removeLineChar(String str) {
        if (str == null)
            return "";
        return str.replaceAll("\r\n", "").replaceAll("\n", "");
    }

    /**
     * 以中文字长度计算，截取字符串
     *
     * @param origin
     * @param len
     * @param c
     * @return
     */
    public static String substring(String origin, int len, String c) {
        if (origin == null || origin.equals("") || len < 1)
            return "";
        String temp = removeLineChar(origin);
        byte[] strByte = new byte[len];
        if (len > length(origin)) {
            return temp;
        }
        try {
            System.arraycopy(temp.getBytes("GBK"), 0, strByte, 0, len);
            int count = 0;
            for (int i = 0; i < len; i++) {
                int value = (int) strByte[i];
                if (value < 0) {
                    count++;
                }
            }
            if (count % 2 != 0) {
                len = (len == 1) ? ++len : --len;
            }
            return new String(strByte, 0, len, "GBK") + c;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static int StrToIntDef(String s, int defaultValue) {
        int res = defaultValue;
        try {
            res = Integer.parseInt(s, 10);
        } catch (Exception e) {
            res = defaultValue;
        }
        return res;
    }

    /**
     * 将数据集合转化拼成字符串
     *
     * @param collection 集合
     * @param delimiter  分隔符
     * @return
     */
    public static String join(Collection<?> collection, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = collection.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    /**
     * 验证身份证是否有效15位或18位<包括对年月日的合法性进行验证>
     *
     * @param idcard
     * @return
     */
    public static boolean isIDCard(String idcard) {
        Pattern pattern = Pattern.compile("^\\d{15}(\\d{2}[0-9xX])?$");
        Matcher macher = pattern.matcher(idcard);
        if (macher.find()) {// 对年月日字符串的验证
            String power = idcard.substring(idcard.length() - 12,
                    idcard.length() - 4);
            pattern = Pattern
                    .compile("^[1-2]+([0-9]{3})+(0[1-9][0-2][0-9]|0[1-9]3[0-1]|1[0-2][0-3][0-1]|1[0-2][0-2][0-9])");
            macher = pattern.matcher(power);
        }
        return macher.find();
    }

    /**
     * 检查是否是正确的email
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 验证邮箱格式
     *
     * @param strEmail
     * @return
     */
    public static boolean isEmailOther(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 判断姓名只能输入中文，或者英文
     *
     * @param name
     * @return
     */
    public static boolean isUserName(String name) {
        return name.matches("(([\u4E00-\u9FA5]{2,7})|([a-zA-Z]{3,10}))");
    }

    /**
     * 对银行卡进行加*号保护设置并格式化
     *
     * @param cardNo
     * @return 如：6222 **** **** 1234
     */
    public static String formateBankNumber(String cardNo) {
        String star = " **** **** ";
        int cardNoLength = cardNo.length();
        if (cardNoLength >= 4) {
            String start = cardNo.substring(0, 4);
            String end = cardNo.substring(cardNoLength - 4, cardNoLength);
            return start + star + end;
        } else {
            return cardNo;
        }
    }

    /**
     * 手机号码加星保护
     *
     * @param phone
     * @return 如：136*****256
     */
    public static String formatPhone(String phone) {
        String star = "******";
        return phone.substring(0, 3) + star
                + phone.substring(phone.length() - 3, phone.length());
    }

    /**
     * 检查是否是数字
     *
     * @param phone
     * @return
     */
    public static boolean checkNum(String phone) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher match = pattern.matcher(phone);
        return match.matches();
    }

    /**
     * 检查是否是电话号码
     */
    public static boolean isMobileNo(String paramString) {
        return Pattern.compile("^1[3|4|5|7|8]\\d{9}$").matcher(paramString)
                .matches();
    }

    /**
     * 检查用户输入账号格式是否正确,只能是邮箱或电话号码
     *
     * @param account
     * @return
     */
    public static boolean checkAccount(String account) {
        return (isMobileNo(account) || checkEmail(account));
    }

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static final String KEY_APP_KEY = "JPUSH_APPKEY";

    /**
     * 获取jpush AppKey
     *
     * @param context
     * @return
     */
    public static String getAppKey(Context context) {
        Bundle metaData = null;
        String appKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai)
                metaData = ai.metaData;
            if (null != metaData) {
                appKey = metaData.getString(KEY_APP_KEY);
                if ((null == appKey) || appKey.length() != 24) {
                    appKey = null;
                }
            }
        } catch (NameNotFoundException e) {

        }
        return appKey;
    }

    /**
     * 将输入流转化为String
     *
     * @param is
     * @return
     */
    public static String readDataFromStream(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    /**
     * 补零操作
     */
    public static String zerofill(Object obj) {
        String parten = "00";
        if (obj instanceof String)
            obj = Double.parseDouble(obj.toString());
        DecimalFormat decimal = new DecimalFormat(parten);
        return decimal.format(obj);
    }

    /**
     * ASCII表中可见字符从!开始，偏移位值为33(Decimal)
     */
    static final char DBC_CHAR_START = 33; // 半角!  

    /**
     * ASCII表中可见字符到~结束，偏移位值为126(Decimal)
     */
    static final char DBC_CHAR_END = 126; // 半角~  

    /**
     * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
     */
    static final char SBC_CHAR_START = 65281; // 全角！  

    /**
     * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
     */
    static final char SBC_CHAR_END = 65374; // 全角～  

    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    static final int CONVERT_STEP = 65248; // 全角半角转换间隔  

    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    static final char SBC_SPACE = 12288; // 全角空格 12288  

    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    static final char DBC_SPACE = ' '; // 半角空格  


    /**
     * <PRE>
     * 半角字符->全角字符转换
     * 只处理空格，!到&tilde;之间的字符，忽略其他
     * </PRE>
     */
    public static String bj2qj(String src) {
        if (src == null) {
            return src;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] == DBC_SPACE) { // 如果是半角空格，直接用全角空格替代  
                buf.append(SBC_SPACE);
            } else if ((ca[i] >= DBC_CHAR_START) && (ca[i] <= DBC_CHAR_END)) { // 字符是!到~之间的可见字符  
                buf.append((char) (ca[i] + CONVERT_STEP));
            } else { // 不对空格以及ascii表中其他可见字符之外的字符做任何处理  
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }

    /**
     * <PRE>
     * 全角字符->半角字符转换
     * 只处理全角的空格，全角！到全角～之间的字符，忽略其他
     * </PRE>
     */
    public static String qj2bj(String src) {
        if (src == null) {
            return src;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < src.length(); i++) {
            if (ca[i] >= SBC_CHAR_START && ca[i] <= SBC_CHAR_END) { // 如果位于全角！到全角～区间内  
                buf.append((char) (ca[i] - CONVERT_STEP));
            } else if (ca[i] == SBC_SPACE) { // 如果是全角空格  
                buf.append(DBC_SPACE);
            } else { // 不处理全角空格，全角！到全角～区间外的字符  
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 字符串按索引截取
     *
     * @param str
     * @return
     */
    public static String substringByLengh(String str, int start, int end) {
        if (str == null) {
            return "";
        }
        if (start > end) {
            return "";
        }
        if (str.length() < start || str.length() < end) {
            return "";
        }

        return str.substring(start, end);
    }

    /**
     * 字符串按单个截取
     *
     * @param str
     * @return
     */
    public static ArrayList<String> substringBySing(String str) {
        if (null == str || "".equals(str)) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            String s = String.valueOf(c[i]);
            list.add(s);
        }
        return list;
    }

    /**
     * 追加内容到当前String对象<br>
     *
     * @return
     */
    public static String append(String oldStr, String appendStr) {
        if (oldStr == null) {
            return appendStr;
        }
        StringBuffer sb = new StringBuffer(oldStr);
        return sb.append(appendStr).toString();
    }

    /**
     * 删除指定位置的字符<br>
     *
     * @return
     */
    public static String deleteCharAt(String oldStr, int index) {
        if (oldStr == null) {
            return null;
        }
        if (oldStr.length() <= 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer(oldStr);
        return sb.deleteCharAt(index).toString();
    }

    /**
     * String对象中插入内容<br>
     *
     * @return
     */
    public static String insert(String oldStr, String insertStr, int index) {
        if (oldStr == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(oldStr);
        return sb.insert(index, insertStr).toString();
    }

    /**
     * String对象内容反转<br>
     *
     * @return
     */
    public static String reverse(String oldStr) {
        if (oldStr == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(oldStr);
        return sb.reverse().toString();
    }

    /**
     * 修改索引值为index位置的字符<br>
     *
     * @return
     */
    public static String setCharAt(String oldStr, char charStr, int index) {
        if (oldStr == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(oldStr);
        sb.setCharAt(index, charStr);

        return sb.toString();
    }

    /**
     * 对某个字符加*操作<br>
     *
     * @return String
     */
    public static String changeTextAddsiix(String oldStr, int[] index) {
        String newStr = oldStr;
        for (int i = 0; i < index.length; i++) {
            if (index[i] < oldStr.length()) {
                newStr = setCharAt(newStr, '*', index[i]);
            }
        }
        return newStr;
    }

    /**
     * 移除某个字符<br>
     *
     * @return String
     */
    public static String replaceStr(String oldStr, CharSequence replaceStr) {
        if (oldStr == null) {
            return null;
        }
        return oldStr.replace(replaceStr, "");
    }

    /**
     * 对double类型的数据保留2位小数
     *
     * @param d
     * @return
     */
    public static double get2Decimal(double d) {
        return Math.round(d * 100) / 100d;
        // BigDecimal b = new BigDecimal(Double.toString(d));
        // return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 对double类型的数据取整
     *
     * @param d
     * @return
     */
    public static double get0Decimal(double d) {
        String str = String.valueOf(d);
        String strNum = str.substring(0, str.lastIndexOf("."));//截取从字符串开始到小数点位置的字符串，就是整数部分
        return parseDouble(strNum);
    }

    /**
     * 判断一个字符串 能否转成一个Double类型的
     *
     * @param str
     * @return
     */
    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * 字符串转成一个Double类型的
     *
     * @param str
     * @return
     */
    public static double parseDouble(String str) {
        if (isDouble(str)) {
            return Double.parseDouble(str);
        }
        return 0.0;
    }

    /**
     * 判断一个字符串 能否转成一个long类型的
     *
     * @param str
     * @return
     */
    public static boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * 字符串转成一个Long类型的
     *
     * @param str
     * @return
     */
    public static long parseLong(String str) {
        if (isLong(str)) {
            return Long.parseLong(str);
        }
        return 0;
    }

    /**
     * 判断一个字符串 能否转成一个int类型的
     *
     * @param str
     * @return
     */
    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * 字符串转成一个Int类型的
     *
     * @param str
     * @return
     */
    public static int parseInt(String str) {
        if (isInt(str)) {
            return Integer.parseInt(str);
        }
        return 0;
    }

    /**
     * 判断一个字符串 能否转成一个float类型的
     *
     * @param str
     * @return
     */
    public static boolean isFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * 字符串转成一个float类型的
     *
     * @param str
     * @return
     */
    public static float parseFloat(String str) {
        if (isFloat(str)) {
            return Float.parseFloat(str);
        }
        return 0f;
    }

    /**
     * 判断一个字符串 能否转成一个Double类型的
     *
     * @param str
     * @return
     */
    public static boolean checkDouble(String str) {
        Pattern pattern = Pattern.compile("^[-//+]?//d+(//.//d*)?|//.//d+$");
        return pattern.matcher(str).matches();
    }

    /**
     * String字符串的匹配（做模糊查询）
     *
     * @param objStr
     * @param inquiryStr
     * @return boolean
     */
    public static boolean vagueInquiry(String objStr, String inquiryStr) {
        if (null == objStr || null == inquiryStr) {
            return false;
        }
        Pattern p = Pattern.compile(inquiryStr);
        Matcher m = p.matcher(objStr);
        return m.find();
    }

    /**
     * 是否是汉字
     */
    public static boolean isChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find())
            flg = true;
        return flg;
    }

    public boolean judgeContainsStr(String cardNum) {
        String regex = ".*[*]+.*";
        Matcher m = Pattern.compile(regex).matcher(cardNum);
        return m.matches();
    }

    /**
     * 是否为车牌号
     */
    public static boolean isCar(String carNum) {
        String regex = "^[京,津,渝,沪,冀,晋,辽,吉,黑,苏,浙,皖,闽,赣,鲁,豫,鄂,湘,粤,琼,川,贵,云,陕,秦,甘,陇,青,台,蒙,桂,宁,新,藏,澳,港,军,海,航,警,使,古][A-Z][0-9,A-Z]{5}$";
        Matcher m = Pattern.compile(regex).matcher(carNum);
        return m.matches();
    }

    /**
     * unicode码转成汉字
     */
    public static String revert(String str) {
        str = (str == null ? "" : str);
        if (str.indexOf("\\u") == -1)//如果不是unicode码则原样返回
            return str;

        StringBuffer sb = new StringBuffer(1000);

        for (int i = 0; i < str.length() - 6; ) {
            String strTemp = str.substring(i, i + 6);
            String value = strTemp.substring(2);
            int c = 0;
            for (int j = 0; j < value.length(); j++) {
                char tempChar = value.charAt(j);
                int t = 0;
                switch (tempChar) {
                    case 'a':
                        t = 10;
                        break;
                    case 'b':
                        t = 11;
                        break;
                    case 'c':
                        t = 12;
                        break;
                    case 'd':
                        t = 13;
                        break;
                    case 'e':
                        t = 14;
                        break;
                    case 'f':
                        t = 15;
                        break;
                    default:
                        t = tempChar - 48;
                        break;
                }

                c += t * ((int) Math.pow(16, (value.length() - j - 1)));
            }
            sb.append((char) c);
            i = i + 6;
        }
        return sb.toString();
    }
}
