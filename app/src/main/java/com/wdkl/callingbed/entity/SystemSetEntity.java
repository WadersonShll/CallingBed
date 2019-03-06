package com.wdkl.callingbed.entity;

public class SystemSetEntity {
    private String dayOrNight;
    private String screenLight;
    private String callVoice;
    private String ringlVoice;
    private String ringlVoiceLoop;
    private String nursingLight;
    private String CallingTimeOut;
    private String screenExtinguishTime;

    public SystemSetEntity() {
        super();
    }

    public SystemSetEntity(String dayOrNight, String screenLight, String callVoice, String ringlVoice, String ringlVoiceLoop, String nursingLight, String callingTimeOut, String screenExtinguishTime) {
        this.dayOrNight = dayOrNight;
        this.screenLight = screenLight;
        this.callVoice = callVoice;
        this.ringlVoice = ringlVoice;
        this.ringlVoiceLoop = ringlVoiceLoop;
        this.nursingLight = nursingLight;
        CallingTimeOut = callingTimeOut;
        this.screenExtinguishTime = screenExtinguishTime;
    }

    public String getDayOrNight() {
        return dayOrNight == null ? "0" : dayOrNight;
    }

    public void setDayOrNight(String dayOrNight) {
        this.dayOrNight = dayOrNight;
    }

    public String getScreenLight() {
        return screenLight == null ? "0" : screenLight;
    }

    public void setScreenLight(String screenLight) {
        this.screenLight = screenLight;
    }

    public String getCallVoice() {
        return callVoice == null ? "0" : callVoice;
    }

    public void setCallVoice(String callVoice) {
        this.callVoice = callVoice;
    }

    public String getRinglVoice() {
        return ringlVoice == null ? "0" : ringlVoice;
    }

    public void setRinglVoice(String ringlVoice) {
        this.ringlVoice = ringlVoice;
    }

    public String getRinglVoiceLoop() {
        return ringlVoiceLoop == null ? "0" : ringlVoiceLoop;
    }

    public void setRinglVoiceLoop(String ringlVoiceLoop) {
        this.ringlVoiceLoop = ringlVoiceLoop;
    }

    public String getNursingLight() {
        return nursingLight == null ? "0" : nursingLight;
    }

    public void setNursingLight(String nursingLight) {
        this.nursingLight = nursingLight;
    }

    public String getCallingTimeOut() {
        return CallingTimeOut == null ? "0" : CallingTimeOut;
    }

    public void setCallingTimeOut(String callingTimeOut) {
        CallingTimeOut = callingTimeOut;
    }

    public String getScreenExtinguishTime() {
        return screenExtinguishTime == null ? "0" : screenExtinguishTime;
    }

    public void setScreenExtinguishTime(String screenExtinguishTime) {
        this.screenExtinguishTime = screenExtinguishTime;
    }

    @Override
    public String toString() {
        return "SystemSetEntity{" +
                "dayOrNight='" + dayOrNight + '\'' +
                ", screenLight='" + screenLight + '\'' +
                ", callVoice='" + callVoice + '\'' +
                ", ringlVoice='" + ringlVoice + '\'' +
                ", ringlVoiceLoop='" + ringlVoiceLoop + '\'' +
                ", nursingLight='" + nursingLight + '\'' +
                ", CallingTimeOut='" + CallingTimeOut + '\'' +
                ", screenExtinguishTime='" + screenExtinguishTime + '\'' +
                '}';
    }

}
