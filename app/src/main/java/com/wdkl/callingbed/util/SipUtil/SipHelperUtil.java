package com.wdkl.callingbed.util.SipUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.WindowManager;

import com.vvsip.ansip.IVvsipService;
import com.vvsip.ansip.IVvsipServiceListener;
import com.vvsip.ansip.VvsipCall;
import com.vvsip.ansip.VvsipService;
import com.vvsip.ansip.VvsipServiceBinder;
import com.vvsip.ansip.VvsipTask;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.ui.CallingBedActivity;
import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus.EthernetWifiCallBackI;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.wdkl.callingbed.common.Constants.CALLING_ENDING;
import static com.wdkl.callingbed.common.Constants.EVENT_SIP_REGISTER_STATUS;
import static com.wdkl.callingbed.common.Constants.SIP_IP_END;

/**
 * Created by dengzhe on 2017/12/18.
 */
public class SipHelperUtil implements EthernetWifiCallBackI {


    /**
     * Sip启动注册.
     */
    protected int mSipRegisterTime = 3000;
    private Handler sipRegisterHandler = null;
    private Runnable sipRegisterRunnable = null;

    /**
     * SIP信息
     */
    public static final String SipInfoTag = "SipInfo";
    /**
     * 电话呼叫对象
     */
    private List<VvsipCall> mVvsipCalls = null;


    private static SipHelperUtil mSipRegisterUtil;

    public Handler getSipRegisterHandler() {
        return sipRegisterHandler;
    }

    public Runnable getSipRegisterRunnable() {
        return sipRegisterRunnable;
    }

    public List<VvsipCall> getmVvsipCalls() {
        return mVvsipCalls;
    }

    /**
     * Gets instance.
     *
     * @param context the context
     * @return the instance
     */
    private static Context contexts;

    public static SipHelperUtil getInstance(Context context) {
        if (mSipRegisterUtil == null) {
            synchronized (SipHelperUtil.class) {
                if (mSipRegisterUtil == null) {
                    mSipRegisterUtil = new SipHelperUtil();
                    contexts = context;
                }
            }
        }
        return mSipRegisterUtil;
    }

    /**
     * Instantiates a new Sip register util.
     */
    private SipHelperUtil() {
        setEthernetWifiCallBack(this);
        if (mVvsipCalls == null) {
            mVvsipCalls = new ArrayList<VvsipCall>();
        }

        // Runnable exiting the splash screen and launching the menu
        sipRegisterRunnable = new Runnable() {
            public void run() {
                isSuccessRegisterSip();
            }
        };

        // Run the exitRunnable in in mSipRegisterTime ms
        sipRegisterHandler = new Handler();

        IVvsipService sipservice = VvsipService.getService();
        if (sipservice != null) {
            sipRegisterHandler.postDelayed(sipRegisterRunnable, 0);
            return;
        }
        sipRegisterHandler.postDelayed(sipRegisterRunnable, mSipRegisterTime);


    }

    /**
     * 检测Sip服务是否注册成功
     */
    public void isSuccessRegisterSip() {
        VvsipTask vvsipTask = VvsipTask.getVvsipTask();
        if (vvsipTask != null && VvsipTask.global_failure != 0) {
            /**
             * ==================================sip服务启动失败 ================================
             */
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(contexts.getApplicationContext(), VvsipService.class);
            contexts.stopService(intent);
//            LogUtil.i(SipInfoTag, "注册失败:lifecycle // isSuccessStartSipService");
        } else {
//            finish();
            /**
             * ==================================sip服务启动成功 ================================
             */
//            Intent intent = new Intent();
//            intent.setClass(this, SipSuccessActivity.class);
//            startActivity(intent);
//            LogUtil.i(SipInfoTag, "sip服务启动:lifecycle // isSuccessStartSipService");

        }
    }

    /**
     * 注销Sip服务
     */
    public void unRegisterSip() {
//        LogUtil.i(SipInfoTag, "lifecycle // onDestroy");

        IVvsipService sipservice = VvsipService.getService();
        if (contexts instanceof IVvsipServiceListener && sipservice != null) {
            sipservice.removeListener((IVvsipServiceListener) contexts);
        }
        getSipServiceStartHandler().removeCallbacks(getSipServiceStartRunnable());
        sipRegisterHandler.removeCallbacks(sipRegisterRunnable);
        if (getSipServiceConnection() != null && isRegister) {
            try {
                contexts.unbindService(getSipServiceConnection());
                setSipServiceConnection(null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (mVvsipCalls != null) {
            mVvsipCalls.clear();
            mVvsipCalls = null;
        }

//        Log.i(SipInfoTag, "lifecycle // onDestroy");
    }

    private boolean isFirstRegister = true;
    public static String sipMessageCounts = "";

    /**
     * Sip信息获取
     */
    public void obtainSipInfo() {
        if (sipMessageCounts.equals(CallingBedActivity.REGISTERCOM) && ethernetS) {//sip注册成功并且以太网连上
            return;
        }
        IVvsipService sipService = VvsipService.getService();
        if (sipService != null) {
            sipService.addListener((IVvsipServiceListener) contexts);
            sipService.setMessageHandler(messageHandler);
        } else {
//            LogUtil.i(SipInfoTag, "lifecycle // _service==null");
        }
        sipRegister();
        failUiRefreshSip();
    }


    /**
     * Sip信息
     */
    private String sipinfo = "";
    private static int handleCount = 0;
    //Sip註冊次數
    private CountDownTimer mCountDownAutoTimer;
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            LogUtil.i("QASE", "handleMessage==" + " msg.obj==" + msg.obj.toString() + " msg.what==" + msg.what);
//            LogUtil.i(SipInfoTag, "#" + msg.obj);
            sipinfo = "" + msg.obj + sipinfo;
//            LogUtil.i(SipInfoTag, "Sip信息" + sipinfo);

            if (msg.what == 22) {//释放资源
                EventBus.getDefault().post(new MessageEvent(msg.what, EVENT_SIP_REGISTER_STATUS));
            }

            if (sipinfo.contains("200 OK")) {//注册成功
                sipMessageCounts = CallingBedActivity.REGISTERCOM;
                EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERCOM, EVENT_SIP_REGISTER_STATUS));
                if (mSipThread != null) {
                    mSipThread.interrupt();
                    mSipThread = null;
                }
                if (msg.obj.toString().contains("408")) {//超时
                    sipMessageCounts = CallingBedActivity.REGISTERFAIL;
                    EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERFAIL, EVENT_SIP_REGISTER_STATUS));
                    sipRegister();
                }
            } else {//注册失败
                sipMessageCounts = CallingBedActivity.REGISTERFAIL;
                EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERFAIL, EVENT_SIP_REGISTER_STATUS));
                if (mSipThread != null) {
                    mSipThread.interrupt();
                    mSipThread = null;
                }
                sipRegister();
            }
            failUiRefreshSip();

            if (msg.obj.toString().contains("autocall")) {
                VvsipCall pCall = null;
//                LogUtil.e(SipInfoTag, "onClick1");
                for (VvsipCall _pCall : mVvsipCalls) {
                    if (_pCall.cid > 0)
//                        LogUtil.e(SipInfoTag, "state#" + _pCall.mState);
                        if (_pCall.cid > 0 && _pCall.mState <= 2) {
                            pCall = _pCall;
                            break;
                        }
                }
//                LogUtil.e(SipInfoTag, "onClick2");
                if (pCall == null)
                    return;
//                LogUtil.e(SipInfoTag, "onClick3#" + pCall.mState);
                IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                VvsipTask _vvsipTask = _service.getVvsipTask();
                if (_vvsipTask == null)
                    return;
                pCall.stop();
                _service.setSpeakerModeOff();
            }
        }
    };

    /**
     * ====================Sip注册======================
     */
    private Thread mSipThread;

    private static class SipThread extends Thread {
        WeakReference<CallingBedActivity> mThreadCallingBedActivity;

        public SipThread(CallingBedActivity activity) {
            mThreadCallingBedActivity = new WeakReference<CallingBedActivity>(
                    activity);
        }

        @Override
        public void run() {
            super.run();
            if (mThreadCallingBedActivity == null)
                return;
            if (mThreadCallingBedActivity.get() != null) {
                IVvsipService sipService = VvsipService.getService();
                try {
                    if (sipService != null && !SipHelperUtil.getInstance(contexts).sipinfo.contains("200 OK") && ethernetS) {//注册正在进行中
                        sipMessageCounts = CallingBedActivity.REGISTERING;
                        EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERING, EVENT_SIP_REGISTER_STATUS));
                        sipService.register(Constants.SIP_IP + SIP_IP_END, Constants.SIP_ID, Constants.SIP_PASS_WORD);
//                    LogUtil.i(SipInfoTag, "Sip地址" + Constants.SIP_IP + SIP_IP_END + "\nSip账号" + Constants.SIP_ID + "\nSip密码" + Constants.SIP_PASS_WORD);
                        handleCount++;
                        LogUtil.e(SipInfoTag, "以太网连接，SIP UI状态刷新为注册中");
                    } else if (sipService != null && SipHelperUtil.getInstance(contexts).sipinfo.contains("200 OK")) {
                        sipMessageCounts = CallingBedActivity.REGISTERCOM;
                        EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERCOM, EVENT_SIP_REGISTER_STATUS));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sipRegister() {
        synchronized (this) {
            mSipThread = new SipThread((CallingBedActivity) contexts);
            if (handleCount < 3) {
                if (mCountDownAutoTimer == null) {
                    mCountDownAutoTimer = new CountDownTimer(10000, 1000) {
                        @Override
                        public void onTick(long l) {
                        }

                        @Override
                        public void onFinish() {
                            handleCount = 0;
                            if (mSipThread != null) {
                                mSipThread.start();
                            }
                            if (mCountDownAutoTimer != null) {
                                mCountDownAutoTimer.cancel();
                                mCountDownAutoTimer = null;
                            }
                        }
                    };
                    mCountDownAutoTimer.start();
                }
                return;
            } else {
                if (mCountDownAutoTimer != null) {
                    mCountDownAutoTimer.cancel();
                    mCountDownAutoTimer = null;
                }
            }
            if (handleCount == 0) {
                mSipThread.start();
            }
        }
    }

    /**
     * UI刷新：SIP失败
     */
    private void failUiRefreshSip() {
        if (!ethernetS) {
            sipMessageCounts = CallingBedActivity.REGISTERFAIL;
            EventBus.getDefault().post(new MessageEvent(CallingBedActivity.REGISTERFAIL, EVENT_SIP_REGISTER_STATUS));
            if (mSipThread != null) {
                mSipThread.interrupt();
                mSipThread = null;
            }
//            LogUtil.e(SipInfoTag, "以太网断开，SIP UI状态刷新为失败");
        }

    }

    public void setmSipThread(Thread mSipThread) {
        this.mSipThread = mSipThread;
    }

    public Thread getmSipThread() {
        return mSipThread;
    }

    /**
     * 开始通话
     */
    public void startCall(String sipUseName) {
        IVvsipService sipService = VvsipService.getService();
        if (sipService == null) return;
        //----------------------------------------------携带呼叫列表转接床头机的Mac地址--------------------------------------------------//
        sipService.initiateOutgoingCall(sipUseName, "");
    }

    /**
     * 结束通话
     */
    public void endCall() {
        VvsipCall call = null;
        for (VvsipCall pCall : mVvsipCalls) {
            if (pCall.cid > 0 && pCall.mState <= 2) {
                call = pCall;
                break;
            }
        }
        if (call == null) return;
        IVvsipService sipService = VvsipService.getService();
        if (sipService == null) return;
        VvsipTask sipTask = sipService.getVvsipTask();
        if (sipTask == null) return;
        VvsipService.getService().mainEndCall(CALLING_ENDING);
        call.stop();
        sipService.setSpeakerModeOff();
        sipService.stopPlayer();
        sipService.setAudioNormalMode();
    }

    /**
     * 添加一个电话呼叫对象
     *
     * @param call
     */
    public void addCallObject(final VvsipCall call) {
        ((Activity) contexts).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (call == null) {
                        return;
                    }

                    if (mVvsipCalls == null)
                        return;
                    mVvsipCalls.add(call);

                    if (Build.VERSION.SDK_INT >= 5) {
                        ((Activity) contexts).getWindow().addFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                // |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }

                } catch (Exception e) {
//                    LogUtil.e(SipInfoTag, "onNewVvsipCallEvent: " + e);
                }
            }
        });
    }

    /**
     * 移除一个电话呼叫对象
     *
     * @param call
     */
    public void removeCallObject(final VvsipCall call) {
        ((Activity) contexts).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (call == null) {
                        return;
                    }

                    // 4 crash detected here for 4.0.9 with mVvsipCalls=NULL
                    if (mVvsipCalls == null)
                        return;
                    mVvsipCalls.remove(call);

                    if (mVvsipCalls.size() == 0) {
                        if (Build.VERSION.SDK_INT >= 5) {
                            ((Activity) contexts).getWindow()
                                    .clearFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                            // |
                                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                } catch (Exception e) {
//                    Log.e(SipInfoTag, "onRemoveVvsipCallEvent: " + e);
                }
            }
        });
    }

    /**
     * 自动接电话
     */
    public void autoTalking() {
        if (mVvsipCalls == null) {
            mVvsipCalls = new ArrayList<VvsipCall>();
        }
        for (VvsipCall _pCall : mVvsipCalls) {
            if (_pCall.cid > 0 && _pCall.mState < 2 && _pCall.mIncomingCall) {
                // ANSWER EXISTING CALL
                int i = _pCall.answer(200, 1);
//                LogUtil.d(SipInfoTag, "ANSWER EXISTING CALL");
                IVvsipService _service = VvsipService.getService();
                if (_service != null) {
                    if (i >= 0) {
                        _service.stopPlayer();
                        _service.setSpeakerModeOff();
                        _service.setAudioInCallMode();
                    }
                }
                break;
            }
        }
    }

    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * #############################
     * <p>
     * Sip启动服务.
     * <p>
     * #############################
     */
    private static Handler sipServiceStartHandler = null;
    private static Runnable sipServiceStartRunnable = null;
    private static ServiceConnection sipServiceConnection;

    public static Runnable getSipServiceStartRunnable() {
        return sipServiceStartRunnable;
    }

    public static Handler getSipServiceStartHandler() {
        return sipServiceStartHandler;
    }

    public static ServiceConnection getSipServiceConnection() {
        return sipServiceConnection;
    }

    public static void setSipServiceConnection(ServiceConnection sipServiceConnections) {
        sipServiceConnection = sipServiceConnections;
    }

    /**
     * 启动服务
     */
    public static Boolean isRegister = false;//是否注册

    public void sipStartService() {
        sipServiceStartHandler = new Handler();

        sipServiceStartRunnable = new Runnable() {
            public void run() {

                Intent intent = new Intent(contexts.getApplicationContext(), VvsipService.class);
                contexts.startService(intent);

                sipServiceConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        IVvsipService sipservice = ((VvsipServiceBinder) service).getService();
                        if (contexts instanceof IVvsipServiceListener) {
                            sipservice.addListener((IVvsipServiceListener) contexts);
//                            LogUtil.i(SipInfoTag, "Connected!");
                            SipHelperUtil.getInstance(contexts).obtainSipInfo();//Sip信息获取
                        }
                    }

                    public void onServiceDisconnected(ComponentName name) {
//                        LogUtil.i(SipInfoTag, "Disconnected!");
                    }
                };
                if (!SipHelperUtil.isServiceRunning(contexts, "com.vvsip.ansip.VvsipService")) {
                    isRegister = contexts.bindService(intent, sipServiceConnection, Context.BIND_AUTO_CREATE);
                }
            }
        };

        sipServiceStartHandler.postDelayed(sipServiceStartRunnable, 0);
    }

    /**
     * 用来解析错误代码
     */
    public static String analyseErrorCode(String errorCode) {
        switch (errorCode) {
            case "200":
                return "成功";
            case "408":
                return "请求超时";
            case "400":
                return "服务器不支持请求的语法";
            case "401":
                return "未授权";
            case "403":
                return "服务器禁止请求";
            case "404":
                return "服务器找不到";
            default:
                return "未知错误";
        }
    }

    //======================Sip回调接口=====================
    private SipCallBackI mSipCallBackI;

    public void setSipCallBack(SipCallBackI mSipCallBackI) {
        this.mSipCallBackI = mSipCallBackI;
    }

    public SipCallBackI getmSipCallBackI() {
        return mSipCallBackI;
    }

    //======================以太网和wifi状态接口=====================
    private EthernetWifiCallBackI mEthernetWifiCallBackI;

    public void setEthernetWifiCallBack(EthernetWifiCallBackI mEthernetWifiCallBackI) {
        this.mEthernetWifiCallBackI = mEthernetWifiCallBackI;
    }

    public EthernetWifiCallBackI getmEthernetWifiCallBackI() {
        return mEthernetWifiCallBackI;
    }

    private static boolean ethernetS = false;//获取以太网状态
    private static boolean wifiS = false;//获取wifi状态

    @Override
    public boolean ethernetStatus(boolean status) {
        ethernetS = status;
        return status;
    }

    @Override
    public boolean wifiStatus(boolean status) {
        wifiS = status;
        return status;
    }
}
