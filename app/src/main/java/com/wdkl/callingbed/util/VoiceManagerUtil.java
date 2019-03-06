package com.wdkl.callingbed.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * 类名称：VoiceManagerUtil <br>
 * 类描述：声音控制工具类 <br>
 * 创建人：Waderson Shll （TEL：15675117662）<br>
 * 创建时间：2018-03-15 <br>
 * 特别提醒：如有需要该类可任意创建与调用；在未通知本人的情况下该类禁止任何修改！<br>
 */
public class VoiceManagerUtil {
    /**
     * 获取提示音音量最大值
     *
     * @param context
     */
    public static int getAlarmMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    /**
     * 获取提示音音量当前值
     *
     * @param context
     */
    public static int getAlarmNow(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
    }

    /**
     * 获取多媒体音量最大值
     *
     * @param context
     */
    public static int getMusicMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取多媒体音量当前值
     *
     * @param context
     */
    public static int getMusicNow(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取铃声音量最大值
     *
     * @param context
     */
    public static int getRingMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    /**
     * 获取铃声音量当前值
     *
     * @param context
     */
    public static int getRingNow(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
    }

    /**
     * 获取系统音量最大值
     *
     * @param context
     */
    public static int getSystemMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
    }

    /**
     * 获取系统音量当前值
     *
     * @param context
     */
    public static int getSystemNow(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    /**
     * 获取通话音量最大值
     *
     * @param context
     */
    public static int getCallMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
    }

    /**
     * 获取通话音量当前值
     *
     * @param context
     */
    public static int getCallNow(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }

    /**
     * 设置提示音音量
     *
     * @param context
     * @param percent （百分比；只能0--100之间）
     */
    public static void setAlarmVoice(Context context, int percent) {
        float vPercent=((float)percent)/100f;
        vPercent = vPercent < 0 ? 0 : vPercent;
        vPercent = vPercent > 1 ? 1 : vPercent;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (int) (getAlarmMax(context) * vPercent), 0);
    }

    /**
     * 设置多媒体音量
     *
     * @param context
     * @param percent （百分比；只能0--100之间）
     */
    public static void setMusicVoice(Context context, int percent) {
        float vPercent=((float)percent)/100f;
        vPercent = vPercent < 0 ? 0 : vPercent;
        vPercent = vPercent > 1 ? 1 : vPercent;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (getMusicMax(context) * vPercent), 0);
    }

    /**
     * 设置铃声音量
     *
     * @param context
     * @param percent （百分比；只能0--100之间）
     */
    public static void setRingVoice(Context context, int percent) {
        float vPercent=((float)percent)/100f;
        vPercent = vPercent < 0 ? 0 : vPercent;
        vPercent = vPercent > 1 ? 1 : vPercent;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, (int) (getRingMax(context) * vPercent), 0);
    }

    /**
     * 设置系统音量
     *
     * @param context
     * @param percent （百分比；只能0--100之间）
     */
    public static void setSystemVoice(Context context, int percent) {
        float vPercent=((float)percent)/100f;
        vPercent = vPercent < 0 ? 0 : vPercent;
        vPercent = vPercent > 1 ? 1 : vPercent;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, (int) (getSystemMax(context) * vPercent), 0);
    }

    /**
     * 设置通话音量
     *
     * @param context
     * @param percent （百分比；只能0--100之间）
     */
    public static void setCallVoice(Context context, int percent) {
        float vPercent=((float)percent)/100f;
        vPercent = vPercent < 0 ? 0 : vPercent;
        vPercent = vPercent > 1 ? 1 : vPercent;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int) (getCallMax(context) * vPercent), 0);
    }

}
