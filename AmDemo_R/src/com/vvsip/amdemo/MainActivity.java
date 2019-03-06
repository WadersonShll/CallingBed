//package com.vvsip.amdemo;
//
//import android.annotation.SuppressLint;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.app.ActionBarActivity;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.vvsip.ansip.IVvsipService;
//import com.vvsip.ansip.IVvsipServiceListener;
//import com.vvsip.ansip.VvsipCall;
//import com.vvsip.ansip.VvsipService;
//import com.vvsip.ansip.VvsipTask;
//
//import java.util.ArrayList;
//import java.util.List;
//
///*
// * ������Activity
// */
//public class MainActivity extends ActionBarActivity implements
//		View.OnClickListener, IVvsipServiceListener {
//
//	private EditText serverAddr;
//	private EditText userphone;
//	private EditText passwd;
//	private EditText callee;
//
//	private TextView callStatus;
//
//	private Button mButton_register;
//	private Button mButton_audioCall;
//	private Button mButton_endCall;
//	private Button mButton_offHook;
//	private Button mButton_addVideo;
//	private EditText dtmf;
//	private Button mButton_sendDTMF;
//
//
//	static String mTag = "MainActivity";
//	private List<VvsipCall> mVvsipCalls = null;
//
//	private String lan_callee;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		serverAddr = (EditText) findViewById(R.id.server_addr);
//		userphone = (EditText) findViewById(R.id.userphone);
//		passwd = (EditText) findViewById(R.id.passwd);
//		callee = (EditText) findViewById(R.id.callee);
//
//		callStatus = (TextView) findViewById(R.id.call_status);
//
//		mButton_register = (Button) findViewById(R.id.register);
//		mButton_audioCall = (Button) findViewById(R.id.audio_call);
//		mButton_endCall = (Button) findViewById(R.id.end_call);
//		mButton_offHook = (Button) findViewById(R.id.off_hook);
//		mButton_addVideo = (Button) findViewById(R.id.add_video);
//		mButton_sendDTMF = (Button) findViewById(R.id.send_dtmf);
//
//
//
//		mButton_register.setOnClickListener(this);
//		mButton_audioCall.setOnClickListener(this);
//		mButton_endCall.setOnClickListener(this);
//		mButton_offHook.setOnClickListener(this);
//		mButton_addVideo.setOnClickListener(this);
//		mButton_sendDTMF.setOnClickListener(this);
//
//		IVvsipService _service = VvsipService.getService();
//		Log.i(mTag, "lifecycle // _service");
//		if (_service != null) {
//
//			_service.addListener(this);
//			_service.setMessageHandler(messageHandler);
//			Log.i(mTag, "lifecycle // addListener");
//		} else {
//			Log.i(mTag, "lifecycle // _service==null");
//		}
//
//		if (mVvsipCalls == null) {
//			mVvsipCalls = new ArrayList<VvsipCall>();
//		}
//
//	}
//
//	@Override
//	public void onDestroy() {
//		IVvsipService _service = VvsipService.getService();
//		if (_service != null)
//			_service.removeListener(this);
//		if (mVvsipCalls != null) {
//			mVvsipCalls.clear();
//			mVvsipCalls = null;
//		}
//		super.onDestroy();
//		Log.i(mTag, "lifecycle // onDestroy");
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onNewVvsipCallEvent(final VvsipCall call) {
//
//		MainActivity.this.runOnUiThread(new Runnable() {
//			public void run() {
//				try {
//					if (call == null) {
//						return;
//					}
//
//					if (mVvsipCalls == null)
//						return;
//					mVvsipCalls.add(call);
//
//					if (Build.VERSION.SDK_INT >= 5) {
//						getWindow()
//								.addFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//											// |
//										WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//												| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//												| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//					}
//
//				} catch (Exception e) {
//					Log.e(mTag, "onNewVvsipCallEvent: " + e);
//				}
//			}
//		});
//	}
//
//	@Override
//	public void onStatusVvsipCallEvent(VvsipCall call) {
//		Log.d("SIPTES","call :"+call.mState);
//	}
//
//	@Override
//	public void onRemoveVvsipCallEvent(final VvsipCall call) {
//
//		MainActivity.this.runOnUiThread(new Runnable() {
//			public void run() {
//				try {
//					if (call == null) {
//						return;
//					}
//
//					// 4 crash detected here for 4.0.9 with mVvsipCalls=NULL
//					if (mVvsipCalls == null)
//						return;
//					mVvsipCalls.remove(call);
//
//					if (mVvsipCalls.size() == 0) {
//						if (Build.VERSION.SDK_INT >= 5) {
//							getWindow()
//									.clearFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//													// |
//											WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//													| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//													| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//						}
//					}
//				} catch (Exception e) {
//					Log.e(mTag, "onRemoveVvsipCallEvent: " + e);
//				}
//			}
//		});
//	}
//
//	@Override
//	public void onRegistrationEvent(final int rid, final String remoteUri,
//			final int code, String reason) {
//
//	}
//
//	@SuppressLint("HandlerLeak")
//	private Handler messageHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			Log.i(mTag, "VvsipEvent received (?" + msg.what + " " + msg.arg1
//					+ " " + msg.arg2 + ")\n");
//			Log.i(mTag, "#" + msg.obj);
//			callStatus.setText("" + msg.obj + callStatus.getText());
//
//			if(msg.obj.toString().contains("autocall")){ //��������
//				VvsipCall pCall = null;
//				Log.e(mTag, "onClick1");
//				for (VvsipCall _pCall : mVvsipCalls) {
//					if(_pCall.cid > 0)
//						Log.e(mTag, "state#"+_pCall.mState);
//					if (_pCall.cid > 0 && _pCall.mState <= 2) {
//						pCall = _pCall;
//						break;
//					}
//				}
//				Log.e(mTag, "onClick2");
//				if (pCall == null)
//					return;
//				Log.e(mTag, "onClick3#"+pCall.mState);
//				IVvsipService _service = VvsipService.getService();
//				if (_service == null)
//					return;
//				VvsipTask _vvsipTask = _service.getVvsipTask();
//				if (_vvsipTask == null)
//					return;
//				pCall.stop();
//				_service.setSpeakerModeOff();
//			}
//
//
//		}
//
//	};
//
//	@Override
//	public void onClick(View v) {
//
//		/*
//		 * ע��
//		 */
//		if (v == mButton_register) {
//			MainActivity.this.runOnUiThread(new Runnable() {
//				public void run() {
//					IVvsipService _service = VvsipService.getService();
//					if (_service != null) {
//						// _service.StartVvsipLayer();
//
//						_service.register(serverAddr.getText().toString(),
//								userphone.getText().toString(), passwd
//										.getText().toString());
//					}
//				}
//			});
//			return;
//		}
//		/*
//		 * �������
//		 */
//		if (v == mButton_audioCall) {
//			if (callee.getText().length() == 0)
//				return;
//			MainActivity.this.runOnUiThread(new Runnable() {
//				public void run() {
//					IVvsipService _service = VvsipService.getService();
//					if (_service == null)
//						return;
//					_service.initiateOutgoingCall(callee.getText().toString());
//				}
//			});
//			return;
//		}
//		/*
//		 * ����ͨ��
//		 */
//		if (v == mButton_endCall) {
//			VvsipCall pCall = null;
//			Log.e(mTag, "onClick1");
//			for (VvsipCall _pCall : mVvsipCalls) {
//				if(_pCall.cid > 0)
//					Log.e(mTag, "state#"+_pCall.mState);
//				if (_pCall.cid > 0 && _pCall.mState <= 2) {
//					pCall = _pCall;
//					break;
//				}
//			}
//			Log.e(mTag, "onClick2");
//			if (pCall == null)
//				return;
//			Log.e(mTag, "onClick3#"+pCall.mState);
//			IVvsipService _service = VvsipService.getService();
//			if (_service == null)
//				return;
//			VvsipTask _vvsipTask = _service.getVvsipTask();
//			if (_vvsipTask == null)
//				return;
//			pCall.stop();
//			_service.setSpeakerModeOff();
//			_service.stopPlayer();   //���йҶϣ�����ֹͣ;��������Closed��Released״̬�л������������ġ�
//									 //����HDL�Ǳ�û��Ч����������������һ�� ,By 20160822
//			_service.setAudioNormalMode();
//			return;
//		}
//
//		/*
//		 * �����绰
//		 */
//		if (v == mButton_offHook) {
//			for (VvsipCall _pCall : mVvsipCalls) {
//				if (_pCall.cid > 0 && _pCall.mState < 2 && _pCall.mIncomingCall) {
//					// ANSWER EXISTING CALL
//					int i = _pCall.answer(200, 1);
//					IVvsipService _service = VvsipService.getService();
//					if (_service != null) {
//						if (i >= 0) {
//							_service.stopPlayer();
//							_service.setSpeakerModeOff();
//							_service.setAudioInCallMode();
//						}
//					}
//					break;
//				}
//			}
//		}
//		/*
//		 * ������Ƶͨ��
//		 */
//		if (v == mButton_addVideo) {
//			MainActivity.this.runOnUiThread(new Runnable() {
//				public void run() {
//					Fragment fragment = null;
//					fragment = new FragmentInVideoCall();
//					FragmentManager frgManager = getFragmentManager();
//					FragmentTransaction ft = frgManager.beginTransaction()
//							.replace(R.id.content_frame, fragment);
//
//					ft.addToBackStack(null);
//					ft.commit();
//					FrameLayout fly = (FrameLayout) MainActivity.this
//							.findViewById(R.id.content_frame);
//					fly.bringToFront();
//				}
//			});
//		}
//
//		/*
//		 * ����DTMF
//		 */
//		if (v == mButton_sendDTMF) {
//			if (dtmf.getText().length() == 0)
//				return;
//			MainActivity.this.runOnUiThread(new Runnable() {
//				public void run() {
//					IVvsipService _service = VvsipService.getService();
//					if (_service == null)
//						return;
//					_service.sendDTMF(dtmf.getText().toString());
//				}
//			});
//			return;
//		}
//
//	}
//}
