package com.vvsip.ansip;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VvsipDTMF {
	
	private String mTag = "VvsipDTMF";
	private static VvsipDTMF mVvsipTask;
	public static int global_failure=0;
	
	private static Handler mainActivityMessageHandler;
	
	public static VvsipDTMF getVvsipDTMF()  {
		if (mVvsipTask!=null)
			return mVvsipTask;
		return new VvsipDTMF();
	}
	
	public VvsipDTMF() {
		mVvsipTask = this;
	}
	
	public int testdtmf(int dtmf){
		Log.e(mTag, "printDTMF:"+dtmf);
		
		String strDTMF = null;
		
		if (mainActivityMessageHandler != null) {
			Message m = new Message();
			m.what = 88;
			
			if(dtmf==35){
				strDTMF = "#";
			}else if(dtmf==42){
				strDTMF = "*";
			}else if(dtmf>=48 && dtmf <=57){
				strDTMF = String.valueOf(dtmf-48);
			}else{
				strDTMF = "unknown";
			}
			
			
			m.obj = "Received DTMF: " + strDTMF + "\n";

			mainActivityMessageHandler.sendMessage(m);
		} else {
			Log.e(mTag, "mainActivityMessageHandler==null");
		}
		
		return 0;
	}
	
	public void setHandler(Handler _mainActivityEventHandler) {
		mainActivityMessageHandler = _mainActivityEventHandler;
	}
}
