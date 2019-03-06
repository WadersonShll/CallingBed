/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.ansip;

import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

/*
 * H264硬解码类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class H264MediaCodecEncoder {

	static final String mTag = "H264MediaCodecEncoder";

	MediaCodec mediaCodec;
	MediaCodec.BufferInfo bufferInfo;
	int width;
	int height;
	int fps;
	int bitrate;

	public static VvsipMediaCodecInfo mMediaCodecInfo = null;

	static public int get_format() {
		return mMediaCodecInfo.mColorFormat;
	}

	static public int get_ms_format() {
		return mMediaCodecInfo.mMSColorFormat;
	}

	H264MediaCodecEncoder() {
		mediaCodec = null;
		bufferInfo = null;
	}

	public void start(int _width, int _height, int _fps, int _bitrate) {
		Log.d(mTag, "H264MediaCodecEncoder: start format=" + mMediaCodecInfo.mColorFormat);
		try {
			bufferInfo = new MediaCodec.BufferInfo();
			mediaCodec = MediaCodec.createByCodecName(mMediaCodecInfo.info.getName());
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(mMediaCodecInfo.mMimeType, _width, _height);
			mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, _bitrate);
			mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, _fps);
			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mMediaCodecInfo.mColorFormat);
			mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
			mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			mediaCodec.start();
			width = _width;
			height = _height;
			fps = _fps;
			bitrate = _bitrate;
		} catch (Exception e) {
			e.printStackTrace();
			mediaCodec = null;
			bufferInfo = null;
		}
	}

	public int close() {
		if (mediaCodec == null)
			return 0;
		try {
			mediaCodec.stop();
			mediaCodec.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaCodec = null;
		bufferInfo = null;
		return 0;
	}

	public int encode_data(int _width, int _height, int _fps, int _bitrate, byte[] input, int input_len) {

		long timeMs = System.currentTimeMillis();
		if (width != _width || height != _height || fps != _fps || bitrate != _bitrate) {
			close();
		}
		if (mediaCodec == null || bufferInfo == null) {
			start(_width, _height, _fps, _bitrate);
		}
		if (mediaCodec == null || bufferInfo == null) {
			return -1;
		}

		try {
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer;
				if (android.os.Build.VERSION.SDK_INT >= 21)
					inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
				else {
					ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
					inputBuffer = inputBuffers[inputBufferIndex];
				}
				inputBuffer.clear();
				inputBuffer.put(input);
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, input_len, timeMs * 1000, 0);
			} else {
				Log.i(mTag, "H264MediaCodecEncoder: no input buffer");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		// Log.d(mTag, "H264MediaCodecEncoder: end of encode_data");
		return 0;
	}

	public int get_h264_data(byte[] output, int max_output_len) {

		int output_len = 0;
		// Log.d(mTag, "H264MediaCodecEncoder: get_h264_data");
		if (mediaCodec == null || bufferInfo == null) {
			return -1;
		}

		try {
			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			if (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer;
				if (android.os.Build.VERSION.SDK_INT >= 21)
					outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
				else {
					ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
					outputBuffer = outputBuffers[outputBufferIndex];
				}
				if (bufferInfo.size > max_output_len) {
					Log.i(mTag, "output buffer too small bufferInfo.size=" + bufferInfo.size + " max=" + max_output_len);
				} else {
					try {
						outputBuffer.get(output, 0, bufferInfo.size);
						output_len = bufferInfo.size;
					} catch (Exception ex) {
						ex.printStackTrace();
						mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
						output_len = 0;
					}

					// Log.i(mTag, output_len + " bytes written");
				}

				mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		// Log.d(mTag,
		// "H264MediaCodecEncoder: end of get_h264_data  output_len=" +
		// output_len);
		return output_len;
	}

	public static boolean TestEncoder() {
		Log.d(mTag, "TestEncoder");
		try {
			MediaCodec mediaCodecTest = MediaCodec.createByCodecName(mMediaCodecInfo.info.getName());
			MediaFormat mediaFormatTest = MediaFormat.createVideoFormat(mMediaCodecInfo.info.getName(), 320, 240);
			mediaFormatTest.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
			mediaFormatTest.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
			mediaFormatTest.setInteger(MediaFormat.KEY_COLOR_FORMAT, mMediaCodecInfo.mColorFormat);
			mediaFormatTest.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
			mediaCodecTest.configure(mediaFormatTest, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			mediaCodecTest.start();
			mediaCodecTest.stop();
			mediaCodecTest.release();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
