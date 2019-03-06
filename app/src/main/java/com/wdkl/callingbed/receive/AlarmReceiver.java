package com.wdkl.callingbed.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.entity.NoticeEntity;

import org.greenrobot.eventbus.EventBus;

/**
 * @author way
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        int position = intent.getIntExtra("position", -1);
        int playFlag = intent.getIntExtra("playFlag", -1);
        NoticeEntity noticeEntity = new NoticeEntity();

        //语音播报数据
        if (playFlag == 0) {
            noticeEntity.setAlarmIndex(position);
            noticeEntity.setAlarmState(true);
            EventBus.getDefault().post(new MessageEvent(noticeEntity, Constants.EVENT_NOTICE));
        }
        //删除通知数据
        if (playFlag == 1) {
            noticeEntity.setAlarmIndex(position);
            noticeEntity.setAlarmState(false);
            EventBus.getDefault().post(new MessageEvent(noticeEntity, Constants.EVENT_NOTICE));
        }
        //开启定时广播
        if (playFlag == 3){
            noticeEntity.setAlarmIndex(position);
            noticeEntity.setAlarmState(true);
            EventBus.getDefault().post(new MessageEvent(noticeEntity, Constants.EVENT_NOTICE));
        }
        //关闭定时广播
        if (playFlag == 4){
            noticeEntity.setAlarmIndex(position);
            noticeEntity.setAlarmState(true);
            EventBus.getDefault().post(new MessageEvent(noticeEntity, Constants.EVENT_NOTICE));

        }
    }
}

