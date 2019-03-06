package com.wdkl.callingbed.entity;

/**
 * Created by xuhuan on 2017/9/20.
 */

public class NoticeEntity {

    private int alarmIndex;//闹钟的索引
    private boolean alarmState;//闹钟开始时间和结束时间的状态


    public boolean isAlarmState() {
        return alarmState;
    }

    public void setAlarmState(boolean alarmState) {
        this.alarmState = alarmState;
    }


    public int getAlarmIndex() {
        return alarmIndex;
    }

    public void setAlarmIndex(int alarmIndex) {
        this.alarmIndex = alarmIndex;
    }
}
