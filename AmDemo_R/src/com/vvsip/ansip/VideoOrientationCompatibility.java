package com.vvsip.ansip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
/*
 * 视频方向适配类，功能：增强兼容性
 */
public class VideoOrientationCompatibility {

	final private static String KEY_PORTRAIT = "key_rotate_portrait";
	final private static String KEY_LANDSCAPE = "key_rotate_landscape";
	final private static String KEY_REVERSE_LANDSCAPE = "key_rotate_reverse_landscape";
	final private static String KEY_REVERSE_PORTRAIT = "key_rotate_reverse_portrait";
	
	final private static List<Integer> possibleDisplayOrientations = Arrays.asList(0, 90, 180, 270);
	final private static List<Integer> possibleImageOrientations = Arrays.asList(0, 1, 2, 3);
	
	private SharedPreferences configuration;
	
	private class Preset {
		public Preset(Integer self, Integer remote) {
			this.self = self;
			this.remote = remote;
		}
		public Integer self;
		public Integer remote;
		
		public List<Integer> getStorable() {
			List<Integer> storable = Arrays.asList(self, remote);
			return storable;
		}
		
		public void fromStorable(Integer[] values) {
			this.self = values[0];
			this.remote = values[1];
		}
	}
	
	private class Settings {
		public Preset frontCamera;
		public Preset rearCamera;
		
		public Settings() {
			this.frontCamera = new Preset(0, 0);
			this.rearCamera = new Preset(0, 0);
		}
		public Settings(Preset front, Preset rear) {
			this.frontCamera = front;
			this.rearCamera = rear;
		}
		
		
		public List<Integer> getStorable() {
			List<Integer> storable = new LinkedList<Integer>(frontCamera.getStorable());
			storable.addAll(rearCamera.getStorable());
			return storable;
		}
		
		public void fromStorable(Integer[] values) {
			Integer[] front = {values[0], values[1]};
			frontCamera.fromStorable(front);
			
			Integer[] rear = {values[2], values[3]};
			rearCamera.fromStorable(rear);
		}
	}
	
	@SuppressLint("UseSparseArrays")
	private Map<Integer, Settings> orientationPresets =
		new HashMap<Integer, Settings>();
	
	public VideoOrientationCompatibility(SharedPreferences configuration) {
		this.configuration = configuration;
		init();
	}
	
	private void init() {
		
		if (hasSettingsStored()) {
			load();
			return;
		}
		
		Settings portrait;
		Settings landscape;
		Settings reverseLandscape;
		Settings reversePortrait;
		
		if (android.os.Build.MODEL.toUpperCase(Locale.US).startsWith("XOOM")) {
			portrait 			= new Settings(new Preset(270, 3), new Preset(270, 3));
			landscape 			= new Settings(new Preset(0,   0), new Preset(0,   0));
			reverseLandscape 	= new Settings(new Preset(180, 2), new Preset(180, 2));
			reversePortrait 	= new Settings(new Preset(90,  1), new Preset(90,  1));
		}
		else if (android.os.Build.MODEL.toUpperCase(Locale.US).startsWith("A500")) {
			portrait 			= new Settings(new Preset(270, 1), new Preset(270, 3));
			landscape 			= new Settings(new Preset(0,   0), new Preset(0,   0));
			reverseLandscape 	= new Settings(new Preset(180, 2), new Preset(180, 2));
			reversePortrait 	= new Settings(new Preset(90,  3), new Preset(90,  1));
		}
		else { // Default, for instance for Samsung Galaxy Nexus
			portrait 			= new Settings(new Preset(90,  3), new Preset(90,  1));
			landscape 			= new Settings(new Preset(0,   0), new Preset(0,   0));
			reverseLandscape 	= new Settings(new Preset(180, 2), new Preset(180, 2));
			reversePortrait 	= new Settings(new Preset(270, 1), new Preset(270, 3));
		}
		
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, portrait);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, landscape);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, reverseLandscape);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, reversePortrait);
	}
	
	public Integer getDisplayRotation(Integer deviceOrientation, Boolean isFrontFacingCamera) {
		return getPreset(deviceOrientation, isFrontFacingCamera).self;
	}
	
	public Integer getImageRotation(Integer deviceOrientation, Boolean isFrontFacingCamera) {
		return getPreset(deviceOrientation, isFrontFacingCamera).remote;
	}
	
	public void setDisplayRotation(Integer deviceOrientation, Boolean isFrontFacingCamera, Integer displayOrientation) {
		
		if (!possibleDisplayOrientations.contains(displayOrientation))
			return;
		
		Preset preset = getPresetOrNull(deviceOrientation, isFrontFacingCamera);
		
		if (preset != null) {
			preset.self = displayOrientation;
			setPreset(deviceOrientation, isFrontFacingCamera, preset);
		}
	}
	
	public void setImageRotation(Integer deviceOrientation, Boolean isFrontFacingCamera, Integer displayOrientation) {
		
		if (!possibleImageOrientations.contains(displayOrientation))
			return;
		
		Preset preset = getPresetOrNull(deviceOrientation, isFrontFacingCamera);
		
		if (preset != null) {
			preset.remote = displayOrientation;
			setPreset(deviceOrientation, isFrontFacingCamera, preset);
		}
	}
	
	private Preset getPreset(Integer deviceOrientation, Boolean isFrontFacingCamera) {
		Preset preset = getPresetOrNull(deviceOrientation, isFrontFacingCamera);
		if (preset != null)
			return preset;
		// Return default 
		return new Preset(0, 0);
	}
	
	private Preset getPresetOrNull(Integer deviceOrientation, Boolean isFrontFacingCamera) {
		Settings settings = orientationPresets.get(deviceOrientation);
		
		if (settings != null) {
			if (isFrontFacingCamera)
				return settings.frontCamera;
			else 
				return settings.rearCamera;
		}
		return null;
	}
	
	private void setPreset(Integer deviceOrientation, Boolean isFrontFacingCamera, Preset preset) {
		if (isFrontFacingCamera)
			orientationPresets.get(deviceOrientation).frontCamera = preset;
		else
			orientationPresets.get(deviceOrientation).rearCamera = preset;
		
		store();
	}
	
	private void store() {
		String portrait = TextUtils.join(";", orientationPresets.get(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).getStorable());
		String landscape = TextUtils.join(";", orientationPresets.get(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE).getStorable());
		String reverseLandscape = TextUtils.join(";", orientationPresets.get(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE).getStorable());
		String reversePortrait = TextUtils.join(";", orientationPresets.get(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT).getStorable());
		
		Editor editor = configuration.edit();
		editor.putString(KEY_PORTRAIT, portrait);
		editor.putString(KEY_LANDSCAPE, landscape);
		editor.putString(KEY_REVERSE_LANDSCAPE, reverseLandscape);
		editor.putString(KEY_REVERSE_PORTRAIT, reversePortrait);
		editor.commit();
	}
	
	private boolean hasSettingsStored() {
		return !configuration.getString(KEY_PORTRAIT, "NOSUCHSTRING").equals("NOSUCHSTRING");
	}
	
	private void load() {
		if (!hasSettingsStored())
			return;
		
		Settings portrait = getSettingsFromString(configuration.getString(KEY_PORTRAIT, ""));
		Settings landscape = getSettingsFromString(configuration.getString(KEY_LANDSCAPE, ""));
		Settings reverseLandscape = getSettingsFromString(configuration.getString(KEY_REVERSE_LANDSCAPE, ""));
		Settings reversePortrait = getSettingsFromString(configuration.getString(KEY_REVERSE_PORTRAIT, ""));
		
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, portrait);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, landscape);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, reverseLandscape);
		orientationPresets.put(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, reversePortrait);
	}
	
	private Settings getSettingsFromString(String str) {
		String[] arr = TextUtils.split(str, ";");
		
		if (arr.length != 4)
			return null;
		
		Integer[] iarr = new Integer[4];
		
		for (int i = 0; i < 4; i++) {
			Integer val = Integer.parseInt(arr[i]);
			if (val == null)
				return null;
			iarr[i] = val;
		}
		
		Settings settings = new Settings();
		settings.fromStorable(iarr);
		return settings;
	}
}
