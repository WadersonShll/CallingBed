///*
//  vvphone is a SIP app for android.
//  vvsip is a SIP library for softphone (SIP -rfc3261-)
//  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
// */
//
//package com.vvsip.amdemo;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import com.vvsip.ansip.IVvsipService;
//import com.vvsip.ansip.IVvsipServiceListener;
//import com.vvsip.ansip.VvsipCall;
//import com.vvsip.ansip.VvsipService;
//import com.vvsip.ansip.VvsipServiceBinder;
//import com.vvsip.ansip.VvsipTask;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.Html;
//import android.text.method.LinkMovementMethod;
//import android.text.method.MovementMethod;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.TextView;
//import android.os.IBinder;
//
//public class SplashActivity extends Activity implements IVvsipServiceListener {
//
//	protected int _splashTime = 3000;
//	protected Handler _exitHandler = null;
//	protected Runnable _exitRunnable = null;
//	protected Handler _startServiceHandler = null;
//	protected Runnable _startServiceRunnable = null;
//
//	private ServiceConnection connection;
//
//	private TextView mTextView_link;
//	private TextView mTextView_licenselink;
//	private TextView myVersion;
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		Log.i("ActivitySplash", "lifecycle // onCreate");
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.splash_layout);
//
//		mTextView_link = (TextView) findViewById(R.id.TextView_link);
//		if (mTextView_link != null) {
//			MovementMethod lMM = LinkMovementMethod.getInstance();
//			if (lMM != null) {
//				mTextView_link.setMovementMethod(lMM);
//				mTextView_link.setText(Html.fromHtml("www.vvsip.com"));
//			}
//			mTextView_licenselink = (TextView) findViewById(R.id.TextView_licencelink);
//			if (lMM != null) {
//				mTextView_licenselink.setMovementMethod(lMM);
//				mTextView_licenselink.setText(Html.fromHtml("vvsip.com"));
//			}
//		}
//
//		myVersion = (TextView) findViewById(R.id.my_version);
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//		String today = formatter.format(new Date());
//		if(today.compareTo("2016-02-08")>0){
//			myVersion.setVisibility(View.VISIBLE);
//			myVersion.setText(myVersion.getText()+"\n"+"This version has expired.Please contact QQ272108638");
//		}else{
//			myVersion.setVisibility(View.GONE);
//		}
//
//		// Runnable exiting the splash screen and launching the menu
//		_exitRunnable = new Runnable() {
//			public void run() {
//				exitSplash();
//			}
//		};
//		// Run the exitRunnable in in _splashTime ms
//		_exitHandler = new Handler();
//
//		IVvsipService _service = VvsipService.getService();
//		if (_service != null) {
//			_exitHandler.postDelayed(_exitRunnable, 0);
//			return;
//		}
//
//		_exitHandler.postDelayed(_exitRunnable, _splashTime);
//
//		_startServiceHandler = new Handler();
//
//		_startServiceRunnable = new Runnable() {
//			public void run() {
//
//				Intent intent = new Intent(SplashActivity.this.getApplicationContext(), VvsipService.class);
//				startService(intent);
//
//				connection = new ServiceConnection() {
//					public void onServiceConnected(ComponentName name, IBinder service) {
//						Log.i("ActivitySplash", "Connected!");
//						IVvsipService _service = ((VvsipServiceBinder) service).getService();
//						_service.addListener(SplashActivity.this);
//					}
//
//					public void onServiceDisconnected(ComponentName name) {
//						Log.i("ActivitySplash", "Disconnected!");
//					}
//				};
//
//				bindService(intent, connection, Context.BIND_AUTO_CREATE);
//				Log.i("ActivitySplash", "bindService done!");
//			}
//		};
//
//		_startServiceHandler.postDelayed(_startServiceRunnable, 0);
//	}
//
//	@Override
//	public void onDestroy() {
//		Log.i("ActivitySplash", "lifecycle // onDestroy");
//		super.onDestroy();
//
//		IVvsipService _service = VvsipService.getService();
//		if (_service != null) {
//			_service.removeListener(this);
//		}
//
//		_exitHandler.removeCallbacks(_startServiceRunnable);
//		_exitHandler.removeCallbacks(_exitRunnable);
//		if (connection != null) {
//			unbindService(connection);
//			connection = null;
//		}
//	}
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			// Remove the exitRunnable callback from the handler queue
//			_exitHandler.removeCallbacks(_exitRunnable);
//			// Run the exit code manually
//			exitSplash();
//		}
//		return true;
//	}
//
//	private void exitSplash() {
//		Log.i("ActivitySplash", "lifecycle // exitSplash");
//		VvsipTask vvsipTask = VvsipTask.getVvsipTask();
//		if (vvsipTask != null && VvsipTask.global_failure != 0) {
//			final AlertDialog.Builder b = new AlertDialog.Builder(this);
//			b.setIcon(R.drawable.ic_launcher);
//			b.setTitle(getString(R.string.app_name));
//			b.setMessage("global_installation_failure");
//
//			b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					finish();
//				}
//			});
//			b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					finish();
//				}
//			});
//			b.show();
//
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.setClass(this.getApplicationContext(), VvsipService.class);
//			stopService(intent);
//		} else {
//			finish();
//
//			Intent intent = new Intent();
//			intent.setClass(SplashActivity.this, MainActivity.class);
//			startActivity(intent);
//		}
//	}
//
//	@Override
//	public void onNewVvsipCallEvent(VvsipCall call) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onRemoveVvsipCallEvent(VvsipCall call) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onStatusVvsipCallEvent(VvsipCall call) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onRegistrationEvent(int rid, String remote_uri, final int code, String reason) {
//		SplashActivity.this.runOnUiThread(new Runnable() {
//			public void run() {
//				if (code >= 200 && code < 300) {
//					// Remove the exitRunnable callback from the handler queue
//					_exitHandler.removeCallbacks(_exitRunnable);
//					// Run the exit code manually
//					exitSplash();
//				}
//			}
//		});
//	}
//}
