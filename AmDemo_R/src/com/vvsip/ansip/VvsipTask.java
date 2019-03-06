/*
  vvsip is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/
package com.vvsip.ansip;


import android.os.*;
import android.util.*;
/*
 * JNI接口类,单例模式
 */
public class VvsipTask {

	private static VvsipTask mVvsipTask;
	public static int global_failure=0;
	
	public static VvsipTask getVvsipTask()  {
		if (mVvsipTask!=null)
			return mVvsipTask;
		return new VvsipTask();
	}
	
	public VvsipTask() {
		mVvsipTask = this;
	}
	
	/* REGISTER related events */
	public static final int EXOSIP_REGISTRATION_SUCCESS=0;       /**< user is successfully registred.  */
	public static final int EXOSIP_REGISTRATION_FAILURE=1;       /**< user is not registred.           */

	/* INVITE related events within calls */
	public static final int EXOSIP_CALL_INVITE=2;            /**< announce a new call                   */
	public static final int EXOSIP_CALL_REINVITE=3;          /**< announce a new INVITE within call     */

	public static final int EXOSIP_CALL_NOANSWER=4;          /**< announce no answer within the timeout */
	public static final int EXOSIP_CALL_PROCEEDING=5;        /**< announce processing by a remote app   */
	public static final int EXOSIP_CALL_RINGING=6;           /**< announce ringback                     */
	public static final int EXOSIP_CALL_ANSWERED=7;          /**< announce start of call                */
	public static final int EXOSIP_CALL_REDIRECTED=8;        /**< announce a redirection                */
	public static final int EXOSIP_CALL_REQUESTFAILURE=9;    /**< announce a request failure            */
	public static final int EXOSIP_CALL_SERVERFAILURE=10;     /**< announce a server failure             */
	public static final int EXOSIP_CALL_GLOBALFAILURE=11;     /**< announce a global failure             */
	public static final int EXOSIP_CALL_ACK=12;               /**< ACK received for 200ok to INVITE      */

	public static final int EXOSIP_CALL_CANCELLED=13;         /**< announce that call has been cancelled */

	/* request related events within calls (except INVITE) */
	public static final int EXOSIP_CALL_MESSAGE_NEW=14;              /**< announce new incoming request. */
	public static final int EXOSIP_CALL_MESSAGE_PROCEEDING=15;       /**< announce a 1xx for request. */
	public static final int EXOSIP_CALL_MESSAGE_ANSWERED=16;         /**< announce a 200ok  */
	public static final int EXOSIP_CALL_MESSAGE_REDIRECTED=17;       /**< announce a failure. */
	public static final int EXOSIP_CALL_MESSAGE_REQUESTFAILURE=18;   /**< announce a failure. */
	public static final int EXOSIP_CALL_MESSAGE_SERVERFAILURE=19;    /**< announce a failure. */
	public static final int EXOSIP_CALL_MESSAGE_GLOBALFAILURE=20;    /**< announce a failure. */

	public static final int EXOSIP_CALL_CLOSED=21;            /**< a BYE was received for this call      */

	/* for both UAS & UAC events */
	public static final int EXOSIP_CALL_RELEASED=22;             /**< call context is cleared.            */

	/* response received for request outside calls */
	public static final int EXOSIP_MESSAGE_NEW=23;              /**< announce new incoming request. */
	public static final int EXOSIP_MESSAGE_PROCEEDING=24;       /**< announce a 1xx for request. */
	public static final int EXOSIP_MESSAGE_ANSWERED=25;         /**< announce a 200ok  */
	public static final int EXOSIP_MESSAGE_REDIRECTED=26;       /**< announce a failure. */
	public static final int EXOSIP_MESSAGE_REQUESTFAILURE=27;   /**< announce a failure. */
	public static final int EXOSIP_MESSAGE_SERVERFAILURE=28;    /**< announce a failure. */
	public static final int EXOSIP_MESSAGE_GLOBALFAILURE=29;    /**< announce a failure. */

    /* Presence and Instant Messaging */
	public static final int EXOSIP_SUBSCRIPTION_NOANSWER=30;          /**< announce no answer              */
	public static final int EXOSIP_SUBSCRIPTION_PROCEEDING=31;        /**< announce a 1xx                  */
	public static final int EXOSIP_SUBSCRIPTION_ANSWERED=32;          /**< announce a 200ok                */
	public static final int EXOSIP_SUBSCRIPTION_REDIRECTED=33;        /**< announce a redirection          */
	public static final int EXOSIP_SUBSCRIPTION_REQUESTFAILURE=34;    /**< announce a request failure      */
	public static final int EXOSIP_SUBSCRIPTION_SERVERFAILURE=35;     /**< announce a server failure       */
	public static final int EXOSIP_SUBSCRIPTION_GLOBALFAILURE=36;     /**< announce a global failure       */
	public static final int EXOSIP_SUBSCRIPTION_NOTIFY=37;            /**< announce new NOTIFY request     */

	public static final int EXOSIP_IN_SUBSCRIPTION_NEW=38;            /**< announce new incoming SUBSCRIBE.*/

	public static final int EXOSIP_NOTIFICATION_NOANSWER=39;          /**< announce no answer              */
	public static final int EXOSIP_NOTIFICATION_PROCEEDING=40;        /**< announce a 1xx                  */
	public static final int EXOSIP_NOTIFICATION_ANSWERED=41;          /**< announce a 200ok                */
	public static final int EXOSIP_NOTIFICATION_REDIRECTED=42;        /**< announce a redirection          */
	public static final int EXOSIP_NOTIFICATION_REQUESTFAILURE=43;    /**< announce a request failure      */
	public static final int EXOSIP_NOTIFICATION_SERVERFAILURE=44;     /**< announce a server failure       */
	public static final int EXOSIP_NOTIFICATION_GLOBALFAILURE=45;     /**< announce a global failure       */
    
	public boolean thread_started=false;
	
	private static Handler mainActivityEventHandler;
	public boolean running=false;
	private Thread taskThread;

	public native int vvinit(int debug_level);
	public native int vvreset(int debug_level);
	public native int vvquit();
	public native int vvcodecinfomodify(int pos, int enable, String codec_name, int mode, int cng, int vbr);
	public native int vvvideocodecinfomodify(int pos, int enable, String codec_name, int mode, int cng, int vbr);
	public native int vvvideocodecattrmodify(int uploadrate, int downloadrate);
	 /* In 3G mode, if audio bitrate is <=30, the SDP will contain only the lower bitrate codec in SDP */
	public native int vvcodecattrmodify(int ptime, int audio_bitrate);
	
	public native String vvoptiongetversion();
	public native int vvoptionsetuseragent(String useragent);
	public native int vvoptionsetinitialaudioport(int initialport);
	public native int vvoptionenablestunserver(String stunserver, int usestunserver);
	public native int vvoptionenableturnserver(String  turnserver, int useturnserver);
	public native int vvoptionsetipv4forgateway(String ipv4forgateway);
	public native int vvoptionenablerport(int enable);
	public native int vvoptionsetdnscapabilities(int dnscapabilities);
	public native int vvoptionenablekeepalive(int interval);
	public native int vvoptionsetaudioprofile(String profile);
	public native int vvoptionsetvideoprofile(String profile);
	public native int vvoptionsettextprofile(String profile);
	public native int vvoptionsetudpftpprofile(String profile);
	public native int vvoptionenablesessiontimers(int sessionexpires);
	public native int vvoptionenablesymmetricrtp(int enable);
	//public native int vvoptionfindoutsoundcard(struct vvsndcard *sndcard);
	//public native int vvoptionfindinsoundcard(struct vvsndcard *sndcard);
	public native int vvoptionselectinsoundcard(int card);
	public native int vvoptionselectoutsoundcard(int card);
	//public native int vvoptionselectincustomsoundcard(MSSndCard *captcard);
	//public native int vvoptionselectoutcustomsoundcard(MSSndCard *playcard);
	public native int vvoptionsetvolumeoutsoundcard(int card, int mixer, int percent);
	public native int vvoptiongetvolumeoutsoundcard(int card, int mixer);
	public native int vvoptionsetvolumeinsoundcard(int card, int percent);
	public native int vvoptiongetvolumeinsoundcard(int card);
	public native int vvoptionsetmuteoutsoundcard(int card, int mixer, int val);
	public native int vvoptionsetmuteinsoundcard(int card, int val);
	public native int vvoptionenablewebrtcapm(int enable, int aecm, int aecm_comfort_noise, int aecm_routing_mode,
                                        int ns, int ns_level,
                                        int agc, int agc_mode, int agc_target_level_dbfs, int agc_compression_gain_db,
                                        int high_pass_filter);
	public native int vvoptionenableechocanceller(int enable, int framesize, int taillength);
	public native int vvoptionenablehalfduplex(int enable, int vadprobstart, int vadprobcontinue);
	public native int vvoptionenableagc(int enable, int agclevel, int maxgain);
	public native int vvoptionsetjittermode(int mode);
	public native int vvoptionvideosetjittermode(int mode);
	public native int vvoptionsetpassword(String realm, String login, String passwd);
	public native int vvoptionloadplugins(String directory);
	public native int vvoptionenableoptionnalencryption(int optionnalencryption);
	public native int vvoptionsetdscpvalue(int dscpvalue);
	public native int vvoptionsetaudiodscp(int dscpvalue);
	public native int vvoptionsetvideodscp(int dscpvalue);
	public native int vvoptionsettextdscp(int dscpvalue);
	public native int vvoptionsetudpftpdscp(int dscpvalue);
	public native int vvoptionadddnscache(String host, String ip);
	public native int vvoptionsetsupportedextensions(String supportedextensions);
	public native int vvoptionsetacceptedtypes(String acceptedtypes);
	public native int vvoptionsetallowedmethods(String allowedmethods);
	public native int vvoptionsetrate(int rate);
	public native int vvoptionsetvolumegain(float capturegain, float playbackgain);
	public native int vvoptionsetecholimitation(int enabled,
			float threshold,
			float speed,
			float force,
			int sustain);

	public native long vveventget();
	public native long vveventwait(int tvs, int tvms);
	public native void vveventrelease(long evt);
	public native int vveventgettype(long evt);
	public native int vveventgettid(long evt);
	public native int vveventgetcid(long evt);
	public native int vveventgetdid(long evt);
	public native int vveventgetrid(long evt);
	public native int vveventgetnid(long evt);
	public native int vveventgetsid(long evt);
	public native String vveventgetmethod(long evt);
	public native String vveventgetreason(long evt);
	public native int vveventgetstatuscode(long evt);
	public native String vveventgetrequestheader(long evt, String hname, int n);
	public native String vveventgetresponseheader(long evt, String hname, int n);
	public native String vvurigetusername(String mSipUri);
	public native String vvurigetdomain(String mSipUri);
	public native String vvurigetdisplayname(String mSipUri);
	
	//For retreiving body in REQUEST (MESSAGE, INVITE, etc..)
	public native byte[] vveventgetrequestbody(long evt, int n);

	//public native int vvnetworkguessip(int family, String address, int size);
	public native String vvnetworkgetnatinfo(String ip, int port);
	public native int vvnetworkmasquerade(String ip, int port);
	public native int vvoptionsettlscertificate(String certificate, String priv_key, String root_ca); 
	public native int vvnetworkstart(String transport, int port);

	public native int vvregisterstart(String identity, String proxy, String outboundproxy, int expires);
	public native int vvregisterrefresh(int rid, int expires);
	public native int vvregisterstop(int rid);

	public native int vvsessionstart(String identity, String target, String proxy, String outboundproxy,String tags);
	public native int vvsessionstartwithvideo(String identity, String target, String proxy, String outboundproxy,String tags);
	
/*	public native int vvsessionstart(String identity, String target, String proxy, String outboundproxy);
	public native int vvsessionstartwithvideo(String identity, String target, String proxy, String outboundproxy);*/
	public native int vvsessionaddvideo(int did);
	public native int vvsessionstopvideo(int did);
    public native int vvsessionadaptvideobitrate(int did, int percent);
	public native int vvmessagegetrequestaudiortpdirection(long evt);
	public native int vvmessagegetrequestvideortpdirection(long evt);
	public native int vvmessagegetresponsevideortpdirection(long evt);
	public native int vvsessionstop(int cid, int did, int code);
	public native int vvsessionanswer(int cid, int did, int code, int enable_audio);

	
	public native float vvsessiongetaudiouploadbandwidth(int did);
	public native float vvsessiongetaudiodownloadbandwidth(int did);
	public native float vvsessiongetaudiopacketloss(int did);
	public native float vvsessiongetaudioremotepacketloss(int did);
	

	public native float vvsessiongetvideouploadbandwidth(int did);
	public native float vvsessiongetvideodownloadbandwidth(int did);
	public native float vvsessiongetvideopacketloss(int did);    
	public native float vvsessiongetvideoremotepacketloss(int did);
	
	public native int vvsessionanswerrequest(int cid, int did, int code);

	public native int vvsessionsenddtmfwithduration(int did, String dtmf, int duration);
	public native int vvsessionsendinbanddtmf(int did, String dtmf);
	public native int vvsessionsendrtpdtmf(int did, String dtmf);
	
	public native int vvexecuteuri(String identity, String target,
			String proxy, String outboundproxy, String body);
	
	public native int vvmessageanswer(int tid, int code);
	
	public native void registeraudio();
	
	//EXPERIMENTAL USAGE OF MEDIACODEC API FOR SDK>=16
	//sidenote: validation of every device is NECESSARY.
	//development device is galaxy nexus
	public native int registermsh264mediaencoder(int version_sdk);
	public native int registermsh264mediadecoder(int version_sdk);
	
	public native int vvoptionenablefilter(String filter_name);
	public native int vvoptiondisablefilter(String filter_name);
	
	public native int setvideodisplay(Object lVideoDisplay);

	/* load the 'vvsip' library on startup.
	 * The library has been unpacked into
	 * /data/data/com.vvsip.vvsip/lib/vvsip.so
	 */
	static {
		CheckCpu ccpu;
		int isArm = -1; 
		int features = 0;
		
		try {
			ccpu = new CheckCpu();
			isArm = ccpu.getcpufamily();
			features = ccpu.getcpufeatures();
		} catch (UnsatisfiedLinkError e) {
			Log.e("VvsipTask", "native library is missing // re-install the application...");
			e.printStackTrace();
			global_failure=1;
		} catch (Exception e) {
			Log.e("VvsipTask", "problem loading checkcpu.so?");
			e.printStackTrace();
			global_failure=1;
		}
		
		if (isArm==1)
		{
			Log.i("VvsipTask", "CPU feature: " + features);
			if ((features & 0x001) == 0x001)
				Log.i("VvsipTask", "CPU feature: ANDROID_CPU_ARM_FEATURE_ARMv7");
			if ((features & 0x002) == 0x002)
				Log.i("VvsipTask", "CPU feature: ANDROID_CPU_ARM_FEATURE_VFPv3");
			if ((features & 0x004) == 0x004)
				Log.i("VvsipTask", "CPU feature: ANDROID_CPU_ARM_FEATURE_NEON");

			try {
				if ((features & 0x001) == 0x001)
				{
					if ((features & 0x004) == 0x004)
						System.loadLibrary("vvsip-v7a-neon");
					else
						System.loadLibrary("vvsip-v7a");
				} else {
					System.loadLibrary("vvsip-v5");
				}
			} catch (UnsatisfiedLinkError e) {
				Log.e("VvsipTask", "native library is missing // re-install the application...");
				e.printStackTrace();
				global_failure=1;
			} catch (Exception e) {
				Log.e("VvsipTask", "problem loading arm vvsip?");
				e.printStackTrace();
				global_failure=1;
			}
		} else if (isArm==2)
			{
				Log.i("VvsipTask", "CPU feature: " + features);
				if ((features & 0x001) == 0x001)
					Log.i("VvsipTask", "CPU feature: ANDROID_CPU_X86_FEATURE_SSSE3");
				if ((features & 0x002) == 0x002)
					Log.i("VvsipTask", "CPU feature: ANDROID_CPU_X86_FEATURE_POPCNT");
				if ((features & 0x004) == 0x004)
					Log.i("VvsipTask", "CPU feature: ANDROID_CPU_X86_FEATURE_MOVBE");
				try {
					System.loadLibrary("vvsip-x86");
				} catch (UnsatisfiedLinkError e) {
					Log.i("VvsipTask", "native library is missing // re-install the application...");
					e.printStackTrace();
					global_failure=1;
				} catch (Exception e) {
					Log.i("VvsipTask", "problem loading x86 vvsip?");
					e.printStackTrace();
					global_failure=1;
				}
		} else {
			try {
				System.loadLibrary("vvsip");
			} catch (UnsatisfiedLinkError e) {
				Log.e("VvsipTask", "native library is missing // re-install the application...");
				e.printStackTrace();
				global_failure=1;
			} catch (Exception e) {
				Log.e("VvsipTask", "problem loading arm vvsip?");
				e.printStackTrace();
				global_failure=1;
			}
		}
		
	}	

	public void stop() {
		
		if (thread_started==false)
			return;
		running = false;

		if (taskThread!=null)
		{
			try {
				taskThread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			taskThread = null;
		}
   		
		while (thread_started==true)
		{

		}
	}

	/*
	 * 开始Handler，获得SIP信令消息与状态
	 */
	public void start(Handler _mainActivityEventHandler) {

	   	if (running==true)
    		return;

	   	running=true;
		thread_started = true;

		mainActivityEventHandler = _mainActivityEventHandler;

		taskThread = new Thread() {
			@Override public void run() {
				vvsipLoop();
				thread_started = false;
			}
		};
		taskThread.start();
	}

	private void vvsipLoop() {
		Log.i(getClass().getSimpleName(), "background task - start");

		int count=0;
		while (running==true) {

			count++;
			if (count%(5*3)==0) /* each 3seconds */
			{
				Message m = Message.obtain(); //new Message(); 
				m.what = -1;
				m.arg1 = -1;
				m.arg2 = -1;

				mainActivityEventHandler.sendMessage(m);
				count=0;
			}
			long evt = vveventwait(0, 200);
			if (evt!=0)
			{
				int type = vveventgettype(evt);

				//Send update to the main thread
				Message m = Message.obtain(); //new Message();
				m.what = type;
				m.arg1 = vveventgetcid(evt);
				m.arg2 = vveventgetdid(evt);
				Long obj = Long.valueOf(evt);
				m.obj = obj;

				mainActivityEventHandler.sendMessage(m); 
			}
		}
		Log.i(getClass().getSimpleName(), "background task - end");
	}

}
