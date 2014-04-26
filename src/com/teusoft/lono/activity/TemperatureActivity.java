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
import com.teusoft.lono.adapter.DayGraphPagerAdapter;
import com.teusoft.lono.adapter.WeekGraphPagerAdapter;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.utils.Utils;

import java.util.ArrayList;
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
    private DayGraphPagerAdapter mPagerAdapter;
    public int pageNumber;// Number of page adapter
    private boolean isDaySelected;// Select day graph

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        initView();
        channel = getIntent().getExtras().getInt(Utils.CHANNEL);

        // load list data form database
        lonoDao = MyDatabaseHelper.getInstance(this).getmSession().getLonoDao();
        setDataDayGraphToPager(channel);
        showLine(channel - 1);
        isDaySelected = true;
    }

    /**
     * Set data for day graph
     *
     * @param channel
     */
    public void setDataDayGraphToPager(int channel) {
        lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel)).orderDesc(LonoDao.Properties.TimeStamp)
                .list();
        if (lonoList.size() > 0) {
            // Get number of page
            long roundStartDate = Utils.getRoundDay(lonoList.get(lonoList.size() - 1).getTimeStamp());
            // Page number is endtime - starttime/ oneday
            pageNumber = (int) ((Utils.getRoundDay(lonoList.get(0).getTimeStamp()) - roundStartDate) / Utils.ONE_DAY);
            mPagerAdapter = new DayGraphPagerAdapter(getSupportFragmentManager(), channel, pageNumber + 1, roundStartDate);
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(pageNumber);
        } else {
            resetView();
        }
    }

    /**
     * Set data for Week Graph
     *
     * @param channel
     */
    public void setDataWeekGraphToPager(int channel) {
        lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel)).orderDesc(LonoDao.Properties.TimeStamp)
                .list();
        if (lonoList.size() > 0) {
            // Get number of page
            long roundStartDate = Utils.getRoundDay(lonoList.get(lonoList.size() - 1).getTimeStamp());
            // Page number is endtime - starttime/ oneday
            pageNumber = (int) ((Utils.getRoundDay(lonoList.get(0).getTimeStamp()) - roundStartDate) / Utils.ONE_WEEK);
            WeekGraphPagerAdapter mPagerAdapter = new WeekGraphPagerAdapter(getSupportFragmentManager(), channel, pageNumber + 1, roundStartDate);
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(pageNumber);
        } else {
            resetView();
        }
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
        for (int i = 0; i < arrayButton.length; i++) {
            arrayButton[i] = (Button) findViewById(arrayButtonId[i]);
            arrayLine[i] = findViewById(arrayLineId[i]);
            arrayButton[i].setOnClickListener(this);
        }
        findViewById(R.id.dayBtn).setOnClickListener(this);
        findViewById(R.id.dayBtn).setBackgroundColor(getResources().getColor(R.color.green_text_color));
        findViewById(R.id.weekBtn).setOnClickListener(this);
        mPager = (ViewPager) findViewById(R.id.view_pager);
    }


    public void init(List<Lono> lonoList) {
        List<Integer> listTemperature = new ArrayList<Integer>();
        List<Integer> listHumidity = new ArrayList<Integer>();

        if (lonoList.size() == 0) {
            return;
        } else {
            for (Lono lono : lonoList) {
                listTemperature.add(lono.getTemperature());
                listHumidity.add(lono.getHumidity());
            }
        }
        nowTv.setText(listTemperature.get(listTemperature.size() - 1) + " °C");
        maxValue = Collections.max(listTemperature);
        minValue = Collections.min(listTemperature);
        minTv.setText(minValue + " °C");
        maxTv.setText(maxValue + " °C");
        averageTv.setText(getAverage(listTemperature) + " °C");
        dewPointTv.setText(getDewPoint(listTemperature.get(listTemperature.size() - 1),
                listHumidity.get(listHumidity.size() - 1)) + " °C");
    }

    public void resetView() {
        nowTv.setText("-- °C");
        minTv.setText("-- °C");
        maxTv.setText("-- °C");
        averageTv.setText("-- °C");
        dewPointTv.setText("-- °C");
        mPagerAdapter = new DayGraphPagerAdapter(getSupportFragmentManager(), channel, 0, 0);
        mPager.setAdapter(mPagerAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                goBack();
                break;
            case R.id.btn1:
                if (isDaySelected) {
                    setDataDayGraphToPager(1);
                } else {
                    setDataWeekGraphToPager(1);
                }
                showLine(0);
                channel = 1;
                break;
            case R.id.btn2:
                if (isDaySelected) {
                    setDataDayGraphToPager(2);
                } else {
                    setDataWeekGraphToPager(2);
                }
                showLine(1);
                channel = 2;
                break;
            case R.id.btn3:
                if (isDaySelected) {
                    setDataDayGraphToPager(3);
                } else {
                    setDataWeekGraphToPager(3);
                }
                showLine(2);
                channel = 3;
                break;
            case R.id.dayBtn:
                isDaySelected = true;
                setDataDayGraphToPager(channel);
                findViewById(R.id.dayBtn).setBackgroundColor(getResources().getColor(R.color.green_text_color));
                findViewById(R.id.weekBtn).setBackgroundColor(0);

                break;
            case R.id.weekBtn:
                isDaySelected = false;
                setDataWeekGraphToPager(channel);
                findViewById(R.id.weekBtn).setBackgroundColor(getResources().getColor(R.color.green_text_color));
                findViewById(R.id.dayBtn).setBackgroundColor(0);

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
