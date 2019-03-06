package com.wdkl.callingbed.entity;

import android.net.sip.SipAudioCall;

/**
 * Created by 胡博文 on 2017/9/7.
 */

public class SipMessageEvent {
    private SipAudioCall sipAudioCall;

    public SipMessageEvent(SipAudioCall sipAudioCall) {
        this.sipAudioCall = sipAudioCall;
    }

    public SipAudioCall getSipAudioCall() {
        return sipAudioCall;
    }

    public void setSipAudioCall(SipAudioCall sipAudioCall) {
        this.sipAudioCall = sipAudioCall;
    }
}
