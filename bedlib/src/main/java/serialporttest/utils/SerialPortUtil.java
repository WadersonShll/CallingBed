package serialporttest.utils;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.SerialPort;
import serialporttest.constants.IConstant;

/**
 * Created by Administrator on 2017/6/6.
 */

public class SerialPortUtil {
    private String TAG = "SerialPortUtil";

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;

    private ISerialPortBedOnclickEvent onDataReceiveListener = null;
    public boolean isOpenSerialPortUtil = false;
    private static byte[] KeyValue = new byte[8];

    int DataIndex = 0;
    int DataValue = -1;

    public Thread receiveThread = null;

    public SerialPortUtil() {
    }

    /**
     * 打开串口的方法
     */
    public void openSerialPort() {
        Log.i(TAG, "打开串口");
        try {
            serialPort = new SerialPort(new File("/dev/" + IConstant.PORT), IConstant.BAUDRATE, 0);
            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            isOpenSerialPortUtil = true;
            receiveSerialPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收串口数据的方法
     */
    private byte[] buffer;
    private String data;

    /**
     * 请求设备维一序列号（设备 ID）
     */
    public static String KEY_ID = "";

    public void receiveSerialPort() {
        initKeyValue();
        Log.i(TAG, "接收串口数据");
        if (receiveThread != null)
            return;
        buffer = new byte[1024];
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (isOpenSerialPortUtil) {
                    try {
                        if (inputStream == null) {
                            return;
                        }
                        int size = inputStream.read(buffer);
                        if (size > 0 && isOpenSerialPortUtil) {
                            data = new String(buffer, 0, size);
                            if (data.contains("ID")) {
                                String str = data.substring(data.indexOf(",") + 1, data.indexOf("1#"));
                                int i = 0;
                                while (i < 10) {
                                    if (str.length() > 4) {
                                        break;
                                    } else {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        getKeyId();
                                        i++;
                                    }
                                }
                                Log.e(TAG, str);
                                KEY_ID = str;
                            } else {
                                if (data.contains("$") && data.contains("#")) {
                                    String str = data.substring(data.indexOf("$") + 1, data.indexOf("#"));
                                    DataIndex = Integer.parseInt(str.substring(3, 4));
                                    DataValue = Integer.parseInt(str.substring(5, 6));
                                    if (DataIndex < 8) {
                                        KeyValue[DataIndex] = (byte) DataValue;
                                    }
                                    if (null != onDataReceiveListener) {
                                        onDataReceiveListener.serialPortBedOnclick(KeyValue);
                                    }
                                    //======================================
                                    for (int i = 0; i < KeyValue.length; i++) {
                                        if (KeyValue[i] > 0) KeyValue[i] = -1;
                                    }
                                    //======================================
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //启动接收线程
        receiveThread.start();
    }

    /**
     * 关闭串口的方法
     * 关闭串口中的输入输出流
     * 然后将flag的值设为flag，终止接收数据线程
     */
    public void closeSerialPort() {
        Log.i(TAG, "关闭串口");
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
                receiveThread = null;
            }
            isOpenSerialPortUtil = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //初始化 key值
    public static void initKeyValue() {
        KeyValue[0] = -1;
        KeyValue[1] = -1;
        KeyValue[2] = -1;
        KeyValue[3] = -1;
        KeyValue[4] = -1;
        KeyValue[5] = -1;
        KeyValue[6] = -1;
        KeyValue[7] = -1;
    }


    /**
     * 心跳信号
     */
    public void startHeart() {
        send("$HEART,1F#");
    }

    /**
     * 关闭心跳<br>
     * 若MCU在10秒内没有收到信号，将自动重启Android.  随机数为“W”时将关闭心跳<br>
     */
    public void closeHeart() {
        send("$HEART,WE#");
    }

    /**
     * 系统重启
     */
    public void systemRestart() {
        ;
        send("$SYSRESET,2D#");
        Log.i(TAG, "系统重启发送");
    }

    /*
    * 进入系统ROM升级模式
    * */
    public void systemUpDate() {
        Log.i(TAG, "系统ROM升级模式串口数据");
        send("$SYSUPDATE,3C#");
        Log.i(TAG, "系统ROM升级模式发送");

    }


    /**
     * 发送串口数据的方法
     *
     * @param command 要发送的数据
     */
    private void send(String command) {
        try {
            if (isOpenSerialPortUtil) {
                byte[] sendData = command.getBytes();
                outputStream.write(sendData);
                Log.d("NURSELIGHT","==command=="+"护理灯串口数据发送成功");
                Log.i(TAG, "串口数据发送成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("NURSELIGHT","==command=="+"护理灯串口数据发送失败");
            Log.i(TAG, "串口数据发送失败");
        }
    }

    public void setOnDataReceiveListener(ISerialPortBedOnclickEvent dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

//    private void saveKeyValue() {
//        if (timer != null) {
//            timer.purge();
//        }
//        timer = new Timer();
//
//        if (timerTask != null) {
//            timerTask.cancel();
//        }
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                if (KeyValue[DataIndex] > 0) {
//                    initKeyValue();
//                }
//            }
//        };
//        timer.schedule(timerTask, 10, mTimeInterval);
//    }

    public interface ISerialPortBedOnclickEvent {
        void serialPortBedOnclick(byte[] buffer);
    }

    //------------------------below things was add by Waderson 20171106  -----------------------------------

    public static final String C_HEARD = "$";//开头符
    public static final String C_END = "#";//结束符
    public static final String C_SEPARATE = ",";//分隔符

    /**
     * 手柄MIC切换<br>
     */
    public static final String MIC = "MIC";
    /**
     * 床头灯的切换<br> 0 关闭 1打开 2闪烁
     */
    public static final String BEDLIGHT = "RELAY";
    /**
     * 卫生间呼叫灯控制<br>
     */
    public static final String ULED = "ULED";
    /**
     * 护理灯光控制<br>
     */
    public static final String NURSELIGHT = "NLED";
    /**
     * 当前 SIP 协议状态<br>
     */
    public static final String SIP_STATUS = "SIP";
    /**
     * 心跳控制<br>
     * 若MCU在10秒内没有收到信号，将自动重启Android.  随机数为“W”时将关闭心跳<br>
     * $ HEART ，1 E #  <br>
     */

    /**
     * 写入串口<br>
     * Waderson 20171103
     * command  命令 <br>
     * random   随机数<br>
     * check  校验符<br>
     */
    public void sendCommand(String command, String random, String check) {//$NLED3,beComeDoublF#
        String random_v = "1", check_v = "F";
        if (null == command || "".equals(command)) {// beComeDoubleStr(rr) + beComeDoubleStr(gg) + beComeDoubleStr(bb), "F"
            return;
        }
        if (null != random && !"".equals(random)) {
            random_v = random;
        }
        if (null != check && !"".equals(check)) {
            check_v = check;
        }
        Log.d("NURSELIGHT","=="+C_HEARD + command + C_SEPARATE + random_v + check_v + C_END);
        send(C_HEARD + command + C_SEPARATE + random_v + check_v + C_END);
    }

    /**
     * 请求设备维一序列号（设备 ID）
     */
    public void getKeyId() {
        send("$ID,11#");
    }


}
