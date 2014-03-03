package com.teusoft.lono.utils;

import android.content.Context;
import android.graphics.Typeface;

public class Utils {
	public static final String LAST_UPDATED = "last_updated";
	public static final String CHANNEL = "channel";
	public static final String LIST_TEMP1 = "listTemp1";
	public static final String LIST_TEMP2 = "listTemp2";
	public static final String LIST_TEMP3 = "listTemp3";

	public static final String LIST_HUMID1 = "listHumid1";
	public static final String LIST_HUMID2 = "listHumid2";
	public static final String LIST_HUMID3 = "listHumid3";

	public static Typeface getTypeface(Context context) {
		Typeface tf = Typeface.createFromAsset(context.getAssets(),
				"Exo-DemiBold.ttf");
		return tf;
	}
}
