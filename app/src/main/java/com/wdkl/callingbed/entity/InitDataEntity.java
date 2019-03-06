package com.wdkl.callingbed.entity;

import java.io.Serializable;

/**
 * Created by fengxiangqian on 2017/9/1.
 * <p>
 * 机器的信息 （Waderson）
 */

public class InitDataEntity implements Serializable {
    private String deviceHumanType;
    private String deviceZone2;
    private String deviceRoomId;//门口机ID
    private String deviceZone3;
    private String deviceRoomNum;
    private String deviceStatus;
    private String deviceCallTimeOut;
    private String deviceZone4;
    private String deviceZone5;
    private String deviceSipId;
    private String deviceBedNum;
    private String deviceScreamSleep;
    private String deviceHostingID;
    private String deviceHumanId;
    private String id;//自己的ID
    private String deviceSipStatus;
    private String deviceName;
    private String deviceSipPassWord;
    private String deviceZone1;
    private String deviceZone0;
    private String devicelrCfg;
    private String deviceBedName;
    private String deviceSipIp;
    private String deviceWifiHostName;
    private String CurTime;

    public String getCurTime() {
        return CurTime == null ? "暂无" : CurTime;
    }

    public void setCurTime(String curTime) {
        CurTime = curTime;
    }


    public void setDeviceHumanType(String deviceHumanType) {
        this.deviceHumanType = deviceHumanType;
    }

    public String getDeviceHumanType() {
        return deviceHumanType == null ? "暂无" : deviceHumanType;
    }

    public void setDeviceZone2(String deviceZone2) {
        this.deviceZone2 = deviceZone2;
    }

    public String getDeviceZone2() {
        return deviceZone2 == null ? "0" : deviceZone2;
    }

    public void setDeviceRoomId(String deviceRoomId) {
        this.deviceRoomId = deviceRoomId;
    }

    public String getDeviceRoomId() {
        return deviceRoomId == null ? "0" : deviceRoomId;
    }

    public void setDeviceZone3(String deviceZone3) {
        this.deviceZone3 = deviceZone3;
    }

    public String getDeviceZone3() {
        return deviceZone3 == null ? "0" : deviceZone3;
    }

    public void setDeviceRoomNum(String deviceRoomNum) {
        this.deviceRoomNum = deviceRoomNum;
    }

    public String getDeviceRoomNum() {
        return deviceRoomNum == null ? "0" : deviceRoomNum;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceStatus() {
        return deviceStatus == null ? "暂无" : deviceStatus;
    }

    public void setDeviceCallTimeOut(String deviceCallTimeOut) {
        this.deviceCallTimeOut = deviceCallTimeOut;
    }

    public String getDeviceCallTimeOut() {
        return deviceCallTimeOut == null ? "0" : deviceCallTimeOut;
    }

    public void setDeviceZone4(String deviceZone4) {
        this.deviceZone4 = deviceZone4;
    }

    public String getDeviceZone4() {
        return deviceZone4 == null ? "0" : deviceZone4;
    }

    public void setDeviceZone5(String deviceZone5) {
        this.deviceZone5 = deviceZone5;
    }

    public String getDeviceZone5() {
        return deviceZone5 == null ? "0" : deviceZone5;
    }

    public void setDeviceSipId(String deviceSipId) {
        this.deviceSipId = deviceSipId;
    }

    public String getDeviceSipId() {
        return deviceSipId == null ? "0" : deviceSipId;
    }

    public void setDeviceBedNum(String deviceBedNum) {
        this.deviceBedNum = deviceBedNum;
    }

    public String getDeviceBedNum() {
        return deviceBedNum == null ? "0" : deviceBedNum;
    }

    public void setDeviceScreamSleep(String deviceScreamSleep) {
        this.deviceScreamSleep = deviceScreamSleep;
    }

    public String getDeviceScreamSleep() {
        return deviceScreamSleep == null ? "0" : deviceScreamSleep;
    }

    public void setDeviceHostingID(String deviceHostingID) {
        this.deviceHostingID = deviceHostingID;
    }

    public String getDeviceHostingID() {
        return deviceHostingID == null ? "0" : deviceHostingID;
    }

    public void setDeviceHumanId(String deviceHumanId) {
        this.deviceHumanId = deviceHumanId;
    }

    public String getDeviceHumanId() {
        return deviceHumanId == null ? "0" : deviceHumanId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id == null ? "0" : id;
    }

    public void setDeviceSipStatus(String deviceSipStatus) {
        this.deviceSipStatus = deviceSipStatus;
    }

    public String getDeviceSipStatus() {
        return deviceSipStatus == null ? "暂无" : deviceSipStatus;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName == null ? "暂无" : deviceName;
    }

    public void setDeviceSipPassWord(String deviceSipPassWord) {
        this.deviceSipPassWord = deviceSipPassWord;
    }

    public String getDeviceSipPassWord() {
        return deviceSipPassWord == null ? "0" : deviceSipPassWord;
    }

    public void setDeviceZone1(String deviceZone1) {
        this.deviceZone1 = deviceZone1;
    }

    public String getDeviceZone1() {
        return deviceZone1 == null ? "0" : deviceZone1;
    }

    public void setDeviceZone0(String deviceZone0) {
        this.deviceZone0 = deviceZone0;
    }

    public String getDeviceZone0() {
        return deviceZone0 == null ? "0" : deviceZone0;
    }

    public void setDevicelrCfg(String devicelrCfg) {
        this.devicelrCfg = devicelrCfg;
    }

    public String getDevicelrCfg() {
        return devicelrCfg == null ? "暂无" : devicelrCfg;
    }

    public void setDeviceBedName(String deviceBedName) {
        this.deviceBedName = deviceBedName;
    }

    public String getDeviceBedName() {
        return deviceBedName == null ? "暂无" : deviceBedName;
    }

    public void setDeviceSipIp(String deviceSipIp) {
        this.deviceSipIp = deviceSipIp;
    }

    public String getDeviceSipIp() {
        return deviceSipIp == null ? "0" : deviceSipIp;
    }

    public void setDeviceWifiHostName(String deviceWifiHostName) {
        this.deviceWifiHostName = deviceWifiHostName;
    }

    public String getDeviceWifiHostName() {
        return deviceWifiHostName == null ? "暂无" : deviceWifiHostName;
    }
}
