package com.teusoft.lono.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Calendar;

public class Utils {
    public static final String LAST_UPDATED1 = "last_updated1";
    public static final String LAST_UPDATED2 = "last_updated2";
    public static final String LAST_UPDATED3 = "last_updated3";
    public static final String CHANNEL = "channel";
    public static final String LIST_TEMP1 = "listTemp1";
    public static final String LIST_TEMP2 = "listTemp2";
    public static final String LIST_TEMP3 = "listTemp3";

    public static final String LIST_HUMID1 = "listHumid1";
    public static final String LIST_HUMID2 = "listHumid2";
    public static final String LIST_HUMID3 = "listHumid3";
    public static final String DEGREE_TYPE = "degreeType";


    public static final long ONE_MINUTE = 60 * 1000;
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;
    public static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;


    public static Typeface getTypeface(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "Exo-DemiBold.ttf");
        return tf;
    }

    public static long getRoundDay(long timeStamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeStamp);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis();
    }

    public static long getRoundWeek(long timeStamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeStamp);
        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis();
    }

    public static int getFValue(int cValue) {
        return cValue * 9 / 5 + 32;
    }
    public static void changeTextDegreeType(TextView mTextView, boolean isDegreeF){
        if (isDegreeF){
            mTextView.setText(mTextView.getText().toString().replace("C", "F"));
        }else{
            mTextView.setText(mTextView.getText().toString().replace("F", "C"));
        }
    }
}
