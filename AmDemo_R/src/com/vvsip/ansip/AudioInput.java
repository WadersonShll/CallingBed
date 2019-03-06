/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.ansip;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
//import android.media.audiofx.AcousticEchoCanceler;
//import android.media.audiofx.AutomaticGainControl;
import android.os.Build;
import android.util.Log;

/*
 * 语音输入类，功能：采集语音
 */
public class AudioInput {

	public static boolean beready=false;
	public static boolean restart = false;
	public static int mAudiomanager_audio_source = MediaRecorder.AudioSource.MIC;
	public static boolean muted = false;

	AudioRecord record;
	// AutomaticGainControl agc;
	// AcousticEchoCanceler aec;
	boolean running = false;
	int offset = 0;
	int mRate = 8000;//20160617.8000->16000

	AudioInput(int rate) {

	//	rate = 8000;//20160617.add line.
		
		mRate = rate;
		
		if (beready==false)
			return;
			
		int bufsize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (bufsize < 2048)
			bufsize = 2048;
		bufsize = AudioCompatibility.getAudioRecord_getMinBufferSize(bufsize);
		Log.i("AudioInput", "AudioRecord will use buffer = " + bufsize);
		Log.i("AudioInput", "AudioRecord will use rate = " + rate);

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		record = new AudioRecord(mAudiomanager_audio_source, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize);
		if (record != null) {
			// if (Build.VERSION.SDK_INT>=16)
			// {
			// try {
			//
			// if (AutomaticGainControl.isAvailable()) {
			// int session_id = record.getAudioSessionId();
			// agc = AutomaticGainControl.create(session_id);
			// Log.w("AudioInput",
			// "AudioRecord with AGC created / session id = " + session_id);
			// if (agc.getEnabled()==false) {
			// Log.w("AudioInput", "AudioRecord with AGC not yet enabled");
			// agc.setEnabled(true);
			// }
			// if (agc.getEnabled()==true) {
			// Log.w("AudioInput", "AudioRecord with AGC enabled");
			// }
			// }
			// } catch (Exception e) {
			// }
			// try {
			// if (AcousticEchoCanceler.isAvailable()) {
			// int session_id = record.getAudioSessionId();
			// aec = AcousticEchoCanceler.create(session_id);
			// Log.w("AudioInput",
			// "AudioRecord with AEC created / session id = " + session_id);
			// if (aec!=null) {
			// if (aec.getEnabled()==false) {
			// Log.w("AudioInput", "AudioRecord with AEC not yet enabled");
			// aec.setEnabled(true);
			// }
			// if (aec.getEnabled()==true) {
			// Log.w("AudioInput",
			// "AudioRecord with AEC enabled and hasControl = " +
			// aec.hasControl());
			// }
			// }
			// }
			// } catch (Exception e) {
			// }
			// }
			if (mAudiomanager_audio_source != record.getAudioSource()) {
				Log.w("AudioInput", "AudioRecord is using " + record.getAudioSource() + " audio source instead of "
						+ mAudiomanager_audio_source);
			}
		}
		if (record == null) {
			/* ressource not yet available? */
			Log.w("AudioInput", "AudioRecord not yet available, retry later");
			running = false;
		} else {
			running = true;
			try {
				record.startRecording();
			} catch (Exception e) {
				try {
					record.stop();
				} catch (Exception e1) {

				}
				record.release();
				record = null;
				// if (Build.VERSION.SDK_INT>=16) {
				// if (agc!=null)
				// agc.release();
				// agc=null;
				// if (aec!=null)
				// aec.release();
				// aec=null;
				// }
				running = false;
			}
		}
	}

	/** Stops running */
	public int stop() {
		if (record != null) {
			running = false;
			record.stop();
			record.release();
			record = null;
			// if (Build.VERSION.SDK_INT>=16) {
			// if (agc!=null)
			// agc.release();
			// agc=null;
			// if (aec!=null)
			// aec.release();
			// aec=null;
			// }
		}
		return 0;
	}

	int read_bytes(byte[] data, int len) {

		if (beready==false) {
			if (record != null) {
				Log.i("AudioInput", "AudioRecord is now unused: stop");
				running = false;
				record.stop();
				record.release();
				record = null;
				restart = false;
				// if (Build.VERSION.SDK_INT>=16) {
				// if (agc!=null)
				// agc.release();
				// agc=null;
				// if (aec!=null)
				// aec.release();
				// aec=null;
				// }
			}
			//Log.i("AudioInput", "AudioRecord is now unused: stopped");
			
			return 0;
		}
		
		if (restart == true && Build.VERSION.SDK_INT >= 14) {
			// let's consider restart is not required anymore for 4.0 and above.
			restart = false;
		}
		if (record == null || restart == true) {
			if (record != null) {
				running = false;
				record.stop();
				record.release();
				record = null;
				// if (Build.VERSION.SDK_INT>=16) {
				// if (agc!=null)
				// agc.release();
				// agc=null;
				// if (aec!=null)
				// aec.release();
				// aec=null;
				// }
			}

			int bufsize = AudioRecord.getMinBufferSize(mRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			if (bufsize < 2048)
				bufsize = 2048;
			bufsize = AudioCompatibility.getAudioRecord_getMinBufferSize(bufsize);
			Log.i("AudioInput", "AudioRecord will use buffer = " + bufsize);

			record = new AudioRecord(mAudiomanager_audio_source, mRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
					bufsize);
			if (record != null) {
				restart = false;
				// if (Build.VERSION.SDK_INT>=16)
				// {
				// try {
				// if (AutomaticGainControl.isAvailable()) {
				// int session_id = record.getAudioSessionId();
				// agc = AutomaticGainControl.create(session_id);
				// if (agc!=null) {
				// Log.w("AudioInput",
				// "AudioRecord with AGC created / session id = " + session_id);
				// if (agc.getEnabled()==false) {
				// Log.w("AudioInput", "AudioRecord with AGC not yet enabled");
				// agc.setEnabled(true);
				// }
				// if (agc.getEnabled()==true) {
				// Log.w("AudioInput", "AudioRecord with AGC enabled");
				// }
				// }
				// }
				// } catch (Exception e) {
				// }
				// try {
				// if (AcousticEchoCanceler.isAvailable()) {
				// int session_id = record.getAudioSessionId();
				// aec = AcousticEchoCanceler.create(session_id);
				// Log.w("AudioInput",
				// "AudioRecord with AEC created / session id = " + session_id);
				// if (aec!=null) {
				// if (aec.getEnabled()==false) {
				// Log.w("AudioInput", "AudioRecord with AEC not yet enabled");
				// aec.setEnabled(true);
				// }
				// if (aec.getEnabled()==true) {
				// Log.w("AudioInput",
				// "AudioRecord with AEC enabled and hasControl = " +
				// aec.hasControl());
				// }
				// }
				// }
				// } catch (Exception e) {
				// }
				// }
				if (mAudiomanager_audio_source != record.getAudioSource()) {
					Log.w("AudioInput", "AudioRecord is using " + record.getAudioSource() + " audio source instead of "
							+ mAudiomanager_audio_source);
				}
			}
			if (record == null) {
				/* resource not yet available? */
				running = false;
			} else {
				Log.w("AudioInput", "AudioRecord finally available!");
				running = true;

				try {
					record.startRecording();
				} catch (Exception e) {
					try {
						record.stop();
					} catch (Exception e1) {

					}
					record.release();
					record = null;
					// if (Build.VERSION.SDK_INT>=16) {
					// if (agc!=null)
					// agc.release();
					// agc=null;
					// if (aec!=null)
					// aec.release();
					// aec=null;
					// }
					running = false;
				}
			}
		}

		if (record != null) {
			int num = record.read(data, 0, len);
			
			if (num==android.media.AudioRecord.ERROR_INVALID_OPERATION) {
				try {
					record.stop();
				} catch (Exception e1) {
	
				}
				record.release();
				record = null;
				running = false;
			}
			
			if (muted == true) {
				for (int k = 0; k < len; k++) {
					data[k] = 0;
				}
			}
			
			offset += num;
			if (num < len) {
				Log.w("AudioInput", "AudioRecord return smaller data than expected!" + num + " asked:" + len);
			}
			return num;
		}

		return 0;
	}
}
