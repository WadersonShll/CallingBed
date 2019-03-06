/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.ansip;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vvsip.amdemo.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 主服务类，负责UI与底层接口的沟通
 */
public class VvsipService extends Service implements IVvsipService {

	private String mTag = "VvsipService";
	private static final int VVSIP_DEBUG_LEVEL = 6;
	private static VvsipService mVvsipService;

	public static final String VVSIP_ALARM_WAKEUP = "com.viewsip.ansip_ALARM_WAKEUP";
	private VvsipAlarmBroadcastReceiver vvsipAlarmBroadcastReceiver = null;
	private NetworkBroadcastReceiver networkBroadcastReceiver = null;
	private VvsipServiceBinder mBinder;
	private SharedPreferences mConfiguration;

	private AudioManager mAManager;
	private Vibrator mVibrator;
	private MediaPlayer mPlayer_ringback;
	private MediaPlayer mPlayer_ringtone;

	private VvsipTask mVvsipTask;
	private VvsipAccount mAccount;
	private List<IVvsipServiceListener> mListeners = null;
	private List<VvsipCall> mVvsipCalls = null;
	private int mMaxConcurentCall = 1;
	private int registration_id = 0;

	private ExecutorService mExecutorService;

	private ExecutorService mExecutorService1;

	private static Handler mainActivityMessageHandler;

	List<VvsipNeighbor> neighborsList;

	public void addListener(IVvsipServiceListener listener) {
		synchronized (mListeners) {
			Log.i(mTag, "mListeners -> addListener");
			mListeners.add(listener);

			for (VvsipCall _pCall : mVvsipCalls) {
				listener.onNewVvsipCallEvent(_pCall);
			}
			Log.i(mTag, "mListeners <- addListener");
		}
	}

	public void removeListener(IVvsipServiceListener listener) {
		if (mListeners != null) {
			synchronized (mListeners) {
				Log.i(mTag, "mListeners -> removeListener");
				mListeners.remove(listener);
				Log.i(mTag, "mListeners <- removeListener");
			}
		}
	}

	@Override
	public void onCreate() {
		Log.i(mTag, "lifecycle // onCreate");
		neighborsList = new ArrayList<VvsipNeighbor>();
		// res.xml.pref***:为默认配置项，需修改时可以从这些文件中修改配置。
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_sip_account, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_sip_optional, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_media_audio, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_media_video, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_media_advanced_audio, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.pref_media_advanced_video, true);
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.advanced_pref, true);

		mConfiguration = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			new Thread(new Runnable() {
				public void run() {
					try {
						boolean checkEncoder = mConfiguration.getBoolean(
								"key_videocodec_h264_hw_encoder", false);
						boolean checkDecoder = mConfiguration.getBoolean(
								"key_videocodec_h264_hw_decoder", false);
						Log.i(mTag, "key_videocodec_h264_hw_encoder"
								+ checkEncoder);
						Log.i(mTag, "key_videocodec_h264_hw_decoder"
								+ checkEncoder);
						VvsipMediaCodecFinder.ListAllMediaCodec("video/avc",
								CodecProfileLevel.AVCProfileBaseline,
								checkEncoder, checkDecoder);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		AudioCompatibility.configureAudioManagerMode(this
				.getApplicationContext());
		AudioCompatibility.configureAudioManagerStreamType();
		AudioCompatibility.configureAudioRecordAudioSource();
		AudioInput.mAudiomanager_audio_source = AudioCompatibility.mAudiomanager_audio_source_internal;

		try {
			createFileFromRessource("holdmusic.wav", "/rings", R.raw.holdmusic);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			mPlayer_ringback = MediaPlayer.create(this.getApplicationContext(),
					R.raw.ringback);
		} catch (Exception e) {
			e.printStackTrace();
			mPlayer_ringback = null;
		}
		if (mPlayer_ringback == null) {
			try {
				mPlayer_ringback = MediaPlayer.create(
						this.getApplicationContext(),
						Settings.System.DEFAULT_RINGTONE_URI);
			} catch (Exception e) {
				e.printStackTrace();
				mPlayer_ringback = null;
			}
		}

		try {
			mPlayer_ringtone = new MediaPlayer();
		} catch (Exception e) {
			e.printStackTrace();
			mPlayer_ringtone = null;
		}

		try {
			if (mPlayer_ringtone != null) {
				mPlayer_ringtone.setAudioStreamType(AudioManager.STREAM_RING);
				Uri ringerUri = RingtoneManager.getActualDefaultRingtoneUri(
						this.getApplicationContext(),
						RingtoneManager.TYPE_RINGTONE);
				mPlayer_ringtone.setDataSource(this.getApplicationContext(),
						ringerUri);
				// mPlayer_ringtone.setDataSource(this.getApplicationContext(),
				// Settings.System.DEFAULT_RINGTONE_URI);
				mPlayer_ringtone.prepare();
			}
		} catch (Exception e) {
			e.printStackTrace();
			mPlayer_ringtone = null;
		}

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		try {
			Method hasVibrator = mVibrator.getClass().getMethod("hasVibrator");
			Object result = hasVibrator.invoke(mVibrator);
			if (result.equals(Boolean.FALSE)) {
				mVibrator = null;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mListeners = Collections
				.synchronizedList(new ArrayList<IVvsipServiceListener>());
		mAManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));

		if (mVvsipCalls == null) {
			mVvsipCalls = new ArrayList<VvsipCall>();
		}

		mAccount = new VvsipAccount();

		mAccount.setDomain(mConfiguration.getString("key_domain", null));
		mAccount.setUserid(mConfiguration.getString("key_username", null));
		mAccount.setPasswd(mConfiguration.getString("key_password", null));
		mAccount.setIdentity(mConfiguration.getString("key_identity", null));
		mAccount.setOutboundProxy(mConfiguration.getString("key_outboundproxy",
				null));
		mAccount.setTransport(mConfiguration.getString("key_protocol", "udp"));
		String exp = mConfiguration.getString("key_interval", null);
		if (exp != null) {
			try {
				mAccount.setRegisterInterval(Integer.valueOf(exp));
			} catch (Exception e) {
				mAccount.setRegisterInterval(Integer.valueOf(600));
			}
		}

		mAccount.setEchoCancellation(mConfiguration.getBoolean(
				"key_echocancellation", false));

		mAccount.isDefined();

		mBinder = new VvsipServiceBinder(this);

		mVvsipTask = VvsipTask.getVvsipTask();

		if (VvsipTask.global_failure != 0)
			return;

		mVvsipService = this;

		mExecutorService = Executors.newSingleThreadExecutor();

		mExecutorService1 = Executors.newSingleThreadExecutor();

		mExecutorService.execute(new Runnable() {
			public void run() {
				Log.i(mTag, "job(start) initVvsipLayer");
				Looper.prepare();
				Log.i(mTag, "job(end) initVvsipLayer");
			}
		});
		mExecutorService.execute(new Runnable() {
			public void run() {
				Log.i(mTag, "job(start) initVvsipLayer");
				initVvsipLayer();
				Log.i(mTag, "job(end) initVvsipLayer");
			}
		});

		mExecutorService.execute(new Runnable() {
			public void run() {
				Log.i(mTag, "job(start) setupBroadcastReceiver");
				setupBroadcastReceiver();
				Log.i(mTag, "job(end) setupBroadcastReceiver");
			}
		});

	}

	public void restartNetworkDetection() {
		unregisterReceiver(networkBroadcastReceiver);
		networkBroadcastReceiver.previous_network = null;
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkBroadcastReceiver, intentFilter);
	}

	public void createFileFromRessource(String _mResFile, String dir, int id) {
		String appPath = getApplication().getApplicationContext().getFilesDir()
				.getAbsolutePath();
		appPath = appPath + dir;
		File _mFile = new File(appPath, _mResFile);
		try {
			File _mDir = new File(appPath);
			if (!_mDir.exists()) {
				if (_mDir.mkdir()) {
					Log.i(mTag, dir + " directory Created");
				} else {
					Log.e(mTag, dir + " directory Not created");
				}
			}

			if (!_mFile.exists()) {
				if (_mFile.createNewFile()) {
					BufferedOutputStream mBufferedOutputStream = new BufferedOutputStream(
							(new FileOutputStream(_mFile)));
					BufferedInputStream mBufferedInputStream = new BufferedInputStream(
							getResources().openRawResource(id));
					byte[] buff = new byte[2048];
					int len;
					while ((len = mBufferedInputStream.read(buff)) > 0) {
						mBufferedOutputStream.write(buff, 0, len);
					}
					mBufferedOutputStream.flush();
					mBufferedOutputStream.close();
					Log.i(mTag, _mFile.getAbsolutePath() + " Created");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Log.i(mTag, "lifecycle // onDestroy");

		if (VvsipTask.global_failure != 0)
			return;

		unregisterReceiver(networkBroadcastReceiver);
		unregisterReceiver(vvsipAlarmBroadcastReceiver);

		mExecutorService.execute(new Runnable() {
			public void run() {
				Log.i(mTag, "job(start_last) StopVvsipLayer");
				StopVvsipLayer();
				Log.i(mTag, "job(end_last) StopVvsipLayer");
			}
		});

		mExecutorService.shutdown();
		try {
			mExecutorService.awaitTermination(40, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mExecutorService = null;
		messageHandler = null;

		mVvsipTask.vvquit();

		mVvsipService = null;
		mConfiguration = null;
		mVvsipTask = null;
		mAccount = null;
		mVvsipCalls.clear();
		mVvsipCalls = null;

		// back to normal settings
		setAudioNormalMode();

		AudioOutput.beready = false;
		AudioInput.beready = false;

		mAManager = null;

		// stop ringer
		stopPlayer();
		mPlayer_ringback = null;
		mPlayer_ringtone = null;
		mVibrator = null;

		// Tell the user we stopped.
		Toast.makeText(this.getApplicationContext(), "服务停止", Toast.LENGTH_SHORT)
				.show();
		synchronized (mListeners) {
			mListeners.clear();
		}
		this.mListeners = null;
		this.mBinder = null;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * 以通知的形式返回给UI Log信息
	 */
	private void notifyAppStatus(int type, int code, String reason,
								 boolean isLog) {
		if (isLog) {
			Log.i(mTag, type + " " + code + " " + reason);
			return;
		}

		if (mainActivityMessageHandler != null) {
			Message m = new Message();
			m.what = type;
			m.obj = " " + code + " " + reason + "\n";

			mainActivityMessageHandler.sendMessage(m);
		} else {
			Log.i(mTag, "mainActivityMessageHandler==null");
		}
	}

	/**
	 * 以通知的形式返回给UI Log信息
	 */
	private void notifyCallStatus(CharSequence text) {
		if (mainActivityMessageHandler != null) {
			Message m = new Message();
			m.what = -1;
			m.obj = text + "\n";

			mainActivityMessageHandler.sendMessage(m);
		} else {
			Log.i(mTag, "mainActivityMessageHandler==null");
		}
	}

	private int CALLING_ENDING = -1;

	public void mainEndCall(int callend) {
		this.CALLING_ENDING = callend;
	}

	public static VvsipService getService() {
		return mVvsipService;
	}

	public VvsipTask getVvsipTask() {
		return mVvsipTask;
	}

	/*
	 * speex，及除webrtc外的其他语音模块的配置
	 */
	private void vvsipapm_configure(Boolean earpiece_mode) {

		if (mConfiguration.getBoolean("key_apm_in_earpiecemode", false) == true)
			earpiece_mode = false;

		if (earpiece_mode == true) {
			mVvsipTask.vvoptionenableechocanceller(0, 0, 0);
			Log.i(mTag, "earpiece_mode: echo canceller disabled");
			try {
				Float threshold = Float.valueOf(mConfiguration.getString(
						"key_threshold", "0.02"));
				Float speed = Float.valueOf(mConfiguration.getString(
						"key_speed", "0.02"));
				Float force = Float.valueOf(mConfiguration.getString(
						"key_force", "50.0"));
				Integer sustain = Integer.valueOf(mConfiguration.getString(
						"key_sustain", "300"));
				mVvsipTask.vvoptionsetecholimitation(0, threshold, speed,
						force, sustain);
				Log.i(mTag, "earpiece_mode: echo limitation disabled");
			} catch (Exception e) {
			}

			return;
		}

		Boolean el = mConfiguration.getBoolean("key_echo_limiter", false);
		if (el == true) {
			mVvsipTask.vvoptionenableechocanceller(0, 0, 0);
			Log.i(mTag, "speaker_mode: echo canceller enabled");
			try {
				Float threshold = Float.valueOf(mConfiguration.getString(
						"key_threshold", "0.02"));
				Float speed = Float.valueOf(mConfiguration.getString(
						"key_speed", "0.02"));
				Float force = Float.valueOf(mConfiguration.getString(
						"key_force", "50.0"));
				Integer sustain = Integer.valueOf(mConfiguration.getString(
						"key_sustain", "300"));
				mVvsipTask.vvoptionsetecholimitation(1, threshold, speed,
						force, sustain);
				Log.i(mTag, "speaker_mode: echo limitation enabled");
			} catch (Exception e) {
			}
			return;
		}

		try {
			Float threshold = Float.valueOf(mConfiguration.getString(
					"key_threshold", "0.02"));
			Float speed = Float.valueOf(mConfiguration.getString("key_speed",
					"0.02"));
			Float force = Float.valueOf(mConfiguration.getString("key_force",
					"50.0"));
			Integer sustain = Integer.valueOf(mConfiguration.getString(
					"key_sustain", "300"));
			mVvsipTask.vvoptionsetecholimitation(0, threshold, speed, force,
					sustain);
			Log.i(mTag, "speaker_mode: echo limitation disabled");
		} catch (Exception e) {
		}

		if (mAccount.isEchoCancellation() == false) {
			mVvsipTask.vvoptionenableechocanceller(0, 0, 0);
			Log.i(mTag, "speaker_mode: echo canceller disabled");
			return;
		}

		mVvsipTask.vvoptionenableechocanceller(1, 160, 1600 * 2);
		Log.i(mTag, "speaker_mode: echo canceller enabled");
	}

	/*
	 * WEBRTC语音模块配置
	 */
	private void webrtcapm_configure(Boolean earpiece_mode) {
		int aec = 0;
		int agc = 0;
		int ns = 0;
		int hpf = 0;

		if (mConfiguration.getBoolean("key_apm_in_earpiecemode", false) == true)
			earpiece_mode = false;

		if (earpiece_mode == true) {
			mVvsipTask.vvoptionenablewebrtcapm(0, aec, 1, 3, // confort noise ,
					// kSpeakerphone
					ns, 3, // kVeryHigh
					agc, 1, 15, 9, // kAdaptiveDigital, 15dbfs, 9 compression
					hpf);
			Log.i(mTag,
					"earpiece_mode: audio processing (aec/ns/agc/hpf) disabled");
			return;
		}

		if (mAccount.isEchoCancellation() == true
				|| mConfiguration.getBoolean("key_echo_limiter", false) == true)
			aec = 1;
		if (mConfiguration.getBoolean("key_agc", false) == true)
			agc = 1;
		if (mConfiguration.getBoolean("key_noisesuppression", false) == true)
			ns = 1;
		if (mConfiguration.getBoolean("key_highpassfilter", false) == true)
			hpf = 1;

		if (aec == 1 || agc == 1 || ns == 1 || hpf == 1) {
			mVvsipTask.vvoptionenablewebrtcapm(1, aec, 1, 3, // confort noise ,
					// kSpeakerphone
					ns, 3, // kVeryHigh
					agc, 1, 15, 9, // kAdaptiveDigital, 15dbfs, 9 compression
					hpf);
			Log.i(mTag,
					"speaker_mode: audio processing (aec/ns/agc/hpf) enabled");
			return;
		}

		mVvsipTask.vvoptionenablewebrtcapm(0, aec, 1, 3, // confort noise ,
				// kSpeakerphone
				ns, 3, // kVeryHigh
				agc, 1, 15, 9, // kAdaptiveDigital, 15dbfs, 9 compression
				hpf);
		Log.i(mTag, "speaker_mode: audio processing (aec/ns/agc/hpf) disabled");
		return;
	}

	private int onVvsipCallEvent(Message msg) {
		long evt = 0;

		String reason;
		int code;
		String remote_uri;

		if (msg.what >= 0) {
			Long _evt = (Long) msg.obj;
			evt = _evt.longValue();
		}

		if (evt == 0) {
			return -1;
		}

		int _tid = mVvsipTask.vveventgettid(evt);
		int _cid = mVvsipTask.vveventgetcid(evt);
		int _did = mVvsipTask.vveventgetdid(evt);

		Log.e(mTag,"id:"+_tid+","+_cid+","+_did);
		// find existing call
		VvsipCall pCall = null;
		for (VvsipCall _pCall : mVvsipCalls) {
			if (_pCall.cid > 0 && _pCall.cid == _cid) {
				pCall = _pCall;
				break;
			}
		}

		if (pCall == null) {
			if (msg.what == VvsipTask.EXOSIP_CALL_INVITE) {
				// count active call
				int count = 0;
				for (VvsipCall _pCall : mVvsipCalls) {
					if (_pCall.cid > 0 && _pCall.mState <= 2)
						count++;
				}

				if (count >= mMaxConcurentCall /*|| mMaxConcurentCall == 2*/) {
//					mMaxConcurentCall = 1;
					// too much call: reject
					mVvsipTask.vvsessionanswer(_tid, _did, 486, 0);

					if (mVibrator != null) {
						long[] pattern = { 0, 300, 200, 300, 200 };
						mVibrator.vibrate(pattern, -1);
					} else {
						// if (mPlayer_ringtone!=null)
						// mPlayer_ringtone.reset();
						// //mPlayer_ringtone =
						// MediaPlayer.create(this.getApplicationContext(),
						// R.raw.ringtone);
						// mPlayer_ringtone =
						// MediaPlayer.create(this.getApplicationContext(),
						// Settings.System.DEFAULT_RINGTONE_URI);
						// if (mPlayer_ringtone!=null)
						// {
						// mPlayer_ringtone.setLooping(false);
						// mPlayer_ringtone.start();
						// }
					}

					notifyCallStatus("未接电话"
							+ "("
							+ mVvsipTask
							.vveventgetrequestheader(evt, "from", 0)
							+ ")");
					return 0;
				}



				// NEW INCOMING CALL
				Log.i(mTag,
						"NEW CALL: "
								+ mVvsipTask.vveventgetrequestheader(evt,
								"from", 0) + "\n");
				pCall = new VvsipCall();
				pCall.tid = _tid;
				pCall.cid = _cid;
				pCall.did = _did;
				pCall.mState = 0;
				pCall.mIncomingCall = true;
				pCall.mRemoteUri = mVvsipTask.vveventgetrequestheader(evt,
						"from", 0);
				String[] strs = pCall.mRemoteUri.split("\"");
				pCall.macStr = strs[1];

				if (mConfiguration.getBoolean("key_audio_fastoutputsetup",
						false) == true) {
					AudioOutput.beready = true;
				}

				if (mConfiguration
						.getBoolean("key_audio_fastinputsetup", false) == true) {
					AudioInput.beready = true;
				}

				mVvsipTask.vvsessionanswer(_tid, _did, 180, 0);
				mVvsipCalls.add(pCall);

				int dir = mVvsipTask.vvmessagegetrequestvideortpdirection(evt);
				if (dir >= 0) {
					pCall.mVideoStarted = true;
				}

				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onNewVvsipCallEvent(pCall);
					}
					for (IVvsipServiceListener listener : mListeners) {
						listener.onStatusVvsipCallEvent(pCall);
					}
				}

//				//==1，取消自动自拨，可以振铃。
//				if(mMaxConcurentCall == 1)
				if(false)
				switch (mAManager.getRingerMode()) {
					case AudioManager.RINGER_MODE_NORMAL:
						try {
							if (mPlayer_ringtone == null) {
								mPlayer_ringtone = new MediaPlayer();
							} else {
								Log.i(mTag, "reusing mPlayer_ringtone");
								mPlayer_ringtone.reset();
							}
						} catch (Exception e) {
							e.printStackTrace();
							mPlayer_ringtone = null;
						}

						try {
							if (mPlayer_ringtone != null) {
								mPlayer_ringtone
										.setAudioStreamType(AudioManager.STREAM_RING);
								Log.i(mTag, "AudioMode: Mode: MODE_RINGTONE");
								mAManager.setMode(AudioManager.MODE_RINGTONE); // incoming
								// call
								// mode
								Uri ringerUri = RingtoneManager
										.getActualDefaultRingtoneUri(
												this.getApplicationContext(),
												RingtoneManager.TYPE_RINGTONE);
								mPlayer_ringtone.setDataSource(
										this.getApplicationContext(), ringerUri);
								// mPlayer_ringtone.setDataSource(this.getApplicationContext(),
								// Settings.System.DEFAULT_RINGTONE_URI);
								mPlayer_ringtone.prepare();
							}
						} catch (Exception e) {
							e.printStackTrace();
							mPlayer_ringtone = null;
						}

						try {
							if (mPlayer_ringtone != null) {
								mPlayer_ringtone.setLooping(true);
								mPlayer_ringtone.start();
							}
						} catch (IllegalStateException e) {
							e.printStackTrace();
							mPlayer_ringtone = null;
						} catch (Exception e) {
							e.printStackTrace();
							mPlayer_ringtone = null;
						}

						try {
							if (mPlayer_ringtone == null) {
								mPlayer_ringtone = new MediaPlayer();

								if (mPlayer_ringtone != null) {
									mPlayer_ringtone
											.setAudioStreamType(AudioManager.STREAM_RING);
									Log.i(mTag, "AudioMode: Mode: MODE_RINGTONE");
									mAManager.setMode(AudioManager.MODE_RINGTONE); // incoming
									// call
									// mode
									Uri ringerUri = RingtoneManager
											.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
									mPlayer_ringtone
											.setDataSource(
													this.getApplicationContext(),
													ringerUri);
									mPlayer_ringtone.prepare();

									mPlayer_ringtone.setLooping(true);
									mPlayer_ringtone.start();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							mPlayer_ringtone = null;
						}

						break;
					case AudioManager.RINGER_MODE_SILENT:
						break;
					case AudioManager.RINGER_MODE_VIBRATE:
						if (mVibrator != null) {
							long[] pattern = { 0, 300, 200, 300, 200 };
							mVibrator.vibrate(pattern, 0);
						}
						break;
				}
				notifyCallStatus("来电" + "(" + pCall.mRemoteUri + ")");



				return 0;
			}
			return 0;
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_REINVITE) {
			Log.i(mTag, "CALL: REINVITE\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL: REINVITE " + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			if (pCall.mState == 2) {
				// return 0 for _SENDRECV
				// return 1 for _SENDONLY
				// return 2 for _RECVONLY
				int dir = mVvsipTask.vvmessagegetrequestvideortpdirection(evt);
				if (dir >= 0) {
					pCall.mVideoStarted = true;
				} else {
					pCall.mVideoStarted = false;
				}
			}
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_ACK) {
			Log.i(mTag, "CALL: ACK\n");
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_CLOSED|| CALLING_ENDING == VvsipTask.EXOSIP_CALL_CLOSED) {
			CALLING_ENDING = -1;
			msg.what = VvsipTask.EXOSIP_CALL_CLOSED;
			Log.i(mTag, "CALL: CLOSED\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL: CLOSED" + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			pCall.cid = _cid;
			pCall.did = _did;
			if (pCall.mState < 2 && pCall.isIncomingCall()) // never replied
			// use-case
			{
				pCall.description = "未接来电";
				notifyCallStatus("未接来电" + "(" + pCall.mRemoteUri + ")");
			} else if (pCall.isIncomingCall()) {

			}

			if (pCall.mState == 2) {
				Calendar cal = new GregorianCalendar();
				pCall.end_date = cal.getTime().getTime();
			}

			pCall.mState = 3;
			// count active call
			int count = 0;
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.mState == 2)
					count++;
			}
			if (count >= 1) {
				// There are active calls
				stopPlayer();
			} else {
				count = 0;
				for (VvsipCall _pCall : mVvsipCalls) {
					if (_pCall.cid > 0 && _pCall.mState < 2)
						count++;
				}
				if (count == 0) {
					// There is no ringing calls/neither active calls
					setAudioNormalMode();
					stopPlayer();
					AudioOutput.beready = false;
					AudioInput.beready = false;
				}
			}

			synchronized (mListeners) {
				for (IVvsipServiceListener listener : mListeners) {
					listener.onStatusVvsipCallEvent(pCall);
				}
			}
			return 0;
		}
		if (msg.what == VvsipTask.EXOSIP_CALL_RELEASED) {
			Log.i(mTag, "CALL: RELEASED\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL: RELEASED" + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			pCall.cid = _cid;
			pCall.did = _did;

			if (pCall.mState == 2) {
				Calendar cal = new GregorianCalendar();
				pCall.end_date = cal.getTime().getTime();
			}

			pCall.mState = 3;
			mVvsipCalls.remove(pCall);
			synchronized (mListeners) {
				for (IVvsipServiceListener listener : mListeners) {
					listener.onRemoveVvsipCallEvent(pCall);
				}
			}
			pCall = null;

			// count active call
			int count = 0;
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.mState == 2)
					count++;
			}
			if (count >= 1) {
				// There are active calls
				stopPlayer();
			} else {
				count = 0;
				for (VvsipCall _pCall : mVvsipCalls) {
					if (_pCall.cid > 0 && _pCall.mState < 2)
						count++;
				}
				if (count == 0) {
					// There is no ringing calls/neither active calls
					setAudioNormalMode();

					stopPlayer();
					AudioOutput.beready = false;
					AudioInput.beready = false;
				}
			}

			return 0;
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_PROCEEDING
				|| msg.what == VvsipTask.EXOSIP_CALL_RINGING) {
			pCall.cid = _cid;
			pCall.did = _did;
			reason = mVvsipTask.vveventgetreason(evt);
			code = mVvsipTask.vveventgetstatuscode(evt);
			remote_uri = mVvsipTask.vveventgetrequestheader(evt, "to", 0);
			if (pCall.mState == 0 && code > 100)
				pCall.mState = 1;

			Log.i(mTag, "CALL: " + remote_uri + " " + code + " " + reason
					+ "\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL:" + remote_uri + " " + code + " " + reason + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			if (pCall.mState < 2) {
				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onStatusVvsipCallEvent(pCall);
					}
				}

				String content_type = mVvsipTask.vveventgetresponseheader(evt,
						"content-type", 0);
				if (content_type == null) {
					if (mPlayer_ringback == null
							|| mPlayer_ringback.isPlaying() == false) {
						// count active call
						int count = 0;
						for (VvsipCall _pCall : mVvsipCalls) {
							if (_pCall.cid > 0 && _pCall.mState <= 2)
								count++;
						}
						if (count > 1) {
							if (mPlayer_ringback != null)
								mPlayer_ringback.reset();

							try {
								mPlayer_ringback = MediaPlayer.create(
										this.getApplicationContext(),
										R.raw.ringback);
							} catch (Exception e) {
								e.printStackTrace();
								mPlayer_ringback = null;
							}
							if (mPlayer_ringback == null) {
								try {
									mPlayer_ringback = MediaPlayer
											.create(this
															.getApplicationContext(),
													Settings.System.DEFAULT_RINGTONE_URI);
								} catch (Exception e) {
									e.printStackTrace();
									mPlayer_ringback = null;
								}
							}
							if (mPlayer_ringback != null) {
								mPlayer_ringback.setLooping(false);
								mPlayer_ringback.start();
							}
						} else {
							if (mPlayer_ringback != null) {
								mPlayer_ringback.reset();
							}
							try {
								mPlayer_ringback = MediaPlayer.create(
										this.getApplicationContext(),
										R.raw.ringback);
							} catch (Exception e) {
								e.printStackTrace();
								mPlayer_ringback = null;
							}
							if (mPlayer_ringback == null) {
								try {
									mPlayer_ringback = MediaPlayer
											.create(this
															.getApplicationContext(),
													Settings.System.DEFAULT_RINGTONE_URI);
								} catch (Exception e) {
									e.printStackTrace();
									mPlayer_ringback = null;
								}
							}
							if (mPlayer_ringback != null) {
								mPlayer_ringback.setLooping(true);
								mPlayer_ringback.start();
							}
						}
					}
				} else {
					Log.i(mTag,
							"Content-Type detected in provisionnal response\n");
					stopPlayer();
					AudioOutput.beready = true;
					AudioInput.beready = true;
				}

			}
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_ANSWERED) {
			pCall.cid = _cid;
			pCall.did = _did;
			reason = mVvsipTask.vveventgetreason(evt);
			code = mVvsipTask.vveventgetstatuscode(evt);
			remote_uri = mVvsipTask.vveventgetrequestheader(evt, "to", 0);

			Log.i(mTag, "CALL: " + remote_uri + " " + code + " " + reason
					+ "\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL:" + remote_uri + " " + code + " " + reason + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			if (pCall.mState < 2) {

				pCall.established_date = SystemClock.elapsedRealtime();
				pCall.mState = 2;
				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onStatusVvsipCallEvent(pCall);
					}
				}

				stopPlayer();
				setAudioInCallMode();
				setSpeakerModeOff();
			}
			AudioOutput.beready = true;
			AudioInput.beready = true;
		}
		if (msg.what == VvsipTask.EXOSIP_CALL_NOANSWER
				|| msg.what == VvsipTask.EXOSIP_CALL_REDIRECTED
				|| msg.what == VvsipTask.EXOSIP_CALL_REQUESTFAILURE
				|| msg.what == VvsipTask.EXOSIP_CALL_SERVERFAILURE
				|| msg.what == VvsipTask.EXOSIP_CALL_GLOBALFAILURE) {
			pCall.cid = _cid;
			pCall.did = _did;
			reason = mVvsipTask.vveventgetreason(evt);
			code = mVvsipTask.vveventgetstatuscode(evt);
			remote_uri = mVvsipTask.vveventgetrequestheader(evt, "to", 0);
			if (code <= 0)
				code = 408;
			if (reason == null)
				reason = "Timeout - No Answer";

			if (pCall.mState < 2) {
				pCall.description = "" + code + " " + reason;
			}

			Log.i(mTag, "CALL: " + remote_uri + " " + code + " " + reason
					+ "\n");
			if (mainActivityMessageHandler != null) {
				Message m = new Message();
				m.what = msg.what;
				m.obj = "CALL:" + remote_uri + " " + code + " " + reason + "\n";

				mainActivityMessageHandler.sendMessage(m);
			} else {
				Log.i(mTag, "mainActivityMessageHandler==null");
			}

			if (pCall.mState < 2 && code != 422 && code != 401 && code != 407) {
				pCall.mState = 3;
				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onStatusVvsipCallEvent(pCall);
					}
				}
				stopPlayer();
			}
		}

		if (msg.what == VvsipTask.EXOSIP_CALL_MESSAGE_NEW) {
			remote_uri = mVvsipTask.vveventgetrequestheader(evt, "from", 0);

			Log.i(mTag, "REQUEST: " + remote_uri + "\n");

			mVvsipTask.vvsessionanswerrequest(_tid, _did, 200);
		}

		return 0;
	}

	private int onVvsipRegistrationEvent(Message msg) {
		long evt = 0;

		String reason;
		int code;
		String remote_uri;

		if (msg.what >= 0) {
			Long _evt = (Long) msg.obj;
			evt = _evt.longValue();
		}

		int rid = mVvsipTask.vveventgetrid(evt);

		switch (msg.what) {
			case VvsipTask.EXOSIP_REGISTRATION_FAILURE:
				// registration failure

				reason = mVvsipTask.vveventgetreason(evt);
				code = mVvsipTask.vveventgetstatuscode(evt);
				if (code == 0)
					code = 408;
				if (reason == null)
					reason = "Timeout";

				remote_uri = mVvsipTask.vveventgetrequestheader(evt, "from", 0);
				Log.i(mTag, "REGISTRATION: " + remote_uri + " " + code + " "
						+ reason + "\n");

				notifyAppStatus(msg.what, code, reason, false);

				if (mainActivityMessageHandler != null) {
					Message m = new Message();
					m.what = msg.what;
					m.arg1 = msg.arg1;
					m.arg2 = msg.arg2;
					m.obj = "REGISTRATION: " + remote_uri + " " + code + " "
							+ reason + "\n";

					mainActivityMessageHandler.sendMessage(m);
					Log.i(mTag, "mainActivityMessageHandler!=null");
				} else {
					Log.i(mTag, "mainActivityMessageHandler==null");
				}

				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onRegistrationEvent(rid, remote_uri, code, reason);
					}
				}
				break;
			case VvsipTask.EXOSIP_REGISTRATION_SUCCESS:
				// registration success
				reason = mVvsipTask.vveventgetreason(evt);
				code = mVvsipTask.vveventgetstatuscode(evt);

				remote_uri = mVvsipTask.vveventgetrequestheader(evt, "from", 0);
				Log.i(mTag, "REGISTRATION: " + remote_uri + " " + code + " "
						+ reason + "\n");

				notifyAppStatus(msg.what, code, reason, false);

				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onRegistrationEvent(rid, remote_uri, code, reason);
					}
				}
				vvsipAlarmBroadcastReceiver.CancelAlarm(getApplicationContext());
				vvsipAlarmBroadcastReceiver.SetAlarm(getApplicationContext(),
						mAccount.getRegisterInterval() * 4 / 5,
						mAccount.getRegisterInterval() / 5);
				break;
		}

		return 0;
	}

	private int onIncomingSubscriptionEvent(Message msg) {
		return 0;
	}

	private int onOutgoingSubscriptionEvent(Message msg) {
		return 0;
	}

	// Instantiating the Handler associated with the main thread.
	@SuppressLint("HandlerLeak")
	private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			long evt = 0;

			if (mVvsipTask == null)
				return;
			if (mVvsipTask.thread_started == false)
				return;

			if (msg.what == -1) {
				synchronized (mListeners) {
					for (IVvsipServiceListener listener : mListeners) {
						listener.onNewVvsipCallEvent(null);
					}
				}
				return;
			}

			if (msg.what >= 0) {
				Long _evt = (Long) msg.obj;
				evt = _evt.longValue();
			}

			if (evt == 0) {
				return;
			}
			Log.i(mTag, "VvsipEvent received (?" + msg.what + " " + msg.arg1
					+ " " + msg.arg2 + ")\n");

			int cid = mVvsipTask.vveventgetcid(evt);
			int rid = mVvsipTask.vveventgetrid(evt);
			int nid = mVvsipTask.vveventgetnid(evt);
			int sid = mVvsipTask.vveventgetsid(evt);

			if (rid > 0) {
				onVvsipRegistrationEvent(msg);
				mVvsipTask.vveventrelease(evt);
				return;
			}
			if (cid > 0) {
				onVvsipCallEvent(msg);
				mVvsipTask.vveventrelease(evt);
				return;
			}
			if (nid > 0) {
				onIncomingSubscriptionEvent(msg);
				mVvsipTask.vveventrelease(evt);
				return;
			}
			if (sid > 0) {
				onOutgoingSubscriptionEvent(msg);
				mVvsipTask.vveventrelease(evt);
				return;
			}

			if (msg.what == VvsipTask.EXOSIP_MESSAGE_NEW) {
				byte[] data = mVvsipTask.vveventgetrequestbody(evt, 0);
				String body_as_string;
				String method = mVvsipTask.vveventgetmethod(evt);
				try {
					if (method.equalsIgnoreCase("MESSAGE")) {
						body_as_string = new String(data, "UTF-8");
						if (body_as_string != null)
							Toast.makeText(getApplicationContext(),
									"Chat Message: \n" + body_as_string,
									Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				int _tid = mVvsipTask.vveventgettid(evt);
				mVvsipTask.vvmessageanswer(_tid, 200);
			}

			mVvsipTask.vveventrelease(evt);
		}

	};

	public class VvsipAlarmBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(mTag, "VvsipAlarmBroadcastReceiver onReceive");
			// Toast.makeText(context, "VvsipAlarm !!!!!!!!!!",
			// Toast.LENGTH_SHORT).show();
			if (registration_id > 0)
				mExecutorService.execute(new Runnable() {
					public void run() {
						Log.i(mTag, "job(start) amregisterrefresh");
						VvsipService.this.mVvsipTask.vvregisterrefresh(
								VvsipService.this.registration_id,
								mAccount.getRegisterInterval());
						Log.i(mTag, "job(end) amregisterrefresh");
					}
				});

			vvsipAlarmBroadcastReceiver.SetAlarm(getApplicationContext(),
					mAccount.getRegisterInterval() * 4 / 5,
					mAccount.getRegisterInterval() / 5);
		}

		public void SetAlarm(Context context, int sec_delay,
							 int sec_approximation) {
			AlarmManager am = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(VvsipService.VVSIP_ALARM_WAKEUP);
			PendingIntent pi = PendingIntent
					.getBroadcast(context, 0, intent, 0);
			long current_time = System.currentTimeMillis();
			if (Build.VERSION.SDK_INT >= 19) {
				am.setWindow(AlarmManager.RTC_WAKEUP, current_time
								+ (sec_delay - sec_approximation) * 1000,
						sec_approximation * 1000, pi);
			} else {
				am.set(AlarmManager.RTC_WAKEUP,
						current_time + sec_delay * 1000, pi);
			}
			Log.i(mTag, "VvsipAlarm SetAlarm");
		}

		public void CancelAlarm(Context context) {
			Intent intent = new Intent(VvsipService.VVSIP_ALARM_WAKEUP);
			PendingIntent sender = PendingIntent.getBroadcast(context, 0,
					intent, 0);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(sender);
			Log.i(mTag, "VvsipAlarm CancelAlarm");
		}
	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {

		public NetworkInfo previous_network = null;

		@Override
		public void onReceive(Context context, Intent intent) {

			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
					.getAction())) {

				ConnectivityManager mContext = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (mContext != null) {
					NetworkInfo info = mContext.getActiveNetworkInfo();
					NetworkInfo infoWIMAX = mContext
							.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
					NetworkInfo infoWIFI = mContext
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					NetworkInfo infoMOBILE = mContext
							.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

					if (info != null)
						Log.i(mTag, "NetworkInfo active:" + info.toString());
					if (Build.VERSION.SDK_INT >= 8) {
						if (infoWIMAX != null)
							Log.i(mTag,
									"NetworkInfo WIMAX:" + infoWIMAX.toString());
					}
					if (infoWIFI != null)
						Log.i(mTag, "NetworkInfo WIFI:" + infoWIFI.toString());
					if (infoMOBILE != null)
						Log.i(mTag,
								"NetworkInfo MOBILE:" + infoMOBILE.toString());

					if (info == null && previous_network == null) {
						Log.i(mTag, "no active network yet");
						return;
					}

					if (previous_network != null) {
						if (info == null) {
							previous_network = info;
							Log.i(mTag, "network is not active any more");
							mExecutorService.execute(new Runnable() {
								public void run() {
									Log.i(mTag, "job(start) StopVvsipLayer");
									StopVvsipLayer();
									Log.i(mTag, "job(end) StopVvsipLayer");
								}
							});
							notifyAppStatus(-2, -1, "无可用网络", false);
							return;
						}

						if (info.toString().compareTo(
								previous_network.toString()) == 0) {
							Log.i(mTag, "detected duplicate broadcast");
							return;
						}
					}

					// info is not null here:
					if (info.getState() == State.CONNECTED) {
						previous_network = info;
						mExecutorService.execute(new Runnable() {
							public void run() {
								Log.i(mTag, "job(start) StopVvsipLayer");
								StopVvsipLayer();
								Log.i(mTag, "job(end) StopVvsipLayer");
							}
						});
						SharedPreferences.Editor lEditor = mConfiguration
								.edit();
						if (info.getType() == ConnectivityManager.TYPE_WIFI
								||info.getType() == ConnectivityManager.TYPE_ETHERNET)
							lEditor.putBoolean("wifi_mode", true);
						else{
							lEditor.putBoolean("wifi_mode", false);
						}
						lEditor.commit();

						if (!TextUtils.isEmpty(mConfiguration.getString(
								"key_domain", "192.168.1.115"))
								&& !TextUtils.isEmpty(mConfiguration.getString(
								"key_username", "1000"))) {
							mExecutorService.execute(new Runnable() {
								public void run() {
									Log.i(mTag, "job(start) StartVvsipLayer");
									initiateOutgoingCallJob("welcome",null);
									StartVvsipLayer();
									Log.i(mTag, "job(end) StartVvsipLayer");
								}
							});
						}
						return;
					}

					previous_network = info;
					mExecutorService.execute(new Runnable() {
						public void run() {
							Log.i(mTag, "job(start) StopVvsipLayer");
							StopVvsipLayer();
							Log.i(mTag, "job(end) StopVvsipLayer");
						}
					});
					notifyAppStatus(-2, -1, "无可用网络", false);
					return;
				}
				return;
			}
		}
	}

	private boolean mInCallAudioMode = false;
	private boolean mSoloEnabled = false;

	private OnAudioFocusChangeListener afChangeListener = null;

	public void setAudioInCallMode() {
		if (mInCallAudioMode == true)
			return;

		Log.i(mTag, "AudioMode: MODE_IN_CALL (using: "
				+ AudioCompatibility.mAudiomanager_mode_internal + ")");

		if (Build.VERSION.SDK_INT >= 8) {

			if (afChangeListener == null) {
				afChangeListener = new OnAudioFocusChangeListener() {
					public void onAudioFocusChange(int focusChange) {
						if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
							Log.i(mTag,
									"OnAudioFocusChangeListener: AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
							// Pause playback
							if (mSoloEnabled == true) {
								mAManager
										.setStreamSolo(
												AudioCompatibility.mAudiomanager_stream_type,
												false);
								mAManager.setMode(AudioManager.MODE_NORMAL);
							}
							mSoloEnabled = false;
							AudioInput.beready = false;
							AudioOutput.beready = false;
						} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
							Log.i(mTag,
									"OnAudioFocusChangeListener: AudioManager.AUDIOFOCUS_GAIN");
							// Resume playback
							if (mSoloEnabled == false) {
								mAManager
										.setMode(AudioCompatibility.mAudiomanager_mode_internal);
								mAManager
										.setStreamSolo(
												AudioCompatibility.mAudiomanager_stream_type,
												true);
							}
							mSoloEnabled = true;
							AudioInput.beready = true;
							AudioOutput.beready = true;
						} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
							Log.i(mTag,
									"OnAudioFocusChangeListener: AudioManager.AUDIOFOCUS_LOSS");
							// Stop playback
						}
					}
				};
			}

			try {
				int audiofocus_hint = AudioManager.AUDIOFOCUS_GAIN;
				if (Build.VERSION.SDK_INT >= 19)
					audiofocus_hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
				int result = mAManager.requestAudioFocus(afChangeListener,
						AudioCompatibility.mAudiomanager_stream_type,
						audiofocus_hint);

				if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
					// Start playback.
				}
			} catch (Exception e) {
				Log.i(mTag, "AudioMode: requestAudioFocus failure");
			}
		}

		if (AudioCompatibility.mAudiomanager_wa_galaxyS >= 0) {
			// For galaxy S we need to set in call mode before to reset stack
			mAManager.setMode(AudioCompatibility.mAudiomanager_wa_galaxyS);
		}

		if (mSoloEnabled == false) {
			mAManager.setMode(AudioCompatibility.mAudiomanager_mode_internal);
			mAManager.setStreamSolo(
					AudioCompatibility.mAudiomanager_stream_type, true);
		}
		mSoloEnabled = true;
		AudioInput.restart = true;
		AudioOutput.restart = true;
		mInCallAudioMode = true;
	}

	public void setAudioNormalMode() {
		if (mInCallAudioMode == false)
			return;

		Log.i(mTag, "AudioMode: MODE_NORMAL (using: "
				+ AudioCompatibility.mAudiomanager_mode_internal + ")");
		if (mSoloEnabled == true) {
			mAManager.setStreamSolo(
					AudioCompatibility.mAudiomanager_stream_type, false);
			mAManager.setMode(AudioManager.MODE_NORMAL);
		}
		mInCallAudioMode = false;

		if (Build.VERSION.SDK_INT >= 8) {
			try {
				mAManager.abandonAudioFocus(afChangeListener);
			} catch (Exception e) {
				Log.i(mTag, "AudioMode: requestAudioFocus failure");
			}
		}
	}

	public void setSpeakerModeOff() {
		Log.i(mTag, "AudioMode: setSpeakerModeOff");

		Float outputgain = 1.0f;
		Float inputgain = 1.0f;
		try {
			outputgain = Float.valueOf(mConfiguration.getString(
					"key_outputgain_speakeroff", "1.0"));
			inputgain = Float.valueOf(mConfiguration.getString(
					"key_inputgain_speakeroff", "1.0"));
		} catch (Exception e) {
			Log.i(mTag, "setSpeakerModeOff: Exception for gain settings");
		}
		mVvsipTask.vvoptionsetvolumegain(inputgain, outputgain);
		if (outputgain != 1.0 || inputgain != 1.0) {
			Log.i(mTag,
					"setSpeakerModeOff: amoptionsetvolumegain enabled (input="
							+ inputgain + "output=" + outputgain + ")");
		}

		if (AudioCompatibility.mAudiomanager_mode_internal != AudioCompatibility.mAudiomanager_mode_speakermode)
			mAManager.setMode(AudioCompatibility.mAudiomanager_mode_internal);
		mAManager.setSpeakerphoneOn(false);

		String apm_module = mConfiguration
				.getString("key_apm_alternative", "0");
		if (apm_module.equalsIgnoreCase("0")) {
			// WEBRTC based config
			webrtcapm_configure(true);
		} else {
			// speex and other audio processing alternative
			vvsipapm_configure(true);
		}

		if (AudioCompatibility.mAudiomanager_audio_source_internal != AudioInput.mAudiomanager_audio_source) {
			Log.i(mTag, "setSpeakerModeOff:"
					+ AudioCompatibility.mAudiomanager_audio_source_internal);
			AudioInput.mAudiomanager_audio_source = AudioCompatibility.mAudiomanager_audio_source_internal;
		}

	}

	public void setSpeakerModeOn() {
		Log.i(mTag, "AudioMode: setSpeakerModeOn");

		Float outputgain = 1.0f;
		Float inputgain = 1.0f;
		try {
			outputgain = Float.valueOf(mConfiguration.getString(
					"key_outputgain_speakeron", "1.0"));
			inputgain = Float.valueOf(mConfiguration.getString(
					"key_inputgain_speakeron", "1.0"));
		} catch (Exception e) {
			Log.i(mTag, "setSpeakerModeOn: Exception for gain settings");
		}
		mVvsipTask.vvoptionsetvolumegain(inputgain, outputgain);
		if (outputgain != 1.0 || inputgain != 1.0) {
			Log.i(mTag,
					"setSpeakerModeOn: amoptionsetvolumegain enabled (input="
							+ inputgain + "output=" + outputgain + ")");
		}

		if (AudioCompatibility.mAudiomanager_mode_internal != AudioCompatibility.mAudiomanager_mode_speakermode)
			mAManager
					.setMode(AudioCompatibility.mAudiomanager_mode_speakermode);
		mAManager.setSpeakerphoneOn(true);

		String apm_module = mConfiguration
				.getString("key_apm_alternative", "0");
		if (apm_module.equalsIgnoreCase("0")) {
			// WEBRTC based config
			webrtcapm_configure(false);
		} else {
			// speex and other audio processing alternative
			vvsipapm_configure(false);
		}

		if (AudioCompatibility.mAudiomanager_audio_source_speakermode != AudioInput.mAudiomanager_audio_source) {
			Log.i(mTag, "setSpeakerModeOn:"
					+ AudioCompatibility.mAudiomanager_audio_source_speakermode);
			AudioInput.mAudiomanager_audio_source = AudioCompatibility.mAudiomanager_audio_source_speakermode;
		}

	}

	public void stopPlayer() {
		if (mVibrator != null)
			mVibrator.cancel();
		if (mPlayer_ringback != null)
			mPlayer_ringback.stop();

		if (mPlayer_ringtone != null)
			mPlayer_ringtone.stop();
	}

	public void initiateOutgoingCall(final String target,final String macStr) {
		mExecutorService.execute(new Runnable() {
			public void run() {
				Log.i(mTag, "job(start) initiateOutgoingCallJob");
				initiateOutgoingCallJob(target,macStr);
				Log.i(mTag, "job(end) initiateOutgoingCallJob");
			}
		});
	}

	public void initiateOutgoingCallJob(String target,String macStr) {

//		getNeighbors();

		if (mAccount.isDefined() == false)
			return;

		if (mVvsipTask.thread_started != true || mVvsipTask.running != true) {
			return;
		}
		// count active call
		int count = 0;
		for (VvsipCall _pCall : mVvsipCalls) {
			if (_pCall.cid > 0 && _pCall.mState <= 2)
				count++;
		}
		if (count >= mMaxConcurentCall) {
			// too much call: reject
			return;
		}

		target.trim();
		if (!target.startsWith("sip:") && !target.startsWith("sips:")
				&& !target.startsWith("tel:") && !target.startsWith("\"")
				&& !target.endsWith(">")) {
			target = String.format("sip:%s@%s", target, mAccount.getDomain());
			Log.i(mTag, target);
		}

		VvsipCall pCall = new VvsipCall();
		pCall.tid = 0;
		pCall.cid = 0;
		pCall.did = 0;
		pCall.mState = 0;
		pCall.mRemoteUri = target;

		int _cid = -1;

		if (mAccount.getOutboundProxy() == null
				|| mAccount.getOutboundProxy().length() == 0)
			_cid = mVvsipTask.vvsessionstartwithvideo(mAccount.getIdentity(),
					target, "sip:" + mAccount.getDomain(), null,macStr);
		else
			_cid = mVvsipTask.vvsessionstartwithvideo(mAccount.getIdentity(),
					target, "sip:" + mAccount.getDomain(),
					"<sip:" + mAccount.getOutboundProxy() + ";lr>",macStr);
		if (_cid > 0) {
			pCall.cid = _cid;

			mVvsipCalls.add(pCall);
			synchronized (mListeners) {
				for (IVvsipServiceListener listener : mListeners) {
					listener.onNewVvsipCallEvent(pCall);
				}
			}

			if (mConfiguration.getBoolean("key_audio_fastoutputsetup", false) == true) {
				AudioOutput.beready = true;
			}

			if (mConfiguration.getBoolean("key_audio_fastinputsetup", false) == true) {
				AudioInput.beready = true;
			}
		} else
			pCall = null;
		return;
	}

	/*
	 * 初始化接口层
	 */
	private void initVvsipLayer() {

		int i;
		i = mVvsipTask.vvinit(6);// -1);//6); //cancel log.
		if (i != 0) {
			return;
		}
		mVvsipTask.registeraudio();

		if (VvsipMediaCodecFinder.codec_loaded == true) {
			if (VvsipMediaCodecFinder.AvcEncoders.size() > 0) {
				i = mVvsipTask
						.registermsh264mediaencoder(Build.VERSION.SDK_INT);
				if (i >= 0) {
					Log.i(mTag, "MediaCodec Encoder H264 available!");
				}
				String val = mConfiguration.getString(
						"key_h264_hardware_encoder_name", "default");
				if (val == null || val.equalsIgnoreCase("default")) {
					H264MediaCodecEncoder.mMediaCodecInfo = VvsipMediaCodecFinder.AvcEncoders
							.get(0);
					Log.i(mTag,
							"H264MediaCodecEncoder H264 default selected = "
									+ H264MediaCodecEncoder.mMediaCodecInfo.info
									.getName()
									+ " ("
									+ H264MediaCodecEncoder.mMediaCodecInfo.mColorFormat
									+ ")");
				} else {
					Log.i(mTag, "H264MediaCodecEncoder H264 preference = "
							+ val);
					for (int idx = 0; idx < VvsipMediaCodecFinder.AvcEncoders
							.size(); ++idx) {
						VvsipMediaCodecInfo amci = VvsipMediaCodecFinder.AvcEncoders
								.get(idx);

						String mColorSpace = "?";
						if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
							mColorSpace = "YUV";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)
							mColorSpace = "YUVPP";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
							mColorSpace = "NV12";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar)
							mColorSpace = "YUVPPS";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)
							mColorSpace = "TIYUVPSP";
						else if (amci.mColorFormat == 0x7f000001)
							mColorSpace = "TIYUVPSPI";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)
							mColorSpace = "QYUVSP";
						String result = amci.info.getName() + "(" + mColorSpace
								+ ")";
						if (result.equalsIgnoreCase(val)) {
							H264MediaCodecEncoder.mMediaCodecInfo = amci;
							Log.i(mTag,
									"H264MediaCodecEncoder H264 selected = "
											+ amci.info.getName() + " ("
											+ amci.mColorFormat + ")");
							break;
						}
					}
				}
			}
			if (VvsipMediaCodecFinder.AvcDecoders.size() > 0) {
				i = mVvsipTask
						.registermsh264mediadecoder(Build.VERSION.SDK_INT);
				if (i >= 0) {
					Log.i(mTag, "MediaCodec Decoder H264 available!");
				}

				String val = mConfiguration.getString(
						"key_h264_hardware_decoder_name", "default");
				if (val == null || val.equalsIgnoreCase("default")) {
					H264MediaCodecDecoder.mMediaCodecInfo = VvsipMediaCodecFinder.AvcDecoders
							.get(0);
					Log.i(mTag,
							"H264MediaCodecDecoder H264 default selected = "
									+ H264MediaCodecDecoder.mMediaCodecInfo.info
									.getName()
									+ " ("
									+ H264MediaCodecDecoder.mMediaCodecInfo.mColorFormat
									+ ")");
				} else {
					Log.i(mTag, "H264MediaCodecDecoder H264 preference = "
							+ val);
					for (int idx = 0; idx < VvsipMediaCodecFinder.AvcDecoders
							.size(); ++idx) {
						VvsipMediaCodecInfo amci = VvsipMediaCodecFinder.AvcDecoders
								.get(idx);

						String mColorSpace = "?";
						if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
							mColorSpace = "YUV";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)
							mColorSpace = "YUVPP";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
							mColorSpace = "NV12";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar)
							mColorSpace = "YUVPPS";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)
							mColorSpace = "TIYUVPSP";
						else if (amci.mColorFormat == 0x7f000001)
							mColorSpace = "TIYUVPSPI";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)
							mColorSpace = "QYUVSP";
						String result = amci.info.getName() + "(" + mColorSpace
								+ ")";
						if (result.equalsIgnoreCase(val)) {
							H264MediaCodecDecoder.mMediaCodecInfo = amci;
							Log.i(mTag,
									"H264MediaCodecDecoder Decoder H264 selected = "
											+ amci.info.getName() + " ("
											+ amci.mColorFormat + ")");
							break;
						}
					}
				}
			}

			if (mConfiguration.getBoolean("key_videocodec_h264_hw_encoder",
					false) == true
					&& H264MediaCodecEncoder.mMediaCodecInfo != null) {
				i = mVvsipTask.vvoptionenablefilter("MSH264MediaCodecEnc");
				i = mVvsipTask.vvoptiondisablefilter("MSX264Enc");
				i = mVvsipTask.vvoptiondisablefilter("MSOPENH264Enc");
			} else {
				i = mVvsipTask.vvoptiondisablefilter("MSH264MediaCodecEnc");
				i = mVvsipTask.vvoptionenablefilter("MSX264Enc");
				i = mVvsipTask.vvoptionenablefilter("MSOPENH264Enc");
			}
			if (mConfiguration.getBoolean("key_videocodec_h264_hw_decoder",
					false) == true
					&& H264MediaCodecDecoder.mMediaCodecInfo != null) {
				i = mVvsipTask.vvoptionenablefilter("MSH264MediaCodecDec");
			} else {
				i = mVvsipTask.vvoptiondisablefilter("MSH264MediaCodecDec");
			}
		}
	}

	private void setupBroadcastReceiver() {
		vvsipAlarmBroadcastReceiver = new VvsipAlarmBroadcastReceiver();
		networkBroadcastReceiver = new NetworkBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkBroadcastReceiver, intentFilter);

		intentFilter = new IntentFilter(VVSIP_ALARM_WAKEUP);
		registerReceiver(vvsipAlarmBroadcastReceiver, intentFilter);
	}

	public int StopVvsipLayer() {
		int i;

		if (mVvsipTask == null)
			return -1;

		if (mVvsipTask.thread_started == true) {
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.did > 0)
					_pCall.stop();
			}
		}
		if (registration_id > 0) {
			mVvsipTask.vvregisterstop(registration_id);
		}

		mVvsipTask.stop();

		registration_id = 0;

		mVvsipCalls.clear();

		stopPlayer();
		setAudioNormalMode();
		AudioOutput.beready = false;
		AudioInput.beready = false;

		vvsipAlarmBroadcastReceiver.CancelAlarm(getApplicationContext());
		i = mVvsipTask.vvreset(VVSIP_DEBUG_LEVEL);
		if (i != 0) {
			Log.e(mTag, "vvreset failed\n");
			return -1;
		}
		Toast.makeText(this.getApplicationContext(), "服务停止", Toast.LENGTH_SHORT)
				.show();
		return 0;
	}

	public int StartVvsipLayer() {
		int i;
		Random random = new Random();
		int x = random.nextInt(8999);
		x = x + 1000;
		mAccount.setDomain(mConfiguration.getString("key_domain", null));
		mAccount.setUserid(mConfiguration.getString("key_username",
				String.valueOf(x)));
		mAccount.setPasswd(mConfiguration.getString("key_password", null));
		mAccount.setIdentity(mConfiguration.getString("key_identity", null));
		mAccount.setOutboundProxy(mConfiguration.getString("key_outboundproxy",
				null));
		mAccount.setTransport(mConfiguration.getString("key_protocol", "udp"));
		String exp = mConfiguration.getString("key_interval", null);
		if (exp != null) {
			try {
				mAccount.setRegisterInterval(Integer.valueOf(exp));
			} catch (Exception e) {
				mAccount.setRegisterInterval(Integer.valueOf(600));
			}
		}
		mAccount.setEchoCancellation(mConfiguration.getBoolean(
				"key_echocancellation", false));

		notifyAppStatus(-1, -1, "Starting", false);

		i = mVvsipTask.vvreset(VVSIP_DEBUG_LEVEL);
		if (i != 0) {
			Log.e(mTag, "vvreset failed\n");
			return -1;
		}

		H264MediaCodecEncoder.mMediaCodecInfo = null;
		H264MediaCodecDecoder.mMediaCodecInfo = null;

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			boolean checkEncoder = mConfiguration.getBoolean(
					"key_videocodec_h264_hw_encoder", false);
			boolean checkDecoder = mConfiguration.getBoolean(
					"key_videocodec_h264_hw_decoder", false);
			if ((VvsipMediaCodecFinder.AvcEncoders.size() == 0 && checkEncoder == true)
					|| VvsipMediaCodecFinder.AvcDecoders.size() == 0
					&& checkDecoder == true) {
				new Thread(new Runnable() {
					public void run() {
						try {
							boolean checkEncoder = mConfiguration.getBoolean(
									"key_videocodec_h264_hw_encoder", false);
							boolean checkDecoder = mConfiguration.getBoolean(
									"key_videocodec_h264_hw_decoder", false);
							VvsipMediaCodecFinder.ListAllMediaCodec(
									"video/avc",
									CodecProfileLevel.AVCProfileBaseline,
									checkEncoder, checkDecoder);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (VvsipMediaCodecFinder.codec_loaded == true) {
			if (VvsipMediaCodecFinder.AvcEncoders.size() > 0) {
				i = mVvsipTask
						.registermsh264mediaencoder(Build.VERSION.SDK_INT);
				if (i >= 0) {
					// Log.i(mTag, "MediaCodec Encoder H264 available!");
				}

				String val = mConfiguration.getString(
						"key_h264_hardware_encoder_name", "default");
				if (val == null || val.equalsIgnoreCase("default")) {
					H264MediaCodecEncoder.mMediaCodecInfo = VvsipMediaCodecFinder.AvcEncoders
							.get(0);
					Log.i(mTag,
							"H264MediaCodecEncoder H264 default selected = "
									+ H264MediaCodecEncoder.mMediaCodecInfo.info
									.getName()
									+ " ("
									+ H264MediaCodecEncoder.mMediaCodecInfo.mColorFormat
									+ ")");
				} else {
					Log.i(mTag, "H264MediaCodecEncoder H264 preference = "
							+ val);
					for (int idx = 0; idx < VvsipMediaCodecFinder.AvcEncoders
							.size(); ++idx) {
						VvsipMediaCodecInfo amci = VvsipMediaCodecFinder.AvcEncoders
								.get(idx);

						String mColorSpace = "?";
						if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
							mColorSpace = "YUV";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)
							mColorSpace = "YUVPP";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
							mColorSpace = "NV12";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar)
							mColorSpace = "YUVPPS";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)
							mColorSpace = "TIYUVPSP";
						else if (amci.mColorFormat == 0x7f000001)
							mColorSpace = "TIYUVPSPI";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)
							mColorSpace = "QYUVSP";
						String result = amci.info.getName() + "(" + mColorSpace
								+ ")";
						if (result.equalsIgnoreCase(val)) {
							H264MediaCodecEncoder.mMediaCodecInfo = amci;
							Log.i(mTag,
									"H264MediaCodecEncoder H264 selected = "
											+ amci.info.getName() + " ("
											+ amci.mColorFormat + ")");
							break;
						}
					}
				}
			}
			if (VvsipMediaCodecFinder.AvcDecoders.size() > 0) {
				i = mVvsipTask
						.registermsh264mediadecoder(Build.VERSION.SDK_INT);
				if (i >= 0) {
					// Log.i(mTag, "MediaCodec Decoder H264 available!");
				}

				String val = mConfiguration.getString(
						"key_h264_hardware_decoder_name", "default");
				if (val == null || val.equalsIgnoreCase("default")) {
					H264MediaCodecDecoder.mMediaCodecInfo = VvsipMediaCodecFinder.AvcDecoders
							.get(0);
					Log.i(mTag,
							"H264MediaCodecDecoder H264 default selected = "
									+ H264MediaCodecDecoder.mMediaCodecInfo.info
									.getName()
									+ " ("
									+ H264MediaCodecDecoder.mMediaCodecInfo.mColorFormat
									+ ")");
				} else {
					Log.i(mTag, "H264MediaCodecDecoder H264 preference = "
							+ val);
					for (int idx = 0; idx < VvsipMediaCodecFinder.AvcDecoders
							.size(); ++idx) {
						VvsipMediaCodecInfo amci = VvsipMediaCodecFinder.AvcDecoders
								.get(idx);

						String mColorSpace = "?";
						if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
							mColorSpace = "YUV";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)
							mColorSpace = "YUVPP";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
							mColorSpace = "NV12";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar)
							mColorSpace = "YUVPPS";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)
							mColorSpace = "TIYUVPSP";
						else if (amci.mColorFormat == 0x7f000001)
							mColorSpace = "TIYUVPSPI";
						else if (amci.mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)
							mColorSpace = "QYUVSP";
						String result = amci.info.getName() + "(" + mColorSpace
								+ ")";
						if (result.equalsIgnoreCase(val)) {
							H264MediaCodecDecoder.mMediaCodecInfo = amci;
							Log.i(mTag,
									"H264MediaCodecDecoder Decoder H264 selected = "
											+ amci.info.getName() + " ("
											+ amci.mColorFormat + ")");
							break;
						}
					}
				}
			}

			if (mConfiguration.getBoolean("key_videocodec_h264_hw_encoder",
					false) == true
					&& H264MediaCodecEncoder.mMediaCodecInfo != null) {
				i = mVvsipTask.vvoptionenablefilter("MSH264MediaCodecEnc");
				i = mVvsipTask.vvoptiondisablefilter("MSX264Enc");
				i = mVvsipTask.vvoptiondisablefilter("MSOPENH264Enc");
			} else {
				i = mVvsipTask.vvoptiondisablefilter("MSH264MediaCodecEnc");
				i = mVvsipTask.vvoptionenablefilter("MSX264Enc");
				i = mVvsipTask.vvoptionenablefilter("MSOPENH264Enc");
			}
			if (mConfiguration.getBoolean("key_videocodec_h264_hw_decoder",
					false) == true
					&& H264MediaCodecDecoder.mMediaCodecInfo != null) {
				i = mVvsipTask.vvoptionenablefilter("MSH264MediaCodecDec");
			} else {
				i = mVvsipTask.vvoptiondisablefilter("MSH264MediaCodecDec");
			}
		}

		i = mVvsipTask.vvcodecinfomodify(0, 0, "PCMU/8000", 0, 0, 0);
		i = mVvsipTask.vvcodecinfomodify(1, 0, "PCMU/8000", 0, 0, 0);
		i = mVvsipTask.vvcodecinfomodify(2, 0, "PCMU/8000", 0, 0, 0);
		i = mVvsipTask.vvcodecinfomodify(3, 0, "PCMU/8000", 0, 0, 0);
		i = mVvsipTask.vvcodecinfomodify(4, 0, "PCMU/8000", 0, 0, 0);

		int index = 0;
		if (mConfiguration.getBoolean("wifi_mode", false)) {
			if (mConfiguration.getBoolean("key_audiocodec_silkwb", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "SILK/16000", 0, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_opus", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "OPUS/48000", 0x41,
						0, 1);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_speex16", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "speex/16000", 6, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_isac16", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "isac/16000", 0, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_g722", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "g722/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_silknb", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "SILK/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_speex8", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "speex/8000", 6, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_ilbc", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "iLBC/8000", 30, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_g729", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "g729/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_gsm", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "GSM/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_pcmu", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "PCMU/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_pcma", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "PCMA/8000", 0, 0, 0);
				index++;
			}
		} else {
			/* 3G mode */
			if (mConfiguration.getBoolean("key_audiocodec_silknb", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "SILK/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_opus", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "OPUS/48000", 0x11,
						0, 1);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_speex8", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "speex/8000", 2, 1,
						1);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_g729", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "g729/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_ilbc", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "iLBC/8000", 30, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_gsm", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "GSM/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_silkwb", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "SILK/16000", 0, 0,
						0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_speex16", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "speex/16000", 2, 1,
						1);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_isac16", false)) {
				i = mVvsipTask.vvcodecinfomodify(index, 1, "isac/16000", 0, 0,
						0);
				index++;
			}
			if (index < 3
					&& mConfiguration.getBoolean("key_audiocodec_g722", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "g722/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_pcmu", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "PCMU/8000", 0, 0, 0);
				index++;
			}
			if (mConfiguration.getBoolean("key_audiocodec_pcma", false)) {
				i = mVvsipTask
						.vvcodecinfomodify(index, 1, "PCMA/8000", 0, 0, 0);
				index++;
			}
		}

		if (index == 0) {
			i = mVvsipTask.vvcodecinfomodify(0, 1, "SILK/8000", 0, 0, 0);
			i = mVvsipTask.vvcodecinfomodify(1, 1, "GSM/8000", 0, 0, 0);
			i = mVvsipTask.vvcodecinfomodify(2, 1, "iLBC/8000", 0, 0, 0);
			i = mVvsipTask.vvcodecinfomodify(3, 1, "PCMU/8000", 0, 0, 0);
			i = mVvsipTask.vvcodecinfomodify(4, 1, "PCMA/8000", 0, 0, 0);
		}

		if (mConfiguration.getBoolean("wifi_mode", false)) {
			i = mVvsipTask.vvcodecattrmodify(0, 0);
		} else {
			/* force to use lower bitrate codec for answering SDP */
			i = mVvsipTask.vvcodecattrmodify(0, 30);
		}

		i = mVvsipTask.vvvideocodecinfomodify(0, 0, "VP8/90000", 0, 0, 0);
		if (mConfiguration.getBoolean("key_videocodec_vp8", false)) {
			i = mVvsipTask.vvvideocodecinfomodify(0, 1, "VP8/90000", 0, 0, 0);
		}
		i = mVvsipTask.vvvideocodecinfomodify(1, 0, "H264/90000", 1, 0, 0);
		if (mConfiguration.getBoolean("key_videocodec_h264", false)) {
			i = mVvsipTask.vvvideocodecinfomodify(1, 1, "H264/90000", 1, 0, 0);
		}
		i = mVvsipTask.vvvideocodecinfomodify(2, 0, "MP4V-ES/90000", 0, 0, 0);
		if (mConfiguration.getBoolean("key_videocodec_mp4v", false)) {
			i = mVvsipTask.vvvideocodecinfomodify(2, 1, "MP4V-ES/90000", 0, 0,
					0);
		}
		i = mVvsipTask.vvvideocodecinfomodify(3, 0, "H263-1998/90000", 0, 0, 0);
		if (mConfiguration.getBoolean("key_videocodec_h2631998", false)) {
			i = mVvsipTask.vvvideocodecinfomodify(3, 1, "H263-1998/90000", 0,
					0, 0);
		}
		i = mVvsipTask.vvvideocodecinfomodify(4, 0, "H263/90000", 0, 0, 0);
		if (mConfiguration.getBoolean("key_videocodec_h263", false)) {
			i = mVvsipTask.vvvideocodecinfomodify(4, 1, "H263/90000", 0, 0, 0);
		}

		int uploadrate = 128;
		int downloadrate = 128;
		String val_upload = mConfiguration.getString("key_videouploadrate",
				"128");
		String val_download = mConfiguration.getString("key_videodownloadrate",
				"128");
		if (val_upload != null) {
			try {
				uploadrate = Integer.valueOf(val_upload);
			} catch (Exception e) {
			}
		}
		if (val_download != null) {
			try {
				downloadrate = Integer.valueOf(val_download);
			} catch (Exception e) {
			}
		}

		if (mConfiguration.getBoolean("wifi_mode", false) == false) {
			/* force lowest rate for 3G video call */
			uploadrate = 64;
			downloadrate = 64;
		}
		i = mVvsipTask.vvvideocodecattrmodify(uploadrate, downloadrate);

		mVvsipTask.vvoptionsetuseragent("vvsip/demo" + " " + Build.MODEL + "/"
				+ Build.VERSION.RELEASE);

		i = mVvsipTask.vvoptionsetrate(AudioCompatibility.getRate(16000));
		if (i != 0) {
			Log.e(mTag, "vvoptionsetrate failed\n");
		}

		Boolean earpiece_mode = true;
		Boolean bool_haveearpiecemode = mConfiguration.getBoolean(
				"key_haveearpiecemode", true);
		if (bool_haveearpiecemode == false) {
			earpiece_mode = false;
		}
		String apm_module = mConfiguration
				.getString("key_apm_alternative", "0");
		if (apm_module.equalsIgnoreCase("0")) {
			// WEBRTC based config
			webrtcapm_configure(earpiece_mode);
		} else {
			// speex and other audio processing alternative
			vvsipapm_configure(earpiece_mode);
		}

		i = mVvsipTask.vvoptionenablerport(1);
		if (i != 0) {
			Log.e(mTag, "vvoptionenablerport failed\n");
		}

		Log.i(mTag, "HW-INFO:" + Build.MODEL);
		Log.i(mTag, "HW-INFO:" + Build.VERSION.RELEASE);
		Log.i(mTag, "HW-INFO:" + Build.DEVICE);
		Log.i(mTag, "HW-INFO:" + Build.BRAND);

		i = mVvsipTask.vvoptionsetdnscapabilities(2);
		if (i != 0) {
			Log.e(mTag, "vvoptionsetdnscapabilities failed\n");
		}

		i = mVvsipTask.vvoptionselectinsoundcard(0);
		if (i != 0)
			i = mVvsipTask.vvoptionselectinsoundcard(0);
		if (i != 0) {
			Log.e(mTag, "vvoptionselectinsoundcard failed\n");
		}
		i = mVvsipTask.vvoptionselectoutsoundcard(0);
		if (i != 0)
			i = mVvsipTask.vvoptionselectoutsoundcard(0);
		if (i != 0) {
			Log.e(mTag, "vvoptionselectoutsoundcard failed\n");
		}

		i = mVvsipTask
				.vvoptionsetallowedmethods("INVITE, ACK, BYE, OPTIONS, CANCEL, INFO, UPDATE, REFER, NOTIFY, MESSAGE");
		if (i != 0) {
			Log.e(mTag, "vvoptionsetallowedmethods failed\n");
		}

		// i =
		// mVvsipTask.vvoptionsetsupportedextensions("timer, 100rel, replaces");
		i = mVvsipTask.vvoptionsetsupportedextensions("100rel, replaces");
		if (i != 0) {
			Log.e(mTag, "vvoptionsetsupportedextensions failed\n");
		}

		i = mVvsipTask.vvoptionenablesessiontimers(0);
		if (i != 0) {
			Log.e(mTag, "vvoptionenablesessiontimers failed\n");
		}

		String domain = mAccount.getDomain();
		if (domain != null) {
			i = mVvsipTask.vvoptionsetipv4forgateway(domain);
			if (i != 0) {
				Log.e(mTag, "vvoptionsetipv4forgateway failed\n");
			}
		}

		String stunserver = mConfiguration.getString("key_stunserver", null);
		if (stunserver != null && stunserver.length() > 0) {
			i = mVvsipTask.vvoptionenableturnserver(stunserver, 1);
			// i = mVvsipTask.vvoptionenablestunserver(stunserver, 1);
		}

		i = mVvsipTask.vvoptionenablesymmetricrtp(1);
		if (i != 0) {
			Log.e(mTag, "vvoptionenablesymmetricrtp failed\n");
		}
		String passwd = mAccount.getPasswd();
		if (passwd != null) {
			i = mVvsipTask
					.vvoptionsetpassword("", mAccount.getUserid(), passwd);
			if (i != 0) {
				Log.e(mTag, "vvoptionsetpassword failed\n");
			}
		}

		String val = mConfiguration.getString("key_encryption", "RTP");
		if (val.equalsIgnoreCase("optional SRTP"))
			i = mVvsipTask.vvoptionenableoptionnalencryption(1);
		else if (val.equalsIgnoreCase("SRTP")) {
			i = mVvsipTask.vvoptionenableoptionnalencryption(0);
			i = mVvsipTask.vvoptionsetaudioprofile("RTP/SAVP");
			i = mVvsipTask.vvoptionsetvideoprofile("RTP/SAVP");
		} else {
			i = mVvsipTask.vvoptionenableoptionnalencryption(0);
		}
		if (i != 0) {
			Log.e(mTag, "vvoptionenableoptionnalencryption failed\n");
		}

		int rtp_port_range = 40100;
		String val_rtp_port_range = mConfiguration.getString(
				"key_rtp_port_range", "40100");
		if (val_rtp_port_range != null) {
			try {
				rtp_port_range = Integer.valueOf(val_rtp_port_range);
			} catch (Exception e) {
			}
		}

		i = mVvsipTask.vvoptionsetinitialaudioport(rtp_port_range);
		if (i != 0) {
			Log.e(mTag, "vvoptionsetinitialaudioport failed\n");
		}

		int sip_port = 0;
		String val_sip_port = mConfiguration.getString("key_local_sip_port",
				"5060");
		val_sip_port = "5060";
		if (val_sip_port != null) {
			try {
				sip_port = Integer.valueOf(val_sip_port);
			} catch (Exception e) {
			}
		}

		i = mVvsipTask.vvoptionsetjittermode(-40);
		if (i != 0) {
			Log.e(mTag, "vvoptionsetjittermode failed\n");
		}

		i = mVvsipTask.vvoptionvideosetjittermode(-20);
		if (i != 0) {
			Log.e(mTag, "vvoptionvideosetjittermode failed\n");
		}

		i = mVvsipTask.vvnetworkstart(mAccount.getTransport(), sip_port);
		if (i < 0)
			i = mVvsipTask.vvnetworkstart(mAccount.getTransport(), 0);
		if (i < 0) {
			Log.e(mTag, "vvnetworkstart failed\n");
			notifyAppStatus(-2, -1, "网络错误", false);
			return -1;
		}

		String identity = mAccount.getIdentity();
		if (identity != null) {
			registration_id = mVvsipTask
					.vvregisterstart(mAccount.getIdentity(),
							"sip:" + mAccount.getDomain(),
							mAccount.getOutboundProxy(),
							mAccount.getRegisterInterval());
			if (registration_id > 0)
				Toast.makeText(this.getApplicationContext(), "服务开始",
						Toast.LENGTH_SHORT).show();
		}

		mVvsipTask.start(messageHandler);
		vvsipAlarmBroadcastReceiver.SetAlarm(getApplicationContext(),
				mAccount.getRegisterInterval() * 4 / 5,
				mAccount.getRegisterInterval() / 5);

		return registration_id;
	}

	public void setMessageHandler(Handler _mainActivityMessageHandler) {

		mainActivityMessageHandler = _mainActivityMessageHandler;
	}

	@Override
	public void register(String domain, String username, String passwd) {
		// TODO Auto-generated method stub
		Log.e(mTag, mAccount.getIdentity() + "sip:" + mAccount.getDomain());

		mAccount.setDomain(domain);
		mAccount.setUserid(username);
		mAccount.setPasswd(passwd);
		mAccount.setIdentity("<sip:" + username + "@" + domain + ">");

		int i = mVvsipTask.vvoptionsetipv4forgateway(mAccount.getDomain());
		if (i != 0) {
			Log.e(mTag, "vvoptionsetipv4forgateway failed\n");
		}
		String password = mAccount.getPasswd();
		if (password != null) {
			i = mVvsipTask.vvoptionsetpassword("", mAccount.getUserid(),
					password);
			if (i != 0) {
				Log.e(mTag, "vvoptionsetpassword failed\n");
			}
		}
		registration_id = mVvsipTask.vvregisterstart(mAccount.getIdentity(),
				"sip:" + mAccount.getDomain(), mAccount.getOutboundProxy(),
				mAccount.getRegisterInterval());
//		if (registration_id > 0)
//			Toast.makeText(this.getApplicationContext(), "服务开始",
//				Toast.LENGTH_SHORT).show();

	}

	public List<String> getNeighborsList() {

		List<String> strList = new ArrayList<String>();

		synchronized (neighborsList) {
			for (VvsipNeighbor nb : neighborsList) {
				strList.add(nb.extension);
			}
		}

		return strList;
	}

	@Override
	public void setNeighbors() {
		mExecutorService.execute(new Runnable() {
			public void run() {
				//
			}
		});
	}

	private VvsipNeighbor findVvsipNeighbor(String lan_callee) {
		synchronized (neighborsList) {
			for (VvsipNeighbor nb : neighborsList) {
				if (nb.extension.equals(lan_callee)) {
					return nb;
				}
			}
		}
		return null;

	}


	@Override
	public void sendDTMF(String dtmf) {
		// TODO Auto-generated method stub
		if (mVvsipTask == null)
			return ;

		if (mVvsipTask.thread_started == true) {
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.did > 0)
					_pCall.sendrtpdtmf(dtmf);
			}
		}
	}

}
