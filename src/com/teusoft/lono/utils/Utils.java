package com.teusoft.lono.utils;

import android.content.Context;
import android.graphics.Typeface;

public class Utils {
	public static Typeface getTypeface(Context context) {
		Typeface tf = Typeface.createFromAsset(context.getAssets(),
				"Exo-DemiBold.ttf");
		return tf;
	}
}
