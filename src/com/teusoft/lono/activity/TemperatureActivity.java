package com.teusoft.lono.activity;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.teusoft.lono.R;
import com.teusoft.lono.service.BluetoothLeService;
import com.teusoft.lono.utils.MySharedPreferences;
import com.teusoft.lono.utils.Utils;

public class TemperatureActivity extends Activity implements OnClickListener {
	Button homeBtn;
	private MySharedPreferences sharedPreferences;
	TextView nowTv, minTv, maxTv, averageTv, dewPointTv;
	public List<Integer> listTemp;
	public List<Integer> listHumidity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);
		sharedPreferences = new MySharedPreferences(this);
		listTemp = sharedPreferences
				.getListInt(BluetoothLeService.EXTRA_TEMPERATURE);
		listHumidity = sharedPreferences
				.getListInt(BluetoothLeService.EXTRA_HUMIDITY);
		initView();
		init();
	}

	public void initView() {
		setFont();
		homeBtn = (Button) findViewById(R.id.home_btn);
		homeBtn.setOnClickListener(this);
		nowTv = (TextView) findViewById(R.id.tv_now_value);
		minTv = (TextView) findViewById(R.id.tv_min_value);
		maxTv = (TextView) findViewById(R.id.tv_max_value);
		averageTv = (TextView) findViewById(R.id.tv_average_value);
		dewPointTv = (TextView) findViewById(R.id.tv_dewpoint_value);
	}

	public void init() {
		nowTv.setText(listTemp.get(listTemp.size() - 1) + " °C");
		minTv.setText(Collections.min(listTemp) + " °C");
		maxTv.setText(Collections.max(listTemp) + " °C");
		averageTv.setText(getAverage(listTemp) + " °C");
		dewPointTv.setText(getDewPoint(listTemp.get(listTemp.size() - 1),
				listHumidity.get(listHumidity.size() - 1)) + " °C");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_btn:
			goBack();
			break;

		default:
			break;
		}

	}

	public void setFont() {
		Typeface tf = Utils.getTypeface(this);
		((TextView) findViewById(R.id.tv_now)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_now_value)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_average)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_average_value)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min_value)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_dewpoint)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_dewpoint_value)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max_value)).setTypeface(tf);
		((TextView) findViewById(R.id.home_btn)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_temperature)).setTypeface(tf);
		((TextView) findViewById(R.id.btn1)).setTypeface(tf);
		((TextView) findViewById(R.id.btn2)).setTypeface(tf);
		((TextView) findViewById(R.id.btn3)).setTypeface(tf);
	}

	private void goBack() {
		finish();
		overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		goBack();
	}

	public int getAverage(List<Integer> listTemp) {
		int sum = 0;
		if (!listTemp.isEmpty()) {
			for (Integer mark : listTemp) {
				sum += mark;
			}
			return sum / listTemp.size();
		}
		return sum;
	}

	/**
	 * Get dew point
	 */
	private int getDewPoint(int temp, int humidity) {
		int dewPoint = (int) (Math.pow(((double) humidity / 100d), 1d / 8d)
				* (112 + 0.9 * temp) + 0.1 * temp - 112);
		return dewPoint;
	}
}
