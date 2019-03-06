package com.wdkl.callingbed.util;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by fengxiangqian on 2017/11/7.
 */

public class MarqueeText extends AppCompatTextView {
    public MarqueeText(Context context) {
        super(context);
    }

    @Override//实现了都获取焦点
    public boolean isFocused() {
        return true;
    }

    public MarqueeText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MarqueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
