/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.ansip.video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 *  Camera SurfaceView的封装
 */
public class VideoCameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "VideoCameraPreview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	List<Integer> mSupportedFormats;
	Camera mCamera;

	private Camera.PreviewCallback mPreviewCallback = null;
	private byte[] camera_preview_buffer = null;
	DisplayMetrics metrics = new DisplayMetrics();

	private Integer rotate_selfview_display = 0;

	private boolean mHasActiveSurface=false;

	public VideoCameraPreview(Context context, AttributeSet attributes) {
		super(context, attributes);

		mSurfaceView = new SurfaceView(context);
		mSurfaceView.setZOrderOnTop(true);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setPreviewCallback(PreviewCallback previewCallback) {
		mPreviewCallback = previewCallback;
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			Camera.Parameters params;
			try {
				params = mCamera.getParameters();
			} catch (Exception e) {
				// detected by report: java.lang.RuntimeException: getParameters
				// failed (empty parameters)
				Log.e(TAG, "Exception caused by mCamera.getParameters()", e);
				mCamera = null;
				return;
			}

			if (Build.VERSION.SDK_INT <= 4) {
				String previewSizeValueString = params.get("preview-size-values");
				Size lSize = params.getPreviewSize();
				if (previewSizeValueString != null && previewSizeValueString.length() > 0) {
					if (previewSizeValueString.contains("176x144")) {
						lSize.width = 176;
						lSize.height = 144;
					} else if (previewSizeValueString.contains("320x240")) {
						lSize.width = 320;
						lSize.height = 240;
					} else if (previewSizeValueString.contains("352x288")) {
						lSize.width = 352;
						lSize.height = 288;
					} else if (previewSizeValueString.contains("640x480")) {
						lSize.width = 640;
						lSize.height = 480;
					} else if (previewSizeValueString.contains("480x320")) {
						lSize.width = 480;
						lSize.height = 320;
					}
					// else, choose the current defined size...
				}

				int lFormat = params.getPreviewFormat();

				mSupportedFormats = new ArrayList<Integer>();
				mSupportedFormats.add(lFormat);

				Log.i("VvsipVideoPreview", "using " + lSize.width + "x" + lSize.height + " for camera size");

				mSupportedPreviewSizes = new ArrayList<Size>();
				mSupportedPreviewSizes.add(lSize);
			} else {
				mSupportedPreviewSizes = params.getSupportedPreviewSizes();
				Log.i("VvsipVideoPreview", "params.getSupportedPreviewSizes " + mSupportedPreviewSizes);

				try {
					mSupportedFormats = params.getSupportedPreviewFormats();
				} catch (Exception e) {
					mSupportedFormats = null;
				}

				if (mSupportedFormats == null) {
					int lFormat = params.getPreviewFormat();
					mSupportedFormats = new ArrayList<Integer>();
					mSupportedFormats.add(lFormat);
				}
			}
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
			if (android.os.Build.VERSION.SDK_INT < 8) {
				camera.setPreviewCallback(mPreviewCallback);
			} else {
				camera.setPreviewCallbackWithBuffer(mPreviewCallback);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}

		if (mSupportedFormats != null) {
			getOptimalSupportedFormat(mSupportedFormats);
		}

		if (mSupportedPreviewSizes != null) {
			SharedPreferences mConfiguration;
			mConfiguration = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			String val = mConfiguration.getString("key_video_size", "default");
			if (val == null || val.compareToIgnoreCase("0") == 0 || val.compareToIgnoreCase("1") == 0
					|| val.compareToIgnoreCase("default") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 320, 240);
			} else if (val.compareToIgnoreCase("small") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 240, 160);
			} else if (val.compareToIgnoreCase("normal") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 320, 240);
			} else if (val.compareToIgnoreCase("large") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 640, 480);
			} else if (val.compareToIgnoreCase("hd") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 1280, 720);
			} else {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 320, 240);
			}

			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
			// width, heigth);
			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 240,
			// 160);
			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 352,
			// 288);
		}

		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		if (Build.VERSION.SDK_INT <= 4) {
			parameters.set("preview-frame-rate", 1);
			parameters.setPreviewFrameRate(1);
		}

		/* SIBO-Q899 failure? */
		try  {
			camera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG, "Exception for camera.setParameters(parameters)", e);
			camera_preview_buffer = null;
		}
		
		try {
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				if (camera_preview_buffer == null)
					camera_preview_buffer = new byte[1800000];
				camera.addCallbackBuffer(camera_preview_buffer);
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception for allocation of camera_preview_buffer", e);
			camera_preview_buffer = null;
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		// final int width = resolveSize(getSuggestedMinimumWidth(),
		// widthMeasureSpec);
		// final int height = resolveSize(getSuggestedMinimumHeight(),
		// heightMeasureSpec);
		// setMeasuredDimension(width, height);

		WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);

		float screen_Xsize = metrics.widthPixels / metrics.xdpi;
		float screen_Ysize = metrics.heightPixels / metrics.ydpi;
		Log.i("VideoCameraPreview", "screen size = " + screen_Xsize + "x" + screen_Ysize + "inch " + metrics.widthPixels + "x"
				+ metrics.heightPixels + "px");

		// example on nexus S:
		// 3.4120736x2.031496 //landscape nexus S
		// 2.047244x3.3858268 //portait nexus S
		// if (screen_Xsize>screen_Ysize) {
		// if (screen_Xsize<4.5)
		// setMeasuredDimension(160, 120);
		// else if (metrics.widthPixels/320>2)
		// setMeasuredDimension(320, 240);
		// else
		// setMeasuredDimension(160, 120);
		// } else {
		// if (screen_Ysize<4.5)
		// setMeasuredDimension(160, 120);
		// else if (metrics.heightPixels/240>2)
		// setMeasuredDimension(320, 240);
		// else
		// setMeasuredDimension(160, 120);
		// }

		if (mSupportedFormats != null) {
			getOptimalSupportedFormat(mSupportedFormats);
		}

		if (mSupportedPreviewSizes != null) {
			SharedPreferences mConfiguration;
			mConfiguration = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			String val = mConfiguration.getString("key_video_size", "default");
			if (val == null || val.compareToIgnoreCase("0") == 0 || val.compareToIgnoreCase("1") == 0
					|| val.compareToIgnoreCase("default") == 0) {
				Log.w("VvsipService", "old config found");
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 320, 240);
			} else if (val.compareToIgnoreCase("small") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 240, 160);
			} else if (val.compareToIgnoreCase("normal") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 320, 240);
			} else if (val.compareToIgnoreCase("large") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 640, 480);
			} else if (val.compareToIgnoreCase("hd") == 0) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 1280, 720);
			}
			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
			// width, heigth);
			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 240,
			// 160);
			// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 352,
			// 288);
		}
		if (mPreviewSize != null) {
			// Log.i("VideoCameraPreview",
			// "onMeasure done // rotate_selfview_display = " +
			// rotate_selfview_display + " optimal size = " + mPreviewSize.width
			// + "x" + mPreviewSize.height);

			int mPreviewSize_width;
			int mPreviewSize_height;
			if (rotate_selfview_display == 90 || rotate_selfview_display == 270) {
				mPreviewSize_width = mPreviewSize.height;
				mPreviewSize_height = mPreviewSize.width;
			} else {
				mPreviewSize_width = mPreviewSize.width;
				mPreviewSize_height = mPreviewSize.height;
			}
			float imageSideRatio = (float) mPreviewSize_width / (float) mPreviewSize_height;
			float viewSideRatio = (float) MeasureSpec.getSize(widthMeasureSpec) / (float) MeasureSpec.getSize(heightMeasureSpec);

			if (imageSideRatio >= viewSideRatio) {
				// Image is wider than the display (ratio)
				int width = MeasureSpec.getSize(widthMeasureSpec);
				int height = (int) (width / imageSideRatio);
				setMeasuredDimension(width, height);
			} else {
				// Image is taller than the display (ratio)
				int height = MeasureSpec.getSize(heightMeasureSpec);
				int width = (int) (height * imageSideRatio);
				setMeasuredDimension(width, height);
			}
		} else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				if (rotate_selfview_display == 90 || rotate_selfview_display == 270) {
					previewWidth = mPreviewSize.height;
					previewHeight = mPreviewSize.width;
				} else {
					previewWidth = mPreviewSize.width;
					previewHeight = mPreviewSize.height;
				}
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
				if (android.os.Build.VERSION.SDK_INT < 8) {
					mCamera.setPreviewCallback(mPreviewCallback);
				} else {
					mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
				}

				try {
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						if (camera_preview_buffer == null)
							camera_preview_buffer = new byte[1800000];
						mCamera.addCallbackBuffer(camera_preview_buffer);
					}
					Log.i(TAG, "camera_preview_buffer allocated");
				} catch (Exception e) {
					Log.e(TAG, "Exception for allocation of camera_preview_buffer", e);
					camera_preview_buffer = null;
				}
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		mHasActiveSurface=false;
		if (mCamera != null) {
			if (android.os.Build.VERSION.SDK_INT < 8) {
				mCamera.setPreviewCallback(null);
			} else {
				mCamera.setPreviewCallbackWithBuffer(null);
			}
			mCamera.stopPreview();
		}
	}

	public int getOptimalSupportedFormat(List<Integer> formats) {
		int format = -1;

		for (Integer fmt : formats) {
			if (fmt.intValue() == ImageFormat.NV21) {
				Log.i(TAG, "format: ImageFormat.NV21");
			} else if (fmt.intValue() == ImageFormat.YUY2) {
				Log.i(TAG, "format: ImageFormat.YUY2");
			} else if (fmt.intValue() == ImageFormat.NV16) { // YUV422P
				Log.i(TAG, "format: ImageFormat.NV16");
			} else {
				Log.i(TAG, "format: -not supported-" + fmt);
			}
		}

		for (Integer fmt : formats) {
			if (fmt.intValue() == ImageFormat.NV21) {
				format = ImageFormat.NV21;
				return format;
			}
		}

		for (Integer fmt : formats) {
			if (fmt.intValue() == ImageFormat.YUY2) {
				format = ImageFormat.YUY2;
				return format;
			}
		}

		return format;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			Log.i("VvsipVideoPreview", "size list " + size.width + "x" + size.height);
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		if (optimalSize == null) {
			for (Size size : sizes) {
				optimalSize = size;
			}
		}

		if (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1010") == true) {
			optimalSize.height = 480;
			optimalSize.width = 640;
		}

		Log.i("VvsipVideoPreview", "final choice " + optimalSize.width + "x" + optimalSize.height);
		return optimalSize;
	}

	static public int find_range_above(List<int[]> fpslist, int higher_than) {
		int max = 100000;
		int best_index = -1;
		for (int i = 0; i < fpslist.size(); i++) {
			int tmp_maxfps = fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
			if (tmp_maxfps >= higher_than && max > tmp_maxfps) {
				max = tmp_maxfps;
				best_index = i;
			}
		}
		if (max == 0)
			return -1;
		return best_index;
	}

	static public int find_range_below(List<int[]> fpslist, int lower_than) {
		int max = 0;
		int best_index = -1;
		for (int i = 0; i < fpslist.size(); i++) {
			int tmp_maxfps = fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
			if (tmp_maxfps <= lower_than && max < tmp_maxfps) {
				max = tmp_maxfps;
				best_index = i;
			}
		}
		if (max == 0)
			return -1;
		return best_index;
	}

	@TargetApi(9)
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		mHasActiveSurface=true;
		if (mCamera != null) {
			Camera.Parameters parameters;
			try {
				parameters = mCamera.getParameters();
			} catch (Exception e) {
				// detected by report: java.lang.RuntimeException: getParameters
				// failed (empty parameters)
				Log.e(TAG, "Exception caused by mCamera.getParameters()", e);
				mCamera = null;
				return;
			}

			// java.lang.NullPointerException
			// at
			// com.vvsip.ansip.video.VideoCameraPreview.surfaceChanged(VideoCameraPreview.java:437)
			// -> means mPreviewSize is null? or parameters?

			try {
				parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
				Log.i("VvsipVideoPreview", "parameters.setPreviewSize " + mPreviewSize.width + "x" + mPreviewSize.height
						+ " for camera size");
			} catch (Exception e) {
				Log.d(TAG, "Failed to call setPreviewSize");
				if (mPreviewSize == null)
					Log.d(TAG, "mPreviewSize is null");
			}
			if (Build.VERSION.SDK_INT <= 4) {
				parameters.set("preview-frame-rate", 1);
				parameters.setPreviewFrameRate(1);
			} else if (Build.VERSION.SDK_INT < 9) {
				parameters.setPreviewFrameRate(15);
			} else {
				int[] fpsrange = new int[2];
				parameters.getPreviewFpsRange(fpsrange);
				Log.d(TAG, "cur minfps= " + fpsrange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]);
				Log.d(TAG, "cur maxfps= " + fpsrange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);

				try {
					// crash received on GT-N7000 with
					// getSupportedPreviewFpsRange returning null
					List<int[]> fpslist = parameters.getSupportedPreviewFpsRange();

					Log.d(TAG, "size= " + fpslist.size());
					for (int i = 0; i < fpslist.size(); i++) {
						Log.d(TAG, i + "found fps= " + fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]);
						Log.d(TAG, i + "found fps= " + fpslist.get(i)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
					}
					int index_1 = find_range_below(fpslist, 15000);
					int index_2 = find_range_above(fpslist, 15000);
					int index = -1;
					if (index_1 >= 0 && index_2 >= 0) {
						if (fpslist.get(index_1)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] >= 10000)
							index = index_1;
						else
							index = index_2;
					} else if (index < 0 && index_1 >= 0) {
						index = index_1;
					} else if (index < 0 && index_2 >= 0) {
						index = index_2;
					}

					if (index >= 0) {
						Log.d(TAG, " new fps= " + fpslist.get(index)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]);
						Log.d(TAG, " new fps= " + fpslist.get(index)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
						parameters.setPreviewFpsRange(fpslist.get(index)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
								fpslist.get(index)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
					}
				} catch (Exception e) {
					Log.d(TAG, "Failed to get/set FPS");
				}
			}
			requestLayout();

			try {
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (Exception exception) {
				Log.e(TAG, "Exception caused by startPreview()", exception);
			}
		}
	}

	public void setRotateValue(Integer _rotate_selfview_display) {
		// TODO Auto-generated method stub
		rotate_selfview_display = _rotate_selfview_display;
	}

	public boolean hasActiveSurface() {
		// TODO Auto-generated method stub
		return mHasActiveSurface;
	}
}