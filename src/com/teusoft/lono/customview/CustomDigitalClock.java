package com.teusoft.lono.customview;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;
import com.teusoft.lono.R;

import java.util.Calendar;

/**
 * You have to make a clone of the file DigitalClock.java to use in your
 * application, modify in the following manner:- private final static String m12
 * = "h:mm aa"; private final static String m24 = "k:mm";
 */

public class CustomDigitalClock extends TextView {

	Calendar mCalendar;
	private final static String m12 = "h:mm:ss aa";
	private final static String m24 = "k:mm:ss";
	private FormatChangeObserver mFormatChangeObserver;

	private Runnable mTicker;
	private Handler mHandler;

	private boolean mTickerStopped = false;
    private boolean is24hMode;

	String mFormat;
    private Context context;

	public CustomDigitalClock(Context context) {
		super(context);
		initClock(context);
	}

	public CustomDigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		initClock(context);
        this.context = context;
	}

	public void initClock(Context context) {
		Resources r = context.getResources();
        this.context = context;

		if (mCalendar == null) {
			mCalendar = Calendar.getInstance();
		}

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		setFormat();
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();

		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {
			public void run() {
				if (mTickerStopped)
					return;
				mCalendar.setTimeInMillis(System.currentTimeMillis());
				setText(DateFormat.format(mFormat, mCalendar));
				invalidate();
				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}

	/**
	 * Pulls 12/24 mode from system settings
	 */
	private boolean get24HourMode() {
		// return android.text.format.DateFormat.is24HourFormat(getContext());
		return is24hMode;
	}

	private void setFormat() {
		if (is24hMode) {
			mFormat = m24;
            this.setTextSize(context.getResources().getDimension(R.dimen.hour_text_large));
        } else {
			mFormat = m12;
            this.setTextSize(context.getResources().getDimension(R.dimen.hour_text_small));
		}
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			setFormat();
		}
	}

    public boolean isIs24hMode() {
        return is24hMode;
    }

    public void setIs24hMode(boolean is24hMode) {
        this.is24hMode = is24hMode;
    }
}