/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

import android.util.Log;
/*
 * 检测CPU接口,增强系统兼容性
 */
public class CheckCpu {


	public native int getcpufamily();
	public native int getcpufeatures();

	/* 
	 */
	static {
		try {
			System.loadLibrary("checkcpu");
		} catch (UnsatisfiedLinkError e) {
			Log.e("VvsipTask", "native library is missing // re-install the application...");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("VvsipTask", "problem loading checkcpu.so?");
			e.printStackTrace();
		}
	}	
}
