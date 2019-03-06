package com.wdkl.callingbed.entity;

/**
 * Created by 胡博文 on 2017/10/26.
 */

public class BroadCastEntity {
    private String Indexes;//索引
    private String path;//路径
    private String voiceInt;
    private String zoneId;

    public String getVoiceInt() {
        return voiceInt == null ? "0" : voiceInt;
    }

    public void setVoiceInt(String voiceInt) {
        this.voiceInt = voiceInt;
    }

    public String getIndexes() {
        return Indexes == null ? "0" : Indexes;
    }

    public void setIndexes(String indexes) {
        Indexes = indexes;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getZoneId() {
        return zoneId == null ? "0" : zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
