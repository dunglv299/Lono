package fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DungLV on 20/4/2014.
 */
public class DayGraphFragment extends Fragment {
    public static final String ARG_PAGE = "page";
    public static final String CHANNEL = "channel";
    public static final String ROUND_STARTDATE = "roundStartDate";


    private int mPageNumber;
    private int channel;
    LinearLayout graphLayout;
    public GraphView.GraphViewData[] dataGraph1;
    public String[] labelX = new String[7];
    private LonoDao lonoDao;
    private List<Lono> lonoList;
    private int maxValue, minValue;
    private long roundStartDate;
    private long roundEndDate;
    private boolean isDegreeF;


    public static DayGraphFragment create(int pageNumber, int channel, long roundStartDate, boolean isDegreeF) {
        DayGraphFragment fragment = new DayGraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        args.putInt(CHANNEL, channel);
        args.putLong(ROUND_STARTDATE, roundStartDate);
        args.putBoolean(Utils.DEGREE_TYPE, isDegreeF);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        channel = getArguments().getInt(CHANNEL);
        roundStartDate = getArguments().getLong(ROUND_STARTDATE) + mPageNumber * Utils.ONE_DAY;
        roundEndDate = roundStartDate + Utils.ONE_DAY;
        isDegreeF = getArguments().getBoolean(Utils.DEGREE_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        View v = inflater
                .inflate(R.layout.fragment_graph, container, false);
        TextView pageNumberTv = (TextView) v.findViewById(R.id.pageNumber);
        if (!isDegreeF) {
            pageNumberTv.setText("" + new SimpleDateFormat("dd/MM/yyyy").format(new Date(roundStartDate)));
        } else {
            pageNumberTv.setText("" + new SimpleDateFormat("MM/dd/yyyy").format(new Date(roundStartDate)));
        }
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
            if (isDegreeF) {
                return Utils.getFValue(lono.getTemperature());
            }
            return lono.getTemperature();
        }
    }

    public void drawGraph(List<Lono> lonos) {
        List<Integer> listData = new ArrayList<Integer>();
        List<Integer> listMinutes = new ArrayList<Integer>();

        for (Lono lono : lonos) {
            listData.add(getValue(lono));
            // get minute of day
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lono.getTimeStamp());
            int minute = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) / 5;
            listMinutes.add(minute);
        }
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
        graphView.getGraphViewStyle().setNumHorizontalLabels(7);
        graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
        graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
        graphView.setViewPort(0, 288);
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
        for (int i = labelX.length - 1; i >= 0; i--) {
            labelX[i] = String.valueOf(24 - (labelX.length - i - 1) * 4) + ":00";
        }
    }
}
