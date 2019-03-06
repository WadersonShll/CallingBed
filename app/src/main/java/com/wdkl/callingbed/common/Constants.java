package com.wdkl.callingbed.common;

import com.wdkl.callingbed.ui.InitActivity;

/**
 * Created by 胡博文 on 2017/9/1.
 */

public class Constants {
    public static final String DELIMITER = Character.toString((char) 3);

    public static final String MSG_SP = "msg_sp";

    public static  String TWO_DIMENSION_CODE_PATH = "http://id.wdklian.com:88/app/?id=";

    public static final String INITENTITY = "initEntity";
    /**
     * SIP呼叫地址
     */
    public static String innetSIPAddress = "@192.168.1.11";
    //mac地址
    public static String MAC_ADDRESS = "";
    //sip账号
    public static String SIP_ID = "";
    //sip密码
    public static String SIP_PASS_WORD = "";
    //sip地址
    public static String SIP_IP = "";
    /**
     * sip端口号
     */
    public static final String SIP_IP_END = ":5060";

    //该设备的wifi热点名称
    public static String DEVICE_WIFI_HOST_NAME = "";
    //该设备的床头号
    public static String BED_ID = "";
    //该设备的门口机号
    public static String ROOM_ID = "";
    //熄屏时间
    public static String DEVICE_SCREEN_SLEEP = "";
    //该设备的主键
    public static String DEVICE_HUMAN_ID = "";
    /**
     * 该设备的所属主机ID
     */
    public static String DEVICE_HOSTING_ID = "";

    //语音呼叫超时时间  默认30s
    public static int CALLING_TIME_OUT = 30;
    //铃声播放次数 默认3次
    public static int CALLING_TIMES = 3;

    /**
     * APP更新flag值<br></>
     * false为无更新指令。
     */
    public static boolean UPDATE_APP_FLAG = false;
    /**
     * 系统音量flag值<br></>
     * false为无更新指令。
     */
    public static boolean VOICE_APP_FLAG = false;

    /**
     * 后台更新flag值<br></>
     * false为无更新指令。
     */
    public static boolean UPDATE_PATIENTUPDATE_FLAG = false;
    /**
     * 呼叫状态
     */
    public static String CALL_STATE = "-1";
    /**
     * 待机状态
     */
    public static final String STANDBY = "0";
    /**
     * 呼叫中
     */
    public static final String IN_CALL = "1";
    /**
     * 通话中
     */
    public static final String IN_CALLING = "2";
    //挂断状态  21
    public static int CALLING_ENDING = 21;
    /**
     * 请求增援呼叫中
     */
    public static final String SUPPORT_IN_CALL = "1";
    /**
     * 请求增援请求通话中.....(貌似没有这个功能)
     */
    public static final String SUPPORT_IN_CALLING = "2";
    /**
     * 呼叫护工呼叫中
     */
    public static final String WORK_IN_CALL = "3";
    /**
     * 呼叫护工通话中
     */
    public static final String WORK_IN_CALLING = "4";
    /**
     * 护士呼叫中
     */
    public static final String NURSE_IN_CALL = "5";
    /**
     * 护士通话中
     */
    public static final String NURSE_IN_CALLING = "6";
    /**
     * 网管地址
     */
    public static String URL = "";
    /**
     * 端口号
     */
    public static final String URL_END = ":81";
    /**
     * 初始化数据
     */
    public static final String CALLINGBED_INIT = "/WDFJ-I/callingBed_init.aspx";
    /**
     * 获取系统的设置数据
     */
    public static final String GETSYS_SETING = "/WDFJ-I/callingBed_getmachinedata.aspx";
    /**
     * 检查APP更新版本
     */
    public static final String APP_CHECK_UPDATE = "/WDFJ-I/callingBed_APP_CheckUpdate.aspx";
    /**
     * 二维码
     */
    public static final String APP_BARCODE_IMG = "http://www.hnwdit.com/barcode.php";
    //主页数据
    public static final String CALLINGBED_MAIN = "/WDFJ-I/callingBed_main.aspx";
    //医嘱数据
    public static final String CALLINGBED_DOCTOR = "/WDFJ-I/callingBed_doctor.aspx";
    //花费查询数据
    public static final String CALLINGBED_COST = "/WDFJ-I/callingBed_cost.aspx";
    //通知栏数据
    public static final String CALLINGBED_NOTICE = "/WDFJ-I/callingBed_notice.aspx";
    //广播音量大小
    public static final String CALLINGMAINNURSE_BROADCAST_VOL = "/WDHS-I/callingMainNurse_BroadcastVol.aspx";


    //EventBus 所需判断类型
    public static final int EVENT_UDP = 0x01;

    public static final int EVENT_SIP = 0x02;

    public static final int EVENT_NOTICE = 0x03;

    public static final int EVENT_BROADCAST = 0x04;

    public static final int EVENT_SETSYS = 0x05;

    /**
     * 检测SIP，以太网ping状态
     */
    public static final int EVENT_SIP_INTERNETPING = 0x06;
    /**
     * 检测网络状态
     */
    public static final int EVENT_INTERNET_STATUS = 0x07;
    /**
     * 数据改变刷新本机
     */
    public static final int EVENT_MGR_PATIENTUPDATE = 0x08;
    /**
     * APP更新
     */
    public static final int EVENT_MGR_APP_UPDATE = 0x09;
    /**
     * Sip注册状态更新ui
     */
    public static final int EVENT_SIP_REGISTER_STATUS = 0x10;

    public static final int EVENT_MGR_RESET = 0x88;

    /**
     * 各种呼叫的机型
     * type :1门口机 2主机 3 子机 4请求增援 5卫生间 6子机转接优先等级依次升高
     */
    public static final String DOOR_CALL = "1";
    public static final String MAIN_CALL = "2";
    public static final String SON_CALL = "3";
    public static final String ROOMHELP_CALL = "4";
    public static final String WSHROOM_CALL = "5";
    public static final String MULTITAP_CALL = "6";

    public static String MYSELF_ID = "";//自己的机器ID
    public static String CALLMAIN_ID = "";//所属主机机器ID
    public static String DOOR_ID = "";//门口机机器ID

    /**
     * 白天还是晚上<br>
     * type :0 白天 1 晚上
     */
    public static String MORNING_NIGTH = "1";
    /**
     * 屏幕亮度百分比<br>
     */
    public static String SCREENLIGHT = "50";
    /**
     * 系统音量百分比<br>
     */
    public static String SYSVOICE = "100";
    /**
     * 铃声音量百分比<br>
     */
    public static String RINGLVOICE = "100";
    /**
     * 铃声循环次数<br>
     */
    public static String RINGLVOICELOOP = "1";
    /**
     * 护理灯亮度<br>
     */
    public static String NURSINGLIGHT = "100";
    /**
     * 语音呼叫超时时间<br>
     */
    public static String CALLINGTIMEOUT = "";
    /**
     * 熄屏时间<br>
     */
    public static String SCREENEXTINGUISHTIME = "";
    /**
     * 门口机通话音量<br>
     */
    public static String DOORCALLVOICE = "100";
    /**
     * 子机通话音量<br>
     */
    public static String BEDCALLVOICE = "100";


    /**
     * 以太网是否ping成功,sip UI同步刷新
     */
    public static final String ETHERNETSTATUS = "ethernetStatus";

    public static String TRUST_NEW_MAIN_ID = "";//托管主机的Id
    /**
     * 定时广播去重
     */
    public static String INTIME_BROADCAST = "";

    /**
     * ANR FC  ANR 奔溃 异常处理
     */
    public static Class ANR_FC = InitActivity.class;

    /**
     * 网络信息初始化错误连续出现5次，不再重启系统.
     */
    public static String NET_ERROR_FIVE_AFTER_TOAST = "fifthReboot";

}
