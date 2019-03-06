package com.wdkl.callingbed.util;

import android.content.Context;

import com.wdkl.callingbed.MyApplication;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.BroadCastEntity;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.entity.SystemSetEntity;
import com.wdkl.callingbed.entity.UdpEntity;

import org.greenrobot.eventbus.EventBus;

import serialporttest.utils.SerialPortUtil;

import static com.wdkl.callingbed.MyApplication.serialPortUtil;

/**
 * Created by 胡博文 on 2017/10/18.
 */

public class AnalysisUdpUtil {
    private AnalysisUdpUtil() {
    }

    public static void AnalysisUdp(String udpMsg, Context context) {
        LogUtil.d("AnalysisUdp", "udpMsg==" + udpMsg);
        try {
            //判断是否为美元符号开头，如果是则是android 端的命令
            if ("$".equals(udpMsg.substring(0, 1))) {
                udpMsg = delHeadAndEnd(udpMsg, "$", "#");
                String[] data = udpMsg.split(Constants.DELIMITER);
                switch (data[0]) {
                    //播放广播
                    case "broadcast_1":
                        if (!Constants.INTIME_BROADCAST.equals(data[1])) {
                            Constants.INTIME_BROADCAST = data[1];
                            BroadCastEntity broadCastEntity = new BroadCastEntity();
                            broadCastEntity.setIndexes(data[0]);
                            broadCastEntity.setPath(data[1]);
                            broadCastEntity.setVoiceInt(data[2]);
                            broadCastEntity.setZoneId(data[3]);
                            EventBus.getDefault().post(new MessageEvent(broadCastEntity, Constants.EVENT_BROADCAST));  //将udp发送出去
                        }
                        break;
                    //停止播放广播
                    case "broadcast_2":
                        Constants.INTIME_BROADCAST = "";
                        BroadCastEntity broadCastEntity1 = new BroadCastEntity();
                        broadCastEntity1.setIndexes(data[0]);
                        broadCastEntity1.setPath(data[1]);
                        EventBus.getDefault().post(new MessageEvent(broadCastEntity1, Constants.EVENT_BROADCAST));
                        break;
                    //全局广播音量大小
                    case "broadcast_v":
                        BroadCastEntity broadCastEntity2 = new BroadCastEntity();
                        broadCastEntity2.setIndexes(data[0]);
                        broadCastEntity2.setPath(data[1]);
                        broadCastEntity2.setVoiceInt(data[2]);
                        EventBus.getDefault().post(new MessageEvent(broadCastEntity2, Constants.EVENT_BROADCAST));
                        break;
                    case "call_1":
                        //床头机呼叫护士主机");
                        break;
                    case "call_2":
                        //卫生间呼叫");
                        break;
                    case "call_3":
                        //床头机呼叫床头机");
                        break;
                    case "call_4":
                        //床头机呼叫增援");
                        break;
                    case "call_5":
                        //床头机呼叫手表");
                        break;
                    case "call_6":
                        //门口机呼叫护士主机");
                        break;
                    case "call_7":
                        //门口机呼叫医生主机");
                        break;
                    case "call_8": //护士主机呼叫分机
                        UdpEntity entity = new UdpEntity();
                        entity.setIndexes(data[0]);
                        entity.setNurseHostID(data[1]);
                        entity.setDoorwayMachineID(data[2]);
                        entity.setHeadMachineID(data[3]);
                        entity.setSipAddress(data[4]);
                        entity.setRoomNumber(data[5]);
                        entity.setBedNumber(data[6]);
                        entity.setLevel(data[7]);
                        entity.setType(data[8]);
                        entity.setName(data[9]);
                        EventBus.getDefault().post(new MessageEvent(entity, Constants.EVENT_UDP));
                        break;
                    case "call_9":
                        //护士主机呼叫护士主机");
                        break;
                    case "call_10":
                        //护士主机呼叫医生主机");
                        break;
                    case "call_11":
                        //手表呼叫护士主机");
                        break;
                    case "end_1":
                        //挂断");
                        break;
                    case "deposit_1":
                        //主机托管给主机");
                        break;
                    case "deposit_2":
                        //取消托管");
                        break;
                    case "nursing_1"://进入护理
                        UdpEntity nursing_1 = new UdpEntity();
                        nursing_1.setIndexes(data[0]);
                        nursing_1.setNurseHostID(data[1]);
                        nursing_1.setDoorwayMachineID(data[2]);
                        nursing_1.setHeadMachineID(data[3]);
                        nursing_1.setSipAddress(data[4]);
                        nursing_1.setRoomNumber(data[5]);
                        nursing_1.setBedNumber(data[6]);
                        nursing_1.setLevel(data[7]);
                        nursing_1.setType(data[8]);
                        nursing_1.setName(data[9]);
                        LogUtil.d(AnalysisUdpUtil.class, "nursing_1==" + nursing_1.toString());
                        EventBus.getDefault().post(new MessageEvent(nursing_1, Constants.EVENT_UDP));
                        break;
                    case "nursing_2"://退出护理
                        UdpEntity nursing_2 = new UdpEntity();
                        nursing_2.setIndexes(data[0]);
                        nursing_2.setNurseHostID(data[1]);
                        nursing_2.setDoorwayMachineID(data[2]);
                        nursing_2.setHeadMachineID(data[3]);
                        nursing_2.setSipAddress(data[4]);
                        nursing_2.setRoomNumber(data[5]);
                        nursing_2.setBedNumber(data[6]);
                        nursing_2.setLevel(data[7]);
                        nursing_2.setType(data[8]);
                        nursing_2.setName(data[9]);
                        LogUtil.d(AnalysisUdpUtil.class, "nursing_2==" + nursing_2.toString());
                        EventBus.getDefault().post(new MessageEvent(nursing_2, Constants.EVENT_UDP));
                        break;
                    case "call_8_upremove": //主机上滑移除了呼叫列表中的一个条目
                        UdpEntity call_8_upremove = new UdpEntity();
                        call_8_upremove.setIndexes(data[0]);
                        call_8_upremove.setNurseHostID(data[1]);
                        call_8_upremove.setDoorwayMachineID(data[2]);
                        call_8_upremove.setHeadMachineID(data[3]);
                        call_8_upremove.setSipAddress(data[4]);
                        call_8_upremove.setRoomNumber(data[5]);
                        call_8_upremove.setBedNumber(data[6]);
                        call_8_upremove.setLevel(data[7]);
                        call_8_upremove.setType(data[8]);
                        call_8_upremove.setName(data[9]);
                        EventBus.getDefault().post(new MessageEvent(call_8_upremove, Constants.EVENT_UDP));
                        break;
                    case "call_8_transfer": //转接护士主机电话
                        UdpEntity call_8_transfer = new UdpEntity();
                        call_8_transfer.setIndexes(data[0]);
                        call_8_transfer.setNurseHostID(data[1]);
                        call_8_transfer.setDoorwayMachineID(data[2]);
                        call_8_transfer.setHeadMachineID(data[3]);
                        call_8_transfer.setSipAddress(data[4]);
                        call_8_transfer.setRoomNumber(data[5]);
                        call_8_transfer.setBedNumber(data[6]);
                        call_8_transfer.setLevel(data[7]);
                        call_8_transfer.setType(data[8]);
                        call_8_transfer.setName(data[9]);
                        EventBus.getDefault().post(new MessageEvent(call_8_transfer, Constants.EVENT_UDP));
                        break;
                    case "back_1":
                        //呼叫回复");
                        break;
                }
            } else if ("#".equals(udpMsg.substring(0, 1))) {
                LogUtil.d("isBelongToHostMachine", "udpMsg==" + udpMsg);
                //判断是否为服务端发过来的命令
                udpMsg = delHeadAndEnd(udpMsg, "#", "$");
                String[] data = udpMsg.split(Constants.DELIMITER);
                switch (data[0]) {
                    case "HEART"://服务端定时发送心跳监听；APP回复
                        if (data[1].equals(Constants.MAC_ADDRESS)) {
                            sendString("HEART," + Constants.MAC_ADDRESS);
                        }
                        break;
                    case "MGR_RESET"://重启AP  //批量重启（）
                        boolean isRESET = false;
                        if (!isRESET) {
                            LogUtil.d("isBelongToHostMachine", "data[1]==" + data[1]);
                            if (data[1].equals(Constants.MAC_ADDRESS) || (data[1].equals("FF:FF:FF:FF:FF:FF") && isBelongToHostMachine(data[2]))) {
                                EventBus.getDefault().post(new MessageEvent("reset", Constants.EVENT_MGR_RESET));
                            }
                        }
                        break;
                    case "MGR_SYSTEM_RESET":
                        if (data[1].equals(Constants.MAC_ADDRESS)) {
                            ((MyApplication) context.getApplicationContext()).serialPortUtil.systemRestart();
                        }
                        break;
                    case "MGR_REG_Q": //给出设备信息
                        try {
                            if (data[1].equals(Constants.MAC_ADDRESS)) {
                                //有线mac地址，设备出场信息，无线mac地址
                                String str = "$MGR_REG_A" + Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "4" + Constants.DELIMITER + android.os.Build.DISPLAY +
                                        Constants.DELIMITER + Constants.MAC_ADDRESS + Constants.DELIMITER + "FF:FF:FF:FF:FF:FF" + Constants.DELIMITER + SerialPortUtil.KEY_ID + "#";
                                UdpHelper.send(str);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                        break;
                    case "MGR_MACHINE_SETSYS": //系统设置
                        if (!Constants.MORNING_NIGTH.equals(data[1])
                                || !Constants.SCREENLIGHT.equals(data[2])
                                || !Constants.SYSVOICE.equals(data[3])
                                || !Constants.RINGLVOICE.equals(data[4])
                                || !Constants.RINGLVOICELOOP.equals(data[5])
                                || !Constants.NURSINGLIGHT.equals(data[6])
                                || !Constants.CALLINGTIMEOUT.equals(data[7])
                                || !Constants.SCREENEXTINGUISHTIME.equals(data[8])
                                || !Constants.DOORCALLVOICE.equals(data[9])
                                || !Constants.BEDCALLVOICE.equals(data[10])
                                ) {
                            Constants.MORNING_NIGTH = data[1];
                            Constants.SCREENLIGHT = data[2];
                            Constants.SYSVOICE = data[3];
                            Constants.RINGLVOICE = data[4];
                            Constants.RINGLVOICELOOP = data[5];
                            Constants.NURSINGLIGHT = data[6];
                            Constants.CALLINGTIMEOUT = data[7];
                            Constants.SCREENEXTINGUISHTIME = data[8];
                            Constants.DOORCALLVOICE = data[9];
                            Constants.BEDCALLVOICE = data[10];
                            SystemSetEntity machine_setsys = new SystemSetEntity(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8]);
                            LogUtil.d(AnalysisUdpUtil.class, "MGR_MACHINE_SETSYS==" + machine_setsys.toString());
                            EventBus.getDefault().post(new MessageEvent(machine_setsys, Constants.EVENT_SETSYS));
                        } else {
                            return;
                        }

                        break;
                    case "MGR_PATIENTUPDATE": //后台数据改变刷新UI
                        LogUtil.d("MGR_PATIENTUPDATE", "bed--11");
                        if (data[2].equals(Constants.MYSELF_ID)) { //ID匹配；刷新本机
                            if (!Constants.UPDATE_PATIENTUPDATE_FLAG) {
                                EventBus.getDefault().post(new MessageEvent(null, Constants.EVENT_MGR_PATIENTUPDATE));
                            }
                            Constants.UPDATE_PATIENTUPDATE_FLAG = true;
                        }
                        break;
                    case "MGR_APPUPDATE": //APP更新
                        if (data[1].equals("4")) {
                            if (!Constants.UPDATE_APP_FLAG) {
                                Constants.UPDATE_APP_FLAG = true;
                                EventBus.getDefault().post(new MessageEvent(null, Constants.EVENT_MGR_APP_UPDATE));
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否是属于所指定的主机<br></>
     */
    public static boolean isBelongToHostMachine(String nurseHostID) {
        if (!StringUtils.notEmpty(nurseHostID)) return false;
        String n = StringUtils.substringByLengh(Constants.CALLMAIN_ID, 0, 1);
        if ("#".equals(n)) {
            String nDis = StringUtils.deleteCharAt(Constants.CALLMAIN_ID, 0);
            String my = nDis.split(",")[0];
            String his = nDis.split(",")[1];
            return nurseHostID.equals(my) || nurseHostID.equals(his);
        } else {
            return StringUtils.notEmpty(nurseHostID) && nurseHostID.equals(Constants.CALLMAIN_ID);
        }
    }

    public static void sendAndroidUdpData(String Indexes, String nurseHostID, String doorwayMachineID, String headMachineID, String sipAddress, String roomNumber, String bedNumber, String level, String type, String name, String deviceMAC) {
        final String strUdp = Indexes + Constants.DELIMITER + nurseHostID +
                Constants.DELIMITER + doorwayMachineID + Constants.DELIMITER
                + headMachineID + Constants.DELIMITER + sipAddress + Constants.DELIMITER
                + roomNumber + Constants.DELIMITER + bedNumber + Constants.DELIMITER + level + Constants.DELIMITER + type + Constants.DELIMITER + name + Constants.DELIMITER + deviceMAC;
        new Thread() {
            @Override
            public void run() {
                //把网络访问的代码放在这里
                UdpHelper.send("$" + strUdp + "#");
            }
        }.start();
    }

    /**
     * 直接UDP 发送一条字符串
     */
    public static void sendString(final String str) {
        new Thread() {
            @Override
            public void run() {
                //把网络访问的代码放在这里
                UdpHelper.send("$" + str + "#");
            }
        }.start();
    }


    public static String delHeadAndEnd(String source, String beginTrim, String endTrim) {
        if (source == null) {
            return "";
        }
        source = source.trim(); // 循环去掉字符串首的beTrim字符
        if (source.isEmpty()) {
            return "";
        }
        String beginChar = source.substring(0, 1);
        if (beginChar.equalsIgnoreCase(beginTrim)) {
            source = source.substring(1, source.length());
            beginChar = source.substring(0, 1);
        }
        // 循环去掉字符串尾的beTrim字符
        String endChar = source.substring(source.length() - 1, source.length());
        if (endChar.equalsIgnoreCase(endTrim)) {
            source = source.substring(0, source.length() - 1);
            endChar = source.substring(source.length() - 1, source.length());
        }
        return source;
    }
}
