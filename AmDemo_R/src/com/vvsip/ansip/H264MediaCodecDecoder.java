/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
*/

package com.vvsip.ansip;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

class OmxFormatInfo {
	int omx_format;
	int mediastreamer2_format;
	int size_mul;
	int line_mul;
	int line_chroma_div;
	
	OmxFormatInfo(int _omx_format, int _mediastreamer2_format, int _size_mul, int _line_mul, int _line_chroma_div) {
		omx_format=_omx_format;
		mediastreamer2_format=_mediastreamer2_format;
		size_mul=_size_mul;
		line_mul=_line_mul;
		line_chroma_div=_line_chroma_div;
	}
};


/*
 * H264硬解码类
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class H264MediaCodecDecoder  {

	static final String mTag = "H264MediaCodecDecoder";
	
	MediaCodec mediaCodec;
	MediaCodec.BufferInfo bufferInfo;
    int ppssps_done;
    int width;
	int height;
    int stride;
    int slice_height;
    int color_format;
    int crop_left;
    int crop_top;
    int crop_right;
    int crop_bottom;

    public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getStride() {
		return stride;
	}

	public int getSliceHeight() {
		return slice_height;
	}

	public int getColorFormat() {
		return color_format;
	}

	public int getCropLeft() {
		return crop_left;
	}

	public int getCropTop() {
		return crop_top;
	}

	public int getCropRight() {
		return crop_right;
	}

	public int getCropBottom() {
		return crop_bottom;
	}
    
    
	public static VvsipMediaCodecInfo mMediaCodecInfo = null;
	
	//static public int __get_ms_format() {
	//	return mMediaCodecInfo.mMSColorFormat;
	//}

	public int getMSColorFormat() {
		int ms_format = -1;
		if (color_format==mMediaCodecInfo.mColorFormat)
			return mMediaCodecInfo.mMSColorFormat;
		
		if (color_format==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
	        ms_format = 0; //MS_YUV420P;
	      else if (color_format==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar)
	    	  ms_format = 0; //MS_YUV420P;
	      else if (color_format==MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar)
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==0x7f000001) /* MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanarInterlaced */
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar)
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==2141391875) /* MediaCodecInfo.CodecCapabilities.QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka (supposed to be NV12MT?) */
	        /* might be usefull?: */
	        /* https://github.com/Owersun/xbmc/blob/8a2f587242eaedc03b8a0dfb0000eee3d0522db7/xbmc/cores/dvdplayer/DVDCodecs/Video/nv12mt_to_yuv420m.neon.S */
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==2141391876) /* MediaCodecInfo.CodecCapabilities.OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar32m (detected on nexus 5) */
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) /*  */
	    	  ms_format = 9; //MS_NV12;
	      else if (color_format==842094169) /* YV12 (U and V reversed) */
	    	  ms_format = 0; //MS_YUV420P;

		return ms_format;
	}
	

	private static Hashtable<Integer,OmxFormatInfo> omxFormatInfoList = new Hashtable<Integer,OmxFormatInfo>();

	static {
		Integer key;
		OmxFormatInfo val;

		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, 0, 3, 1, 2);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar);
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar, 0, 3, 1, 2);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar); //NV21 detected on samsung S3?
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, 8 , 3, 1, 1);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar); //NV21
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, 8, 3, 1, 1);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar); //NV12
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar, 9, 3, 1, 2);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(0x7FA30C03); //NV12 //OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka
		val = new OmxFormatInfo(0x7FA30C03, 0, 3, 1, 1);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr); //YUYV
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr, 1, 4, 2, 0);
		omxFormatInfoList.put(key, val);
		key = Integer.valueOf(MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY); //UYVY
		val = new OmxFormatInfo(MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY, 5, 4, 2, 0);
		omxFormatInfoList.put(key, val);
    }

	H264MediaCodecDecoder() {
		
		
		mediaCodec = null;
		bufferInfo = null;
	}
	
	public void start() {
		Log.d(mTag, "H264MediaCodecDecoder: start");
		ppssps_done=0;
		try {
			Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: codec name: " + mMediaCodecInfo.info.getName());
		    bufferInfo = new MediaCodec.BufferInfo();
			mediaCodec = MediaCodec.createByCodecName(mMediaCodecInfo.info.getName());
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(mMediaCodecInfo.mMimeType, 352, 288);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mMediaCodecInfo.mColorFormat);
		    //mediaFormat.setByteBuffer("csd-0", bytes);
		    mediaCodec.configure(mediaFormat, null, null, 0);
		    mediaCodec.start();
		} catch (Exception e) {
	        e.printStackTrace();
	        mediaCodec=null;
	        bufferInfo=null;
		}
	}
	
	public int close() {
		if (mediaCodec==null)
			return 0;
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	    } catch (Exception e){
	        e.printStackTrace();
	    }
        mediaCodec=null;
        bufferInfo=null;
        return 0;
	}

	
	public int decode_data(int flag, byte[] input, int input_len) {
		
		//Log.d(mTag, "H264MediaCodecDecoder: decode_data");
		if (mediaCodec==null || bufferInfo==null) {
			start();
		}
		if (mediaCodec==null || bufferInfo==null) {
			return -1;
		}
		if (ppssps_done==0 && flag!=2) {
            Log.i(mTag, "H264MediaCodecDecoder: missing sps/pps");
			return -2;
		}
		
	    try {
	    	int inputBufferIndex;
	        try {
	        	inputBufferIndex = mediaCodec.dequeueInputBuffer(5000);
	        }catch (Exception e) {
	        	Log.e(mTag, "H264MediaCodecDecoder: dequeueInputBuffer" + e.getMessage());
	        	close();
	        	return -1;
			} 
	        
	        if (inputBufferIndex >= 0) {
	            ByteBuffer inputBuffer;
	        	if (android.os.Build.VERSION.SDK_INT>=21)
	        		inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
	        	else {
	    	        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
	        		inputBuffer = inputBuffers[inputBufferIndex];
	        	}
	            inputBuffer.clear();
	            inputBuffer.put(input);
	            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input_len, 0, flag);
	    		if (flag==2)
	    			ppssps_done=1;
	        } else {
	            //Log.i(mTag, "H264MediaCodecDecoder: no input buffer");
	            return -3;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
		    return -1;
	    }

	    //Log.d(mTag, "H264MediaCodecDecoder: end of decode_data");
	    return 0;
	}
	
	//private static List<String> nopadding_decoders2 = new ArrayList<String>() {{ add("OMX.SEC.avc.dec"); add("OMX.SEC.avcdec"); add("OMX.SEC.MPEG4.Decoder"); add("OMX.SEC.vc1.dec"); }};
	private static List<String> nopadding_decoders = Collections.unmodifiableList(Arrays.asList(new String[] {
			"OMX.SEC.avc.dec",
			"OMX.SEC.avcdec",
			"OMX.SEC.MPEG4.Decoder",
			"OMX.SEC.mpeg4.dec",
			"OMX.SEC.vc1.dec"}));
	
	public int get_raw_data(byte[] output, int max_output_len) {
		
		
		int output_len=0;
		//Log.d(mTag, "H264MediaCodecDecoder: get_h264_data");
		if (mediaCodec==null || bufferInfo==null) {
			return -1;
		}
		
	    try {
	        ByteBuffer[] outputBuffers;
	        

	        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
	        if (outputBufferIndex >= 0) {
	        	ByteBuffer outputBuffer;
	        	if (android.os.Build.VERSION.SDK_INT>=21)
	        		outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
	        	else {
	    	        try {
	    	        	outputBuffers = mediaCodec.getOutputBuffers();
	    	        }catch (Exception e) {
	    	        	Log.e(mTag, "H264MediaCodecDecoder: getOutputBuffers" + e.getMessage());
	    	        	close();
	    	        	return -1;
	    			} 
		            outputBuffer = outputBuffers[outputBufferIndex];
	        		
	        	}
	            //byte[] outData = new byte[bufferInfo.size];
	            if (outputBuffer.remaining()>max_output_len) {
		            Log.i(mTag, "output buffer too small outputBuffer.remaining()=" + outputBuffer.remaining() + " bufferInfo.size=" + bufferInfo.size + " max=" + max_output_len);
		            output_len = -outputBuffer.remaining(); //return new required allocation size
	            } else {
	            	//Log.i(mTag, "remaining bytes in outputBuffer=" + outputBuffer.remaining() + " bufferInfo.size=" + bufferInfo.size + " bufferInfo.offset=" + bufferInfo.offset);
		            //output_len = bufferInfo.offset+bufferInfo.size;
	            	//outputBuffer.get(output, 0, bufferInfo.offset+bufferInfo.size);
		            output_len = outputBuffer.remaining();
	            	outputBuffer.get(output, 0, outputBuffer.remaining());
	            }

	            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
	        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
	            outputBuffers = mediaCodec.getOutputBuffers();
	            return 0;
	        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
	            // Subsequent data will conform to new format.
	            MediaFormat format = mediaCodec.getOutputFormat();
	            
	            width        = format.getInteger("width");
	            height       = format.getInteger("height");
	            stride       = format.getInteger("stride");
	            slice_height = format.getInteger("slice-height");
	            color_format = format.getInteger("color-format");
	            crop_left    = format.getInteger("crop-left");
	            crop_top     = format.getInteger("crop-top");
	            crop_right   = format.getInteger("crop-right");
	            crop_bottom  = format.getInteger("crop-bottom");
	            
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: width: " + width);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: height: " + height);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: stride: " + stride);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: slice-height: " + slice_height);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: color-format: " + color_format);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: crop-left: " + crop_left);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: crop-top: " + crop_top);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: crop-right: " + crop_right);
	            Log.e(mTag, "INFO_OUTPUT_FORMAT_CHANGED: crop-bottom: " + crop_bottom);
	            
	            if (stride<=0)
	            	stride=width;
	            if (slice_height<=0)
	            	slice_height=height;
	            
	            if (color_format == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar) {
	            	//ignore:
	            	//slice_height -= crop_top/2;
	            	//crop_top=0;
	            	//crop_left=0;
	            }
	            int i_width=crop_right + 1 - crop_left;
	            //int i_heigth=crop_bottom + 1 - crop_top;
	            if (nopadding_decoders.contains(mMediaCodecInfo.info.getName())) {
	            	slice_height=0; /* ? */
	            	stride = i_width;
	            }

	            /* Align on macroblock boundary */
	            //int aligned_width = (i_width + 15) & ~0xF;
	            //int aligned_height = (i_heigth + 15) & ~0xF;
	            
	            
	            OmxFormatInfo val = omxFormatInfoList.get(Integer.valueOf(color_format));
	            if (val!=null) {
		            //int size = aligned_width * aligned_height * val.size_mul / 2;
		            //int pitch = aligned_width * aligned_height * val.line_mul;
		            
		            Log.d(mTag, "H264MediaCodecDecoder: =" + val.omx_format);
		            Log.d(mTag, "H264MediaCodecDecoder: =" + val.size_mul);
		            Log.d(mTag, "H264MediaCodecDecoder: =" + val.line_mul);
		            Log.d(mTag, "H264MediaCodecDecoder: =" + val.line_chroma_div);
	            }
            	return -999;
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
		    return -1;
	    }
	    //Log.d(mTag, "H264MediaCodecDecoder: end of get_h264_data  output_len=" + output_len);
	    return output_len;
	}
	
	public static boolean TestDecoder() {
		Log.d(mTag, "TestDecoder");
		try {
			MediaCodec mediaCodecTest = MediaCodec.createByCodecName(mMediaCodecInfo.info.getName());
			MediaFormat mediaFormatTest = MediaFormat.createVideoFormat(mMediaCodecInfo.info.getName(), 320, 240);
			mediaCodecTest.configure(mediaFormatTest, null, null, 0);
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
