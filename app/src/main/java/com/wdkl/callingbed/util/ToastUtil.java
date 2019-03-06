package com.wdkl.callingbed.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdkl.callingbed.MyApplication;

/**
 * 类名称：ToastUtil <br>
 * 类描述：弹出Toast工具类 <br>
 * 创建人：Waderson Shll <br>
 * 创建时间：2017-11-2 <br>
 */
public class ToastUtil {

    public static Toast toast = null;
    public static boolean is_Debug = true;

    /**
     * 弹出调试Toast
     *
     * @param context
     * @param text
     */
    public static void showToast_Debug(Context context, String text) {
        if (is_Debug) {
            if (toast == null) {
                toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            }
            toast.setText(text);
            toast.show();
        }
    }

    /**
     * 弹出调试Toast
     *
     * @param text
     */
    public static void showToast_Debug(String text) {
        if (is_Debug) {
            if (toast == null) {
                toast = Toast.makeText(MyApplication.getAppContext(), text,
                        Toast.LENGTH_SHORT);
            }
            toast.setText(text);
            toast.show();
        }
    }

    /**
     * 弹出Toast
     *
     * @param context
     * @param text
     */
    public static void showToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.show();
    }

    /**
     * 弹出Toast
     *
     */
    public static void showToast(String text) {
        if (toast == null) {
            toast = Toast.makeText(MyApplication.getAppContext(), text,
                    Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.show();
    }

    /**
     * 弹出指定Toast的时间
     *
     * @param context
     * @param text
     */
    public static void showToast(Context context, String text, int time) {
        if (toast == null) {
            toast = Toast.makeText(context, text, time);
        }
        toast.setText(text);
        toast.show();
    }

    /**
     * 弹出自定义Toast
     *
     * @param context         （上下文）
     * @param text            （Toast内容）
     * @param time            （Toast的显示时间）
     * @param gravity         （Toast显示的位置）
     * @param backgroundcolor （背景颜色）
     * @param textColor       （Toast字体的颜色）
     * @param imageresource   （Toast添加图片的资源ID）
     */
    public static void myselfToast(Context context, String text, int time,
                                   int gravity, int backgroundcolor, int textColor, int imageresource) {
        Toast toast = Toast.makeText(context, text, time);
        // toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        // toast.setGravity(0|0 , 0, 200); // 设置显示位置
        toast.setGravity(gravity, 0, 0); // 设置显示位置
        LinearLayout layout = (LinearLayout) toast.getView();
        layout.setBackgroundColor(backgroundcolor);
        if (imageresource != -1) {
            ImageView image = new ImageView(context);
            image.setImageResource(imageresource);
            layout.addView(image, 0);
        }
        TextView v = toast.getView().findViewById(
                android.R.id.message);
        v.setTextColor(textColor); // 设置字体颜色
        toast.show();
    }

    /**
     * 弹出自定义Toast
     *
     * @param context       （上下文）
     * @param inflater      (LayoutInflater对象)
     * @param text          （Toast内容）
     * @param time          （Toast的显示时间）
     * @param layout        (Toast的自定义布局)
     * @param gravity       （Toast显示的位置）
     * @param imgResId      （Toast添加图片的RESID）
     * @param textResId     （Toast添加文字的RESID）
     * @param imageresource （Toast添加图片的资源ID）
     */
    public static void myselfToast(Context context, LayoutInflater inflater,
                                   String text, int time, int layout, int gravity, int imgResId,
                                   int textResId, int imageresource) {
        View view = inflater.inflate(layout, null);
        if (imageresource != -1) {
            ImageView img_logo = view.findViewById(imgResId);
            img_logo.setImageResource(imageresource);
        }
        TextView tv_msg = view.findViewById(textResId);
        tv_msg.setText(text);
        Toast toast = new Toast(context);
        toast.setGravity(gravity, 0, 0);
        toast.setDuration(time);
        toast.setView(view);
        toast.show();
    }
}
