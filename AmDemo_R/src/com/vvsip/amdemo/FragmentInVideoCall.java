/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.amdemo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.vvsip.ansip.IVvsipService;
import com.vvsip.ansip.IVvsipServiceListener;
import com.vvsip.ansip.VvsipCall;
import com.vvsip.ansip.VvsipService;
import com.vvsip.ansip.VvsipTask;
import com.vvsip.ansip.AudioInput;
import com.vvsip.ansip.AudioOutput;
import com.vvsip.ansip.VideoOrientationCompatibility;
import com.vvsip.ansip.video.VideoCameraPreview;

//import android.support.v7.app.ActionBar;
//import android.support.v7.app.ActionBarActivity;

// ----------------------------------------------------------------------

public class FragmentInVideoCall extends Fragment implements View.OnClickListener, IVvsipServiceListener {

	static private final String mTag = "FragmentInVideoCall";
	public static final String ARG_DID = "did";
	private int oldpriority = android.os.Process.THREAD_PRIORITY_DISPLAY;
	private List<VvsipCall> mVvsipCalls = null;

	// CAMERA PREVIEW DECLARATIONS
	private boolean mPrivacy = false;
	private VideoCameraPreview mPreview;

	private TextView mTextView_lossrate;
	private TextView mTextView_remotelossrate;
	private TextView mTextView_uploadrate;
	private TextView mTextView_downloadrate;
	private TextView mTextView_videolossrate;
	private TextView mTextView_videoremotelossrate;
	private TextView mTextView_videouploadrate;
	private TextView mTextView_videodownloadrate;
	private ImageButton mButton_hang;
	private ImageButton mButton_stopvideo;
	boolean display_statistics = false;

	VideoOrientationCompatibility videoOrientationCompatibility;
	private boolean useFrontFacingCamera = true;

	Camera mCamera;
	int numberOfCameras;
	int frontFacingCameraId;

	private int mStat_nb_outgoing_image = 0;
	int drop = 0;

	AsyncImagePoster imagePoster;
	BlockingQueue<AsyncImageObject> available_object;

	static class ImageHandler extends Handler {

	}

	ImageHandler imagePosterHandler = null;

	public class AsyncImageObject {
		public AsyncImageObject() {
			size = 0;
			data = null;
		}

		public AsyncImageObject(byte[] _data, int _size) {
			size = _size;
			data = new byte[size];
			System.arraycopy(_data, 0, data, 0, size);
		}

		public void update_image(byte[] _data, int _size) {
			if (size != _size || data == null) {
				data = new byte[_size];
			}
			size = _size;
			System.arraycopy(_data, 0, data, 0, size);
		}

		byte[] data;
		int size;
	}

	private static class AsyncImagePoster extends Thread {
		private OutputStream mRemoteVideoSenderStream = null;
		private Socket mRemoteVideoSender = null;
		int countImagePosted = 0;
		boolean imagePosterRunning = false;
		byte bSize[] = new byte[4];
		byte[] bytesStaticImage = null;

		private final WeakReference<FragmentInVideoCall> myWeakRef;

		public AsyncImagePoster(FragmentInVideoCall activity) {
			myWeakRef = new WeakReference<FragmentInVideoCall>(activity);
		}

		public void stopthread() {
			imagePosterRunning = false;
		}

		public void releaseressource() {
			if (mRemoteVideoSender != null) {
				try {
					mRemoteVideoSender.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mRemoteVideoSender = null;
			}
			myWeakRef.clear();
			bytesStaticImage = null;
			bSize = null;
		}

		static int slow_log = 0;

		private void connect() {
			if (mRemoteVideoSender == null) {
				try {
					mRemoteVideoSender = new Socket(InetAddress.getByName("127.0.0.1"), 5676);
				} catch (IOException e1) {
					if (slow_log == 0) {
						Log.e(mTag, "cannot connect to graph // video is probably not started");
					}
					slow_log++;
					if (slow_log > 100) {
						slow_log = 0;
					}

					mRemoteVideoSender = null;
				}
			}
			if (mRemoteVideoSender == null) {
				return;
			}
			if (mRemoteVideoSender != null) {
				try {
					mRemoteVideoSenderStream = mRemoteVideoSender.getOutputStream();
				} catch (IOException e2) {
					e2.printStackTrace();
					mRemoteVideoSenderStream = null;
					try {
						mRemoteVideoSender.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mRemoteVideoSender = null;
				}
			}

		}

		public void run() {

			int size = 352 * 288 * 3;
			if (bytesStaticImage == null) {
				bytesStaticImage = new byte[size];
				for (int i = 0; i < size; i++) {
					if (i % 3 == 0)
						bytesStaticImage[i] = 0;
					else
						bytesStaticImage[i] = (byte) 150;
				}
			}

			Looper.prepare();
			FragmentInVideoCall fragment = myWeakRef.get();

			fragment.imagePosterHandler = new ImageHandler() {

				@Override
				public void handleMessage(Message msg) {

					FragmentInVideoCall activity = myWeakRef.get();
					AsyncImageObject lAsyncImageObject = null;
					if (msg.what != -1) {
						lAsyncImageObject = (AsyncImageObject) msg.obj;
					}

					if (imagePosterRunning == false || msg.what == -1) {
						if (Looper.myLooper() != null)
							Looper.myLooper().quit();
						if (this.getLooper() != null)
							this.getLooper().quit();

						if (lAsyncImageObject != null) {
							if (activity.available_object.offer(lAsyncImageObject) == false) {
								Log.i(mTag, "available_object.offer // bug");
							}
						}
						return;
					}

					if (mRemoteVideoSenderStream == null) {
						connect();
					}
					if (mRemoteVideoSenderStream == null) {
						if (lAsyncImageObject != null) {
							if (activity.available_object.offer(lAsyncImageObject) == false) {
								Log.i(mTag, "available_object.offer // bug");
							}
						}
						return;
					}
					countImagePosted++;

					if (countImagePosted % 100 == 0) {
						if (activity.display_statistics == true) {
							activity.displayStatistics();
						}
					}
					if (lAsyncImageObject == null) {
						// not possible?
						return;
					}

					int width = msg.arg1;
					int height = msg.arg2;

					int size = lAsyncImageObject.size;

					int rotate_selfview_image = 0;
					boolean currentPrivacy = activity.mPrivacy;
					if (currentPrivacy == true) {
						width = 352;
						height = 288;
						size = width * height * 3;
						msg.what = 2;
					} else {
						// rotation only for webcam image
						rotate_selfview_image = activity.videoOrientationCompatibility.getImageRotation(activity.getResources()
								.getConfiguration().orientation, activity.useFrontFacingCamera == true
								&& activity.frontFacingCameraId != -1);
					}

					try {
						bSize[0] = (byte) size;
						bSize[1] = (byte) (size >>> 8);
						bSize[2] = (byte) (size >>> 16);
						bSize[3] = (byte) (size >>> 24);
						mRemoteVideoSenderStream.write(bSize, 0, 4);

						bSize[0] = (byte) msg.what;
						bSize[1] = (byte) rotate_selfview_image; // ROTATE: 0,
																	// 1, 2, 3
						mRemoteVideoSenderStream.write(bSize, 0, 2);
						bSize[0] = (byte) width;
						bSize[1] = (byte) (width >>> 8);
						mRemoteVideoSenderStream.write(bSize, 0, 2);
						bSize[0] = (byte) height;
						bSize[1] = (byte) (height >>> 8);
						mRemoteVideoSenderStream.write(bSize, 0, 2);

						if (currentPrivacy == true) {
							mRemoteVideoSenderStream.write(bytesStaticImage, 0, size);
						} else {
							mRemoteVideoSenderStream.write(lAsyncImageObject.data, 0, size);
						}
					} catch (NullPointerException e) {
						Log.i(mTag, "Disconnected from filter");
						mRemoteVideoSenderStream = null;
						try {
							mRemoteVideoSender.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						mRemoteVideoSender = null;
					} catch (Exception e) {
						Log.i(mTag, "Disconnected from filter");
						mRemoteVideoSenderStream = null;
						try {
							mRemoteVideoSender.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						mRemoteVideoSender = null;
					}
					if (activity.available_object.offer(lAsyncImageObject) == false) {
						Log.i(mTag, "available_object.offer // bug");
					}

				}
			};
			countImagePosted = 0;
			imagePosterRunning = true;

			connect();

			fragment = null; // THIS IS REQUIRED TO AVOID MEMORY LEAK = Don't
								// know the reason, but keep it.
			Looper.loop();
		}

	}

	int mCurrentWidth;
	int mCurrentHeight;
	int mCurrentFormat;
	int mCurrentLength;

	private void resetCameraFormat() {
		mCurrentWidth = 0;
		mCurrentHeight = 0;
		mCurrentFormat = 0;
		mCurrentLength = 0;
	}

	protected void displayStatistics() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					// first established
					VvsipCall pCall = null;
					for (VvsipCall _pCall : mVvsipCalls) {
						if (_pCall.cid > 0 && _pCall.mState == 2) {
							pCall = _pCall;
							break;
						}
					}
					if (pCall == null)
						return;

					mTextView_downloadrate.setVisibility(View.VISIBLE);
					mTextView_uploadrate.setVisibility(View.VISIBLE);
					mTextView_lossrate.setVisibility(View.VISIBLE);
					mTextView_remotelossrate.setVisibility(View.VISIBLE);
					mTextView_videodownloadrate.setVisibility(View.VISIBLE);
					mTextView_videouploadrate.setVisibility(View.VISIBLE);
					mTextView_videolossrate.setVisibility(View.VISIBLE);
					mTextView_videoremotelossrate.setVisibility(View.VISIBLE);

					IVvsipService _service = VvsipService.getService();
					if (_service == null)
						return;
					VvsipTask _vvsipTask = _service.getVvsipTask();
					if (_vvsipTask == null)
						return;

					float val;
					float val_upload;
					val = _vvsipTask.vvsessiongetaudiouploadbandwidth(pCall.did);
					mTextView_uploadrate.setText(String.format("up: %.02f Kb/s", val));
					val = _vvsipTask.vvsessiongetaudiodownloadbandwidth(pCall.did);
					mTextView_downloadrate.setText(String.format("down: %.02f Kb/s", val));
					val = _vvsipTask.vvsessiongetaudiopacketloss(pCall.did);
					mTextView_lossrate.setText(String.format("loss: %.2f%%", val));

					val = _vvsipTask.vvsessiongetaudioremotepacketloss(pCall.did);
					if (val==-1) {}
					else if (val>=0)
						mTextView_remotelossrate.setText(String.format("rloss: %.2f%%", val));
					while (val!=-2) {
						val = _vvsipTask.vvsessiongetaudioremotepacketloss(pCall.did);
						if (val==-1) {}
						else if (val>=0)
							mTextView_remotelossrate.setText(String.format("rloss: %.2f%%", val));
					}
					
					val_upload = _vvsipTask.vvsessiongetvideouploadbandwidth(pCall.did);
					mTextView_videouploadrate.setText(String.format("up: %.02f Kb/s", val_upload));
					val = _vvsipTask.vvsessiongetvideodownloadbandwidth(pCall.did);
					mTextView_videodownloadrate.setText(String.format("down: %.02f Kb/s", val));
					val = _vvsipTask.vvsessiongetvideopacketloss(pCall.did);
					mTextView_videolossrate.setText(String.format("loss: %.2f%%", val));

					float remoteloss = 0;
					val = _vvsipTask.vvsessiongetvideoremotepacketloss(pCall.did);
					if (val==-1) {}
					else if (val>=0) {
						if (val>10.0 && val_upload>128) {
							remoteloss=val;
						}
						mTextView_videoremotelossrate.setText(String.format("rloss: %.2f%%", val));
					}
					while (val!=-2) {
						val = _vvsipTask.vvsessiongetvideoremotepacketloss(pCall.did);
						if (val==-1) {}
						else if (val>=0) {
							if (val>10.0 && val_upload>128)
								remoteloss=val;
							else
								remoteloss=0;
							mTextView_videoremotelossrate.setText(String.format("rloss: %.2f%%", val));
						}
					}
					if (remoteloss>0) {
						//check only last one
						Toast.makeText(getActivity().getApplicationContext(), "Quality issue detected! bitrate is reduced", Toast.LENGTH_SHORT).show();
						Log.i(mTag, "Decreasing upload bitrate by 50% (val_upload=" + val_upload + " remote loss=" + remoteloss);
						_vvsipTask.vvsessionadaptvideobitrate(pCall.did, 50);
					}
				} catch (Exception e) {
				}
			}
		});
	}


	private final Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {

		public void onPreviewFrame(byte[] data, Camera arg1) {

			if (Build.VERSION.SDK_INT <= 4) {
				// slow down for old CPU?
				drop++;
				if (drop % 15 != 0) {
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						arg1.addCallbackBuffer(data);
					}
					return;
				}
			}
			if (data == null) {
				Log.i(mTag, "Empty data provided");
				return;
			}

			mStat_nb_outgoing_image++;
			if (mStat_nb_outgoing_image % 100 == 0)
				Log.i(mTag, "stat: nb_outgoing_image=" + mStat_nb_outgoing_image);

			if (mCurrentWidth == 0) {
				try {
					Parameters mParameters = arg1.getParameters();
					Size mCurrentSize = mParameters.getPreviewSize();
					mCurrentWidth = mCurrentSize.width;
					mCurrentHeight = mCurrentSize.height;
					mCurrentFormat = mParameters.getPreviewFormat();
					mParameters = null;
				} catch (Exception e) {
					Log.i(mTag, "camera is most probably in a wrong state");
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						arg1.addCallbackBuffer(data);
					}
					return;
				}
				if (mCurrentFormat != ImageFormat.NV21) // PixelFormat.YCbCr_420_SP)
				{
					mCurrentWidth = 0;
					mCurrentHeight = 0;
					mCurrentFormat = 0;
					mCurrentLength = 0;
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						arg1.addCallbackBuffer(data);
					}
					return;
				}

				if (Build.VERSION.SDK_INT <= 7) {
					mCurrentLength = mCurrentWidth * mCurrentHeight * 12 / 8;
				} else {
					try {
						Class<?> cameraClass = Class.forName("android.graphics.ImageFormat");
						Method getBitsPerPixelMethod = cameraClass.getMethod("getBitsPerPixel", int.class);
						if (getBitsPerPixelMethod != null) {
							mCurrentLength = mCurrentWidth * mCurrentHeight * (Integer) getBitsPerPixelMethod.invoke(null, mCurrentFormat)
									/ 8;
						}
					} catch (ClassNotFoundException e) {
						Log.e(mTag, "ClassNotFoundException" + e.getLocalizedMessage());
					} catch (NoSuchMethodException e) {
						Log.e(mTag, "NoSuchMethodException" + e.getLocalizedMessage());
					} catch (IllegalAccessException e) {
						Log.e(mTag, "IllegalAccessException" + e.getLocalizedMessage());
					} catch (InvocationTargetException e) {
						Log.e(mTag, "InvocationTargetException" + e.getLocalizedMessage());
					} catch (SecurityException e) {
						Log.e(mTag, "SecurityException" + e.getLocalizedMessage());
					}
				}

			}

			if (imagePoster == null) {
				available_object = new ArrayBlockingQueue<FragmentInVideoCall.AsyncImageObject>(2);
				AsyncImageObject obj = new AsyncImageObject();
				boolean added = available_object.add(obj);
				if (added == false) {
					Log.e(mTag, "available_object.add failure");
				}

				imagePoster = new AsyncImagePoster(FragmentInVideoCall.this);
				imagePoster.start();
			}
			if (imagePosterHandler == null) {
				if (android.os.Build.VERSION.SDK_INT >= 8) {
					arg1.addCallbackBuffer(data);
				}
				return;
			}

			AsyncImageObject lAsyncImageObject = available_object.poll();
			if (lAsyncImageObject == null) {
				Log.e(mTag, "available_object is empty?");
				if (android.os.Build.VERSION.SDK_INT >= 8) {
					arg1.addCallbackBuffer(data);
				}
				return; /* already an image being processed */
			}
			try {
				lAsyncImageObject.update_image(data, mCurrentLength);
			} catch (Exception e) {
				Log.e(mTag, "Failed to copy data into lAsyncImageObject -- Exception:" + e.getMessage());

				if (available_object.offer(lAsyncImageObject) == false) {
					Log.i(mTag, "available_object.offer // bug");
				}
				if (android.os.Build.VERSION.SDK_INT >= 8) {
					arg1.addCallbackBuffer(data);
				}
				return;
			}

			Message m = Message.obtain(); // new Message();

			if (Build.VERSION.SDK_INT <= 4) {
				m.what = 8; // MS_NV21
			} else {
				if (mCurrentFormat == ImageFormat.NV21) {
					if (android.os.Build.MODEL.equalsIgnoreCase("Titanium"))
						m.what = 9; // MS_NV12
					else
						m.what = 8; // MS_NV21
				} else if (mCurrentFormat == ImageFormat.YUY2)
					m.what = 6; // MS_YUY2
			}
			m.arg1 = mCurrentWidth;
			m.arg2 = mCurrentHeight;

			m.obj = lAsyncImageObject;

			imagePosterHandler.sendMessage(m);

			if (android.os.Build.VERSION.SDK_INT >= 8) {
				arg1.addCallbackBuffer(data);
			}

		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

		Integer rotate_selfview_display;
		try {
			rotate_selfview_display = videoOrientationCompatibility.getDisplayRotation(newConfig.orientation, useFrontFacingCamera == true && frontFacingCameraId != -1);
			mPreview.setRotateValue(rotate_selfview_display);
			mCamera.setDisplayOrientation(rotate_selfview_display);
		} catch (Exception exception) {
			Log.e(mTag, "Exception raised", exception);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i(mTag, "lifecycle // onCreate");
		super.onCreate(savedInstanceState);

		mStat_nb_outgoing_image = 0;

		Window w = getActivity().getWindow();
		final int keepScreenOnFlag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		if ((w.getAttributes().flags & keepScreenOnFlag) == 0) {
			w.addFlags(keepScreenOnFlag);
		}

		resetCameraFormat();

		numberOfCameras = GingerBread_getNumberOfCamerasMethod();
		frontFacingCameraId = GingerBread_getFrontFacingCameraId();

		if (Build.VERSION.SDK_INT <= 7) {
			Toast.makeText(getActivity().getApplicationContext(), "no video", Toast.LENGTH_LONG).show();
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(mTag, "lifecycle // onStart");

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(mTag, "lifecycle // onResume");

		mStat_nb_outgoing_image = 0;

		oldpriority = android.os.Process.getThreadPriority((int) Thread.currentThread().getId());
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);

		if (mPreview == null) {
			return;
		}

		SharedPreferences mConfiguration = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		if (mConfiguration.getBoolean("key_video_disablecamera", false) == false) {
			mCamera = FindFrontFacingCamera();
			if (mCamera != null) {
				// we start the preview only if the surface is already created
				if (mPreview.hasActiveSurface()) {
					mPreview.switchCamera(mCamera);
					try {
						mCamera.startPreview();
						Log.i(mTag, "mCamera.startPreview() ?");
					} catch (Exception exception) {
						Log.e(mTag, "Exception caused by startPreview()", exception);
					}
				} else {
					mPreview.setCamera(mCamera);
				}
			}

		}

		if (mVvsipCalls == null) {
			mVvsipCalls = new ArrayList<VvsipCall>();
		}

		IVvsipService _service = VvsipService.getService();
		if (_service != null)
			_service.addListener(this);

		// first established
		VvsipCall pCall = null;
		for (VvsipCall _pCall : mVvsipCalls) {
			if (_pCall.cid > 0 && _pCall.mState == 2) {
				pCall = _pCall;
				break;
			}
		}
		if (pCall == null)
			return;
		if (_service != null && pCall.did > 0) {
			_service.setSpeakerModeOn();
			AudioInput.restart = true;
			AudioOutput.restart = true;
		}
	}

	@Override
	public void onPause() {
		Log.i(mTag, "lifecycle // onPause");
		android.os.Process.setThreadPriority(oldpriority);
		super.onPause();

		if (imagePoster != null) {
			imagePoster.stopthread();
		}

		// release
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
			resetCameraFormat();
		}

		if (imagePoster != null) {
			try {
				imagePoster.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			imagePoster.releaseressource();
			imagePoster = null;
		}
		if (imagePosterHandler != null) {
			Log.e(mTag, "available_object checking for message in the queue");
			imagePosterHandler.removeCallbacksAndMessages(null);
			if (available_object != null) {
				if (available_object.peek() == null) {
					Log.e(mTag, "available_object.peek shows an empty BlockingQueue: Object was lost during stop");
					AsyncImageObject obj = new AsyncImageObject();
					boolean added = available_object.add(obj);
					if (added == false) {
						Log.e(mTag, "available_object.add failure");
					}
				}
			}
		}
		imagePosterHandler = null;

		IVvsipService _service = VvsipService.getService();
		if (_service != null) {
			_service.setSpeakerModeOff();
			AudioInput.restart = true;
			AudioOutput.restart = true;
		}

		if (_service != null) {
			_service.removeListener(this);
		}
		mVvsipCalls.clear();
		mVvsipCalls = null;
	}

	@Override
	public void onDestroy() {
		Log.i(mTag, "lifecycle // onDestroy");

		super.onDestroy();
	}

	/* Return the view that is used by the fragment */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.video_camera, container, false);
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("FragmentInVideoCall", "lifecycle // onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mPreview = (VideoCameraPreview) getActivity().findViewById(R.id.camera_preview2);

		SurfaceView remoteView = (SurfaceView) getActivity().findViewById(R.id.video_view2);

		mTextView_uploadrate = (TextView) getActivity().findViewById(R.id.TextView_uploadrate);
		mTextView_downloadrate = (TextView) getActivity().findViewById(R.id.TextView_downloadrate);
		mTextView_lossrate = (TextView) getActivity().findViewById(R.id.TextView_lossrate);
		mTextView_remotelossrate = (TextView) getActivity().findViewById(R.id.TextView_remotelossrate);
		mTextView_videouploadrate = (TextView) getActivity().findViewById(R.id.TextView_videouploadrate);
		mTextView_videodownloadrate = (TextView) getActivity().findViewById(R.id.TextView_videodownloadrate);
		mTextView_videolossrate = (TextView) getActivity().findViewById(R.id.TextView_videolossrate);
		mTextView_videoremotelossrate = (TextView) getActivity().findViewById(R.id.TextView_videoremotelossrate);

		mButton_hang = (ImageButton) getActivity().findViewById(R.id.Button_hang);
		mButton_stopvideo = (ImageButton) getActivity().findViewById(R.id.Button_stopvideo);

		mButton_hang.setVisibility(View.INVISIBLE);
		mButton_stopvideo.setVisibility(View.INVISIBLE);
		mButton_hang.setOnClickListener(this);
		mButton_stopvideo.setOnClickListener(this);

		remoteView.setOnTouchListener(new SurfaceView.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (mButton_hang.getVisibility() == View.INVISIBLE) {
						mButton_hang.setVisibility(View.VISIBLE);
						mButton_stopvideo.setVisibility(View.VISIBLE);
						display_statistics = true;
					} else {
						mButton_hang.setVisibility(View.INVISIBLE);
						mButton_stopvideo.setVisibility(View.INVISIBLE);
						display_statistics = false;
					}
					if (display_statistics == true) {
						mTextView_downloadrate.setVisibility(View.VISIBLE);
						mTextView_uploadrate.setVisibility(View.VISIBLE);
						mTextView_lossrate.setVisibility(View.VISIBLE);
						mTextView_remotelossrate.setVisibility(View.VISIBLE);
						mTextView_videodownloadrate.setVisibility(View.VISIBLE);
						mTextView_videouploadrate.setVisibility(View.VISIBLE);
						mTextView_videolossrate.setVisibility(View.VISIBLE);
						mTextView_videoremotelossrate.setVisibility(View.VISIBLE);

						mTextView_uploadrate.setText("up: 0.00 Kb/s");
						mTextView_downloadrate.setText("down: 0.00 Kb/s");
						mTextView_lossrate.setText("loss: 0.00%");
						mTextView_remotelossrate.setText("loss: 0.00%");

						mTextView_videouploadrate.setText("up: 0.00 Kb/s");
						mTextView_videodownloadrate.setText("down: 0.00 Kb/s");
						mTextView_videolossrate.setText("loss: 0.00%");
						mTextView_videoremotelossrate.setText("loss: 0.00%");
						displayStatistics();

					} else {
						mTextView_downloadrate.setVisibility(View.GONE);
						mTextView_uploadrate.setVisibility(View.GONE);
						mTextView_lossrate.setVisibility(View.GONE);
						mTextView_remotelossrate.setVisibility(View.GONE);
						mTextView_videodownloadrate.setVisibility(View.GONE);
						mTextView_videouploadrate.setVisibility(View.GONE);
						mTextView_videolossrate.setVisibility(View.GONE);
						mTextView_videoremotelossrate.setVisibility(View.GONE);
					}

					v.performClick();
					return true;
				case MotionEvent.ACTION_MOVE:
					return false;
				case MotionEvent.ACTION_DOWN:
					return true;
				}
				return false;
			}

		});

		mPreview.setPreviewCallback(mPreviewCallback);
		Integer rotate_selfview_display;
		try {
			SharedPreferences mConfiguration = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			videoOrientationCompatibility = new VideoOrientationCompatibility(mConfiguration);
			rotate_selfview_display = videoOrientationCompatibility.getDisplayRotation(getResources().getConfiguration().orientation,
					useFrontFacingCamera == true && frontFacingCameraId != -1);
			mPreview.setRotateValue(rotate_selfview_display);
		} catch (Exception exception) {
			Log.e(mTag, "Exception raised", exception);
		}

//		mPreview.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		Log.e(mTag, "onClick");
		if (v == mButton_hang) {
			// first established
			VvsipCall pCall = null;
			Log.e(mTag, "onClick1");
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.mState == 2) {
					pCall = _pCall;
					break;
				}
			}
			Log.e(mTag, "onClick2");
			if (pCall == null){
				getFragmentManager().popBackStack();
				return;
			}
			Log.e(mTag, "onClick3");
			IVvsipService _service = VvsipService.getService();
			if (_service == null)
				return;
			VvsipTask _vvsipTask = _service.getVvsipTask();
			if (_vvsipTask == null)
				return;
			pCall.stop();
			_service.setSpeakerModeOff();
			getFragmentManager().popBackStack();
			return;
		}

		if (v == mButton_stopvideo) {
			// first established
			VvsipCall pCall = null;
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.mState == 2) {
					pCall = _pCall;
					break;
				}
			}
			if (pCall == null)
				return;
			IVvsipService _service = VvsipService.getService();
			if (_service == null)
				return;
			VvsipTask _vvsipTask = _service.getVvsipTask();
			if (_vvsipTask == null)
				return;
			_service.setSpeakerModeOff();
			getFragmentManager().popBackStack();
			return;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

//		inflater.inflate(R.menu.menu_video, menu);
	}


	/**
	 * Open the camera. First attempt to find and open the front-facing camera.
	 * If that attempt fails, then fall back to whatever camera is available.
	 *
	 * @return a Camera object
	 */
	private int GingerBread_getNumberOfCamerasMethod() {
		int cameraCount = 0;

		PackageManager pm = getActivity().getPackageManager();

		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {

			cameraCount = 1;

			if (Build.VERSION.SDK_INT >= 9) {
				try {
					Class<?> cameraClass = Class.forName("android.hardware.Camera");
					Method getNumberOfCamerasMethod = cameraClass.getMethod("getNumberOfCameras");
					if (getNumberOfCamerasMethod != null) {
						cameraCount = (Integer) getNumberOfCamerasMethod.invoke(null, (Object[]) null);
					}
				} catch (ClassNotFoundException e) {
					Log.e(mTag, "ClassNotFoundException" + e.getLocalizedMessage());
				} catch (NoSuchMethodException e) {
					Log.e(mTag, "NoSuchMethodException" + e.getLocalizedMessage());
				} catch (IllegalAccessException e) {
					Log.e(mTag, "IllegalAccessException" + e.getLocalizedMessage());
				} catch (InvocationTargetException e) {
					Log.e(mTag, "InvocationTargetException" + e.getLocalizedMessage());
				} catch (SecurityException e) {
					Log.e(mTag, "SecurityException" + e.getLocalizedMessage());
				}
			}
		}

		return cameraCount;
	}

	/**
	 * Open the camera. First attempt to find and open the front-facing camera.
	 * If that attempt fails, then fall back to whatever camera is available.
	 *
	 * @return a Camera object
	 */
	private int GingerBread_getFrontFacingCameraId() {
		PackageManager pm = getActivity().getPackageManager();

		// FEATURE_CAMERA_FRONT only exist in API 9
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {

			if (Build.VERSION.SDK_INT >= 9) {
				try {
					Class<?> cameraClass = Class.forName("android.hardware.Camera");
					Object cameraInfo = null;
					Field field = null;

					Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
					if (cameraInfoClass != null) {
						cameraInfo = cameraInfoClass.newInstance();
					}
					if (cameraInfo != null) {
						field = cameraInfo.getClass().getField("facing");
					}
					Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo", Integer.TYPE, cameraInfoClass);
					if (getCameraInfoMethod != null && cameraInfoClass != null && field != null) {
						for (int camIdx = 0; camIdx < numberOfCameras; camIdx++) {
							getCameraInfoMethod.invoke(null, camIdx, cameraInfo);
							int facing = field.getInt(cameraInfo);
							if (facing == 1 || numberOfCameras == 1) { // Camera.CameraInfo.CAMERA_FACING_FRONT
								try {
									Method cameraOpenMethod = cameraClass.getMethod("open", Integer.TYPE);
									if (cameraOpenMethod != null) {
										return camIdx;
									}
								} catch (RuntimeException e) {
									Log.e(mTag, "Camera failed to open: " + e.getLocalizedMessage());
								}
							}
						}
					}
				} catch (ClassNotFoundException e) {
					Log.e(mTag, "ClassNotFoundException" + e.getLocalizedMessage());
				} catch (NoSuchMethodException e) {
					Log.e(mTag, "NoSuchMethodException" + e.getLocalizedMessage());
				} catch (NoSuchFieldException e) {
					Log.e(mTag, "NoSuchFieldException" + e.getLocalizedMessage());
				} catch (IllegalAccessException e) {
					Log.e(mTag, "IllegalAccessException" + e.getLocalizedMessage());
				} catch (InvocationTargetException e) {
					Log.e(mTag, "InvocationTargetException" + e.getLocalizedMessage());
				} catch (SecurityException e) {
					Log.e(mTag, "SecurityException" + e.getLocalizedMessage());
				} catch (java.lang.InstantiationException e) {
					Log.e(mTag, "java.lang.InstantiationException" + e.getLocalizedMessage());
				}
			}
		}

		return -1;
	}

	/**
	 * Open the camera. First attempt to find and open the front-facing camera.
	 * If that attempt fails, then fall back to whatever camera is available.
	 *
	 * @return a Camera object
	 */
	private Camera GingerBread_openCamera(int cameraId) {
		Camera camera = null;

		PackageManager pm = getActivity().getPackageManager();

		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {

			if (Build.VERSION.SDK_INT >= 9) {
				try {
					Class<?> cameraClass = Class.forName("android.hardware.Camera");

					try {
						Method cameraOpenMethod = cameraClass.getMethod("open", Integer.TYPE);
						if (cameraOpenMethod != null) {
							camera = (Camera) cameraOpenMethod.invoke(null, cameraId);
						}
						if (camera != null) {

							Integer orientation = getResources().getConfiguration().orientation;
							Method cameraSetDisplayOrientation = cameraClass.getMethod("setDisplayOrientation", Integer.TYPE);
							if (cameraSetDisplayOrientation != null) {
								cameraSetDisplayOrientation.invoke(camera,
										videoOrientationCompatibility.getDisplayRotation(orientation, frontFacingCameraId == cameraId));

								Log.i(mTag, "TEST orientation = " + orientation);
								Log.i(mTag, "TEST getActivity().getRequestedOrientation() = " + getActivity().getRequestedOrientation());
								Log.i(mTag,
										"TEST videoOrientationCompatibility.getDisplayRotation(getResources().getConfiguration().orientation, frontFacingCameraId == cameraId) = "
												+ videoOrientationCompatibility.getDisplayRotation(
														getResources().getConfiguration().orientation, frontFacingCameraId == cameraId));
							}
						}

					} catch (RuntimeException e) {
						Log.e(mTag, "Camera failed to open: " + e.getLocalizedMessage());
					}
				} catch (ClassNotFoundException e) {
					Log.e(mTag, "ClassNotFoundException" + e.getLocalizedMessage());
				} catch (NoSuchMethodException e) {
					Log.e(mTag, "NoSuchMethodException" + e.getLocalizedMessage());
				} catch (IllegalAccessException e) {
					Log.e(mTag, "IllegalAccessException" + e.getLocalizedMessage());
				} catch (InvocationTargetException e) {
					Log.e(mTag, "InvocationTargetException" + e.getLocalizedMessage());
				} catch (SecurityException e) {
					Log.e(mTag, "SecurityException" + e.getLocalizedMessage());
				}
			}
		}

		return camera;
	}

	private Camera FindFrontFacingCamera() {

		Camera mCamera = null;

		PackageManager pm = getActivity().getPackageManager();

		if (numberOfCameras < 1)
			return null;

		if (Build.VERSION.SDK_INT >= 9) {
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false
					&& pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) == false)
				return null;
		} else {
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false)
				return null;
		}
		if (useFrontFacingCamera == true) {
			if (Build.VERSION.SDK_INT >= 9) {
				return GingerBread_openCamera(frontFacingCameraId);
			}

			try {
				Method method = Class.forName("android.hardware.HtcFrontFacingCamera").getDeclaredMethod("getCamera", (Class[]) null);
				mCamera = (Camera) method.invoke(null, (Object[]) null);
				Log.i(mTag, "camera selected: android.hardware.HtcFrontFacingCamera");
			} catch (Exception exception) {
				mCamera = null;
			}

			if (mCamera == null) {
				try {
					Method method = Class.forName("com.sprint.hardware.twinCamDevice.FrontFacingCamera").getDeclaredMethod(
							"getFrontFacingCamera", (Class[]) null);
					mCamera = (Camera) method.invoke(null, (Object[]) null);
					Log.i(mTag, "camera selected: com.sprint.hardware.twinCamDevice.FrontFacingCamera");
				} catch (Exception exception) {
					mCamera = null;
				}
			}

			if (mCamera == null) {
				try {
					// Huawei U8230
					Method method = Class.forName("android.hardware.CameraSlave").getDeclaredMethod("open", (Class[]) null);
					mCamera = (Camera) method.invoke(null, (Object[]) null);
					Log.i(mTag, "camera selected: android.hardware.CameraSlave");
				} catch (Exception exception) {
					mCamera = null;
				}
			}
		}

		if (mCamera == null) {
			try {
				if (Build.VERSION.SDK_INT >= 9) {
					return GingerBread_openCamera((frontFacingCameraId + 1) % numberOfCameras);
				}

				mCamera = Camera.open();
				if (useFrontFacingCamera == true) {
					Method lDualCameraSwitchMethod = null;
					try {
						lDualCameraSwitchMethod = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch", int.class);
					} catch (Exception e) {
						lDualCameraSwitchMethod = null;
					}
					if (lDualCameraSwitchMethod != null) {
						lDualCameraSwitchMethod.invoke(mCamera, (int) 1);
						Log.i(mTag, "camera selected: android.hardware.Camera");
					} else {

						if (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-")
								|| android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("SHW-")) {
							// if
							// (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1010")==false)
							// { // GT-P1010 -> camera-id=2 doesn't work?
							Camera.Parameters parameters = mCamera.getParameters();
							parameters.set("camera-id", 2);
							mCamera.setParameters(parameters);
							Log.i(mTag, "camera selected: camera-id 2 as parameter");
							// }
						}
					}

				}
			} catch (Exception exception) {
				exception.printStackTrace();
				if (mCamera != null) {
					mCamera.release();
				}
				mCamera = null;
			}
		}

		return mCamera;
	}

	@Override
	public void onNewVvsipCallEvent(VvsipCall call) {
		try {
			if (call == null) {
				return;
			}

			if (mVvsipCalls==null)
				return;
			
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid == call.cid && _pCall.did == call.did) {
					Log.e("FragmentInVideoCall", "Adding twice the same call on FragmentInVideoCall");
					return;
				}
			}

			mVvsipCalls.add(call);
		} catch (Exception e) {
			Log.e(mTag, "onNewVvsipCallEvent: "+e);
		}
	}

	@Override
	public void onRemoveVvsipCallEvent(VvsipCall call) {

		try {
			if (mVvsipCalls==null)
				return;
			
	    	mVvsipCalls.remove(call);
	    	
			// first established
			VvsipCall pCall = null;
			for (VvsipCall _pCall : mVvsipCalls) {
				if (_pCall.cid > 0 && _pCall.mState == 2) {
					pCall = _pCall;
					break;
				}
			}
			
			if (pCall == null) {
				getFragmentManager().popBackStack();
			}
		} catch (Exception e) {
			Log.e(mTag, "onRemoveVvsipCallEvent: "+e);
		}
	}

	@Override
	public void onStatusVvsipCallEvent(VvsipCall call) {

		// first established
		VvsipCall pCall = null;
		for (VvsipCall _pCall : mVvsipCalls) {
			if (_pCall.cid > 0 && _pCall.mState == 2) {
				pCall = _pCall;
				break;
			}
		}
		
		if (pCall == null) {
			getFragmentManager().popBackStack();
		}
	}

	@Override
	public void onRegistrationEvent(int rid, String remote_uri, int code, String reason) {

	}

}
