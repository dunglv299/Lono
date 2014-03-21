package com.teusoft.lono.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.teusoft.lono.R;

import java.util.Collections;

public class HumidityActivity extends TemperatureActivity implements
        OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        ((Button) findViewById(R.id.tv_temperature)).setText(R.string.humidity);
        findViewById(R.id.dewpoint_layout).setVisibility(View.GONE);
    }

    @Override
    public void init() {
        nowTv.setText(listHumidity.get(listHumidity.size() - 1) + " %");
        maxValue = Collections.max(listHumidity);
        minValue = Collections.min(listHumidity);
        minTv.setText(minValue + " %");
        maxTv.setText(maxValue + " %");
        averageTv.setText(getAverage(listHumidity) + " %");
        drawGraph(listHumidity);

    }
}
