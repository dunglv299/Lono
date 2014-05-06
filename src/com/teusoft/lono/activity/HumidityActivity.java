package com.teusoft.lono.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.teusoft.lono.R;
import com.teusoft.lono.adapter.DayGraphPagerAdapter;
import com.teusoft.lono.dao.Lono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HumidityActivity extends TemperatureActivity implements
        OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        ((TextView) findViewById(R.id.tv_temperature)).setText(R.string.humidity);
        findViewById(R.id.dewpoint_layout).setVisibility(View.GONE);
    }

    public void init(List<Lono> lonoList) {
        List<Integer> listHumidity = new ArrayList<Integer>();
        if (lonoList.size() == 0) {
            return;
        } else {
            for (Lono lono : lonoList) {
                listHumidity.add(lono.getHumidity());
            }
        }
        nowTv.setText(listHumidity.get(listHumidity.size() - 1) + " %");
        maxValue = Collections.max(listHumidity);
        minValue = Collections.min(listHumidity);
        minTv.setText(minValue + " %");
        maxTv.setText(maxValue + " %");
        averageTv.setText(getAverage(listHumidity) + " %");
    }

    public void resetView() {
        nowTv.setText("-- %");
        minTv.setText("-- %");
        maxTv.setText("-- %");
        averageTv.setText("-- %");
        dewPointTv.setText("-- %");
        mPagerAdapter = new DayGraphPagerAdapter(getSupportFragmentManager(), 0, 0, 0, false);
        mPager.setAdapter(mPagerAdapter);
    }
}
