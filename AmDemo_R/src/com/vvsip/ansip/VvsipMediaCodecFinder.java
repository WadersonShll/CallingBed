/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.os.Build;
import android.util.Log;


//Information on encoder, decoder, phone tested...

/*
HW-INFO:Galaxy Nexus
HW-INFO:4.3
HW-INFO:maguro
HW-INFO:google
Encoder: OMX.TI.DUCATI1.VIDEO.H264E Mimetype:video/avc supported with NV12 (COLOR_TI_FormatYUV420PackedSemiPlanar)
Decoder: OMX.TI.DUCATI1.VIDEO.DECODER Mimetype:video/avc supported with NV12 (COLOR_TI_FormatYUV420PackedSemiPlanar)

sidenote: was working with 4.2
sidenote: MS_NV12 for both decoder/encoder
sidenote: the decoded data(second plane) is shift by 16 octets?? (end of plane 1 + 16)
          src_pic.planes[1]=d->data_nv12->b_rptr+d->stride*slice_height+(ysize)/2+16;
          TODO: verify other received size
sidenote: decoder tested with CIF and QCIF
sidenote: encoder tested with 320x240 (and I think CIF and most probably other too...)

HW-INFO:GT-I9305
HW-INFO:4.1.2
HW-INFO:m3
HW-INFO:samsung

Encoder: OMX.SEC.avc.enc Mimetype:video/avc supported with NV21 (COLOR_FormatYUV420SemiPlanar)
Decoder: OMX.SEC.avc.dec Mimetype:video/avc supported with NV12 (COLOR_FormatYUV420SemiPlanar)

sidenote: ENCODER ********MS_NV21*******
          DECODER ********MS_NV12*******
          -> WHY ARE THEY DIFFERENT???
sidenote: example decoder:
INFO_OUTPUT_FORMAT_CHANGED: codec name: OMX.SEC.avc.dec
INFO_OUTPUT_FORMAT_CHANGED: width: 352
INFO_OUTPUT_FORMAT_CHANGED: height: 288
INFO_OUTPUT_FORMAT_CHANGED: stride: 352
INFO_OUTPUT_FORMAT_CHANGED: slice-height: 288
INFO_OUTPUT_FORMAT_CHANGED: color-format: 21
INFO_OUTPUT_FORMAT_CHANGED: crop-left: 0
INFO_OUTPUT_FORMAT_CHANGED: crop-top: 0
INFO_OUTPUT_FORMAT_CHANGED: crop-right: 351
INFO_OUTPUT_FORMAT_CHANGED: crop-bottom: 287
sidenote: decoder tested with CIF and QCIF



nexus7:

09-04 14:35:32.218: D/VvsipMediaCodecFinder(4104): Encoder: OMX.Nvidia.h264.encoder Mimetype:video/avc supported with YUV420P (COLOR_FormatYUV420Planar)
09-04 14:35:32.248: D/VvsipMediaCodecFinder(4104): Decoder: OMX.Nvidia.h264.decode Mimetype:video/avc supported with YUV420P (COLOR_FormatYUV420Planar)
09-04 14:36:15.688: I/VvsipService(4104): HW-INFO:Nexus 7
09-04 14:36:15.688: I/VvsipService(4104): HW-INFO:4.2.2
09-04 14:36:15.688: I/VvsipService(4104): HW-INFO:tilapia
09-04 14:36:15.688: I/VvsipService(4104): HW-INFO:google

09-04 14:40:39.238: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: codec name: OMX.Nvidia.h264.decode
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: width: 352
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: height: 288
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: stride: 352
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: slice-height: 0
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: color-format: 19
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: crop-left: 0
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: crop-top: 0
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: crop-right: 351
09-04 14:40:39.988: E/H264MediaCodecDecoder(4104): INFO_OUTPUT_FORMAT_CHANGED: crop-bottom: 287

sidenote: decoder CIF and QCIF tested

HW-INFO:POV_TAB-PROTAB25XXL8
HW-INFO:4.1.1
HW-INFO:POV_TAB-PROTAB25XXL
HW-INFO:POV

 Decoder: OMX.google.h264.decoder Mimetype:video/avc supported with YUV420P (COLOR_FormatYUV420Planar)
 No usable Encoder
 
sidenote: receiving QCIF -> crash
sidenote: second decoder receiving QCIF -> crash (same as OMX.google.h264.decoder?)
   it reports:  Decoder: video/avc supported with YUV420P (COLOR_FormatYUV420Planar)


GT-N7000 and GT-I9100

Encoder: NV12 with COLOR_FormatYUV420SemiPlanar

*/
/*
 * 枚举硬编解码器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VvsipMediaCodecFinder {

	static final String mTag = "VvsipMediaCodecFinder";
	
	static public ArrayList<VvsipMediaCodecInfo> AvcEncoders = new ArrayList<VvsipMediaCodecInfo>();
	static public ArrayList<VvsipMediaCodecInfo> AvcDecoders = new ArrayList<VvsipMediaCodecInfo>();
	static public boolean codec_loaded = false;
    static public int codecCount = 0;
    static public int totalSize = 0;

    /*
     * Find whether the given codec is supported
     */
    private static boolean isFormatSupported(MediaCodecInfo info, CodecCapabilities cap, int colorFormat) {

        try {
            for (int k = 0; k < cap.colorFormats.length; ++k) {
                if (cap.colorFormats[k] == colorFormat) {
                    //Log.d(mTag, "isFormatSupported: Found " + info.getName() + " // format: " + cap.colorFormats[k]);
                	return true;
                }
            }
    	}catch (Exception e) {
            Log.d(mTag, "Codec: Skipping codec " + info.getName());
            e.printStackTrace();
		}
    	return false;
    }
    
	public static int ListAllMediaCodec(String mMimeType, int profile, boolean checkEncoder, boolean checkDecoder) {
    	
    	if (android.os.Build.VERSION.SDK_INT<16) {
    		return totalSize;
    	}
    	if (AvcEncoders.size()>0)
    		checkEncoder=false;
    	if (AvcDecoders.size()>0)
    		checkDecoder=false;
    	/* already done or not required */
    	if (checkEncoder==false && checkDecoder==false) {
            codec_loaded=true;
    		return totalSize;
    	}

		totalSize=0;
        codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; ++i) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            String[] types = info.getSupportedTypes();

        	if (info.isEncoder()==true && types.length>0)
        		Log.d(mTag, "Codec: Found " + info.getName() + " // + " + types[0] + " // encoder");
        	else if (info.isEncoder()==true)
        		Log.d(mTag, "Codec: Found " + info.getName() + " // decoder");
        	else if (types.length>0)
        		Log.d(mTag, "Codec: Found " + info.getName() + " // + " + types[0] + " // decoder");
        	else
        		Log.d(mTag, "Codec: Found " + info.getName() + " // decoder");
        	
            totalSize += 1;
            
            if (checkEncoder==false && info.isEncoder())
            	continue;
            if (checkDecoder==false && info.isEncoder()==false)
            	continue;
            
        	try {
                for (int j = 0; j < types.length; ++j) {
                	int profile_found=0;
            		if (types[j].compareTo(mMimeType) != 0)
            			continue;
                    CodecCapabilities cap = info.getCapabilitiesForType(types[j]);
                    CodecProfileLevel[] profileLevels = cap.profileLevels;
                    for (int k = 0; k < profileLevels.length; ++k) {
                        //Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // profile: " + profileLevels[k].profile + " level:" + profileLevels[k].level);
                        if (profile==profileLevels[k].profile) {
                        	profile_found=1;
                        }
                    }
                	if (profile_found!=0) {
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_FormatYUV420SemiPlanar");
                            VvsipMediaCodecInfo apci;
                    		if (info.getName().equalsIgnoreCase("OMX.Intel.VideoDecoder.AVC"))
                    			continue; //can't find the correct format...
                    		else if (info.getName().equalsIgnoreCase("OMX.SEC.avc.enc") && android.os.Build.VERSION.SDK_INT<17)
                    			apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, 8); // NV21
                    		else
                    			apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, 9); // NV12
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_FormatYUV420Planar");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, 0); // YUV420P
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_FormatYUV420PackedPlanar");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar, 0); // YUV420P
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_FormatYUV420PackedSemiPlanar");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar, 9); // NV12
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_TI_FormatYUV420PackedSemiPlanar");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar, 9); // NV12
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, 0x7f000001)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_TI_FormatYUV420PackedSemiPlanarInterlaced");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, 0x7f000001, 9); // NV12
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                        if (isFormatSupported(info, cap, MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)==true) {
                            Log.d(mTag, "Codec: Found " + info.getName() + " // " + types[j] + " // COLOR_QCOM_FormatYUV420SemiPlanar");
                    		VvsipMediaCodecInfo apci = new VvsipMediaCodecInfo(info, mMimeType, MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar, 9); // NV12
                    		if (info.isEncoder()==true) AvcEncoders.add(apci);
                    		else AvcDecoders.add(apci);
                        }
                    }
                }
        	}catch (Exception e) {
                Log.d(mTag, "Codec: Skipping codec " + info.getName());
                e.printStackTrace();
			}
        }
        codec_loaded=true;
        return totalSize;
	}
}
