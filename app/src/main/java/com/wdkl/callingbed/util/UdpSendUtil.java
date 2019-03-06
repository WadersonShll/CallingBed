package com.wdkl.callingbed.util;

import android.content.Context;

import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.InitDataEntity;
import com.wdkl.callingbed.entity.MainDataEntity;


/**
 * Created by dengzhe on 2018/4/28.
 * #############UDP发送工具类#############
 */

public class UdpSendUtil {


    //----------------------------------------------------------接收UDP----------------------------------------------------------//
    public static void receiveUdp(String data, Context context) {
        AnalysisUdpUtil.AnalysisUdp(data, context);
    }

    //----------------------------------------------------------手动重启系统----------------------------------------------------------/
    public static void sendManualReboot(String str) {
        AnalysisUdpUtil.sendString(str);
    }

    /**
     * 卫生间呼叫
     *
     * @param initDataEntity
     */
    public static void sendCall2(InitDataEntity initDataEntity) {
        AnalysisUdpUtil.sendAndroidUdpData("call_2"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , Constants.BED_ID
                , Constants.SIP_ID
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , "255"
                , Constants.WSHROOM_CALL
                , "卫生间"
                , "0");
    }

    /**
     * 请求增援呼叫
     *
     * @param initDataEntity
     * @param mainDataEntity
     */
    public static void sendCall4(InitDataEntity initDataEntity, MainDataEntity mainDataEntity) {
        AnalysisUdpUtil.sendAndroidUdpData("call_4"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , Constants.BED_ID
                , Constants.SIP_ID
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , mainDataEntity.getNurseLevel()
                , Constants.ROOMHELP_CALL
                , "请求增援"
                , "0");
    }

    /**
     * 呼叫护士站
     *
     * @param initDataEntity
     * @param mainDataEntity
     */
    public static void sendCall1(InitDataEntity initDataEntity, MainDataEntity mainDataEntity) {
        AnalysisUdpUtil.sendAndroidUdpData("call_1"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , initDataEntity.getId()
                , initDataEntity.getDeviceSipId()
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , mainDataEntity.getNurseLevel()
                , Constants.SON_CALL
                , mainDataEntity.getName()
                , "0");
    }

    /**
     * 护士点了退出护理<br></>
     * 以子机的名义发给主机；通知主机删除对应的呼叫条目
     */
    public static void sendCall1b1(InitDataEntity initDataEntity, MainDataEntity mainDataEntity) {
        AnalysisUdpUtil.sendAndroidUdpData("call_1_b1"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , Constants.BED_ID
                , Constants.SIP_ID
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , mainDataEntity.getNurseLevel()
                , Constants.WSHROOM_CALL
                , "卫生间"
                , "0");
    }

    /**
     * @param initDataEntity
     * @param mainDataEntity
     */
    public static void sendCall1Transfer(InitDataEntity initDataEntity, MainDataEntity mainDataEntity) {
        AnalysisUdpUtil.sendAndroidUdpData("call_1_transfer"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , Constants.BED_ID
                , Constants.SIP_ID
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , mainDataEntity.getNurseLevel()
                , Constants.MULTITAP_CALL
                , "子机转接"
                , "0");
    }

    /**
     * 呼叫护士站接听中通知门口机
     *
     * @param initDataEntity
     * @param mainDataEntity
     * @param type           (0:默认；1：闪烁；2：绿色)
     */
    public static void sendCallNoticeDoor(InitDataEntity initDataEntity, MainDataEntity mainDataEntity, String type) {
        AnalysisUdpUtil.sendAndroidUdpData("calling_notice"
                , initDataEntity.getDeviceHostingID()
                , initDataEntity.getDeviceRoomId()
                , initDataEntity.getId()
                , initDataEntity.getDeviceSipId()
                , initDataEntity.getDeviceRoomNum()
                , initDataEntity.getDeviceBedNum()
                , mainDataEntity.getNurseLevel()
                , type
                , mainDataEntity.getName()
                , "0");
    }
}
