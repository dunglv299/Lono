package com.teusoft.lono.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.teusoft.lono.R;
import com.teusoft.lono.adapter.MainViewPagerAdapter;
import com.teusoft.lono.business.CSVExport;
import com.teusoft.lono.business.MyGattServices;
import com.teusoft.lono.customview.CustomDigitalClock;
import com.teusoft.lono.dao.Lono;
import com.teusoft.lono.dao.LonoDao;
import com.teusoft.lono.dao.MyDatabaseHelper;
import com.teusoft.lono.service.BluetoothLeService;
import com.teusoft.lono.utils.LogUtils;
import com.teusoft.lono.utils.MySharedPreferences;
import com.teusoft.lono.utils.Utils;
import de.greenrobot.dao.query.Query;
import fragment.MainViewFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnClickListener, ViewPager.OnPageChangeListener, View.OnLongClickListener {
    private static final int mInterval = 5 * 60 * 1000;
    private static final long SCAN_PERIOD = 100000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int BUTTONS_SIZE = 3;

    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private Handler scanHandler;
    private BluetoothAdapter mBluetoothAdapter;

    Button[] arrayButton = new Button[BUTTONS_SIZE];
    CustomDigitalClock dc;
    RelativeLayout progressLayout;
    TextView scanningTextView;


    private int[] arrayButtonId = {R.id.btn1, R.id.btn2, R.id.btn3};
    private ArrayList<Integer> listTemperature;
    private ArrayList<Integer> listHumidity;
    private int channel;
    private MySharedPreferences mySharedPreferences;
    private List<String> listDevice;
    private Handler repeatHandler;
    private int mDeviceNumber;
    private long lastUpdate;
    private LonoDao lonoDao;
    private MyGattServices myGattServices;
    private ToggleButton mDegreeToggle;
    private boolean isDegreeF;
    private String[] addressArray = new String[3];

    private ViewPager mPager;
    private MainViewPagerAdapter mainViewPagerAdapter;


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
        mPager = (ViewPager) findViewById(R.id.main_view_pager);
        mPager.setOffscreenPageLimit(2);
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mainViewPagerAdapter);
        mPager.setOnPageChangeListener(this);


        setFont();
        for (int i = 0; i < arrayButton.length; i++) {
            arrayButton[i] = (Button) findViewById(arrayButtonId[i]);
//            arrayLine[i] = findViewById(arrayLineId[i]);
            arrayButton[i].setOnClickListener(this);
            arrayButton[i].setOnLongClickListener(this);
        }

        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        scanningTextView = (TextView) findViewById(R.id.scanning_textView);
        progressLayout.setVisibility(View.VISIBLE);
        // init export btn
        findViewById(R.id.exportBtn).setOnClickListener(this);
        mDegreeToggle = (ToggleButton) findViewById(R.id.degree_toggle);
        mDegreeToggle.setOnClickListener(this);
    }

    private void init() {
        listTemperature = new ArrayList<Integer>();
        listHumidity = new ArrayList<Integer>();
        mySharedPreferences = new MySharedPreferences(this);
        listDevice = new ArrayList<String>();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        // Init DAO
        lonoDao = MyDatabaseHelper.getInstance(this).getmSession().getLonoDao();
        // Setting C/F degree
        isDegreeF = mySharedPreferences.getBoolean(Utils.DEGREE_TYPE);
        mDegreeToggle.setChecked(isDegreeF);
        // Change clock format
        dc.setIs24hMode(!isDegreeF);
        dc.initClock(this);
    }


    public void setFont() {
        Typeface tf = Utils.getTypeface(this);
        dc = (CustomDigitalClock) findViewById(R.id.tv_clock);
        dc.setTypeface(tf);

        ((TextView) findViewById(R.id.btn1)).setTypeface(tf);
        ((TextView) findViewById(R.id.btn2)).setTypeface(tf);
        ((TextView) findViewById(R.id.btn3)).setTypeface(tf);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Init channel name
        for (int i = 0; i < BUTTONS_SIZE; i++) {
            String channelName = mySharedPreferences.getString("channel_name_" + (i + 1));
            if (!TextUtils.isEmpty(channelName)) {
                arrayButton[i].setText(channelName);
                arrayButton[i].setTextSize(getResources().getDimension(R.dimen.graph_font_size));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                channel = 1;
                onChannelPress(channel);
                break;
            case R.id.btn2:
                channel = 2;
                onChannelPress(channel);
                break;
            case R.id.btn3:
                channel = 3;
                onChannelPress(channel);
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

    /**
     * set view and scan when select channel
     *
     * @param channel
     */
    public void onChannelPress(int channel) {
        listDevice.clear();
        if (TextUtils.isEmpty(addressArray[channel - 1])) {
            scanLeDevice(true);
        }
        goToChannel(channel - 1);
        showLine(arrayButtonId[channel - 1]);
        mySharedPreferences.putBoolean(Utils.AUTO_CONNECT, false);
    }

    private void showLine(int id) {
        int index = getIndex(id);
        for (int i = 0; i < arrayButtonId.length; i++) {
            if (i == index) {
//                arrayLine[i].setVisibility(View.VISIBLE);
                arrayButton[i].setBackgroundColor(getResources().getColor(
                        R.color.color_botton_enable));
            } else {
//                arrayLine[i].setVisibility(View.GONE);
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

    private void goToChannel(int channel) {
        mPager.setCurrentItem(channel);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress, channel);
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
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                LogUtils.e("Disconnect");
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
                myGattServices.displayGattServices(mBluetoothLeService
                        .getSupportedGattServices());
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
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
                listTemperature.add(temperature);
                listHumidity.add(humidity);
                if (type == 0 && indexArray == 1) {
                    // Get address
                    addressArray[channel - 1] = mBluetoothLeService.getmBluetoothDeviceAddress();
                    lastUpdate = System.currentTimeMillis();
                    displayData(listTemperature, listHumidity,
                            lastUpdate);
                    insertDao(listTemperature, listHumidity, channel);
                    if (!mySharedPreferences.getBoolean(Utils.AUTO_CONNECT)) {
                        scanLeDevice(false);
                        unbindService(mServiceConnection);
                    }
                }
            }
        }
    };

    private void connectDevice(int index) {
        mDeviceAddress = listDevice.get(index);
        Intent gattServiceIntent = new Intent(MainActivity.this,
                BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void reconnect() {
        progressLayout.setVisibility(View.VISIBLE);
        scanningTextView.setText("Reconnect...");
        mDeviceAddress = addressArray[channel - 1];
        Intent gattServiceIntent = new Intent(MainActivity.this,
                BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void displayData(ArrayList<Integer> listTemperature,
                             ArrayList<Integer> listHumidity, long lastUpdate) {
        LogUtils.e("display Data");
        showLine(arrayButtonId[channel - 1]);
        scanningTextView.setText("Scanning");
        MainViewFragment fragment = getActiveFragment();
        if (fragment != null) {
            fragment.updateDisplay(listTemperature, listHumidity, lastUpdate);
            mPager.setCurrentItem(channel - 1);
        }
    }

    /**
     * Get active fragment in viewpager
     *
     * @return
     */
    public MainViewFragment getActiveFragment() {
        return
                (MainViewFragment) getSupportFragmentManager().findFragmentByTag(
                        "android:switcher:" + R.id.main_view_pager + ":" + (channel - 1));
    }

    /**
     * Insert data to DAO
     *
     * @param listTemperature
     * @param listHumidity
     * @param channel
     */
    private void insertDao(final ArrayList<Integer> listTemperature, final ArrayList<Integer> listHumidity, final int channel) {
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
            scanHandler.postDelayed(stopScanRunnable, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            progressLayout.setVisibility(View.VISIBLE);
        } else {
            scanHandler.removeCallbacks(stopScanRunnable);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            progressLayout.setVisibility(View.INVISIBLE);
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
                LogUtils.e("device: " + device.getAddress());
                listDevice.add(device.getAddress());
                mDeviceAddress = device.getAddress();
                Intent gattServiceIntent = new Intent(MainActivity.this,
                        BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection,
                        BIND_AUTO_CREATE);
            }
        }
    };


    // Create repeat scan
    private Runnable mScanRequest = new Runnable() {
        @Override
        public void run() {
            listDevice = new ArrayList<String>();
            mDeviceNumber = 0;
            LogUtils.e("Repeat");
            scanLeDevice(true);
            repeatHandler.postDelayed(mScanRequest, mInterval);
        }
    };
    // Stop scan runnable
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("Stop", "Stop");
            progressLayout.setVisibility(View.GONE);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            invalidateOptionsMenu();
        }
    };

    private void startRepeatingScan() {
        mySharedPreferences.putBoolean(Utils.AUTO_CONNECT, true);
        mScanRequest.run();
    }

    private void stopRepeatingScan() {
        repeatHandler.removeCallbacks(mScanRequest);
    }

    public void onToggleClicked(View view) {
        // Is the toggle on?
        isDegreeF = ((ToggleButton) view).isChecked();
        // Change time type
        if (isDegreeF){
            dc.setIs24hMode(false);
            dc.initClock(this);
        }else{
            dc.setIs24hMode(true);
            dc.initClock(this);
        }
        mySharedPreferences.putBoolean(Utils.DEGREE_TYPE, isDegreeF);
        // Change clock
        // TODO change clock type
        // Change view in fragment
        for (int i = 0; i < 3; i++) {
            MainViewFragment fragment = (MainViewFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + R.id.main_view_pager + ":" + i);
            if (fragment != null) {
                fragment.setDegreeView(isDegreeF, lastUpdate);
            }
        }
    }

    public int getChannel() {
        return channel;
    }

    public boolean isDegreeF() {
        return isDegreeF;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int position) {
        channel = position + 1;
        showLine(arrayButtonId[position]);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                TemperatureActivity.showEditChannelDialog(this, arrayButton, mySharedPreferences, 1);
                break;
            case R.id.btn2:
                TemperatureActivity.showEditChannelDialog(this, arrayButton, mySharedPreferences, 2);
                break;
            case R.id.btn3:
                TemperatureActivity.showEditChannelDialog(this, arrayButton, mySharedPreferences, 3);
                break;
        }
        return true;
    }

}
