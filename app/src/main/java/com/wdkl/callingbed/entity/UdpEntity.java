package com.wdkl.callingbed.entity;

/**
 * Created by fengxiangqian on 2017/8/31.
 */

public class UdpEntity {
    private String showText;//说明字段
    private String Indexes;//索引
    private String nurseHostID;//主机id
    private String doorwayMachineID;//门口机id
    private String headMachineID;//床头机id
    private String sipAddress;//sip地址
    private String roomNumber;//房间号
    private String bedNumber;//床号
    private String name;//名称
    private String Type;//类型
    private String level;//等级

    public String getLevel() {
        return null == level ? "" : level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return null == Type ? "" : Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getName() {
        return null == name ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowText() {
        return null == showText ? "" : showText;
    }

    public void setShowText(String showText) {
        this.showText = showText;
    }

    public String getIndexes() {
        return null == Indexes ? "" : Indexes;
    }

    public void setIndexes(String indexes) {
        Indexes = indexes;
    }

    public String getNurseHostID() {
        return null == nurseHostID ? "" : nurseHostID;
    }

    public void setNurseHostID(String nurseHostID) {
        this.nurseHostID = nurseHostID;
    }

    public String getDoorwayMachineID() {
        return null == doorwayMachineID ? "" : doorwayMachineID;
    }

    public void setDoorwayMachineID(String doorwayMachineID) {
        this.doorwayMachineID = doorwayMachineID;
    }

    public String getHeadMachineID() {
        return null == headMachineID ? "" : headMachineID;
    }

    public void setHeadMachineID(String headMachineID) {
        this.headMachineID = headMachineID;
    }

    public String getSipAddress() {
        return null == sipAddress ? "" : sipAddress;
    }

    public void setSipAddress(String sipAddress) {
        this.sipAddress = sipAddress;
    }

    public String getRoomNumber() {
        return null == roomNumber ? "" : roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBedNumber() {
        return null == bedNumber ? "" : bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UdpEntity) {
            UdpEntity udpEntity = (UdpEntity) obj;
            return name.equals(udpEntity.getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return "UdpEntity{" +
                "showText='" + showText + '\'' +
                ", Indexes='" + Indexes + '\'' +
                ", nurseHostID='" + nurseHostID + '\'' +
                ", doorwayMachineID='" + doorwayMachineID + '\'' +
                ", headMachineID='" + headMachineID + '\'' +
                ", sipAddress='" + sipAddress + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", bedNumber='" + bedNumber + '\'' +
                ", name='" + name + '\'' +
                ", Type='" + Type + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
