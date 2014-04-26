package fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.teusoft.lono.R;
import com.teusoft.lono.activity.HumidityActivity;
import com.teusoft.lono.activity.TemperatureActivity;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by DungLV on 20/4/2014.
 */
public class WeekGraphFragment extends Fragment {
    public static final String ARG_PAGE = "page";
    public static final String CHANNEL = "channel";
    public static final String ROUND_STARTDATE = "roundStartDate";


    private int mPageNumber;
    private int channel;
    LinearLayout graphLayout;
    public GraphView.GraphViewData[] dataGraph1;
    public String[] labelX = new String[8];
    private LonoDao lonoDao;
    private List<Lono> lonoList;
    private int maxValue, minValue;
    private long roundStartDate;
    private long roundEndDate;

    public static WeekGraphFragment create(int pageNumber, int channel, long roundStartDate) {
        WeekGraphFragment fragment = new WeekGraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        args.putInt(CHANNEL, channel);
        args.putLong(ROUND_STARTDATE, roundStartDate);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        channel = getArguments().getInt(CHANNEL);
        roundStartDate = getArguments().getLong(ROUND_STARTDATE) + mPageNumber * Utils.ONE_WEEK;
        roundEndDate = roundStartDate + (mPageNumber + 1) * Utils.ONE_WEEK;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        View v = inflater
                .inflate(R.layout.fragment_graph, container, false);
        TextView pageNumberTv = (TextView) v.findViewById(R.id.pageNumber);
        pageNumberTv.setText("Week: " + mPageNumber);
        graphLayout = (LinearLayout) v.findViewById(R.id.graph_layout);

        // load data from database
        lonoDao = MyDatabaseHelper.getInstance(getActivity()).getmSession().getLonoDao();
        lonoList = lonoDao.queryBuilder()
                .where(LonoDao.Properties.Channel.eq(channel), LonoDao.Properties.TimeStamp.between(roundStartDate, roundEndDate - 1))
                .orderAsc(LonoDao.Properties.TimeStamp)
                .list();
        drawGraph(lonoList);

        // Reset textview in activity
        TemperatureActivity activity = (TemperatureActivity) getActivity();
        if (activity.pageNumber == mPageNumber) {
            activity.init(lonoList);
        }
        return v;
    }

    public int getValue(Lono lono) {
        Activity mActivity = getActivity();
        if (mActivity instanceof HumidityActivity) {
            return lono.getHumidity();
        } else {
            return lono.getTemperature();
        }
    }

    public void drawGraph(List<Lono> lonos) {
        List<Integer> listData = new ArrayList<Integer>();
        List<Integer> listMinutes = new ArrayList<Integer>();
        // list of x-coordinate
        for (int i = 0; i < lonos.size(); i++) {
            Lono lono = lonos.get(i);
            listData.add(getValue(lono));
            // get minute of day
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lono.getTimeStamp());
            int minute = (calendar.get(Calendar.DAY_OF_WEEK) - 1) * 288 + (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) / 5;
            listMinutes.add(minute);
        }
        Log.e("list data fragment", listData.size() + "");
        if (listData.size() == 0) {
            return;
        }
        graphLayout.removeAllViews();
        // Init max min value
        maxValue = Collections.max(listData);
        minValue = Collections.min(listData);
        // init example series data
        int size = listData.size();
        dataGraph1 = new GraphView.GraphViewData[size];

        // Set data for graph
        for (int i = 0; i < size; i++) {
            dataGraph1[i] = new GraphView.GraphViewData(listMinutes.get(i), getValue(
                    lonos.get(i)));
        }
        GraphViewSeries seriesData = new GraphViewSeries("dunglv",
                new GraphViewSeries.GraphViewSeriesStyle(Color.WHITE, (int) getResources()
                        .getDimension(R.dimen.thick_size)), dataGraph1
        );

        LineGraphView graphView = new LineGraphView(getActivity(), "");
        graphView.getGraphViewStyle().setTextSize(
                getResources().getDimension(R.dimen.graph_font_size));
        graphView.getGraphViewStyle().setNumVerticalLabels(5);
        graphView.getGraphViewStyle().setNumHorizontalLabels(8);
        graphView.setViewPort(0, 288 * 7);
        setLabelX();
        graphView.setHorizontalLabels(labelX);
        graphView.addSeries(seriesData); // data
        int maxY = maxValue + Math.abs(minValue / 2);
        if (minValue < 0) {
            graphView.setManualYAxisBounds(maxY,
                    minValue - 10);
        } else {
            if (maxY > 60) {
                graphView.setManualYAxisBounds(maxY, 0);
            } else {
                graphView.setManualYAxisBounds(60, 0);
            }
        }
        graphLayout.addView(graphView);
    }

    public void setLabelX() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        labelX[0] = "SUN";
        labelX[1] = "MON";
        labelX[2] = "TUE";
        labelX[3] = "WED";
        labelX[4] = "THU";
        labelX[5] = "FRI";
        labelX[6] = "SAT";
        labelX[7] = "";

    }
}
