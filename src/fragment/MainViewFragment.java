package fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.teusoft.lono.R;
import com.teusoft.lono.activity.HumidityActivity;
import com.teusoft.lono.activity.MainActivity;
import com.teusoft.lono.activity.TemperatureActivity;
import com.teusoft.lono.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by DungLV on 22/5/2014.
 */
public class MainViewFragment extends BaseFragment implements View.OnClickListener {
    TextView currentTempTv, currentHumidityTv;
    private TextView minTempTv, maxTempTv, minHumidityTv, maxHumidityTv, lastUpdatedTv, lowTv, normalTv, hightTv;
    private MainActivity activity;
    private ArrayList<Integer> listTemperature;
    private ArrayList<Integer> listHumidity;
    public TextView reconnectBtn;

    @Override
    protected void init(View view) {
        minTempTv = (TextView) view.findViewById(R.id.tv_min_temp);
        maxTempTv = (TextView) view.findViewById(R.id.tv_max_temp);
        minHumidityTv = (TextView) view.findViewById(R.id.tv_min_humidity);
        maxHumidityTv = (TextView) view.findViewById(R.id.tv_max_humidity);
        lastUpdatedTv = (TextView) view.findViewById(R.id.tv_lastupdated);
        lowTv = (TextView) view.findViewById(R.id.tv_low);
        normalTv = (TextView) view.findViewById(R.id.tv_normal);
        hightTv = (TextView) view.findViewById(R.id.tv_hight);
        view.findViewById(R.id.temp_layout).setOnClickListener(this);
        view.findViewById(R.id.humidity_layout).setOnClickListener(this);
        currentTempTv = (TextView) view.findViewById(R.id.tv_temp);
        currentHumidityTv = (TextView) view.findViewById(R.id.tv_humidity);
        setFont(view);
        // Set data
        activity = (MainActivity) getActivity();
        listTemperature = new ArrayList<Integer>();
        listHumidity = new ArrayList<Integer>();
        Utils.changeTextDegreeType(currentTempTv, activity.isDegreeF());
        Utils.changeTextDegreeType(minTempTv, activity.isDegreeF());
        Utils.changeTextDegreeType(maxTempTv, activity.isDegreeF());

        // Init Reconnect button
        reconnectBtn = (TextView) view.findViewById(R.id.reconnect_btn);
        reconnectBtn.setOnClickListener(this);
    }

    public static MainViewFragment newInstance() {
        MainViewFragment fragment = new MainViewFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.temp_layout:
                goToTemperatureActivity();
                break;
            case R.id.humidity_layout:
                goToHumidityActivity();
                break;
            case R.id.reconnect_btn:
                activity.reconnect();
                break;
        }
    }

    public void setFont(View view) {
        Typeface tf = Utils.getTypeface(getActivity());
        ((TextView) view.findViewById(R.id.tv_lastupdated)).setTypeface(tf);
        currentTempTv.setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_min)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_min_temp)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_max)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_max_temp)).setTypeface(tf);
        currentHumidityTv.setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_normal)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_hight)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_low)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_min2)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_max2)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_min_humidity)).setTypeface(tf);
        ((TextView) view.findViewById(R.id.tv_max_humidity)).setTypeface(tf);
    }

    public void updateDisplay(ArrayList<Integer> listTemperature, ArrayList<Integer> listHumidity, long lastUpdate) {
        this.listTemperature.clear();
        this.listTemperature.addAll(listTemperature);
        this.listHumidity = listHumidity;
        setDegreeView(activity.isDegreeF(), lastUpdate);
        currentHumidityTv.setText(listHumidity.get(listHumidity.size() - 1) + " %");
        minHumidityTv.setText(Collections.min(listHumidity) + " %");
        maxHumidityTv.setText(Collections.max(listHumidity) + " %");
        setHumidityRange(listHumidity.get(listHumidity.size() - 1));
        lastUpdatedTv.setVisibility(View.VISIBLE);
        reconnectBtn.setVisibility(View.VISIBLE);
    }

    private void setHumidityRange(int value) {
        if (value >= 0 && value <= 25) {
            lowTv.setTextColor(Color.WHITE);
            normalTv.setTextColor(getResources().getColor(R.color.color_hight));
            hightTv.setTextColor(getResources().getColor(R.color.color_hight));
        } else if (value > 25 && value <= 50) {
            lowTv.setTextColor(getResources().getColor(R.color.color_hight));
            normalTv.setTextColor(Color.WHITE);
            hightTv.setTextColor(getResources().getColor(R.color.color_hight));
        } else {
            lowTv.setTextColor(getResources().getColor(R.color.color_hight));
            normalTv.setTextColor(getResources().getColor(R.color.color_hight));
            hightTv.setTextColor(Color.WHITE);
        }
    }

    private void goToTemperatureActivity() {
        Intent mIntent = new Intent(getActivity(), TemperatureActivity.class);
        mIntent.putExtra(Utils.CHANNEL, activity.getChannel());
        startActivity(mIntent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    private void goToHumidityActivity() {
        Intent mIntent = new Intent(getActivity(), HumidityActivity.class);
        mIntent.putExtra(Utils.CHANNEL, activity.getChannel());
        startActivity(mIntent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    public void setDegreeView(boolean isDegreeF, long lastUpdate) {
        Utils.changeTextDegreeType(currentTempTv, isDegreeF);
        Utils.changeTextDegreeType(minTempTv, isDegreeF);
        Utils.changeTextDegreeType(maxTempTv, isDegreeF);
        if (listTemperature.size() == 0) {
            return;
        }
        Log.e("dunglv", listTemperature.size() + "");
        int currentTemp = listTemperature.get(listTemperature.size() - 1);
        if (activity.isDegreeF()) {
            currentTempTv.setText(Utils.getFValue(currentTemp) + " °F");
            minTempTv.setText(Utils.getFValue(Collections.min(listTemperature)) + " °F");
            maxTempTv.setText(Utils.getFValue(Collections.max(listTemperature)) + " °F");
        } else {
            currentTempTv.setText(currentTemp + " °C");
            minTempTv.setText(Collections.min(listTemperature) + " °C");
            maxTempTv.setText(Collections.max(listTemperature) + " °C");
        }
        // Change time type
        if (activity.isDegreeF()) {
            lastUpdatedTv.setText(getString(R.string.last_updated) + ","
                    + new SimpleDateFormat("MMM dd hh:mm:ss aa")
                    .format(new Date(lastUpdate)));
        } else {
            lastUpdatedTv.setText(getString(R.string.last_updated) + ","
                    + new SimpleDateFormat("MMM dd HH:mm:ss")
                    .format(new Date(lastUpdate)));
        }
    }
}
