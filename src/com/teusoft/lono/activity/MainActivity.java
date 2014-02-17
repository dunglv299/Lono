package com.teusoft.lono.activity;

import java.util.Arrays;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.teusoft.lono.R;
import com.teusoft.lono.utils.Utils;

public class MainActivity extends Activity implements OnClickListener {
	private static final int BUTTONS_SIZE = 3;
	int[] arrayButtonId = { R.id.btn1, R.id.btn2, R.id.btn3 };
	int[] arrayLineId = { R.id.line_btn1, R.id.line_btn2, R.id.line_btn3 };
	Button[] arrayButton = new Button[BUTTONS_SIZE];
	View[] arrayLine = new View[BUTTONS_SIZE];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setFont();
		for (int i = 0; i < arrayButton.length; i++) {
			arrayButton[i] = (Button) findViewById(arrayButtonId[i]);
			arrayLine[i] = findViewById(arrayLineId[i]);
			arrayButton[i].setOnClickListener(this);
		}
	}

	public void setFont() {
		Typeface tf = Utils.getTypeface(this);
		((TextView) findViewById(R.id.tv_clock)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_lastupdated)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_temp)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min_temp)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max_temp)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_humidity)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_normal)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_hight)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_low)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min2)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max2)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_min_humidity)).setTypeface(tf);
		((TextView) findViewById(R.id.tv_max_humidity)).setTypeface(tf);
		((TextView) findViewById(R.id.btn1)).setTypeface(tf);
		((TextView) findViewById(R.id.btn2)).setTypeface(tf);
		((TextView) findViewById(R.id.btn3)).setTypeface(tf);

	}

	@Override
	public void onClick(View v) {
		showLine(v);
		switch (v.getId()) {
		case R.id.btn1:
			onButton1Press();
			break;
		case R.id.btn2:
			onButton2Press();
			break;
		case R.id.btn3:
			onButton3Press();
			break;
		default:
			break;
		}
	}

	private void showLine(View v) {
		int index = getIndex(v);
		for (int i = 0; i < arrayButtonId.length; i++) {
			if (i == index) {
				arrayLine[i].setVisibility(View.VISIBLE);
				arrayButton[i].setBackgroundColor(getResources().getColor(
						R.color.color_botton_enable));
			} else {
				arrayLine[i].setVisibility(View.GONE);
				arrayButton[i].setBackgroundColor(getResources().getColor(
						R.color.color_botton_disable));
			}
		}
	}

	// Get index of button
	private int getIndex(View v) {
		for (int i = 0; i < arrayButtonId.length; i++) {
			if (arrayButtonId[i] == v.getId()) {
				return i;
			}
		}
		return -1;
	}

	private void onButton1Press() {

	}

	private void onButton3Press() {

	}

	private void onButton2Press() {

	}

}
