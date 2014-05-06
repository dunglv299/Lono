package com.teusoft.lono.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.teusoft.lono.R;
import com.teusoft.lono.business.CSVExport;
import com.teusoft.lono.business.MyGattServices;
import com.teusoft.lono.customview.CustomDigitalClock;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.service.BluetoothLeService;
import com.teusoft.lono.utils.LonoDataSharedPreferences;
import com.teusoft.lono.utils.MySharedPreferences;
import com.teusoft.lono.utils.Utils;
import de.greenrobot.dao.query.Query;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity implements OnClickListener {
    // Init variable for getdata from service
    private final static String TAG = MainActivity.class.getSimpleName();
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private Handler scanHandler;
    private static final long SCAN_PERIOD = 80000;
    private BluetoothAdapter mBluetoothAdapter;

    private static final int BUTTONS_SIZE = 3;
    private int[] arrayButtonId = {R.id.btn1, R.id.btn2, R.id.btn3};
    private int[] arrayLineId = {R.id.line_btn1, R.id.line_btn2,
            R.id.line_btn3};
    private Button[] arrayButton = new Button[BUTTONS_SIZE];
    private View[] arrayLine = new View[BUTTONS_SIZE];
    private RelativeLayout tempLayout;
    private RelativeLayout humidityLayout;
    private TextView currentTempTv, currentHumidityTv, minTempTv, maxTempTv,
            minHumidityTv, maxHumidityTv, lastUpdatedTv, lowTv, normalTv,
            hightTv;
    private ArrayList<Integer> listTemperature1;
    private ArrayList<Integer> listHumidity1;
    private ArrayList<Integer> listTemperature2;
    private ArrayList<Integer> listHumidity2;
    private ArrayList<Integer> listTemperature3;
    private ArrayList<Integer> listHumidity3;
    private int channel;
    private LonoDataSharedPreferences lonoDataSharedPreferences;
    private MySharedPreferences mySharedPreferences;
    private long lastUpdate1, lastUpdate2, lastUpdate3;
    private RelativeLayout progressLayout;
    private List<String> listDevice;
    private int mInterval = 5 * 60 * 1000;
    private Handler repeatHandler;
    private int mDeviceNumber;
    private LonoDao lonoDao;
    private TextView exportBtn, scanningTextView;
    private MyGattServices myGattServices;
    private ToggleButton mDegreeToggle;
    private boolean isDegreeF;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        init();
        scanHandler = new Handler();
        repeatHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Start scan
            startRepeatingScan();
        }

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        currentTempTv = (TextView) findViewById(R.id.tv_temp);
        currentHumidityTv = (TextView) findViewById(R.id.tv_humidity);
        minTempTv = (TextView) findViewById(R.id.tv_min_temp);
        maxTempTv = (TextView) findViewById(R.id.tv_max_temp);
        minHumidityTv = (TextView) findViewById(R.id.tv_min_humidity);
        maxHumidityTv = (TextView) findViewById(R.id.tv_max_humidity);
        lastUpdatedTv = (TextView) findViewById(R.id.tv_lastupdated);
        lowTv = (TextView) findViewById(R.id.tv_low);
        normalTv = (TextView) findViewById(R.id.tv_normal);
        hightTv = (TextView) findViewById(R.id.tv_hight);
        setFont();
        for (int i = 0; i < arrayButton.length; i++) {
            arrayButton[i] = (Button) findViewById(arrayButtonId[i]);
            arrayLine[i] = findViewById(arrayLineId[i]);
            arrayButton[i].setOnClickListener(this);
        }
        tempLayout = (RelativeLayout) findViewById(R.id.temp_layout);
        humidityLayout = (RelativeLayout) findViewById(R.id.humidity_layout);
        tempLayout.setOnClickListener(this);
        humidityLayout.setOnClickListener(this);
        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        scanningTextView = (TextView) findViewById(R.id.scanning_textView);
        progressLayout.setVisibility(View.VISIBLE);
        // showLine(R.id.btn1);
        // init export btn
        exportBtn = (TextView) findViewById(R.id.exportBtn);
        exportBtn.setOnClickListener(this);
        mDegreeToggle = (ToggleButton) findViewById(R.id.degree_toggle);
        mDegreeToggle.setOnClickListener(this);
    }

    private void init() {
        listTemperature1 = new ArrayList<Integer>();
        listHumidity1 = new ArrayList<Integer>();
        listTemperature2 = new ArrayList<Integer>();
        listHumidity2 = new ArrayList<Integer>();
        listTemperature3 = new ArrayList<Integer>();
        listHumidity3 = new ArrayList<Integer>();
        lonoDataSharedPreferences = new LonoDataSharedPreferences(this);
        mySharedPreferences = new MySharedPreferences(this);
        listDevice = new ArrayList<String>();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        // Init DAO
        lonoDao = MyDatabaseHelper.getInstance(this).getmSession().getLonoDao();
        // Setting C/F degree
        isDegreeF = mySharedPreferences.getBoolean(Utils.DEGREE_TYPE);
        mDegreeToggle.setChecked(isDegreeF);
        Utils.changeTextDegreeType(currentTempTv, isDegreeF);
        Utils.changeTextDegreeType(minTempTv, isDegreeF);
        Utils.changeTextDegreeType(maxTempTv, isDegreeF);
    }

    public void setFont() {
        Typeface tf = Utils.getTypeface(this);
        CustomDigitalClock dc = (CustomDigitalClock) findViewById(R.id.tv_clock);
        dc.setTypeface(tf);
        ((TextView) findViewById(R.id.tv_lastupdated)).setTypeface(tf);
        currentTempTv.setTypeface(tf);
        ((TextView) findViewById(R.id.tv_min)).setTypeface(tf);
        ((TextView) findViewById(R.id.tv_min_temp)).setTypeface(tf);
        ((TextView) findViewById(R.id.tv_max)).setTypeface(tf);
        ((TextView) findViewById(R.id.tv_max_temp)).setTypeface(tf);
        currentHumidityTv.setTypeface(tf);
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
        switch (v.getId()) {
            case R.id.temp_layout:
                goToTemperatureActivity();
                break;
            case R.id.humidity_layout:
                goToHumidityActivity();
                break;
            case R.id.btn1:
                onButton1Press();
                break;
            case R.id.btn2:
                onButton2Press();
                break;
            case R.id.btn3:
                onButton3Press();
                break;
            case R.id.exportBtn:
                new CSVExport(this, lonoDao, isDegreeF).export();
                break;
            case R.id.degree_toggle:
                onToggleClicked(v);
                break;
            default:
                break;
        }
    }

    private void goToTemperatureActivity() {
        Intent mIntent = new Intent(this, TemperatureActivity.class);
        mIntent.putExtra(Utils.CHANNEL, channel);
        startActivity(mIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    private void goToHumidityActivity() {
        Intent mIntent = new Intent(this, HumidityActivity.class);
        mIntent.putExtra(Utils.CHANNEL, channel);
        startActivity(mIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    private void showLine(int id) {
        int index = getIndex(id);
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
    private int getIndex(int id) {
        for (int i = 0; i < arrayButtonId.length; i++) {
            if (arrayButtonId[i] == id) {
                return i;
            }
        }
        return -1;
    }

    private void onButton1Press() {
        ArrayList<Integer> listTemperature = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_TEMP1);
        ArrayList<Integer> listHumidity = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_HUMID1);
        if (listTemperature.size() > 0) {
            channel = 1;
            displayData(listTemperature, listHumidity, lastUpdate1);
        }
    }

    private void onButton2Press() {
        ArrayList<Integer> listTemperature = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_TEMP2);
        ArrayList<Integer> listHumidity = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_HUMID2);
        if (listTemperature.size() > 0) {
            channel = 2;
            displayData(listTemperature, listHumidity, lastUpdate2);
        }
    }

    private void onButton3Press() {
        ArrayList<Integer> listTemperature = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_TEMP3);
        ArrayList<Integer> listHumidity = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt(Utils.LIST_HUMID3);
        if (listTemperature.size() > 0) {
            channel = 3;
            displayData(listTemperature, listHumidity, lastUpdate3);
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e("onServiceConnected", "onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            myGattServices = new MyGattServices(MainActivity.this, mBluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e("Connected", "Connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                Log.e("Disconnected", "Disconnected");
                mConnected = false;
                invalidateOptionsMenu();
                mDeviceNumber++;
                unbindService(mServiceConnection);
                if (listDevice.size() > mDeviceNumber) {
                    connectDevice(mDeviceNumber);
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                mConnected = true;
                Log.e("Discovered", "Discovered");
                myGattServices.displayGattServices(mBluetoothLeService
                        .getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                int indexArray = intent.getIntExtra(
                        BluetoothLeService.EXTRA_COUNT, 0);
                int temperature = intent.getIntExtra(
                        BluetoothLeService.EXTRA_TEMPERATURE, 0);
                int humidity = intent.getIntExtra(
                        BluetoothLeService.EXTRA_HUMIDITY, 0);
                channel = intent.getIntExtra(BluetoothLeService.EXTRA_CHANNEL,
                        0);
                int type = intent.getIntExtra(BluetoothLeService.EXTRA_TYPE, 0);
                // Loading data
                scanningTextView.setText("Loading data");
                if (channel == 1) {
                    listTemperature1.add(temperature);
                    listHumidity1.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate1 = System.currentTimeMillis();
                        displayData(listTemperature1, listHumidity1,
                                lastUpdate1);
                        insertTemperatureDao(listTemperature1, listHumidity1, channel);
                    }
                } else if (channel == 2) {
                    listTemperature2.add(temperature);
                    listHumidity2.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate2 = System.currentTimeMillis();
                        displayData(listTemperature2, listHumidity2,
                                lastUpdate2);
                        insertTemperatureDao(listTemperature2, listHumidity2, channel);

                    }
                } else if (channel == 3) {
                    listTemperature3.add(temperature);
                    listHumidity3.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate3 = System.currentTimeMillis();
                        displayData(listTemperature3, listHumidity3,
                                lastUpdate3);
                        insertTemperatureDao(listTemperature3, listHumidity3, channel);
                    }
                }
            }
        }
    };

    private void connectDevice(int index) {
        Log.e("connectDevice", (index + 1) + "");
        mDeviceAddress = listDevice.get(index);
        Intent gattServiceIntent = new Intent(MainActivity.this,
                BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void displayData(ArrayList<Integer> listTemperature,
                             ArrayList<Integer> listHumidity, long lastUpdate) {
        Log.e("display Data", "display Data");
        int currentTemp = listTemperature.get(listTemperature.size() - 1);
        if (isDegreeF) {
            currentTempTv.setText(Utils.getFValue(currentTemp) + " °F");
            minTempTv.setText(Utils.getFValue(Collections.min(listTemperature)) + " °F");
            maxTempTv.setText(Utils.getFValue(Collections.max(listTemperature)) + " °F");


        } else {
            currentTempTv.setText(currentTemp + " °C");
            minTempTv.setText(Collections.min(listTemperature) + " °C");
            maxTempTv.setText(Collections.max(listTemperature) + " °C");

        }
        currentHumidityTv.setText(listHumidity.get(listHumidity.size() - 1) + " %");
        Log.e("size", listTemperature.size() + "");
        showLine(arrayButtonId[channel - 1]);

        minHumidityTv.setText(Collections.min(listHumidity) + " %");
        maxHumidityTv.setText(Collections.max(listHumidity) + " %");
        setHumidityRange(listHumidity.get(listHumidity.size() - 1));
        if (lastUpdate > 0) {
            lastUpdatedTv.setText("Last updated,"
                    + new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US)
                    .format(new Date(lastUpdate)));
        }
        lastUpdatedTv.setVisibility(View.VISIBLE);
        putDataToSharedPreference(listTemperature, listHumidity);
        scanningTextView.setText("Scanning");
    }

    private void insertTemperatureDao(final ArrayList<Integer> listTemperature, final ArrayList<Integer> listHumidity, final int channel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Set timestamp
                Calendar calendar = Calendar.getInstance();
                long roundTime = calendar.getTimeInMillis() - (calendar.getTimeInMillis() % (5 * Utils.ONE_MINUTE));
                calendar.setTimeInMillis(roundTime);
                for (int i = listTemperature.size() - 1; i >= 0; i--) {
                    Lono lono = new Lono();
                    lono.setTemperature(listTemperature.get(i));
                    lono.setHumidity(listHumidity.get(i));
                    lono.setChannel(channel);
                    lono.setIndex(i);
                    // Increate minute from i = 1
                    if (i < listTemperature.size() - 1) {
                        calendar.add(Calendar.MINUTE, -5);
                    }
                    lono.setTimeStamp(calendar.getTimeInMillis());
                    // Check record is exist
                    Query query = lonoDao.queryBuilder().where(LonoDao.Properties.Channel.eq(channel),
                            LonoDao.Properties.TimeStamp.eq(calendar.getTimeInMillis())).build();
                    if (query.list().size() == 0) {
                        lonoDao.insert(lono);
                    }
                }
                listTemperature.clear();
                listHumidity.clear();
            }
        }).start();
    }

    /**
     * Push data to shared preference
     *
     * @param listTemperature
     * @param listHumidity
     */
    private void putDataToSharedPreference(ArrayList<Integer> listTemperature, ArrayList<Integer> listHumidity) {
        if (channel == 1) {
            lonoDataSharedPreferences.putList(Utils.LIST_TEMP1, listTemperature);
            lonoDataSharedPreferences.putList(Utils.LIST_HUMID1, listHumidity);
        } else if (channel == 2) {
            lonoDataSharedPreferences.putList(Utils.LIST_TEMP2, listTemperature);
            lonoDataSharedPreferences.putList(Utils.LIST_HUMID2, listHumidity);
        } else if (channel == 3) {
            lonoDataSharedPreferences.putList(Utils.LIST_TEMP3, listTemperature);
            lonoDataSharedPreferences.putList(Utils.LIST_HUMID3, listHumidity);
        }
        lonoDataSharedPreferences.putLong(Utils.CHANNEL, channel);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stopService(new Intent(this, BluetoothLeService.class));
        unregisterReceiver(mGattUpdateReceiver);
        stopService(new Intent(this, BluetoothLeService.class));
        if (mBluetoothLeService != null) {
            mBluetoothLeService = null;
        }
        lonoDataSharedPreferences.clear();
        stopRepeatingScan();
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // For scan device
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("Stop", "Stop");
                    progressLayout.setVisibility(View.GONE);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            progressLayout.setVisibility(View.VISIBLE);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else {
            startRepeatingScan();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            if (device != null && !listDevice.contains(device.getAddress())
                    && device.getName().equals("NGE76")) {
                Log.e("device", device.getAddress());
                listDevice.add(device.getAddress());
                mDeviceAddress = device.getAddress();
                Intent gattServiceIntent = new Intent(MainActivity.this,
                        BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection,
                        BIND_AUTO_CREATE);
            }
        }
    };


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

    // Create repeat scan
    Runnable mScanRequest = new Runnable() {
        @Override
        public void run() {
            listDevice = new ArrayList<String>();
            mDeviceNumber = 0;
            Log.e("Repeat", "Repeat");
            scanLeDevice(true);
            repeatHandler.postDelayed(mScanRequest, mInterval);
        }
    };

    private void startRepeatingScan() {
        mScanRequest.run();
    }

    private void stopRepeatingScan() {
        repeatHandler.removeCallbacks(mScanRequest);
    }

    public void onToggleClicked(View view) {
        // Is the toggle on?
        isDegreeF = ((ToggleButton) view).isChecked();
        mySharedPreferences.putBoolean(Utils.DEGREE_TYPE, isDegreeF);
        // Redisplay
        ArrayList<Integer> listTemperature = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt("listTemp" + channel);
        ArrayList<Integer> listHumidity = (ArrayList<Integer>) lonoDataSharedPreferences.getListInt("listHumid" + channel);
        if (listTemperature.size() > 0) {
            displayData(listTemperature, listHumidity, 0);
        }
        Utils.changeTextDegreeType(currentTempTv, isDegreeF);
        Utils.changeTextDegreeType(minTempTv, isDegreeF);
        Utils.changeTextDegreeType(maxTempTv, isDegreeF);
    }

}
