package com.wdkl.callingbed.util.sendcommand;

import android.content.Context;

import com.wdkl.callingbed.MyApplication;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.util.ScreenManagerUtil;
import com.wdkl.callingbed.util.StringUtils;
import com.wdkl.callingbed.util.VoiceManagerUtil;

import serialporttest.utils.SerialPortUtil;

/**
 * 类名称：CallingBedSendCommand <br>
 * 类描述：发送串口命令 ，设置硬件参数<br>
 * 创建人：Waderson Shll <br>
 * 创建时间：2017-12-08 <br>
 *
 * @version V1.0
 */

public class CallingBedSendCommand {
    /**
     * 设置床头灯是否打开
     */
    public static boolean setBedLight(SerialPortUtil serialPortUtil, boolean isBedLight, int type) {
        if (isBedLight) {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.BEDLIGHT + type, "1", "F");
                return true;
            }
        } else {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.BEDLIGHT + type, "0", "F");
                return false;
            }
        }
        return false;
    }

    /**
     * 设置手柄MIC
     */
    public static boolean setHandsMIC(SerialPortUtil serialPortUtil, boolean isHandsMIC) {
        if (isHandsMIC) {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.MIC, "0", "F");
                return true;
            }
        } else {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.MIC, "1", "F");
                return false;
            }
        }
        return false;
    }

    /**
     * 设置卫生间灯的闪烁0关闭1打开2闪烁
     */
    public static boolean setWSHLightFlicker(SerialPortUtil serialPortUtil, boolean isFlicker) {
        if (isFlicker) {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.ULED, "2", "F");
                return true;
            }
        } else {
            if (null != serialPortUtil) {
                if ("0".equals(Constants.MORNING_NIGTH)) {//白天
                    serialPortUtil.sendCommand(SerialPortUtil.ULED, "0", "F");
                } else {//晚上
                    serialPortUtil.sendCommand(SerialPortUtil.ULED, "1", "F");
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 关闭心跳
     */
    public static void closeHeart() {
        MyApplication.HEARTBEAT = false;
        if (null != MyApplication.serialPortUtil) {
            MyApplication.serialPortUtil.closeHeart();
        }
    }

    /**
     * 设置卫生间灯的开关
     */
    public static boolean setWSHLight(SerialPortUtil serialPortUtil, boolean isLight) {
        if (isLight) {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.ULED, "1", "F");
                return true;
            }
        } else {
            if (null != serialPortUtil) {
                serialPortUtil.sendCommand(SerialPortUtil.ULED, "0", "F");
                return false;
            }
        }
        return false;
    }

    /**
     * 设置当前 SIP 协议状态
     * 0：注册失败
     * 1：注册中
     * 2：注册成功
     */
    public static void setSipStatus(SerialPortUtil serialPortUtil,String random) {
        if (null != serialPortUtil) {
            serialPortUtil.sendCommand(SerialPortUtil.SIP_STATUS, random, "1");
        }
    }

    /**
     * 设置系统的亮度
     */
    public static void setSYSBrightness(Context context, int brightnessPercent) {
        float p = (float) brightnessPercent / 100;
        ScreenManagerUtil.setScreenBrightness(context, (int) (ScreenManagerUtil.maxBrightness * p));
    }

    /**
     * 设置系统的音量
     */
    public static void setSYSVoice(Context context, int voicePercent) {
        VoiceManagerUtil.setSystemVoice(context, voicePercent);
    }


    /**
     * 设置Player的音量
     */
    public static void setPlayerVoice(Context context, int voicePercent) {
        //暂时没有Player的声音；这个方法先预留
    }

    /**
     * 设置护理灯;
     * 快速写入串口时；串口反应不过来；所以必须将线程休眠
     */
    public static void setNurseBrightness(SerialPortUtil serialPortUtil, int brightnessPercent, String oneColor, String twoColor, String threeColor, String fourColor, String fiveColor) throws InterruptedException {
        LogUtil.d("NURSELIGHT", "==" + oneColor + "==" + twoColor + "==" + threeColor + "==" + fourColor + "==" + fiveColor);
        if (null != serialPortUtil) {
            float p = (float) brightnessPercent / 100;
            if (StringUtils.notEmpty(oneColor)) {
                int rr = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(oneColor, 0, 2))) * p);
                int gg = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(oneColor, 2, 4))) * p);
                int bb = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(oneColor, 4, 6))) * p);
                serialPortUtil.sendCommand(SerialPortUtil.NURSELIGHT + "0", beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F");
            }
            Thread.sleep(300);
            if (StringUtils.notEmpty(twoColor)) {
                int rr = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(twoColor, 0, 2))) * p);
                int gg = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(twoColor, 2, 4))) * p);
                int bb = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(twoColor, 4, 6))) * p);
                serialPortUtil.sendCommand(SerialPortUtil.NURSELIGHT + "1", beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F");
            }
            Thread.sleep(300);
            if (StringUtils.notEmpty(threeColor)) {
                int rr = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(threeColor, 0, 2))) * p);
                int gg = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(threeColor, 2, 4))) * p);
                int bb = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(threeColor, 4, 6))) * p);
                serialPortUtil.sendCommand(SerialPortUtil.NURSELIGHT + "2", beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F");
            }
            Thread.sleep(300);
            if (StringUtils.notEmpty(fourColor)) {
                int rr = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fourColor, 0, 2))) * p);
                int gg = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fourColor, 2, 4))) * p);
                int bb = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fourColor, 4, 6))) * p);
                serialPortUtil.sendCommand(SerialPortUtil.NURSELIGHT + "3", beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F");
            }
            Thread.sleep(300);
            if (StringUtils.notEmpty(fiveColor)) {
                int rr = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fiveColor, 0, 2))) * p);
                int gg = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fiveColor, 2, 4))) * p);
                int bb = (int) ((StringUtils.parseFloat(StringUtils.substringByLengh(fiveColor, 4, 6))) * p);
                serialPortUtil.sendCommand(SerialPortUtil.NURSELIGHT + "4", beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F");
            }
        }
    }

    public static String beComeDoubleStr(int b) {
        if (b >= 0 && b < 10) {
            return "0" + b;
        } else if (b >= 10 && b < 100) {
            return "" + b;
        } else {
            return "99";
        }
    }
}
