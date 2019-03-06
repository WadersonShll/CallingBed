package com.wdkl.callingbed.entity;

/**
 * Created by Administrator on 2016/8/15 0015.
 */
public class MessageEvent {
    private Object message;
    private int type;

    public MessageEvent(Object message, int type) {
        this.message = message;
        this.type = type;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
