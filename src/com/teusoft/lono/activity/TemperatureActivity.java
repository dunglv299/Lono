package com.teusoft.lono.activity;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
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
	public int maxValue, minValue;
	public GraphViewData[] dataGraph1;
	public long lastUpdated;
	public String[] labelX = new String[7];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);
		sharedPreferences = new MySharedPreferences(this);
		listTemp = sharedPreferences
				.getListInt(BluetoothLeService.EXTRA_TEMPERATURE);
		listHumidity = sharedPreferences
				.getListInt(BluetoothLeService.EXTRA_HUMIDITY);
		lastUpdated = sharedPreferences.getLong(Utils.LAST_UPDATED);
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
		nowTv.setText(listTemp.get(listTemp.size() - 1) + " �C");
		maxValue = Collections.max(listTemp);
		minValue = Collections.min(listTemp);
		minTv.setText(minValue + " �C");
		maxTv.setText(maxValue + " �C");
		averageTv.setText(getAverage(listTemp) + " �C");
		dewPointTv.setText(getDewPoint(listTemp.get(listTemp.size() - 1),
				listHumidity.get(listHumidity.size() - 1)) + " �C");
		drawGraph(listTemp);
	}

	public void drawGraph(List<Integer> listData) {
		// init example series data
		int size = listData.size() % 288;
		if (size == 0) {
			size = 288;
		}
		dataGraph1 = new GraphViewData[size];
		GraphViewData[] dataGraph2 = new GraphViewData[288 - size];
		for (int i = 288 - size; i < 288; i++) {
			dataGraph1[size - 288 + i] = new GraphViewData(i, listData.get(size
					- 288 + i));
		}
		for (int i = 0; i < 288 - size; i++) {
			dataGraph2[i] = new GraphViewData(i, 0);
		}
		GraphViewSeries seriesData1 = new GraphViewSeries("dunglv",
				new GraphViewSeriesStyle(Color.WHITE, 6), dataGraph1);
		GraphViewSeries seriesData2 = new GraphViewSeries("dunglv",
				new GraphViewSeriesStyle(Color.TRANSPARENT, 6), dataGraph2);

		LineGraphView graphView = new LineGraphView(this, "");
		graphView.getGraphViewStyle().setNumVerticalLabels(6);
		graphView.getGraphViewStyle().setNumHorizontalLabels(7);
		setLabelX(listData);
		graphView.setHorizontalLabels(labelX);
		graphView.addSeries(seriesData1); // data
		graphView.addSeries(seriesData2); // data

		if (minValue < 0) {
			graphView.setManualYAxisBounds(maxValue + minValue / 2,
					minValue - 10);
		} else {
			graphView.setManualYAxisBounds(maxValue + minValue / 2, 0);
		}

		LinearLayout layout = (LinearLayout) findViewById(R.id.graph_layout);
		layout.addView(graphView);
	}

	public void setLabelX(List<Integer> listData) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastUpdated);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		for (int i = labelX.length - 1; i >= 0; i--) {
			labelX[i] = calendar.get(Calendar.HOUR_OF_DAY) + ":00";
			calendar.add(Calendar.HOUR_OF_DAY, -4);
		}
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
