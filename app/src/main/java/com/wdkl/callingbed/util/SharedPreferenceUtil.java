package com.wdkl.callingbed.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dengzhe on 2018/4/27.
 */

public class SharedPreferenceUtil {

    /**
     * SharedPreferences字符串的存入
     *
     * @param context
     * @param spname
     * @param vname
     * @param vdata
     */
    public static void putStringSp(Context context, String spname, String vname, String vdata) {
        SharedPreferences sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE);
        sp.edit().putString(vname, vdata).commit();
    }

    /**
     * SharedPreferences数据的清除
     *
     * @param context
     * @param spname
     */
    public static void deleSp(Context context, String spname) {
        SharedPreferences sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }

    /**
     * SharedPreferences字符串的取出
     *
     * @param context
     * @param spname
     * @param vname
     * @return
     */
    public static String getStringSp(Context context, String spname, String vname) {
        SharedPreferences sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE);
        String name = sp.getString(vname, "");
        return name;
    }
}
