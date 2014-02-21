package com.teusoft.lono.activity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);
		sharedPreferences = new MySharedPreferences(this);
		Log.e("listTempsize",
				sharedPreferences.getList(BluetoothLeService.EXTRA_TEMPERATURE)
						.size() + "");
		initView();
	}

	private void initView() {
		setFont();
		homeBtn = (Button) findViewById(R.id.home_btn);
		homeBtn.setOnClickListener(this);
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

}
