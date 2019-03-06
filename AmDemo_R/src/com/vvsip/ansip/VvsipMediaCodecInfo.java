/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.os.Build;
/*
 * 硬编解码器信息类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VvsipMediaCodecInfo  {

	public MediaCodecInfo info;
	public int mColorFormat;
	int mMSColorFormat;
	String mMimeType;
	
	VvsipMediaCodecInfo(MediaCodecInfo _info, String _mMimeType, int _mColorFormat, int _mMSColorFormat) {
		info = _info;
		mMimeType = _mMimeType;
		mColorFormat = _mColorFormat;
		mMSColorFormat = _mMSColorFormat;
	}
}
