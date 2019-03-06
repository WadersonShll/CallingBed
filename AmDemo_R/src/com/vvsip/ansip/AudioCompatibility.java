package com.vvsip.ansip;

import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

/*
 * 语音兼容性解决方案，此类不必改动，可直接使用
 */
public class AudioCompatibility {

    public static int mAudiomanager_mode_internal = AudioManager.MODE_IN_COMMUNICATION;
    public static int mAudiomanager_mode_speakermode = AudioManager.MODE_IN_COMMUNICATION;
    public static int mAudiomanager_wa_galaxyS = -1; 
    public static int mAudiomanager_stream_type = AudioManager.STREAM_VOICE_CALL;
    public static int mAudiomanager_audio_source_internal = MediaRecorder.AudioSource.MIC;
    public static int mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.MIC;
    
    static public void configureAudioManagerMode(Context context)
    {
		Log.i("AudioCompatibility", "API level is " + Build.VERSION.SDK_INT);
    	
        if (Build.VERSION.SDK_INT<=4)
			mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
        else if (Build.VERSION.SDK_INT<10)
			mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
        else if (Build.VERSION.SDK_INT<11)
			mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;
        else 
			mAudiomanager_mode_internal = AudioManager.MODE_IN_COMMUNICATION;

        //TESTED
    	mAudiomanager_wa_galaxyS = -1;
        if (Build.VERSION.SDK_INT<10)
        {
    		if (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-I9000")
    				||android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("SHW-M110S")) {
            	//low gain for MIC in all use-case: (wrong for speacker mode)
            	mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
            	//setSpeakerphoneOn -> will use WRONG microphone with above code in speaker mode
            	//setSpeakerphoneOff -> will use CORRECT microphone with above code in earpiece mode

            	//YOU CAN USE THIS HACK INSTEAD TO HAVE HIGHER GAIN IN SPEAKER MODE:
            	//high gain for MIC in ALL use-case: (wrong for earpiece mode)
            	// mAudiomanager_wa_galaxyS = AudioManager.MODE_IN_CALL;
            	// mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;
        	}
        }

        //UNTESTED
        if (Build.VERSION.SDK_INT<10 && android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1000")) {
        	//low gain for MIC in all use-case: (wrong for speacker mode)
        	mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
        	//setSpeakerphoneOn -> will use WRONG microphone with above code in speaker mode
        	//setSpeakerphoneOff -> will use CORRECT microphone with above code in earpiece mode

        	//high gain for MIC in ALL use-case: (wrong for earpiece mode)
        	// mAudiomanager_wa_galaxyS = AudioManager.MODE_IN_CALL;
        	// mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;
        }
        //UNTESTED
        if (Build.VERSION.SDK_INT<10 && android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-S5660")) {
        	//low gain for MIC in all use-case: (wrong for speacker mode)
        	mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
        	//setSpeakerphoneOn -> will use WRONG microphone with above code in speaker mode
        	//setSpeakerphoneOff -> will use CORRECT microphone with above code in earpiece mode

        	//high gain for MIC in ALL use-case: (wrong for earpiece mode)
        	// mAudiomanager_wa_galaxyS = AudioManager.MODE_IN_CALL;
        	// mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;
        }
        
        //TESTED FOR "GT-P1010" (default)
		//if (Build.VERSION.SDK_INT<10 && android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1010"))
        //	mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL; // this allow to have CORRECT microphone gain

        if ( android.os.Build.BRAND.equalsIgnoreCase("Samsung") && (8 == Build.VERSION.SDK_INT)) {
            // Set Samsung specific VoIP mode for 2.2 devices
        	AudioManager mAManager;
            mAManager = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE));
        	int mode = 4;
        	mAManager.setMode(mode);
            if (mAManager.getMode() == mode) {
        		Log.i("AudioCompatibility", "Samsung 2.2 detected // AudioManager.4 support detected");
        		mAudiomanager_mode_internal = mode;
        	}
        	mAManager.setMode(AudioManager.MODE_NORMAL);
        }

        if ( (9 == Build.VERSION.SDK_INT || 10 == Build.VERSION.SDK_INT) ) {
        	AudioManager mAManager;
            mAManager = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE));
        	int mode = AudioManager.MODE_IN_COMMUNICATION;
        	mAManager.setMode(mode);
            if (mAManager.getMode() == mode) {
        		Log.i("AudioCompatibility", "Android 2.3.x  // AudioManager.MODE_IN_COMMUNICATION support detected");
        		mAudiomanager_mode_internal = mode;
        	}
        	mAManager.setMode(AudioManager.MODE_NORMAL);
        }
        
        //UNTESTED
        if (android.os.Build.BRAND.equalsIgnoreCase("sdg"))
			mAudiomanager_mode_internal = AudioManager.MODE_IN_COMMUNICATION;

        if (android.os.Build.MODEL.equalsIgnoreCase("Milestone"))
			mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;

        if (android.os.Build.MODEL.equalsIgnoreCase("Titanium"))
        	mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;

        if (android.os.Build.BRAND.equalsIgnoreCase("motorola") && mAudiomanager_mode_internal == AudioManager.MODE_IN_CALL) {
        	mAudiomanager_mode_internal = AudioManager.MODE_NORMAL;
        }
        
        
        if (android.os.Build.MODEL.equalsIgnoreCase("Galaxy nexus"))
			mAudiomanager_mode_internal = AudioManager.MODE_IN_CALL;
        
		if (mAudiomanager_mode_internal == AudioManager.MODE_NORMAL)
			Log.i("AudioCompatibility", "MODE for earpiece mode: configure to use AudioManager.MODE_NORMAL");
		else if (mAudiomanager_mode_internal == AudioManager.MODE_IN_CALL)
			Log.i("AudioCompatibility", "MODE for earpiece mode: configure to use AudioManager.MODE_IN_CALL");
		else if (mAudiomanager_mode_internal == AudioManager.MODE_IN_COMMUNICATION)
			Log.i("AudioCompatibility", "MODE for earpiece mode: configure to use AudioManager.MODE_IN_COMMUNICATION");
		else if (mAudiomanager_mode_internal == 4)
			Log.i("AudioCompatibility", "MODE for earpiece mode: configure to use AudioManager.4");

    	if (mAudiomanager_wa_galaxyS == AudioManager.MODE_IN_CALL)
    		Log.i("AudioCompatibility", "MODE for earpiece mode: AudioManager.MODE_IN_CALL hack enabled!");
    	else if (mAudiomanager_wa_galaxyS == AudioManager.MODE_NORMAL)
    		Log.i("AudioCompatibility", "MODE for earpiece mode: AudioManager.MODE_NORMAL hack enabled!");
    
    	configureAudioManagerMode_speakermode(context);
    }

    static private void configureAudioManagerMode_speakermode(Context context)
    {
		mAudiomanager_mode_speakermode = mAudiomanager_mode_internal;

        if (android.os.Build.MODEL.equalsIgnoreCase("Galaxy nexus"))
			mAudiomanager_mode_speakermode = AudioManager.MODE_IN_COMMUNICATION;
        
		if (mAudiomanager_mode_speakermode == AudioManager.MODE_NORMAL)
			Log.i("AudioCompatibility", "MODE for speakermode: configure to use AudioManager.MODE_NORMAL");
		else if (mAudiomanager_mode_speakermode == AudioManager.MODE_IN_CALL)
			Log.i("AudioCompatibility", "MODE for speakermode: configure to use AudioManager.MODE_IN_CALL");
		else if (mAudiomanager_mode_speakermode == AudioManager.MODE_IN_COMMUNICATION)
			Log.i("AudioCompatibility", "MODE for speakermode: configure to use AudioManager.MODE_IN_COMMUNICATION");
		else if (mAudiomanager_mode_speakermode == 4)
			Log.i("AudioCompatibility", "MODE for speakermode: configure to use AudioManager.4");

    	if (mAudiomanager_wa_galaxyS == AudioManager.MODE_IN_CALL)
    		Log.i("AudioCompatibility", "MODE for speakermode: AudioManager.MODE_IN_CALL hack enabled!");
    	else if (mAudiomanager_wa_galaxyS == AudioManager.MODE_NORMAL)
    		Log.i("AudioCompatibility", "MODE for speakermode: AudioManager.MODE_NORMAL hack enabled!");
    }
    
    static public void configureAudioManagerStreamType()
    {
    	if (android.os.Build.BRAND.toUpperCase(Locale.US).startsWith("ARCHOS")) {
    		mAudiomanager_stream_type = AudioManager.STREAM_MUSIC;
    	}
    	if (android.os.Build.MODEL.toUpperCase(Locale.US).startsWith("ST8002")) {
    		mAudiomanager_stream_type = AudioManager.STREAM_MUSIC;
    	}
    	if (android.os.Build.MODEL.toUpperCase(Locale.US).startsWith("SAMBM8001ND")) {
    		mAudiomanager_stream_type = AudioManager.STREAM_MUSIC;
    	}

		if (mAudiomanager_stream_type == AudioManager.STREAM_MUSIC)
			Log.i("AudioCompatibility", "configure to use AudioManager.STREAM_MUSIC");
		else if (mAudiomanager_stream_type == AudioManager.STREAM_VOICE_CALL)
			Log.i("AudioCompatibility", "configure to use AudioManager.STREAM_VOICE_CALL");
    }

    static public void configureAudioRecordAudioSource()
    {
    	//API 11
    	if (Build.VERSION.SDK_INT>=11)
    		mAudiomanager_audio_source_internal = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    	else
    		mAudiomanager_audio_source_internal = MediaRecorder.AudioSource.MIC;
    	
    	if (android.os.Build.MODEL.equalsIgnoreCase("NEXUS S")) {
    		mAudiomanager_audio_source_internal = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    	}
    	
    	if (android.os.Build.MODEL.equalsIgnoreCase("MZ604")) {
    		mAudiomanager_audio_source_internal = MediaRecorder.AudioSource.MIC;
    	}
    	
    	//fix for Galaxy Nexus: using VOICE_COMMUNICATION -> "no sound mainly when coming back from speakermode" 
    	if (android.os.Build.MODEL.equalsIgnoreCase("Galaxy Nexus")) {
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.MIC;
    	}
    	
		if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.DEFAULT)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.DEFAULT");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.MIC)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.MIC");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.VOICE_UPLINK)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.VOICE_UPLINK");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.VOICE_DOWNLINK)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.VOICE_DOWNLINK");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.VOICE_CALL) //uplink + downlink
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.VOICE_CALL");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.CAMCORDER)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.CAMCORDER");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.VOICE_RECOGNITION)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.VOICE_RECOGNITION");
		else if (mAudiomanager_audio_source_internal == MediaRecorder.AudioSource.VOICE_COMMUNICATION)
			Log.i("AudioCompatibility", "configure to use MediaRecorder.AudioSource.VOICE_COMMUNICATION");
		
		configureAudioRecordAudioSource_speakermode();
    }

    static private void configureAudioRecordAudioSource_speakermode()
    {
    	if (Build.VERSION.SDK_INT>=11)
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    	else
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.MIC;

    	if (android.os.Build.MODEL.equalsIgnoreCase("NEXUS S")) {
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    	}
    	
    	if (android.os.Build.MODEL.equalsIgnoreCase("MZ604")) {
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.MIC;
    	}
    	
    	//fix for Galaxy Nexus: using VOICE_COMMUNICATION -> "no sound mainly when coming back from speakermode" 
    	if (android.os.Build.MODEL.equalsIgnoreCase("Galaxy Nexus")) {
    		mAudiomanager_audio_source_speakermode = MediaRecorder.AudioSource.MIC;
    	}
    	
		if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.DEFAULT)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.DEFAULT");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.MIC)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.MIC");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.VOICE_UPLINK)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.VOICE_UPLINK");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.VOICE_DOWNLINK)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.VOICE_DOWNLINK");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.VOICE_CALL) //uplink + downlink
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.VOICE_CALL");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.CAMCORDER)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.CAMCORDER");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.VOICE_RECOGNITION)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.VOICE_RECOGNITION");
		else if (mAudiomanager_audio_source_speakermode == MediaRecorder.AudioSource.VOICE_COMMUNICATION)
			Log.i("AudioCompatibility", "MIC for speakermode: configure to use MediaRecorder.AudioSource.VOICE_COMMUNICATION");
    }
    
    static public int getRate(int mRate)
    {
		if (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1010"))
    		return 44100;
		return mRate;
    }
    
    static public int getAudioRecord_getMinBufferSize(int bufsize)
    {
		if (android.os.Build.DEVICE.toUpperCase(Locale.US).startsWith("GT-P1010"))
    		return bufsize*3;
		return bufsize;
    }    
}
