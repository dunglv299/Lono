package com.teusoft.lono.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LonoDataSharedPreferences {
	private Context context;
	private SharedPreferences appSharedPrefs;
	private Editor prefsEditor;
	private static String nameSharedPreferences = "lono_data";

	public LonoDataSharedPreferences(Context context) {
		this.context = context;
		appSharedPrefs = this.context.getSharedPreferences(
				nameSharedPreferences, Context.MODE_PRIVATE);
		prefsEditor = appSharedPrefs.edit();
	}

	// put & get int
	public void putInt(String key, int value) {
		prefsEditor.putInt(key, value);
		prefsEditor.commit();
	}

	public int getInt(String key) {
		try {
			return appSharedPrefs.getInt(key, 0);
		} catch (Exception e) {
			return 0;
		}
	}

	// put & get long
	public void putLong(String key, long value) {
		prefsEditor.putLong(key, value);
		prefsEditor.commit();
	}

	public long getLong(String key) {
		try {
			return appSharedPrefs.getLong(key, 0l);
		} catch (Exception e) {
			return 0l;
		}
	}

	// put & get boolean
	public void putBoolean(String key, boolean value) {
		prefsEditor.putBoolean(key, value);
		prefsEditor.commit();
	}

	public boolean getBoolean(String key) {
		try {
			return appSharedPrefs.getBoolean(key, false);
		} catch (Exception e) {
			return false;
		}
	}

	// put & get String
	public void putString(String key, String value) {
		prefsEditor.putString(key, value);
		prefsEditor.commit();
	}

	public String getString(String key) {
		try {
			return appSharedPrefs.getString(key, null);
		} catch (Exception e) {
			return null;
		}
	}

	public void putList(String key, ArrayList<?> listData) {
		// save the task list to preference
		try {
			prefsEditor.putString(key, ObjectSerializer.serialize(listData));
			prefsEditor.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		prefsEditor.clear().commit();
	}

	public List<Integer> getListInt(String key) {
		ArrayList<Integer> list;
		try {
			list = (ArrayList<Integer>) ObjectSerializer
					.deserialize(appSharedPrefs.getString(key, ObjectSerializer
							.serialize(new ArrayList<Integer>())));
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
