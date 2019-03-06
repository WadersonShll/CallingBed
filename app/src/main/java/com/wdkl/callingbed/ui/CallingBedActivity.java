package com.wdkl.callingbed.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.vvsip.ansip.IVvsipServiceListener;
import com.vvsip.ansip.VvsipCall;
import com.wdkl.callingbed.MyApplication;
import com.wdkl.callingbed.R;
import com.wdkl.callingbed.adapter.CostAdapter;
import com.wdkl.callingbed.adapter.DoctorAdapter;
import com.wdkl.callingbed.base.BaseActivity;
import com.wdkl.callingbed.common.Constants;
import com.wdkl.callingbed.entity.BroadCastEntity;
import com.wdkl.callingbed.entity.CostDataEntity;
import com.wdkl.callingbed.entity.CostDataEntity.CostArray;
import com.wdkl.callingbed.entity.DoctorDataEntity;
import com.wdkl.callingbed.entity.DoctorDataEntity.DoctorChargeArray;
import com.wdkl.callingbed.entity.InitDataEntity;
import com.wdkl.callingbed.entity.MainDataEntity;
import com.wdkl.callingbed.entity.MessageEvent;
import com.wdkl.callingbed.entity.NoticeDataEntity;
import com.wdkl.callingbed.entity.NoticeEntity;
import com.wdkl.callingbed.entity.UdpEntity;
import com.wdkl.callingbed.receive.AlarmReceiver;
import com.wdkl.callingbed.receive.NetworkConnectChangedReceiver;
import com.wdkl.callingbed.util.BitmapUtils;
import com.wdkl.callingbed.util.DateUtil;
import com.wdkl.callingbed.util.DensityUtils;
import com.wdkl.callingbed.util.DownloadUtil;
import com.wdkl.callingbed.util.FullyLinearLayoutManager;
import com.wdkl.callingbed.util.LogUtil;
import com.wdkl.callingbed.util.MarqueeText;
import com.wdkl.callingbed.util.MediaPlayerManger;
import com.wdkl.callingbed.util.NetUtil;
import com.wdkl.callingbed.util.SharedPreferencesUtil;
import com.wdkl.callingbed.util.SipUtil.SipCallBackI;
import com.wdkl.callingbed.util.SipUtil.SipHelperUtil;
import com.wdkl.callingbed.util.StringUtils;
import com.wdkl.callingbed.util.TimeUtil;
import com.wdkl.callingbed.util.TwoDimensionCodeUtil;
import com.wdkl.callingbed.util.UdpSendUtil;
import com.wdkl.callingbed.util.VoiceManagerUtil;
import com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus.WifiBindSipStatusConnector;
import com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand;
import com.wdkl.callingbed.widget.loading.IMediaPlayerVolume;
import com.wdkl.callingbed.widget.view.MyTextView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import net.frakbot.jumpingbeans.JumpingBeans;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import okhttp3.Call;
import serialporttest.utils.SerialPortUtil;
import serialporttest.utils.SerialPortUtil.ISerialPortBedOnclickEvent;

import static com.wdkl.callingbed.MyApplication.mScreenExtinguishUtil;
import static com.wdkl.callingbed.common.Constants.EVENT_MGR_RESET;
import static com.wdkl.callingbed.common.Constants.EVENT_SIP_INTERNETPING;
import static com.wdkl.callingbed.common.Constants.EVENT_SIP_REGISTER_STATUS;
import static com.wdkl.callingbed.common.Constants.STANDBY;
import static com.wdkl.callingbed.common.Constants.innetSIPAddress;
import static com.wdkl.callingbed.util.ToastUtil.showToast;
import static com.wdkl.callingbed.util.ethernetwifiwithsipconnectstatus.WifiBindSipStatusConnector.ethernetStatus;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.closeHeart;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setBedLight;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setHandsMIC;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setNurseBrightness;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setSYSBrightness;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setSYSVoice;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setWSHLight;
import static com.wdkl.callingbed.util.sendcommand.CallingBedSendCommand.setWSHLightFlicker;

/**
 * Created by 胡博文 on 2017/8/17.
 */

public class CallingBedActivity extends BaseActivity implements ISerialPortBedOnclickEvent, IMediaPlayerVolume, IVvsipServiceListener
        , SipCallBackI, MediaPlayerManger.PlayMusicCompleteListener {

    //初始化数据实体
    private InitDataEntity initDataEntity;
    //主界面数据实体
    private MainDataEntity mainDataEntity;
    //医嘱界面的数据实体
    private DoctorDataEntity doctorDataEntity;
    private List<DoctorChargeArray> doctorChargeArrayList;
    private DoctorAdapter doctorAdapter;
    //花费界面的数据实体
    private CostDataEntity costDataEntity;
    private List<CostArray> costArrayList;
    private CostAdapter costAdapter;
    //通知界面的数据实体
    private NoticeDataEntity noticeDataEntity;
    private List<NoticeDataEntity.NoticeArray> noticeArrayList;
    //通话的状态
    private String CALL_STATUS = Constants.STANDBY;
    //串口工具类
    private SerialPortUtil serialPortUtil;

    /**
     * 注册中
     */
    public static final String REGISTERING = "register_ing";
    /**
     * 注册失败
     */
    public static final String REGISTERFAIL = "register_fail";
    /**
     * 注册完成
     */
    public static final String REGISTERCOM = "register_com";

    /**
     * 注册检测wifi强度广播
     */
    public BroadcastReceiver wifiInfoPowerReceiver;

    private static IMediaPlayerVolume iMediaPlayerVolume;

    /**
     * 通话中压低广播音量
     */
    public static void initPlayerVolume(float vol) {
        if (iMediaPlayerVolume != null)
            iMediaPlayerVolume.quietAndRestore(vol);
    }


    @Bind(R.id.activity_calling_bed_layout_rl_main)
    RelativeLayout rlMain;
    @Bind(R.id.activity_calling_bed_layout_rl_right)
    LinearLayout llRight;

    //状态栏
    @Bind(R.id.activity_calling_bed_layout_title)
    View vTitle;
    //状态栏中的信息
    @Bind(R.id.view_title_layout_tv_point)
    TextView tvSipStatePoint;
    @Bind(R.id.view_title_layout_tv_hospital_name)
    TextView tvHospitalName;
    @Bind(R.id.view_title_layout_tv_no)
    TextView tvExtensionNum;
    @Bind(R.id.view_title_layout_tv_time)
    TextView tvTime;
    @Bind(R.id.view_title_layout_iv_current_trust)
    ImageView ivTrust;
    @Bind(R.id.view_title_layout_iv_ethernet)
    ImageView ivEthernet;
    @Bind(R.id.view_title_layout_iv_wifi)
    ImageView ivWifi;

    //所有的页面view
    @Bind(R.id.activity_calling_bed_layout_main)
    View vMain;
    @Bind(R.id.activity_calling_bed_layout_doctor)
    RelativeLayout vDoctor;
    @Bind(R.id.activity_calling_bed_layout_cost)
    View vCost;
    @Bind(R.id.activity_calling_bed_layout_qr_code)
    View vQrCode;
    @Bind(R.id.activity_calling_bed_layout_support)
    View vSupport;
    @Bind(R.id.activity_calling_bed_layout_call)
    View vCall;
    @Bind(R.id.activity_calling_bed_layout_nurse)
    View vNurse;
    @Bind(R.id.activity_calling_bed_layout_welcome)
    View vWelcome;

    @Bind(R.id.view_qr_code_layout_qr_code)
    ImageView qrBarcodeImg;
    //侧滑栏的text
    @Bind(R.id.activity_calling_bed_layout_tv_doctor_text)
    TextView tvDoctor;

    @Bind(R.id.activity_calling_bed_layout_tv_version)
    TextView tvVersion;

    @Bind(R.id.activity_calling_bed_layout_tv_cost_text)
    TextView tvCost;
    @Bind(R.id.activity_calling_bed_layout_tv_qr_code_text)
    TextView tvQrCode;
    @Bind(R.id.activity_calling_bed_layout_tv_support_text)
    TextView tvSupport;
    @Bind(R.id.activity_calling_bed_layout_tv_call_text)
    TextView tvCall;
    @Bind(R.id.view_main_layout_tv_broadcasting)
    MarqueeText tvBroadcasting;


    /*主界面id*/
    //侧滑栏
    @Bind(R.id.view_main_layout_left_one_title)
    TextView tvLeftOneTitle;
    @Bind(R.id.view_main_layout_left_one_content)
    TextView tvLeftOneContent;
    @Bind(R.id.view_main_layout_left_two_title)
    TextView tvLeftTwoTitle;
    @Bind(R.id.view_main_layout_left_two_content)
    TextView tvLeftTwoContent;
    @Bind(R.id.view_main_layout_left_three_title)
    TextView tvLeftThreeTitle;
    @Bind(R.id.view_main_layout_left_three_content)
    TextView tvLeftThreeContent;
    @Bind(R.id.view_main_layout_left_four_title)
    TextView tvLeftFourTitle;
    @Bind(R.id.view_main_layout_left_four_content)
    TextView tvLeftFourContent;
    @Bind(R.id.view_main_layout_left_five_title)
    TextView tvLeftFiveTitle;
    @Bind(R.id.view_main_layout_left_five_content)
    TextView tvLeftFiveContent;

    @Bind(R.id.view_main_layout_tv_bed_num)
    TextView tvBedNum;
    @Bind(R.id.view_main_layout_tv_name)
    TextView tvName;
    @Bind(R.id.view_main_layout_tv_detail)
    TextView tvDetail;
    @Bind(R.id.view_main_layout_tv_illness)
    TextView tvIllness;
    @Bind(R.id.view_main_layout_tv_admission)
    TextView tvAdmission;
    @Bind(R.id.view_main_layout_tv_doctor_name)
    TextView tvDoctorName;
    @Bind(R.id.view_main_layout_tv_nurse_name)
    TextView tvNurseName;
    @Bind(R.id.view_main_layout_tv_hosp_number)
    TextView tvHospNumber;
    @Bind(R.id.view_main_layout_tv_hosp_data)
    TextView tvHospData;
    @Bind(R.id.view_main_layout_iv_doctor)
    ImageView ivDoctor;
    @Bind(R.id.view_main_layout_iv_nurse)
    ImageView ivNurse;

    @Bind(R.id.view_main_layout_img_two_dimensional_code)
    ImageView ivTwoDimensionalCode;

    //医嘱界面
    @Bind(R.id.view_doctor_tv_name)
    TextView tvDoctorPageName;
    @Bind(R.id.view_doctor_tv_inpatient_num)
    TextView tvDoctorPageInpatientNum;
    @Bind(R.id.view_doctor_layout_rv_doctor)
    RecyclerView rvDoctor;

    //花费界面
    @Bind(R.id.view_cost_layout_tv_name)
    TextView tvCostPageName;
    @Bind(R.id.view_cost_layout_tv_inpatient_num)
    TextView tvCostPageInpatientNum;
    @Bind(R.id.view_cost_layout_rv_cost)
    RecyclerView rvCost;

    //请求增援界面
    @Bind(R.id.view_support_layout_tv_call_text)
    TextView tvSupportText;

    //呼叫增援界面倒计时
    @Bind(R.id.view_support_layout_tv_call_timeout)
    TextView tvSupportTimeOut;


    //呼叫护工界面
    @Bind(R.id.view_call_layout_tv_call_text)
    TextView tvCallText;

    //呼叫护工界面倒计时
    @Bind(R.id.view_call_layout_tv_call_timeout)
    TextView tvCallTextTimeOut;


    //呼叫护士页面
    @Bind(R.id.view_nurse_layout_tv_call_text)
    TextView tvNurseText;

    //呼叫超时倒计时
    @Bind(R.id.view_nurse_layout_tv_call_timeout)
    TextView tvNurseTimeOut;


    private RelativeLayout rlBottomMessage;
    private ImageView ivWelcome;
    private MyTextView tvNoticeContent;
    // 语音播报
    private TextToSpeech textToSpeech;
    //开启的线程
    private TimeThread timeThread;
//    /**
//     * 外部物理键==（呼叫护士键）是否按住没松开
//     */
//    boolean isPressingNurseButton = false;

    /**
     * 侧边栏所有的View切换
     */
    private List<View> allViews;

    /**
     * 网络状态
     */
    private NetworkConnectChangedReceiver networkConnectChangedReceiver;

    //原设定：1分钟去检测网络：不通则互相循环切换以太网和wifi去ping网，
    // 发现检测状态虽及时更新到ui上显示，但ping网切换wifi和以太网会延时，需要等待一段时间
    // 现换成：30 秒发送一次检测网络切换信号
    private WifiBindSipStatusConnector wac;

    /**
     * 呼叫超时时间
     */
    private long callTimeOut = (StringUtils.parseInt(Constants.CALLINGTIMEOUT) == 0 ? 30000 : StringUtils.parseInt(Constants.CALLINGTIMEOUT) * 1000);//默认30秒


    /**
     * 时间线程
     */
    //MyHandler Flag值
    private final int TIME_WHAT = 1000;
    private final int INIT_SYSTEM = 2000;//成功
    private final int SYSTEM_DATA_RESPONSE = 2001;
    private final int SYSTEM_DATA_ERROR = 2002;
    private final int MAIN_DATA_RESPONSE = 2003;
    private final int MAIN_DATA_ERROR = 2004;
    private MyHandler handler = new MyHandler(this);

    class MyHandler extends Handler {
        // 弱引用 ，防止内存泄露
        private WeakReference<CallingBedActivity> weakReference;

        public MyHandler(CallingBedActivity callingBedActivity) {
            weakReference = new WeakReference<CallingBedActivity>(callingBedActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 通过  软引用  看能否得到activity示例
            CallingBedActivity handlerMemoryActivity = weakReference.get();
            // 防止内存泄露
            if (handlerMemoryActivity != null) {
                switch (msg.what) {
                    case TIME_WHAT:
                        //检测网络状态
                        checkNetWorkStatus();
                        //护士呼叫超时自动挂断
                        callTimeOut();
                        //请求增援超时自动挂断
                        callReinforceTimeOut();
                        //呼叫护工超时自动挂断
                        callNursingWorkersTimeOut();
                        //刷新时间
                        tvTime.setText(TimeUtil.getInstance().getDataTime());
                        //刷新通知
                        if (TimeUtil.getInstance().getSecond().equals("0")) {
                            getNoticeData();
                        }
                        break;
                    case INIT_SYSTEM:
                        LogUtil.d("NURSELIGHT", "INIT_SYSTEM");
                        setSYSParameter();
                        break;
                    case SYSTEM_DATA_RESPONSE:
                        LogUtil.d("NURSELIGHT", "SYSTEM_DATA_RESPONSE");
                        setSYSParameter();
                        break;
                    case SYSTEM_DATA_ERROR:
                        LogUtil.d("NURSELIGHT", "SYSTEM_DATA_ERROR");
                        getSystemSettingData();
                        break;
                    case MAIN_DATA_RESPONSE:
                        LogUtil.d("NURSELIGHT", "MAIN_DATA_RESPONSE");
                        setSYSParameter();
                        break;
                    case MAIN_DATA_ERROR:
                        LogUtil.d("NURSELIGHT", "MAIN_DATA_ERROR");
                        getMainData(1);
                        break;
                }
            } else {
                // 没有实例不进行操作
            }
        }
    }


    boolean netWorkRecovery = true;

    public void checkNetWorkStatus() {
        try {
            Constants.URL = "http://" + NetUtil.getLocalElement(NetUtil.getLocalInetAddress().toString());
        } catch (Exception e) {
            CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
            netWorkRecovery = false;
            e.printStackTrace();
        }
        ConnectivityManager cm = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    CallingBedSendCommand.setSipStatus(serialPortUtil, "2");
                    if (!netWorkRecovery) {
                        //网络好了刷新该机器数据
                        resetAPP();
                    }
                    netWorkRecovery = true;
                } else {
                    CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
                    netWorkRecovery = false;
                }
            } else {
                CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
                netWorkRecovery = false;
            }
        } else {
            CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
            netWorkRecovery = false;
        }
    }

    private boolean isTimeWorking = true;

    public class TimeThread extends Thread {
        @Override
        public void run() {
            while (isTimeWorking) {
                try {
                    Thread.sleep(1000);
                    Message msg = handler.obtainMessage();
                    msg.what = TIME_WHAT;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stop() {
        if (isTimeWorking) {
            if (timeThread != null && timeThread.isAlive()) {
                timeThread.interrupt();
                timeThread = null;
            }
            isTimeWorking = false;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化view
        allViews = new ArrayList<>(Arrays.asList(vMain, vDoctor, vCost, vQrCode, vSupport, vCall, vNurse));
        initReceive();
    }

    @Override
    public int getLayoutId() {
        //SIP准备工作
        initSip();
        return R.layout.activity_calling_bed_layout;
    }

    @Override
    protected void initView() {
        //更新include 的ui  bind 不到
        /*******************************************************/
        rlBottomMessage = vMain.findViewById(R.id.view_main_layout_rl_message);
        ivWelcome = vWelcome.findViewById(R.id.view_welcome_iv_main);
        tvNoticeContent = vMain.findViewById(R.id.view_main_layout_tv_msg);
        /*******************************************************/

        timeThread = new TimeThread();
        timeThread.start();

        appCheckUpdate();
    }

    @Override
    protected void initUtil() {
        serialPortUtil = ((MyApplication) this.getApplication()).serialPortUtil;
        serialPortUtil.setOnDataReceiveListener(this);
        iMediaPlayerVolume = this;
        //设置语言
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    }
                }
            }
        });
    }

    @Override
    protected void initData() {
        //以太网和wifi切换
        changeNetConnect();
        //上层实体类
        initDataEntity = (InitDataEntity) getIntent().getExtras().get(Constants.INITENTITY);
        if (null == initDataEntity) {
            return;
        }
        //对于rv 的初始化
        costArrayList = new ArrayList<>();
        costAdapter = new CostAdapter(costArrayList);
        rvCost.setLayoutManager(new FullyLinearLayoutManager(this));

        doctorChargeArrayList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(doctorChargeArrayList);
        rvDoctor.setLayoutManager(new FullyLinearLayoutManager(this));

        noticeArrayList = new ArrayList<>();

        getMainData(0);
        getSystemSettingData();
        getNoticeData();
        setTrustIcon();
        /**
         * 由于机器性能方面的差异有些机子的串口反应速度比较慢；急速初始化将失效！
         * */
        handler.sendEmptyMessageDelayed(INIT_SYSTEM, 3000);
    }

    /**
     * 初始化SIP
     */
    private void initSip() {
        //=============================================SIP启动服务===================================//
        SipHelperUtil.getInstance(this).sipStartService();
        //=============================================SIP状态回调===================================//
        SipHelperUtil.getInstance(this).setSipCallBack(this);
        //=============================================SIP服务监听===================================//
        sipServiceListener();
    }

    /**
     * 初始化广播
     */
    private void initReceive() {
        //网络状态广播
        networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkConnectChangedReceiver, intentFilter);
        //wifi强度广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        wifiInfoPowerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int s = NetUtil.obtainWifiInfo(MyApplication.getAppContext());
                LogUtil.d("sss", "wifi强度" + s);
            }
        };
        this.registerReceiver(wifiInfoPowerReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
    }


    /**
     * 以太网和wifi切换
     */
    private void changeNetConnect() {
        wac = new WifiBindSipStatusConnector(null);
        //循环切换网络状态
        wac.sendNetStatus(this);
        //主动连接wifi
        wac.setmHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 操作界面
                LogUtil.d("AutoConnectWifi", msg.obj + "");
                super.handleMessage(msg);
            }
        });
    }


    static String state_bk = "";

    /**
     * Sip状态灯显示
     *
     * @param state
     */
    private void updateStatus(final String state) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                switch (state) {
                    case REGISTERING:
                        tvSipStatePoint.setBackgroundResource(R.color.yellow_color);
                        //CallingBedSendCommand.setSipStatus(serialPortUtil, "1");
                        break;
                    case REGISTERFAIL:
                        tvSipStatePoint.setBackgroundResource(R.color.red_color);
                        //CallingBedSendCommand.setSipStatus(serialPortUtil, "0");
                        break;
                    case REGISTERCOM:
                        tvSipStatePoint.setBackgroundResource(R.color.green);
                        //CallingBedSendCommand.setSipStatus(serialPortUtil, "2");
                        //if (!state_bk.equals(REGISTERCOM)) {
                        //    upDateParameter();
                        //}
                        break;
                }
                state_bk = state;
            }
        });
    }

    @Override
    public View getLoadingTargetView() {
        return rlMain;
    }

    private void setTitle() {
        if (null != mainDataEntity) {
            tvHospitalName.setText(mainDataEntity.getHospital() + mainDataEntity.getDepartments());
        }
        if (null != initDataEntity) {

            String str = "000000";
            String str_m = str.substring(0, 6 - initDataEntity.getId().length()) + initDataEntity.getId();

            tvExtensionNum.setText("分机号：" + str_m);
        }
    }

//---------------------------------------------数据请求部分 ---------------------------------------------------------------------

    /**
     * 获得医嘱信息
     */
    private void getDoctorData() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        showProgress();
        OkHttpUtils
                .post()
                .url(Constants.URL + Constants.URL_END + Constants.CALLINGBED_DOCTOR)
                .addParams("deviceHumanId", Constants.DEVICE_HUMAN_ID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        hideProgress();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        String data = response.substring(0, response.length() - 4);
                        try {
                            Gson gson = new Gson();
                            doctorDataEntity = gson.fromJson(data, DoctorDataEntity.class);
                            setDoctorView();
                            hideProgress();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获得费用信息
     */
    private void getCostData() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        showProgress();
        OkHttpUtils
                .post()
                .url(Constants.URL + Constants.URL_END + Constants.CALLINGBED_COST)
                .addParams("deviceHumanId", Constants.DEVICE_HUMAN_ID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        hideProgress();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        String data = response.substring(0, response.length() - 4);
                        try {
                            Gson gson = new Gson();
                            costDataEntity = gson.fromJson(data, CostDataEntity.class);
                            setCostView();
                            hideProgress();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取系统设置信息
     */
    private void getSystemSettingData() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        try {
            OkHttpUtils.post().url(Constants.URL + Constants.URL_END + Constants.GETSYS_SETING).build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            handler.sendEmptyMessage(SYSTEM_DATA_ERROR);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            String data = response.substring(0, response.length() - 4);
                            LogUtil.d(CallingBedActivity.class, "getSystemSettingData==" + data);
                            try {
                                JSONObject object = new JSONObject(data);
                                if (object.getString("Code").equals("ERROR!")) {
                                    handler.sendEmptyMessage(SYSTEM_DATA_ERROR);
                                } else {
                                    Constants.MORNING_NIGTH = object.getString("dayOrNight");
                                    Constants.SCREENLIGHT = object.getString("screenLight");
                                    Constants.SYSVOICE = object.getString("callVoice");
                                    Constants.RINGLVOICE = object.getString("ringlVoice");
                                    Constants.RINGLVOICELOOP = object.getString("ringlVoiceLoop");
                                    Constants.NURSINGLIGHT = object.getString("nursingLight");
                                    Constants.CALLINGTIMEOUT = object.getString("CallingTimeOut");
                                    Constants.SCREENEXTINGUISHTIME = object.getString("screenExtinguishTime");
                                    Constants.DOORCALLVOICE = object.getString("DoorSipVol");
                                    Constants.BEDCALLVOICE = object.getString("BedSipVol");
                                    handler.sendEmptyMessage(SYSTEM_DATA_RESPONSE);
                                    setSYSParameter();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得通知信息
     */
    private void getNoticeData() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        try {
            OkHttpUtils
                    .post()
                    .url(Constants.URL + Constants.URL_END + Constants.CALLINGBED_NOTICE)
                    .addParams("deviceHumanId", Constants.DEVICE_HUMAN_ID)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            hideProgress();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            String data = response.substring(0, response.length() - 4);
                            try {
                                Gson gson = new Gson();
                                noticeDataEntity = gson.fromJson(data, NoticeDataEntity.class);
                                noticeArrayList = noticeDataEntity.getNoticeArray();
                                setNoticeView();

                                if (null != noticeArrayList) {
                                    setNoticeClock(noticeArrayList);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得主信息
     */
    private void getMainData(final int tag) {
        if (null == initDataEntity.getDeviceHumanId() || "".equals(initDataEntity.getDeviceHumanId())) {
            initDataEntity.setDeviceHumanId("0"); //HumanID 为空 则为0
        }
        LogUtil.d("getMainData", "Constants.URL==" + Constants.URL);
        if (!StringUtils.notEmpty(Constants.URL)) return;
        LogUtil.d("getMainData", "initDataEntity.getDeviceHumanId(22)==" + initDataEntity.getDeviceHumanId());
        if (Integer.parseInt(initDataEntity.getDeviceHumanId()) != 0) {
            if (0 == tag) showProgress();
            LogUtil.d("getMainData", "Constants.DEVICE_HUMAN_ID==" + Constants.DEVICE_HUMAN_ID);
            OkHttpUtils
                    .post()
                    .url(Constants.URL + Constants.URL_END + Constants.CALLINGBED_MAIN)
                    .addParams("deviceHumanId", Constants.DEVICE_HUMAN_ID)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            LogUtil.d("getMainData", "onError==onError");
                            if (0 == tag) hideProgress();
                            Constants.UPDATE_PATIENTUPDATE_FLAG = false;
                            handler.sendEmptyMessage(MAIN_DATA_ERROR);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            LogUtil.d("getMainData", "onResponse==onResponse");
                            String data = response.substring(0, response.length() - 4);
                            LogUtil.d("getMainData", "getMainData==data==" + data);
                            try {
                                Gson gson = new Gson();
                                mainDataEntity = gson.fromJson(data, MainDataEntity.class);
                                if (null != mainDataEntity) {
                                    handler.sendEmptyMessage(MAIN_DATA_RESPONSE);
                                    if (StringUtils.parseInt(initDataEntity.getDeviceHumanId()) != 0) {
                                        setMainView();
                                    }
                                } else {
                                    handler.sendEmptyMessage(MAIN_DATA_ERROR);
                                }
                                if (0 == tag) hideProgress();
                            } catch (Exception e) {
                                LogUtil.d("getMainData", "Exception==" + e.toString());
                                if (0 == tag) hideProgress();
                                e.printStackTrace();
                                handler.sendEmptyMessage(MAIN_DATA_ERROR);
                            }
                            Constants.UPDATE_PATIENTUPDATE_FLAG = false;
                        }
                    });
        } else {
            LogUtil.d("getMainData", "setWelcomeView==succeed==");
            setWelcomeView();
            dismissNurseBrightness();
        }
    }

    /**
     * 获取二维码图片
     */
    private void getQrCodeData() {

        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.erlogo);
        qrBarcodeImg.setImageBitmap(TwoDimensionCodeUtil.CreatePicture(Constants.TWO_DIMENSION_CODE_PATH + SerialPortUtil.KEY_ID, 200, 200, logoBitmap, true));

//        if (StringUtils.notEmpty(initDataEntity.getId())) {//现在没有搞这个请求了
//            OkHttpUtils.get().url(Constants.APP_BARCODE_IMG)
//                    .addParams("bedid", initDataEntity.getId()).build().execute(new BitmapCallback() {
//                @Override
//                public void onError(Call call, Exception e, int id) {
//                    LogUtil.d("getQrCodeData", "onError:" + e.getMessage());
//                }
//
//                @Override
//                public void onResponse(Bitmap response, int id) {
//                    LogUtil.d("getQrCodeData", "response:" + response.toString());
//                    qr_Barcode_IMG.setImageBitmap(response);
//                }
//            });
//        }
    }

    /**
     * 检查APP更新版本
     */
    private void appCheckUpdate() {
        if (!StringUtils.notEmpty(Constants.URL)) return;
        try {
            OkHttpUtils.post().url(Constants.URL + Constants.URL_END + Constants.APP_CHECK_UPDATE).build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Constants.UPDATE_APP_FLAG = false;
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            String data = response.substring(0, response.length() - 4);
                            LogUtil.d(CallingBedActivity.class, "appCheckUpdate==" + data);
                            try {
                                JSONObject object = new JSONObject(data);
                                if (object.getString("Code").equals("OK!")) {
                                    float APPVersion = StringUtils.parseFloat(StringUtils.deleteCharAt(object.getString("APPVersion"), 0));
                                    float APPVersion_Now = StringUtils.parseFloat(StringUtils.getAppVersionName(CallingBedActivity.this));
                                    String downloadURL = object.getString("downloadURL");
                                    if (APPVersion_Now != APPVersion) {//本来是“<”的；但有个别机器老是不升级成功
                                        if (StringUtils.notEmpty(downloadURL)) {
                                            closeHeart();//关闭心跳
                                            DownloadUtil.APP_VERSION = APPVersion;
                                            Intent intent = new Intent(CallingBedActivity.this, APPUpdateActivity.class);
                                            intent.putExtra("downLoadURL", Constants.URL + Constants.URL_END + "/" + downloadURL);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Constants.UPDATE_APP_FLAG = false;
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Constants.UPDATE_APP_FLAG = false;
        }

    }

    /**
     * 刷新各项参数<br></>
     */
    public void upDateParameter() {
        getMainData(0);
        getSystemSettingData();
        getNoticeData();
    }

//---------------------------------------------U I 更改部 （上）---------------------------------------------------------------------

    /**
     * 托管状态
     */
    private void setTrustIcon() {
        if (initDataEntity.getDeviceHostingID().contains("#")) {
            //隐藏未托管图标
            ivTrust.setImageResource(R.mipmap.ic_trust);
            isTrusting = true;
        } else if (!isTrusting) {
            //显示未托管图标
            ivTrust.setImageResource(R.mipmap.ic_calling_sickbed_bed_wrong_trusteeship);
        }
        try {
            tvVersion.setText("V" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新欢迎界面
     */
    private void setWelcomeView() {
        Picasso.with(context).load(Constants.URL + Constants.URL_END + "/WDFJ-I/WDFJ-I.png").fit().memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(ivWelcome);
        setTitle();
        vMain.setVisibility(View.GONE);
        vWelcome.setVisibility(View.VISIBLE);
    }

    /**
     * 更新主界面UI
     */
    private void setMainView() {
        if (null != mainDataEntity) {
            //关闭欢迎界面
            vMain.setVisibility(View.VISIBLE);
            vWelcome.setVisibility(View.GONE);

            // Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.erlogo);
            //ivTwoDimensionalCode.setImageBitmap(TwoDimensionCodeUtil.CreatePicture(Constants.TWO_DIMENSION_CODE_PATH + SerialPortUtil.KEY_ID, 200, 200, logoBitmap, true));

            tvLeftOneTitle.setText(mainDataEntity.getLeftOneTitle());
            tvLeftOneContent.setText(mainDataEntity.getLeftOneContent());

            tvLeftTwoTitle.setText(mainDataEntity.getLeftTwoTitle());
            tvLeftTwoContent.setText(mainDataEntity.getLeftTwoContent());

            tvLeftThreeTitle.setText(mainDataEntity.getLeftThreeTitle());
            tvLeftThreeContent.setText(mainDataEntity.getLeftThreeContent());

            tvLeftFourTitle.setText(mainDataEntity.getLeftFourTitle());
            tvLeftFourContent.setText(mainDataEntity.getLeftFourContent());

            tvLeftFiveTitle.setText(mainDataEntity.getLeftFiveTitle());
            tvLeftFiveContent.setText(mainDataEntity.getLeftFiveContent());

            String name = mainDataEntity.getName();
            if (StringUtils.notEmpty(name)) {
                if (name.length() >= 4) {
                    tvName.setTextSize(69);
                    tvName.setText(name);
                } else {
                    tvName.setTextSize(95);
                    tvName.setText(name);
                }
            } else {
                tvName.setText("");
            }

            tvDetail.setText("[ " + mainDataEntity.getSex() + " ]");//"【"+mainDataEntity.getSex()+"】"

            tvIllness.setText(mainDataEntity.getAgeNum() + " 岁");

            tvHospNumber.setText("入院:" + mainDataEntity.getInpatientNum());

            tvHospData.setText(mainDataEntity.getAdmissionTime());

            tvDoctorName.setText(mainDataEntity.getResponsDoctor());

            tvNurseName.setText(mainDataEntity.getResponsNurse());

            if (null != initDataEntity) {
                tvBedNum.setText(initDataEntity.getDeviceBedNum());
            }

            Picasso.with(context).load(Constants.URL + Constants.URL_END + "/" + mainDataEntity.getResponsDoctorPic()).transform(new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    Bitmap bitmap = BitmapUtils.zoom(source, DensityUtils.dp2px(context, 62), DensityUtils.dp2px(context, 62));
                    //圆形处理
                    bitmap = BitmapUtils.circleBitmap(bitmap);
                    //回收bitmap资源
                    source.recycle();
                    return bitmap;
                }

                @Override
                public String key() {
                    return "";//需要保证返回值不能为null。否则报错
                }
            }).fit().memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(ivDoctor);

            Picasso.with(context).load(Constants.URL + Constants.URL_END + "/" + mainDataEntity.getResponsNursePic()).transform(new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    Bitmap bitmap = BitmapUtils.zoom(source, DensityUtils.dp2px(context, 62), DensityUtils.dp2px(context, 62));
                    //圆形处理
                    bitmap = BitmapUtils.circleBitmap(bitmap);
                    //回收bitmap资源
                    source.recycle();
                    return bitmap;
                }

                @Override
                public String key() {
                    return "";//需要保证返回值不能为null。否则报错
                }
            }).fit().memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(ivNurse);

            setTitle();
        }
    }

    /**
     * 设置通知页面
     */
    private void setNoticeView() {
        if (null != noticeArrayList) {
            String strNotice = "";
            for (int i = 0; i < noticeArrayList.size(); i++) {
                strNotice += "      【" + (i + 1) + "】 " + noticeArrayList.get(i).getNoticeContent() + "  " + noticeArrayList.get(i).getNoticeEndTime();
            }
            tvNoticeContent.setText(strNotice);
        }
    }

    /**
     * 设置医嘱界面
     */
    private void setDoctorView() {
        if (null != doctorDataEntity) {
            if (null != doctorDataEntity.getDoctorChargeArray()) {
                doctorChargeArrayList = doctorDataEntity.getDoctorChargeArray();
            }
            doctorAdapter.upDateList(doctorChargeArrayList);
            rvDoctor.setAdapter(doctorAdapter);

            tvDoctorPageName.setText("姓名：  " + doctorDataEntity.getName());
            tvDoctorPageInpatientNum.setText("住院号：  " + doctorDataEntity.getInpatientNum());
        }
    }

    /**
     * 设置费用界面
     */
    private void setCostView() {
        if (null != costDataEntity) {
            if (null != costDataEntity.getCostArray()) {
                costArrayList = costDataEntity.getCostArray();
            }
            costAdapter.upDateList(costArrayList);
            rvCost.setAdapter(costAdapter);

            tvCostPageName.setText("姓名：  " + costDataEntity.getName());
            tvCostPageInpatientNum.setText("住院号：  " + costDataEntity.getInpatientNum());
        }
    }


//---------------------------------------------U I更改部分（下）---------------------------------------------------------------------

    /**
     * 设置闹钟
     */
    int alarmId = 0;

    private void setNoticeClock(List<NoticeDataEntity.NoticeArray> list) {
        for (int i = 0; i < list.size(); i++) {
            if (DateUtil.judgeCurrTime(noticeArrayList.get(i).getNoticeStartTime())) {
                openAlarm(list.get(i).getNoticeStartTime(), i, 0);
            }
            if (DateUtil.judgeCurrTime(noticeArrayList.get(i).getNoticeEndTime())) {
                openAlarm(list.get(i).getNoticeEndTime(), i, 1);
            }
        }
    }

    /**
     * 开启闹钟
     */
    private void openAlarm(String time, int position, int playFlag) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("position", position);
        intent.putExtra("playFlag", playFlag);

        alarmId = alarmId + 1;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent, 0);
        AlarmManager alarmManager;
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Date curDate = new Date(time);//获取当前时间
        long triggerAtMillis = curDate.getTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    /**
     * 按任意键是否关闭广播
     */
    private boolean isCloseBroadcast = false;

    /**
     * 病人需要休息按任意键关闭广播
     */
    private boolean anyKeyCloseBroadcast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //=====================停止廣播==================//
                MediaPlayerManger.getInstance().stopMediaPlayer();
                tvBroadcasting.setText("");
                tvBroadcasting.setVisibility(View.INVISIBLE);
                isCloseBroadcast = true;
            }
        });
        return isCloseBroadcast;
    }

    //---------------------------------------------串口点击事件监听----------------------------------------------------------
    @Override
    public void serialPortBedOnclick(byte[] buffer) {
        LogUtil.e("serialPortBedOnclick", "buffer[0]:" + buffer[0] + "buffer[1]:" + buffer[1] + "buffer[2]:" + buffer[2] + "buffer[3]:" + buffer[3] + "buffer[4]:"
                + buffer[4] + "buffer[5]:" + buffer[5]);
        if (buffer[7] == 1) { //卫生间短按松开
            CallingBedActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    LogUtil.d(CallingBedActivity.class, "卫生间短按松开");
                    UdpSendUtil.sendCall2(initDataEntity);
                    if (isNurseStatus) {
                        if (flickerStatus) {
                            flickerStatus = setWSHLightFlicker(serialPortUtil, false);
                        } else {
                            flickerStatus = setWSHLightFlicker(serialPortUtil, true);
                        }
                    } else {
                        flickerStatus = setWSHLightFlicker(serialPortUtil, true);
                    }
                }
            });
        }
        if (buffer[7] == 2) {//卫生间长按松开
            CallingBedActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    LogUtil.d(CallingBedActivity.class, "卫生间长按松开");
                    UdpSendUtil.sendCall2(initDataEntity);
                    if (isNurseStatus) {
                        if (flickerStatus) {
                            flickerStatus = setWSHLightFlicker(serialPortUtil, false);
                        } else {
                            flickerStatus = setWSHLightFlicker(serialPortUtil, true);
                        }
                    } else {
                        flickerStatus = setWSHLightFlicker(serialPortUtil, true);
                    }
                }
            });
        }

        //病人需要休息按任意键关闭广播
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 1 || buffer[i] == 2) {
                if (anyKeyCloseBroadcast()) {
                    isCloseBroadcast = false;
                    break;
                }
            }
        }
        if (null != vWelcome) {
            //只有在欢迎界面没有显示的时候串口才能用
            if (vWelcome.getVisibility() == View.GONE) {
                //医嘱键短按松开
                if (buffer[0] == 1) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVDoctor();
                        }
                    });
                }
                //医嘱键长按松开
                if (buffer[0] == 2) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVDoctor();
                            //sendMultitapNurse(initDataEntity, mainDataEntity);
                        }
                    });
                }
                //费用查询键短按松开
                if (buffer[1] == 1) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVCost();
                        }
                    });
                }
                //费用查询键长按松开
                if (buffer[1] == 2) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVCost();
                        }
                    });
                }
                //二维码键短按松开
                if (buffer[2] == 1) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVQrCode();
                        }
                    });
                }
                //二维码键长按松开
                if (buffer[2] == 2) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVQrCode();
                        }
                    });
                }
                if (buffer[3] == 1) { //请求增援键短按松开
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVSupport();
                        }
                    });
                }
                if (buffer[3] == 2) {//请求增援键长按松开
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVSupport();
                        }
                    });
                }
                //呼叫护工键短按松开
                if (buffer[4] == 1) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVCall();
                        }
                    });
                }
                //呼叫护工键长按松开
                if (buffer[4] == 2) {
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            clickVCall();
                        }
                    });
                }
                if (buffer[5] == 0) { //呼叫护士键按住不动
                    LogUtil.d("WWW", "呼叫护士键==0");
                } else if (buffer[5] == 1) {//呼叫护士键短按松开
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            LogUtil.d("WWW", "呼叫护士键==1");
                            clickVNurse();
                            //呼叫超时倒计时开启
                            callTypeCallNurse = "1";
                            localMillsCallNurse = System.currentTimeMillis();
                            tvNurseTimeOut.setText("");

                            handsMICStatus = setHandsMIC(serialPortUtil, false);//点击了外部按键就将手柄MIC关掉
                        }
                    });
                } else if (buffer[5] == 2) {//呼叫护士键长按松开
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            LogUtil.d("WWW", "呼叫护士键==2");
                            if (isNurseStatus) {
                                UdpSendUtil.sendCall1Transfer(initDataEntity, mainDataEntity);
                            } else {
                                clickVNurse();

                                //呼叫超时倒计时开启
                                callTypeCallNurse = "1";
                                localMillsCallNurse = System.currentTimeMillis();
                                tvNurseTimeOut.setText("");

                                handsMICStatus = setHandsMIC(serialPortUtil, false);//点击了外部按键就将手柄MIC关掉
                            }
                        }
                    });
                } else if (buffer[5] == -1) {//按键常态
                    LogUtil.d("WWW", "呼叫护士键==-1");
                }
                //------------------------below things was add by Waderson 20171106  -----------------------------------
                if (buffer[6] == 1) { //手柄短按松开
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            LogUtil.d("WWW", "呼叫护士键==-2");
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            if (CALL_STATUS.equals(Constants.STANDBY)) {//待机状态
                                handsMICStatus = setHandsMIC(serialPortUtil, true);
                            } else {
                                handsMICStatus = setHandsMIC(serialPortUtil, false);
                            }
                            clickVNurse();

                            //呼叫超时倒计时开启
                            callTypeCallNurse = "2";
                            localMillsCallNurse = System.currentTimeMillis();
                            tvNurseTimeOut.setText("");


                        }
                    });
                }
                if (buffer[6] == 2) {//手柄长按松开
                    LogUtil.d(CallingBedActivity.class, "手柄长按松开");
                    CallingBedActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            LogUtil.d("WWW", "呼叫护士键==-3");
                            mScreenExtinguishUtil.touchScreen();//===============================息屏
                            if (bedLightStatus) {
                                bedLightStatus = setBedLight(serialPortUtil, false, 0);
                                // showToast("床头灯关闭");
                            } else {
                                bedLightStatus = setBedLight(serialPortUtil, true, 0);
                                // showToast("床头灯点亮");
                            }
                        }
                    });
                }
            } else {
                mScreenExtinguishUtil.touchScreen();//===============================息屏
            }
        }
    }

    //---------------------------------------------串口点击事件监听----------------------------------------------------------

    private long localMillsCallNurse = 0;//呼叫护士拨打的时刻
    private String callTypeCallNurse = "";//1:外部物理按键呼叫；2：手柄按钮呼叫

    /**
     * @param sel 1:外部物理按键呼叫；2：手柄按钮呼叫
     *            呼叫护士超时
     */
    private void autoHangUpCallNurse(String sel) {
        mScreenExtinguishUtil.touchScreen();//===============================息屏
        clickVNurse();
        if (sel.equals("1")) {
            handsMICStatus = setHandsMIC(serialPortUtil, false);
        } else {
            if (CALL_STATUS.equals(Constants.STANDBY)) {//待机状态
                handsMICStatus = setHandsMIC(serialPortUtil, true);
            } else {
                handsMICStatus = setHandsMIC(serialPortUtil, false);
            }
        }
    }

    //---------------------------------------------EventBus发送事件监听-------------------------------------------------------
    /**
     * 托管状态
     */
    private boolean isTrusting = false;
    //去重
    private boolean isReset = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case Constants.EVENT_UDP:
                UdpEntity udpEntity = (UdpEntity) messageEvent.getMessage();
                switch (udpEntity.getIndexes()) {
                    case "call_8_upremove"://主机通知对应的子机改变自己的UI
                        if (Constants.MYSELF_ID.equals(udpEntity.getHeadMachineID())) {
                            LogUtil.d(CallingBedActivity.class, "主机上滑Remove掉了。。。");
                            if (!udpEntity.getType().equals(Constants.WSHROOM_CALL)) {//排除卫生间；卫生间是不能呼叫的
                                if (CALL_STATUS != STANDBY) {
                                    CALL_STATUS = STANDBY;
                                    showThisView(vMain);
                                }
                            } else {
                                flickerStatus = setWSHLightFlicker(serialPortUtil, false);
                            }
                        }
                        break;
                    case "call_8_transfer"://转接护士主机电话
                        LogUtil.d("call_8_transfer", "Constants.MYSELF_ID=="
                                + Constants.MYSELF_ID + "\nudpEntity.getBedNumber()==" + udpEntity.getBedNumber()
                                + "\nudpEntity.getHeadMachineID()==" + udpEntity.getHeadMachineID());
                        if (Constants.MYSELF_ID.equals(udpEntity.getBedNumber()) && !Constants.MYSELF_ID.equals(udpEntity.getHeadMachineID())) {
                            //开始连接拨打电话。
                            //开始转接 20171219
                            //=============================================SIP拨打电话===================================//
                            SipHelperUtil.getInstance(this).getmSipCallBackI().startCall(udpEntity.getSipAddress());
                            LogUtil.d("call_8_transfer", "Constants.MYSELF_ID  "
                                    + Constants.MYSELF_ID
                                    + "\nudpEntity.getHeadMachineID()  " + udpEntity.getHeadMachineID() + "\nudpEntity.getSipAddress()  "
                                    + udpEntity.getSipAddress() + innetSIPAddress);
                        }
                        break;
                    case "nursing_1": //进入护理
                        isNurseStatus = true;
                        break;
                    case "nursing_2": //退出护理
                        flickerStatus = setWSHLightFlicker(serialPortUtil, false);
                        isNurseStatus = false;
                        //发送UDP通知护士主机，护理已完成
                        UdpSendUtil.sendCall1b1(initDataEntity, mainDataEntity);
                        break;
                }
                break;
            case Constants.EVENT_NOTICE:
                if (((NoticeEntity) messageEvent.getMessage()).getAlarmIndex() != -1) {
                    if (((NoticeEntity) messageEvent.getMessage()).isAlarmState()) {
                        textToSpeech.speak(noticeArrayList.get(((NoticeEntity) messageEvent.getMessage()).getAlarmIndex()).getNoticeContent(), TextToSpeech.QUEUE_FLUSH, null);
                        setNoticeView();
                    }
                    if (!((NoticeEntity) messageEvent.getMessage()).isAlarmState()) {
                        noticeArrayList.remove(((NoticeEntity) messageEvent.getMessage()).getAlarmIndex());
                        setNoticeView();
                    }
                }
                break;
            case Constants.EVENT_SETSYS://系统设置参数
                setSYSParameter();
                break;
            case Constants.EVENT_MGR_PATIENTUPDATE://数据改变刷新本机
                upDateParameter();
                break;
            case Constants.EVENT_MGR_APP_UPDATE://APP更新
                appCheckUpdate();
                break;
            case Constants.EVENT_BROADCAST:
                BroadCastEntity broadCastEntity = (BroadCastEntity) messageEvent.getMessage();
                switch (broadCastEntity.getIndexes()) {
                    case "broadcast_1":
                        boolean isPlay = false;
                        switch (Integer.parseInt(broadCastEntity.getZoneId())) {
                            case 0:
                                if (initDataEntity.getDeviceZone0().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                            case 1:
                                if (initDataEntity.getDeviceZone1().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                            case 2:
                                if (initDataEntity.getDeviceZone2().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                            case 3:
                                if (initDataEntity.getDeviceZone3().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                            case 4:
                                if (initDataEntity.getDeviceZone4().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                            case 5:
                                if (initDataEntity.getDeviceZone5().equals("1")) {
                                    isPlay = true;
                                }
                                break;
                        }
                        if (isPlay) {
                            try {
                                //=====================停止廣播===================//
                                MediaPlayerManger.getInstance().stopMediaPlayer();
                                //=====================播放廣播===================//
                                MediaPlayerManger.getInstance()
                                        .playMusic(Constants.URL + Constants.URL_END + "/"
                                                + broadCastEntity.getPath(), this, MediaPlayerManger.PLAY);
                                if (!SharedPreferencesUtil.getStringSp(this, "SetUpBroadcastVoice", "voice").equals("")) {
                                    float voice = Float.parseFloat(SharedPreferencesUtil.getStringSp(this, "SetUpBroadcastVoice", "voice"));
                                    if (mVolume == 1.0f) {
                                        MediaPlayerManger.getInstance().setVolume(voice);
                                    } else {
                                        MediaPlayerManger.getInstance().setVolume(0);
                                    }
                                }
                                if (!broadCastEntity.getVoiceInt().equals("0")) {
                                    float voice = Float.parseFloat(broadCastEntity.getVoiceInt()) / 100;
                                    if (mVolume == 1.0f) {
                                        MediaPlayerManger.getInstance().setVolume(voice);
                                    } else {
                                        MediaPlayerManger.getInstance().setVolume(0);
                                    }
                                }
                                tvBroadcasting.setVisibility(View.VISIBLE);
                                tvBroadcasting.setText("======广播中======");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "broadcast_2":
                        try {
                            MediaPlayerManger.getInstance().stopMediaPlayer();
                            tvBroadcasting.setText("");
                            tvBroadcasting.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "broadcast_v":
                        try {
                            float voice = Float.parseFloat(broadCastEntity.getVoiceInt()) / 100;
                            if (mVolume == 1.0f) {
                                MediaPlayerManger.getInstance().setVolume(voice);
                            } else {
                                MediaPlayerManger.getInstance().setVolume(0);
                            }
                            SharedPreferencesUtil.putStringSp(this, "SetUpBroadcastVoice", "voice", String.valueOf(voice));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            case EVENT_SIP_INTERNETPING://循环更新以太网ping信息和sip信息
                SipHelperUtil.getInstance(this).obtainSipInfo();//获取sip信息
                if (ethernetStatus) {
                    ivEthernet.setImageResource(R.mipmap.ic_etherneting);
                    ivWifi.setImageResource(R.mipmap.ic_wifi_nor);
                } else {
                    ivEthernet.setImageResource(R.mipmap.ic_etherneted);
                    ivWifi.setImageResource(R.mipmap.ic_wifi_nor);
                }
                break;
            case EVENT_SIP_REGISTER_STATUS://刷新SIP注册状态
                if (messageEvent.getMessage() instanceof String) {
                    String status = (String) messageEvent.getMessage();
                    updateStatus(status);
                } else if (messageEvent.getMessage() instanceof Integer) {// 22：Sip释放资源 断电重置
//                    CallingBedActivity.initPlayerVolum(1.0f);
//                    CALL_STATUS = Constants.STANDBY;
//                    showThisView(vMain);
                }
                break;
            case EVENT_MGR_RESET:
                resetAPP();
                break;
//            case EVENT_INTERNET_STATUS://刷新网络状态==================================暂时注掉（排除以太网开关硬件问题）===============================
//                switch ((int) messageEvent.getMessage()) {
//                    case NetUtil.NETWORK_ETHERNET://本地：打开  Wifi: 关闭
//                        ivEthernet.setImageResource(R.mipmap.ic_etherneting);
//                        ivWifi.setImageResource(R.mipmap.ic_wifi_nor);
//
//                        break;
//                    case NetUtil.NETWORK_WIFI://本地：关闭  Wifi: 打开
//                        ivEthernet.setImageResource(R.mipmap.ic_etherneted);
//                        ivWifi.setImageResource(R.mipmap.ic_wifi_high);
//                        break;
//                    case NetUtil.NETWORK_NONE://本地：关闭  Wifi: 关闭
//                        ivEthernet.setImageResource(R.mipmap.ic_etherneted);
//                        ivWifi.setImageResource(R.mipmap.ic_wifi_nor);
//                        break;
//                }
//                SipHelperUtil.getInstance(this).obtainSipInfo();//获取sip信息
//                break;
        }
    }

    /**
     * 重启APP
     */
    public void resetAPP() {
        if (!isReset) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isReset = true;
                    Intent mStartActivity = new Intent(context, InitActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, mPendingIntent);
                    System.exit(0);
                }
            }, 2000);
        }
    }


    //---------------------------------------------EventBus发送事件监听----------------------------------------------------------
    //---------------------------------------------设置硬件参数部分（上）-----------------------------------------------------------------
    boolean handsMICStatus = false;//是否是手柄MIC
    boolean flickerStatus = false;//卫生间灯是否在闪烁
    boolean lightStatus = false;//卫生间灯是否开启
    boolean isNurseStatus = false;//护士是否进入了护理
    boolean bedLightStatus = false;//床头灯是否打开.

    /**
     * 设置系统的各项参数，根据后台UDP的指令改变机器的状态（声音大小；亮度；。。。）<br></>
     */
    private void setSYSParameter() {
        if (StringUtils.notEmpty(Constants.MORNING_NIGTH)) {//初始化卫生间按键灯
            flickerStatus = setWSHLightFlicker(serialPortUtil, flickerStatus);
        }
        if (StringUtils.notEmpty(Constants.MORNING_NIGTH) && Constants.MORNING_NIGTH.equals("1")) {//晚上
            lightStatus = setWSHLight(serialPortUtil, true);
        } else {
            lightStatus = setWSHLight(serialPortUtil, false);
        }
        if (StringUtils.isInt(Constants.SCREENLIGHT)) {
            setSYSBrightness(CallingBedActivity.this, StringUtils.parseInt(Constants.SCREENLIGHT));
        }
        if (StringUtils.isInt(Constants.SYSVOICE)) {
            setSYSVoice(CallingBedActivity.this, StringUtils.parseInt(Constants.SYSVOICE));
        }
        if (StringUtils.isInt(Constants.NURSINGLIGHT) && null != mainDataEntity) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtil.d("NURSELIGHT", "设置护理灯" + Constants.NURSINGLIGHT + "---" + mainDataEntity.getLeftOneColor() + "---" + mainDataEntity.getLeftTwoColor() + "---" + mainDataEntity.getLeftThreeColor() + "---" + mainDataEntity.getLeftFourColor() + "---" + mainDataEntity.getLeftFiveColor());
                        setNurseBrightness(serialPortUtil, StringUtils.parseInt(Constants.NURSINGLIGHT), mainDataEntity.getLeftOneColor(), mainDataEntity.getLeftTwoColor(),
                                mainDataEntity.getLeftThreeColor(), mainDataEntity.getLeftFourColor(), mainDataEntity.getLeftFiveColor());
                    } catch (InterruptedException e) {
                        LogUtil.d("NURSELIGHT", "设置护理灯" + "InterruptedException");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        VoiceManagerUtil.setCallVoice(CallingBedActivity.this, Integer.parseInt(Constants.BEDCALLVOICE));
        //刷新呼叫超时时间
        callTimeOut = (StringUtils.parseInt(Constants.CALLINGTIMEOUT) == 0 ? 30000 : StringUtils.parseInt(Constants.CALLINGTIMEOUT) * 1000);
    }

    //---------------------------------------------设置硬件参数部分（下）-----------------------------------------------------------------
    //---------------------------------------------串口点击事件部分（上）---------------------------------------------------------------------
    public void clickVDoctor() {
        if (isVisible(vDoctor)) {
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                if (isVisible(llRight)) {
                    showThisView(vDoctor);
                    changeTextColor(tvDoctor);
                    goneLlRightView();
                    getDoctorData();
                } else {
                    showLlRightView();
                }
            } else {
                if (!isVisible(vSupport) && !isVisible(vCall) && !isVisible(vNurse)) {//排除呼叫状态
                    showThisView(vDoctor);
                    getDoctorData();
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }

    public void clickVCost() {
        if (isVisible(vCost)) {
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                if (isVisible(llRight)) {
                    showThisView(vCost);
                    changeTextColor(tvCost);
                    goneLlRightView();
                    getCostData();
                } else {
                    showLlRightView();
                }
            } else {
                if (!isVisible(vSupport) && !isVisible(vCall) && !isVisible(vNurse)) {//排除呼叫状态
                    showThisView(vCost);
                    getCostData();
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }

    public void clickVQrCode() {
        if (isVisible(vQrCode)) {
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                if (isVisible(llRight)) {
                    showThisView(vQrCode);
                    changeTextColor(tvQrCode);
                    goneLlRightView();
                    getQrCodeData();
                } else {
                    showLlRightView();
                }
            } else {
                if (!isVisible(vSupport) && !isVisible(vCall) && !isVisible(vNurse)) {//排除呼叫状态
                    showThisView(vQrCode);
                    getQrCodeData();
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }

    JumpingBeans jbVSupport;

    public void clickVSupport() {
        if (isVisible(vSupport)) {
            callTypeReinforce = "";
            CALL_STATUS = STANDBY;
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                if (isVisible(llRight)) {
                    setReinforce();
                    showThisView(vSupport);
                    changeTextColor(tvSupport);
                    goneLlRightView();
                    UdpSendUtil.sendCall4(initDataEntity, mainDataEntity);
                    CALL_STATUS = Constants.SUPPORT_IN_CALL;
                    if (null != initDataEntity) {
                        jbVSupport = setJumpingBeans(tvSupportText, initDataEntity.getDeviceBedNum() + "床正在请求增援...");
                    }
                } else {
                    showLlRightView();
                }
            } else {
                if (!isVisible(vCall) && !isVisible(vNurse)) {//排除呼叫状态
                    setReinforce();
                    showThisView(vSupport);
                    UdpSendUtil.sendCall4(initDataEntity, mainDataEntity);
                    CALL_STATUS = Constants.SUPPORT_IN_CALL;
                    if (null != initDataEntity) {
                        jbVSupport = setJumpingBeans(tvSupportText, initDataEntity.getDeviceBedNum() + "床正在请求增援...");
                    }
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }

    JumpingBeans jbVCall;

    public void clickVCall() {
        if (isVisible(vCall)) {
            callTypeNursingWorkers = "";
            CALL_STATUS = STANDBY;
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                if (isVisible(llRight)) {
                    setNursingWorkers();
                    showThisView(vCall);
                    changeTextColor(tvCall);
                    goneLlRightView();
                    //sendProtectHelp();//呼叫护工还没有做；这个先预留
                    CALL_STATUS = Constants.WORK_IN_CALL;
                    if (null != initDataEntity) {
                        jbVCall = setJumpingBeans(tvCallText, initDataEntity.getDeviceBedNum() + "床正在呼叫护工...");
                    }
                } else {
                    showLlRightView();
                }
            } else {
                if (!isVisible(vSupport) && !isVisible(vNurse)) {//排除呼叫状态
                    setNursingWorkers();
                    showThisView(vCall);
                    //sendProtectHelp();//呼叫护工还没有做；这个先预留
                    CALL_STATUS = Constants.WORK_IN_CALL;
                    if (null != initDataEntity) {
                        jbVCall = setJumpingBeans(tvCallText, initDataEntity.getDeviceBedNum() + "床正在呼叫护工...");
                    }
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }

    JumpingBeans jbVNurse;


    public void clickVNurse() {

        if (isVisible(vNurse)) {
            SipHelperUtil.getInstance(CallingBedActivity.this).getmSipCallBackI().endCall();
            CALL_STATUS = STANDBY;
            showThisView(vMain);
        } else {
            if (isVisible(vMain)) {
                showThisView(vNurse);
                UdpSendUtil.sendCall1(initDataEntity, mainDataEntity);
                CALL_STATUS = Constants.NURSE_IN_CALL;
                if (null != initDataEntity) {
                    jbVNurse = setJumpingBeans(tvNurseText, initDataEntity.getDeviceBedNum() + "床已呼叫，等待护士接听中...");
                }

            } else {
                if (!isVisible(vSupport) && !isVisible(vCall)) {//排除呼叫状态
                    showThisView(vNurse);
                    UdpSendUtil.sendCall1(initDataEntity, mainDataEntity);
                    CALL_STATUS = Constants.NURSE_IN_CALL;
                    if (null != initDataEntity) {
                        jbVNurse = setJumpingBeans(tvNurseText, initDataEntity.getDeviceBedNum() + "床已呼叫，等待护士接听中...");
                    }
                } else {
                    showToast("请先取消呼叫！");
                }
            }
        }
    }
    //---------------------------------------------串口点击事件部分（下）---------------------------------------------------------------------
    //---------------------------------------------UI更改操作部分（上）---------------------------------------------------------------------

    /**
     * 显示侧边栏
     */
    protected void showLlRightView() {
        changeTextColor(null);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_righttoleft);
        llRight.setAnimation(animation);
        llRight.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏侧边栏
     */
    protected void goneLlRightView() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_lefttoright);
        llRight.setAnimation(animation);
        llRight.setVisibility(View.GONE);
    }

    /**
     * 需要显示的VIEW
     */
    protected void showThisView(View v) {
        //---------------------------------------------修复主动打床头机ui不变化-----------------------------------------------//
        if (v.getVisibility() == View.VISIBLE) return;
        if (!StringUtils.listNotEmpty(allViews)) {
            allViews = new ArrayList<>(Arrays.asList(vMain, vDoctor, vCost, vQrCode, vSupport, vCall, vNurse));
        }
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_righttoleft);
        Animation closeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out_righttoleft);
        for (View av : allViews) {
            if (v == av) {
                av.setAnimation(animation);
                av.setVisibility(View.VISIBLE);
            } else {
                if (av.getVisibility() == View.VISIBLE) {
                    av.setAnimation(closeAnimation);
                    av.setVisibility(View.GONE);
                }
            }
        }
    }

    protected boolean isVisible(View v) {
        return v.getVisibility() == View.VISIBLE;
    }

    /**
     * 设置跳跃字体
     */
    public JumpingBeans setJumpingBeans(TextView textview, String textStr) {
        textview.setText(textStr);
        JumpingBeans jumpingBeans = JumpingBeans.with(textview)
                .makeTextJump(0, textview.getText().length())
                .setIsWave(true)
                .setLoopDuration(2000)
                .build();
        return jumpingBeans;
    }

    /**
     * 侧滑栏的颜色变高亮
     */
    private void changeTextColor(TextView textView) {
        tvDoctor.setTextColor(ContextCompat.getColor(context, R.color.white));
        tvCost.setTextColor(ContextCompat.getColor(context, R.color.white));
        tvQrCode.setTextColor(ContextCompat.getColor(context, R.color.white));
        tvSupport.setTextColor(ContextCompat.getColor(context, R.color.white));
        tvCall.setTextColor(ContextCompat.getColor(context, R.color.white));
        if (null != textView)
            textView.setTextColor(ContextCompat.getColor(context, R.color.yellow_color));
    }

    //---------------------------------------------UI更改操作部分（下）---------------------------------------------------------------------

    /**
     * 灭掉护理灯
     */
    public void dismissNurseBrightness() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setNurseBrightness(serialPortUtil, 1, "000000", "000000", "000000", "000000", "000000");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //=======================释放广播=====================//
        MediaPlayerManger.getInstance().releaseMediaPlayer();

        if (textToSpeech != null) {
            textToSpeech.shutdown();//关闭tts引擎
        }
        unregisterReceiver(wifiInfoPowerReceiver);
        unregisterReceiver(networkConnectChangedReceiver);

        serialPortUtil.closeSerialPort();
        timeThread.interrupted();
        //关闭线程
        stop();
        //=============================================SIP关闭服务===================================//
        SipHelperUtil.getInstance(this).unRegisterSip();
    }

    /**
     * --------------------------------sip 的回调操作------------------------------------------
     */
    @Override
    public void startCall(String sipAddress) {//
        //=============================================SIP开始通话===================================//
        SipHelperUtil.getInstance(CallingBedActivity.this).startCall(sipAddress);
    }

    @Override
    public void endCall() {
        //=============================================SIP结束通话===================================//
        SipHelperUtil.getInstance(CallingBedActivity.this).endCall();
        //发送udp让门口机text变白色
        UdpSendUtil.sendCallNoticeDoor(initDataEntity, mainDataEntity, "0");
    }

    @Override
    public void autoTalking() {
        //=============================================SIP自动接听===================================//
        SipHelperUtil.getInstance(CallingBedActivity.this).autoTalking();
    }

    @Override
    public void onNewVvsipCallEvent(VvsipCall call) {
        LogUtil.d("QQWW", "onNewVvsipCallEvent----");
        SipHelperUtil.getInstance(this).addCallObject(call);
    }

    @Override
    public void onRemoveVvsipCallEvent(VvsipCall call) {
        LogUtil.d("QQWW", "onRemoveVvsipCallEvent----call.mState：==" + call.mState);
        SipHelperUtil.getInstance(this).removeCallObject(call);
    }

    @Override
    public void onStatusVvsipCallEvent(VvsipCall call) {
        LogUtil.d("QQWW", "onStatusVvsipCallEvent----call.mState：==" + call.mState + call.isIncomingCall());
        switch (call.mState) {
            case 0://正在呼叫中:子机自动接通 isIncomingCall() = true  mState = 0  //正在通话中：isIncomingCall() = true
                CALL_STATUS = Constants.IN_CALL;
                //=============================================SIP自动接听===================================//
                SipHelperUtil.getInstance(CallingBedActivity.this).getmSipCallBackI().autoTalking();

                //正在通话中：isIncomingCall() = true
                CallingBedActivity.initPlayerVolume(0.0f);

                CALL_STATUS = Constants.IN_CALLING;
                showThisView(vNurse);
                jbVNurse = setJumpingBeans(tvNurseText, "正在通话中...");
                jbVNurse.stopJumping();
                tvNurseText.setText("正在通话中...");
                tvNurseTimeOut.setText("");
                LogUtil.d("自动挂掉", "正在通话中");
                //发送udp让门口机text变绿色
                UdpSendUtil.sendCallNoticeDoor(initDataEntity, mainDataEntity, "2");
                break;
            case 1:

                break;
            case 2://正在通话中：isIncomingCall() = true  mState = 2

                break;
            case 3://无人应答/挂断：isIncomingCall() = true  mState = 3
                CallingBedActivity.initPlayerVolume(1.0f);
                CALL_STATUS = Constants.STANDBY;
                showThisView(vMain);
                //发送udp让门口机text变白色
                UdpSendUtil.sendCallNoticeDoor(initDataEntity, mainDataEntity, "0");
                break;
        }
    }

    @Override
    public void onRegistrationEvent(int rid, String remote_uri, final int code, String reason) {
        LogUtil.d("QQWW", "onRegistrationEvent----code" + code);
//        LogUtil.d(SipHelperUtil.SipInfoTag, "onRegistrationEvent："
//                + " \nrid" + rid + " \nremote_uri" + remote_uri + " \ncode" + code + " \nreason" + reason);
        CallingBedActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (code >= 200 && code < 300) {
                    // Remove the exitRunnable callback from the handler queue
                    SipHelperUtil.getInstance(CallingBedActivity.this)
                            .getSipRegisterHandler()
                            .removeCallbacks(SipHelperUtil.getInstance(CallingBedActivity.this).getSipRegisterRunnable());
                    // Run the exit code manually
                    SipHelperUtil.getInstance(CallingBedActivity.this)
                            .isSuccessRegisterSip();
                }
            }
        });

    }

    /**
     * Sip服务监听
     */
    private void sipServiceListener() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SipHelperUtil.getInstance(CallingBedActivity.this).obtainSipInfo();
            }
        }, 0);
    }

    @Override
    public void playMusicComplete(int position) {
        tvBroadcasting.setText("");
        tvBroadcasting.setVisibility(View.INVISIBLE);
        //播放广播恢复默认状态
        Constants.INTIME_BROADCAST = "";


    }

    private float mVolume = 1.0f;

    @Override
    public void quietAndRestore(float volume) {
        mVolume = volume;
        if (volume == 0.0f) {
            MediaPlayerManger.getInstance().setVolume(volume);
        } else {
            if (!SharedPreferencesUtil.getStringSp(this, "SetUpBroadcastVoice", "voice").equals("")) {
                float voice = Float.parseFloat(SharedPreferencesUtil.getStringSp(this, "SetUpBroadcastVoice", "voice"));
                MediaPlayerManger.getInstance().setVolume(voice);
            }
        }
    }

    /**
     * 呼叫超时自动挂断
     */
    private void callTimeOut() {
        if (!CALL_STATUS.equals(Constants.STANDBY) && !callTypeCallNurse.equals("")) {//当前不在待机状态并且之前有呼叫护士的操作
            if ((TimeUtil.getInstance().getMills() - localMillsCallNurse) >= callTimeOut  //N秒拨打超时自动挂断
                    && localMillsCallNurse != 0 && !CALL_STATUS.equals(Constants.IN_CALLING)) {//当前不在正在通话中
                localMillsCallNurse = 0;
                callTypeCallNurse = "";//复位
                autoHangUpCallNurse(callTypeCallNurse);
                tvNurseTimeOut.setText("");
//                //发送udp让门口机text变白色
//                UdpSendUtil.sendCallNoticeDoor(initDataEntity, mainDataEntity, "0");
            } else if ((TimeUtil.getInstance().getMills() - localMillsCallNurse) < callTimeOut
                    && localMillsCallNurse != 0 && !CALL_STATUS.equals(Constants.IN_CALLING)) {//N秒之前拨打倒计时
//                            LogUtil.d("呼叫倒计时:", Constants.CALLINGTIMEOUT);
                tvNurseTimeOut.setText((callTimeOut / 1000 - (TimeUtil.getInstance().getMills() - localMillsCallNurse) / 1000) + "秒");
            }
        }
    }

    /**
     * ===============================呼叫请求增援超时自动挂断===============================
     */
    private long localMillsReinforce = 0;//请求增援拨打的时刻
    private String callTypeReinforce = "";//1:呼叫发起；"":呼叫默认值

    private void callReinforceTimeOut() {
        if (callTypeReinforce.equals("1")) {//当前有呼叫请求增援的操作
            if ((TimeUtil.getInstance().getMills() - localMillsReinforce) >= callTimeOut  //N秒拨打超时自动挂断
                    && localMillsReinforce != 0) {
                localMillsReinforce = 0;
                callTypeReinforce = "";//复位
                autoHangUpReinforce();
                tvSupportTimeOut.setText("");
            } else if ((TimeUtil.getInstance().getMills() - localMillsReinforce) < callTimeOut
                    && localMillsReinforce != 0) {//N秒之前拨打倒计时
                tvSupportTimeOut.setText((callTimeOut / 1000 - (TimeUtil.getInstance().getMills() - localMillsReinforce) / 1000) + "秒");
            }
        }
    }

    /**
     * 呼叫请求增援开启倒计时
     */
    private void setReinforce() {
        localMillsReinforce = System.currentTimeMillis();
        callTypeReinforce = "1";
        tvSupportTimeOut.setText("");

    }

    /**
     * 呼叫请求增援超时复位
     */
    private void autoHangUpReinforce() {
        mScreenExtinguishUtil.touchScreen();//===============================息屏
        clickVSupport();
    }


    /**
     * ===============================呼叫护工超时自动挂断===============================
     */
    private long localMillsNursingWorkers = 0;//拨打护工的时刻
    private String callTypeNursingWorkers = "";//1:呼叫发起；"":呼叫默认值

    private void callNursingWorkersTimeOut() {
        if (callTypeNursingWorkers.equals("1")) {//当前有呼叫护工的操作
            if ((TimeUtil.getInstance().getMills() - localMillsNursingWorkers) >= callTimeOut  //N秒拨打超时自动挂断
                    && localMillsNursingWorkers != 0) {
                localMillsNursingWorkers = 0;
                callTypeNursingWorkers = "";//复位
                autoHangUpNursingWorkers();
                tvCallTextTimeOut.setText("");
            } else if ((TimeUtil.getInstance().getMills() - localMillsNursingWorkers) < callTimeOut
                    && localMillsNursingWorkers != 0) {//N秒之前拨打倒计时
                tvCallTextTimeOut.setText((callTimeOut / 1000 - (TimeUtil.getInstance().getMills() - localMillsNursingWorkers) / 1000) + "秒");
            }
        }
    }

    /**
     * 呼叫护工开启倒计时
     */
    private void setNursingWorkers() {
        localMillsNursingWorkers = System.currentTimeMillis();
        callTypeNursingWorkers = "1";
        tvCallTextTimeOut.setText("");

    }

    /**
     * 呼叫护工超时复位
     */
    private void autoHangUpNursingWorkers() {
        mScreenExtinguishUtil.touchScreen();//===============================息屏
        clickVCall();
    }

}
