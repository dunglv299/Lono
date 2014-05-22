package com.teusoft.lono.utils;

import android.util.Log;
import com.teusoft.lono.BuildConfig;

/**
 * Log Helper. The messages are only loged when in {@link BuildConfig#DEBUG}
 * 
 */
public class LogUtils {

	/**
	 * Log in debug mode.
	 * 
	 * @param message
	 */
	public static void d(String message) {
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[1];
		if (BuildConfig.DEBUG)
			Log.d(stackTraceElement.getFileName() + " in "
					+ stackTraceElement.getMethodName() + " at line: "
					+ stackTraceElement.getLineNumber(), message);
	}

	/**
	 * Log in debug mode.
	 * 
	 * @param message
	 */
	public static void w(String message) {
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[1];
		if (BuildConfig.DEBUG)
			Log.w(stackTraceElement.getFileName() + " in "
					+ stackTraceElement.getMethodName() + " at line: "
					+ stackTraceElement.getLineNumber(), message);
	}

	/**
	 * Log in info mode.
	 * 
	 * @param message
	 */
	public static void i(String message) {
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[1];
		if (BuildConfig.DEBUG)
			Log.i(stackTraceElement.getFileName() + " in "
					+ stackTraceElement.getMethodName() + " at line: "
					+ stackTraceElement.getLineNumber(), message);
	}

	/**
	 * Log in error mode.
	 * 
	 * @param message
	 */
	public static void e(String message) {
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[1];
		if (BuildConfig.DEBUG)
			Log.e(stackTraceElement.getFileName() + " in "
					+ stackTraceElement.getMethodName() + " at line: "
					+ stackTraceElement.getLineNumber(), message);
	}
}
