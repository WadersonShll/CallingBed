package com.wdkl.callingbed.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 胡博文 on 2017/9/5.
 */

public class NoticeDataEntity implements Serializable {
    private List<NoticeArray> NoticeArray;

    public void setNoticeArray(List<NoticeArray> NoticeArray) {
        this.NoticeArray = NoticeArray;
    }

    public List<NoticeArray> getNoticeArray() {
        return NoticeArray;
    }

    public class NoticeArray implements Serializable {

        private String noticeId;
        private String noticeContent;
        private String noticeStartTime;
        private String noticeEndTime;

        public String getNoticeId() {
            return noticeId;
        }

        public void setNoticeId(String noticeId) {
            this.noticeId = noticeId;
        }

        public void setNoticeContent(String noticeContent) {
            this.noticeContent = noticeContent;
        }

        public String getNoticeContent() {
            return noticeContent == null ? "暂无" : noticeContent;
        }

        public void setNoticeStartTime(String noticeStartTime) {
            this.noticeStartTime = noticeStartTime;
        }

        public String getNoticeStartTime() {
            return noticeStartTime == null ? "" : noticeStartTime;
        }

        public void setNoticeEndTime(String noticeEndTime) {
            this.noticeEndTime = noticeEndTime;
        }

        public String getNoticeEndTime() {
            return noticeEndTime == null ? "" : noticeEndTime;
        }

    }
}
