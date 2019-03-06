package com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus;

/**
 * ======================以太网和wifi状态接口=====================
 * Created by dengzhe on 2018/2/7.
 */

public interface EthernetWifiCallBackI {

    boolean ethernetStatus(boolean status);//以太网状态

    boolean wifiStatus(boolean status);//wifi状态

}
