/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */
package com.vvsip.viewsip;


import com.vvsip.amdemo.R;
import com.vvsip.ansip.IVvsipService;
import com.vvsip.ansip.VvsipService;
import com.vvsip.ansip.VvsipTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * SurfaceView的封装，功能：显示对方视频
 */
public class VideoDisplay extends SurfaceView {
	private final String mTag = "VideoDisplay";

	private Bitmap mStaticImage;
	private SurfaceHolder holder;

	private Bitmap mIncomingImage;

	private boolean running = false;

    int mVideoViewWidth;
    int mVideoViewHeight;

	private int onscreen_width=0;
	private int onscreen_height=0;
	private int android_scaling=0;
	Rect orig = null;
	Rect dest = null;
	
	public VideoDisplay(Context context, AttributeSet attrset) {
		super(context, attrset);
		mStaticImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		holder = getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {

				IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                VvsipTask _vvsipTask = _service.getVvsipTask();
				if (_vvsipTask == null)
					return;
				running = false;
				Log.i(mTag, "videoout: VideoDisplay removed");
				_vvsipTask.setvideodisplay(null);
				if (mIncomingImage!=null)
					mIncomingImage.recycle();
				mIncomingImage=null;
			}

			@SuppressLint("WrongCall")
			@Override
			public void surfaceCreated(SurfaceHolder holder) {

				try {
					Canvas c = holder.lockCanvas(null);
					onDraw(c);
					holder.unlockCanvasAndPost(c);
				} catch (Exception e) {

				}

				IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                VvsipTask _vvsipTask = _service.getVvsipTask();
				if (_vvsipTask == null)
					return;
				_vvsipTask.setvideodisplay(VideoDisplay.this);
				running = true;
				Log.i(mTag, "videoout: VideoDisplay provided");
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});
	}

	public VideoDisplay(Context context) {
		super(context);
		mStaticImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		holder = getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {

				IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                VvsipTask _vvsipTask = _service.getVvsipTask();
				if (_vvsipTask == null)
					return;
				running = false;
				Log.i(mTag, "videoout: VideoDisplay removed");
				_vvsipTask.setvideodisplay(null);
				if (mIncomingImage!=null)
					mIncomingImage.recycle();
				mIncomingImage=null;
			}

			@SuppressLint("WrongCall")
			@Override
			public void surfaceCreated(SurfaceHolder holder) {

				try {
					Canvas c = holder.lockCanvas(null);
					onDraw(c);
					holder.unlockCanvasAndPost(c);
				} catch (Exception e) {

				}

				IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                VvsipTask _vvsipTask = _service.getVvsipTask();
				if (_vvsipTask == null)
					return;
				_vvsipTask.setvideodisplay(VideoDisplay.this);
				running = true;
				Log.i(mTag, "videoout: VideoDisplay provided (2)");
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});
	}

	static int gcd(int m, int n)
	{
	   if(n == 0)
	     return m;
	   else
	     return gcd(n, m % n);
	}

	int ratiow=0;
	int ratioh=0;

	void reduce(int num, int denom)
	{
	   int divisor = gcd(num, denom);
	   ratiow = num/divisor;
	   ratioh = denom/divisor;
	}

	public Bitmap lockIncomingImage(int width, int height) {
		if (running == false)
			return null;

		if (mIncomingImage == null || mIncomingImage.getWidth() != width
				|| mIncomingImage.getHeight() != height) {
			Log.i(mTag, "videoout: Creating bitmap");
			
			reduce(width, height);
			Log.i(mTag, "videoout: transform " + width + "x" + height + " ratio " + ratiow +"x"+ ratioh);
		    mVideoViewWidth = findViewById(R.id.video_view2).getWidth();
		    mVideoViewHeight = findViewById(R.id.video_view2).getHeight();
			Log.i(mTag, "videoout: transform " + width + "x" + height + " into " + mVideoViewWidth +"x"+ mVideoViewHeight);
            int wtmp = mVideoViewWidth/ratiow;
            wtmp = wtmp*ratiow;
            int htmp = mVideoViewHeight/ratioh;
            htmp = htmp*ratioh;
            if (htmp*ratiow>wtmp*ratioh)
            {
                htmp = wtmp*ratioh/ratiow;
            } else {
                wtmp = htmp*ratiow/ratioh;
            }
			Log.i(mTag, "videoout: transform " + width + "x" + height + " final size " + wtmp +"x"+ htmp);
			
			if (wtmp>3*width && Build.VERSION.SDK_INT<11)
			{
				//refuse to have such bigger resizing
				wtmp = width*3;
				htmp = height*3;
				Log.i(mTag, "videoout: transform " + width + "x" + height + " final size reduced to " + wtmp +"x"+ htmp);
			}
			
			if (Build.VERSION.SDK_INT<=8) {
				//keep good performance on device older than android 2.2 
				onscreen_width=width;
				onscreen_height=height;
				if (wtmp>2*width)
				{
					//refuse to have such bigger resizing
					wtmp = width*2;
					htmp = height*2;
					Log.i(mTag, "videoout: transform " + width + "x" + height + " final size reduced to " + wtmp +"x"+ htmp);
				} else {
					//refuse to have such bigger resizing
					wtmp = width;
					htmp = height;
					Log.i(mTag, "videoout: transform " + width + "x" + height + " final size reduced to " + wtmp +"x"+ htmp);
				}
			}

			try {
				if (android_scaling==0) {
					onscreen_width=wtmp;
					onscreen_height=htmp;
					mIncomingImage = Bitmap.createBitmap(width, height, Config.RGB_565);
				} else {
					mIncomingImage = Bitmap.createBitmap(wtmp, htmp, Config.RGB_565);
				}
			} catch (Exception e) {
				mIncomingImage = null;
				Log.e(mTag, "Bitmap.createBitmap failed -- Exception: " + e.getMessage());
			}
		}
		return mIncomingImage;
	}

	@SuppressLint("WrongCall")
	public synchronized void unlockIncomingImage() {
		try {
			if (running == false)
				return;
			Canvas c = holder.lockCanvas(null);
			onDraw(c);
			holder.unlockCanvasAndPost(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mStaticImage==null) {
			canvas.drawColor(Color.BLACK);
			return;
		}

		if (mIncomingImage == null) {
			int posx = (this.getWidth() - mStaticImage.getWidth()) / 2;
			int posh = (this.getHeight() - mStaticImage.getHeight()) / 2;
			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(mStaticImage, posx, posh, null);
		} else {
			
			if (android_scaling==0) {
				int posx = (this.getWidth() - onscreen_width) / 2;
				int post = (this.getHeight() - onscreen_height) / 2;
				int posr = (this.getWidth() - onscreen_width) / 2 + onscreen_width;
				int posb = (this.getHeight() - onscreen_height) / 2 + onscreen_height;
				
				if (orig==null || dest==null) {
					orig = new Rect(0, 0, mIncomingImage.getWidth(), mIncomingImage.getHeight());
					dest = new Rect(posx, post, posr, posb);
				} else {
					orig.set(0, 0, mIncomingImage.getWidth(), mIncomingImage.getHeight());
					dest.set(posx, post, posr, posb);
				}
				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap (mIncomingImage, orig, dest, null);
			} else {
				int posx = (this.getWidth() - mIncomingImage.getWidth()) / 2;
				int post = (this.getHeight() - mIncomingImage.getHeight()) / 2;
				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap(mIncomingImage, posx, post, null);
			}
		}
	}

}
