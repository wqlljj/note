package com.example.wangqi.locationsharing.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * 轻量级存储工具
 */
public class SharePreferenceUtils {

	private static Context gContext;

	public static void setContext(Context context){
		gContext = context;
	}

	public static String getPrefString(String key, final String defaultValue) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		return settings.getString(key, defaultValue);
	}

	public static void setPrefString(final String key, final String value) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		settings.edit().putString(key, value).commit();
	}

	public static boolean getPrefBoolean(final String key, final boolean defaultValue) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		return settings.getBoolean(key, defaultValue);
	}

	public static boolean hasKey(final String key) {
		return PreferenceManager.getDefaultSharedPreferences(gContext).contains(key);
	}

	public static void setPrefBoolean(final String key, final boolean value) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		settings.edit().putBoolean(key, value).commit();
	}

	public static void setPrefInt(final String key, final int value) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		settings.edit().putInt(key, value).commit();
	}

	public static int getPrefInt(final String key, final int defaultValue) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		return settings.getInt(key, defaultValue);
	}

	public static void setPrefFloat(final String key, final float value) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		settings.edit().putFloat(key, value).commit();
	}

	public static float getPrefFloat(Context context, final String key, final float defaultValue) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getFloat(key, defaultValue);
	}

	public static void setSettingLong(final String key, final long value) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		settings.edit().putLong(key, value).commit();
	}

	public static long getPrefLong(final String key, final long defaultValue) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(gContext);
		return settings.getLong(key, defaultValue);
	}

	public static void clearPreference(final SharedPreferences p) {
		final Editor editor = p.edit();
		editor.clear();
		editor.commit();
	}
}
