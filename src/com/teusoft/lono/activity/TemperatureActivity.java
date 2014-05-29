package com.teusoft.lono.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.teusoft.lono.R;
import com.teusoft.lono.adapter.DayGraphPagerAdapter;
import com.teusoft.lono.adapter.WeekGraphPagerAdapter;
import com.teusoft.lono.business.Sharing;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.utils.LogUtils;
import com.teusoft.lono.utils.MySharedPreferences;
import com.teusoft.lono.utils.Utils;
import fragment.DayGraphFragment;
import fragment.WeekGraphFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TemperatureActivity extends FragmentActivity implements OnClickListener, View.OnLongClickListener, ViewPager.OnPageChangeListener {
    Button homeBtn;
    TextView nowTv, minTv, maxTv, averageTv, dewPointTv;
    private int[] arrayButtonId = {R.id.btn1, R.id.btn2, R.id.btn3};
    private Button[] arrayButton = new Button[BUTTONS_SIZE];
    private static final int BUTTONS_SIZE = 3;
    public List<Integer> listHumidity;
    public int maxValue, minValue;
    public long lastUpdated;
    private int channel;
    private LonoDao lonoDao;
    public ViewPager mPager;
    protected DayGraphPagerAdapter mDayPagerAdapter;
    private WeekGraphPagerAdapter mWeekPagerAdapter;
    public int pageNumber;// Number of page adapter
    private boolean isDaySelected;// Select day graph
    private boolean isDegreeF; // Degree C or F
    RelativeLayout mainLayout;
    private MySharedPreferences mySharedPreferences;
    public boolean isSetNowTv; // Set now Tv first time when open last fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        mySharedPreferences = new MySharedPreferences(this);
        initView();
        channel = getIntent().getExtras().getInt(Utils.CHANNEL);
        isDegreeF = mySharedPreferences.getBoolean(Utils.DEGREE_TYPE);
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
        List<Lono> lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel)).orderDesc(LonoDao.Properties.TimeStamp)
                .list();
        if (lonoList.size() > 0) {
            // Get number of page
            long roundStartDate = Utils.getRoundDay(lonoList.get(lonoList.size() - 1).getTimeStamp());
            // Page number is endtime - starttime/ oneday
            pageNumber = (int) ((Utils.getRoundDay(lonoList.get(0).getTimeStamp()) - roundStartDate) / Utils.ONE_DAY);
            clearAdapter();
            mDayPagerAdapter = new DayGraphPagerAdapter(getSupportFragmentManager(), channel, pageNumber + 1, roundStartDate, isDegreeF);
            mPager.setAdapter(mDayPagerAdapter);
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
        List<Lono> lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel)).orderDesc(LonoDao.Properties.TimeStamp)
                .list();
        if (lonoList.size() > 0) {
            // Get number of page
            long roundStartDate = Utils.getRoundDay(lonoList.get(lonoList.size() - 1).getTimeStamp());
            // Page number is endtime - starttime/ oneday
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lonoList.get(0).getTimeStamp());
            int endWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            calendar.setTimeInMillis(roundStartDate);
            pageNumber = endWeek - calendar.get(Calendar.WEEK_OF_YEAR);
            // Clear all fragment in day graph
            clearAdapter();
            mWeekPagerAdapter = new WeekGraphPagerAdapter(getSupportFragmentManager(), channel, pageNumber + 1, roundStartDate, isDegreeF);
            mPager.setAdapter(mWeekPagerAdapter);
            mPager.setCurrentItem(pageNumber);

        } else {
            resetView();
        }
    }

    /**
     * Clear fragment inside adapter when change pagerAdapter
     */
    public void clearAdapter() {
        if (mDayPagerAdapter != null) {
            mDayPagerAdapter.clearAll();
        }
        if (mWeekPagerAdapter != null) {
            mWeekPagerAdapter.clearAll();
        }
        mPager.removeAllViews();
        mPager.setAdapter(null);
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
            arrayButton[i].setOnClickListener(this);
            arrayButton[i].setOnLongClickListener(this);
        }
        findViewById(R.id.dayBtn).setOnClickListener(this);
        findViewById(R.id.dayBtn).setBackgroundColor(getResources().getColor(R.color.green_text_color));
        findViewById(R.id.weekBtn).setOnClickListener(this);
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setOnPageChangeListener(this);

        // Share btn
        findViewById(R.id.share_btn).setOnClickListener(this);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        // Init channel name
        for (int i = 0; i < BUTTONS_SIZE; i++) {
            String channelName = mySharedPreferences.getString("channel_name_" + (i + 1));
            if (!TextUtils.isEmpty(channelName)) {
                arrayButton[i].setText(channelName);
                arrayButton[i].setTextSize(getResources().getDimension(R.dimen.graph_font_size));
            }
        }
    }

    /**
     * Change information text view: min, max ... when user swipe viewpager
     *
     * @param lonoList
     */
    public void initInfo(List<Lono> lonoList) {
        List<Integer> listTemperature = new ArrayList<Integer>();
        List<Integer> listHumidity = new ArrayList<Integer>();

        if (lonoList.size() == 0) {
            return;
        } else {
            for (Lono lono : lonoList) {
                if (isDegreeF) {
                    listTemperature.add(Utils.getFValue(lono.getTemperature()));
                } else {
                    listTemperature.add(lono.getTemperature());
                }
                listHumidity.add(lono.getHumidity());
            }
        }
        maxValue = Collections.max(listTemperature);
        minValue = Collections.min(listTemperature);
        // Set now text view in the first time
        if (!isSetNowTv) {
            nowTv.setText(listTemperature.get(listTemperature.size() - 1) + " °C");
        }
        minTv.setText(minValue + " °C");
        maxTv.setText(maxValue + " °C");
        averageTv.setText(getAverage(listTemperature) + " °C");
        dewPointTv.setText(getDewPoint(listTemperature.get(listTemperature.size() - 1),
                listHumidity.get(listHumidity.size() - 1)) + " °C");
        Utils.changeTextDegreeType(nowTv, isDegreeF);
        Utils.changeTextDegreeType(minTv, isDegreeF);
        Utils.changeTextDegreeType(maxTv, isDegreeF);
        Utils.changeTextDegreeType(averageTv, isDegreeF);
        Utils.changeTextDegreeType(dewPointTv, isDegreeF);
    }

    /**
     * Reset view when there is not data in channel
     */
    public void resetView() {
        isSetNowTv = false;
        nowTv.setText("-- °C");
        minTv.setText("-- °C");
        maxTv.setText("-- °C");
        averageTv.setText("-- °C");
        dewPointTv.setText("-- °C");
        Utils.changeTextDegreeType(nowTv, isDegreeF);
        Utils.changeTextDegreeType(minTv, isDegreeF);
        Utils.changeTextDegreeType(maxTv, isDegreeF);
        Utils.changeTextDegreeType(averageTv, isDegreeF);
        Utils.changeTextDegreeType(dewPointTv, isDegreeF);
        mDayPagerAdapter = new DayGraphPagerAdapter(getSupportFragmentManager(), channel, 0, 0, false);
        mPager.setAdapter(mDayPagerAdapter);
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
            case R.id.share_btn:
                new Sharing(this, mainLayout).share();
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

    public static int getAverage(List<Integer> listTemp) {
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
    private static int getDewPoint(int temp, int humidity) {
        int dewPoint = (int) (Math.pow(((double) humidity / 100d), 1d / 8d)
                * (112 + 0.9 * temp) + 0.1 * temp - 112);
        return dewPoint;
    }

    private void showLine(int index) {
        for (int i = 0; i < arrayButtonId.length; i++) {
            if (i == index) {
                arrayButton[i].setBackgroundColor(getResources().getColor(
                        R.color.color_botton_enable));
            } else {
                arrayButton[i].setBackgroundColor(getResources().getColor(
                        R.color.color_botton_disable));
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                showEditChannelDialog(this, arrayButton, mySharedPreferences, 1);
                break;
            case R.id.btn2:
                showEditChannelDialog(this, arrayButton, mySharedPreferences, 2);
                break;
            case R.id.btn3:
                showEditChannelDialog(this, arrayButton, mySharedPreferences, 3);
                break;
        }
        return true;
    }

    public static void showEditChannelDialog(final Context context, final Button[] arrayButton,
                                             final MySharedPreferences mySharedPreferences, final int channelNumber) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Rename channel " + channelNumber);
        // Set an EditText view to get user input
        final EditText editText = new EditText(context);
        editText.setHint("Input new channel name");
        alert.setView(editText);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = channelNumber + "\n" + editText.getText().toString().trim();
                if (TextUtils.isEmpty(value)) {
                    Utils.showAlert(context, "Your channel name is blank. Please try again!");
                    return;
                }
                arrayButton[channelNumber - 1].setText(value);
                arrayButton[channelNumber - 1].setTextSize(context.getResources().getDimension(R.dimen.graph_font_size));
                mySharedPreferences.putString("channel_name_" + channelNumber, value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int position) {
        LogUtils.e("position " + position);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.view_pager + ":" + position);
        if (fragment != null) {
            // Change textview min, max , average
            if (fragment instanceof DayGraphFragment) {
                ((DayGraphFragment) fragment).setChangeInformation();
            } else {
                ((WeekGraphFragment) fragment).setChangeInformation();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
