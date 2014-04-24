package com.teusoft.lono.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.teusoft.lono.R;
import com.teusoft.lono.adapter.GraphPagerAdapter;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TemperatureActivity extends FragmentActivity implements OnClickListener {
    Button homeBtn;
    TextView nowTv, minTv, maxTv, averageTv, dewPointTv;
    private int[] arrayButtonId = {R.id.btn1, R.id.btn2, R.id.btn3};
    private Button[] arrayButton = new Button[BUTTONS_SIZE];
    private static final int BUTTONS_SIZE = 3;
    private View[] arrayLine = new View[BUTTONS_SIZE];
    private int[] arrayLineId = {R.id.line_btn1, R.id.line_btn2,
            R.id.line_btn3};

    public List<Integer> listHumidity;
    public int maxValue, minValue;
    public long lastUpdated;
    private int channel;
    private LonoDao lonoDao;
    private List<Lono> lonoList;

    private ViewPager mPager;
    private GraphPagerAdapter mPagerAdapter;
    public int pageNumber;// Number of page adapter
    public boolean isTemperatureActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        initView();
        channel = getIntent().getExtras().getInt(Utils.CHANNEL);

        // load list data form database
        lonoDao = MyDatabaseHelper.getInstance(this).getmSession().getLonoDao();
        setDataToPager(channel);
        showLine(channel - 1);
    }

    public void setDataToPager(int channel) {
        lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel)).orderDesc(LonoDao.Properties.TimeStamp)
                .list();
        if (lonoList.size() > 0) {
            // Get number of page
            pageNumber = (int) ((lonoList.get(0).getTimeStamp() + 5 * Utils.ONE_MINUTE
                    - lonoList.get(lonoList.size() - 1).getTimeStamp()) / Utils.ONE_DAY);
            long startDate = lonoList.get(lonoList.size() - 1).getTimeStamp();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startDate);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            long roundStartDate = c.getTimeInMillis();
            mPagerAdapter = new GraphPagerAdapter(getSupportFragmentManager(), channel, pageNumber + 1, roundStartDate, isTemperatureActivity);
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(pageNumber);
        } else {
            mPagerAdapter = new GraphPagerAdapter(getSupportFragmentManager(), channel, 0, 0, true);
            mPager.setAdapter(mPagerAdapter);
            resetView();
        }
    }

    public void initView() {
        isTemperatureActivity = true;
        setFont();
        homeBtn = (Button) findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(this);
        nowTv = (TextView) findViewById(R.id.tv_now_value);
        minTv = (TextView) findViewById(R.id.tv_min_value);
        maxTv = (TextView) findViewById(R.id.tv_max_value);
        averageTv = (TextView) findViewById(R.id.tv_average_value);
        dewPointTv = (TextView) findViewById(R.id.tv_dewpoint_value);
        for (int i = 0; i < arrayButton.length; i++) {
            arrayButton[i] = (Button) findViewById(arrayButtonId[i]);
            arrayLine[i] = findViewById(arrayLineId[i]);
            arrayButton[i].setOnClickListener(this);
        }
        mPager = (ViewPager) findViewById(R.id.view_pager);
    }


    public void init(List<Lono> lonoList) {
        List<Integer> listTemperature = new ArrayList<Integer>();
        if (lonoList.size() == 0) {
            return;
        } else {
            for (Lono lono : lonoList) {
                listTemperature.add(lono.getTemperature());
            }
        }
        nowTv.setText(listTemperature.get(listTemperature.size() - 1) + " °C");
        maxValue = Collections.max(listTemperature);
        minValue = Collections.min(listTemperature);
        minTv.setText(minValue + " °C");
        maxTv.setText(maxValue + " °C");
        averageTv.setText(getAverage(listTemperature) + " °C");
//        dewPointTv.setText(getDewPoint(listTemp.get(listTemp.size() - 1),
//                listHumidity.get(listHumidity.size() - 1)) + " °C");
    }

    public void resetView() {
        nowTv.setText("-- °C");
        minTv.setText("-- °C");
        maxTv.setText("-- °C");
        averageTv.setText("-- °C");
        dewPointTv.setText("-- °C");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                goBack();
                break;
            case R.id.btn1:
                setDataToPager(1);
                showLine(0);
                break;
            case R.id.btn2:
                setDataToPager(2);
                showLine(1);
                break;
            case R.id.btn3:
                setDataToPager(3);
                showLine(2);
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

    private void showLine(int index) {
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
}
