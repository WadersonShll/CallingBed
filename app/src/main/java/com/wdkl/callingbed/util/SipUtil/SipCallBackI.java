package com.wdkl.callingbed.util.SipUtil;

/**
 * ======================Sip回调接口=====================
 * Created by dengzhe on 2018/2/7.
 */

public interface SipCallBackI {
    void startCall(String sipAddress);//开始拨打

    void autoTalking();//自动接听

    void endCall();//结束通话


}
