package com.teusoft.lono.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.*;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.teusoft.lono.R;
import com.teusoft.lono.customview.CustomDigitalClock;
import com.teusoft.lono.service.BluetoothLeService;
import com.teusoft.lono.service.SampleGattAttributes;
import com.teusoft.lono.utils.MySharedPreferences;
import com.teusoft.lono.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity implements OnClickListener {
    // Init variable for getdata from service
    private final static String TAG = MainActivity.class.getSimpleName();
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
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
    private MySharedPreferences sharedPreferences;
    private long lastUpdate1, lastUpdate2, lastUpdate3;
    private RelativeLayout progressLayout;
    ProgressDialog mProgressDialog;
    private List<String> listDevice;
    private int mInterval = 5 * 60 * 1000;
    private Handler repeatHandler;
    private int mDeviceNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
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
            // scanLeDevice(true);
            startRepeatingScan();
        }

    }

    private void initData() {
        listTemperature1 = new ArrayList<Integer>();
        listHumidity1 = new ArrayList<Integer>();
        listTemperature2 = new ArrayList<Integer>();
        listHumidity2 = new ArrayList<Integer>();
        listTemperature3 = new ArrayList<Integer>();
        listHumidity3 = new ArrayList<Integer>();
        sharedPreferences = new MySharedPreferences(this);
        listDevice = new ArrayList<String>();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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
        progressLayout.setVisibility(View.VISIBLE);
        // showLine(R.id.btn1);
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
        if (listTemperature1.size() > 0) {
            channel = 1;
            displayData(listTemperature1, listHumidity1, lastUpdate1);
        }
    }

    private void onButton2Press() {
        if (listTemperature2.size() > 0) {
            channel = 2;
            displayData(listTemperature2, listHumidity2, lastUpdate2);
        }
    }

    private void onButton3Press() {
        if (listTemperature3.size() > 0) {
            channel = 3;
            displayData(listTemperature3, listHumidity3, lastUpdate3);
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
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
                displayGattServices(mBluetoothLeService
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
                // Show dialog
                showProgressDialog();
                if (channel == 1) {
                    listTemperature1.add(temperature);
                    listHumidity1.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate1 = System.currentTimeMillis();
                        displayData(listTemperature1, listHumidity1,
                                lastUpdate1);
                    }
                } else if (channel == 2) {
                    listTemperature2.add(temperature);
                    listHumidity2.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate2 = System.currentTimeMillis();
                        displayData(listTemperature2, listHumidity2,
                                lastUpdate2);
                    }
                } else if (channel == 3) {
                    listTemperature3.add(temperature);
                    listHumidity3.add(humidity);
                    if (type == 0 && indexArray == 1) {
                        lastUpdate3 = System.currentTimeMillis();
                        displayData(listTemperature3, listHumidity3,
                                lastUpdate3);
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
        // progressLayout.setVisibility(View.GONE);
        mProgressDialog.dismiss();
        Log.e("display Data", "display Data");
        currentTempTv.setText(listTemperature.get(listTemperature.size() - 1)
                + " °C");
        currentHumidityTv.setText(listHumidity.get(listHumidity.size() - 1)
                + " %");
        Log.e("size", listTemperature.size() + "");
        showLine(arrayButtonId[channel - 1]);

        // Set min max textview
        minTempTv.setText(Collections.min(listTemperature) + " °C");
        maxTempTv.setText(Collections.max(listTemperature) + " °C");
        minHumidityTv.setText(Collections.min(listHumidity) + " %");
        maxHumidityTv.setText(Collections.max(listHumidity) + " %");
        setHumidityRange(listHumidity.get(listHumidity.size() - 1));
        if (lastUpdate > 0) {
            lastUpdatedTv.setText("Last updated,"
                    + new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US)
                    .format(new Date(lastUpdate)));
        }
        lastUpdatedTv.setVisibility(View.VISIBLE);
        putDataToSharedPreference();
    }

    /**
     * Push data to shared preference
     */
    private void putDataToSharedPreference() {
        sharedPreferences.putList(Utils.LIST_TEMP1, listTemperature1);
        sharedPreferences.putList(Utils.LIST_TEMP2, listTemperature2);
        sharedPreferences.putList(Utils.LIST_TEMP3, listTemperature3);
        sharedPreferences.putList(Utils.LIST_HUMID1, listHumidity1);
        sharedPreferences.putList(Utils.LIST_HUMID2, listHumidity2);
        sharedPreferences.putList(Utils.LIST_HUMID3, listHumidity3);
        sharedPreferences.putLong(Utils.LAST_UPDATED1, lastUpdate1);
        sharedPreferences.putLong(Utils.LAST_UPDATED2, lastUpdate2);
        sharedPreferences.putLong(Utils.LAST_UPDATED3, lastUpdate3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if (mBluetoothLeService != null) {
        // final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        // Log.d(TAG, "Connect request result=" + result);
        // }
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
        sharedPreferences.clear();
        stopRepeatingScan();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(
                R.string.unknown_service);
        String unknownCharaString = getResources().getString(
                R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME,
                    SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME,
                        SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                // Notify data to screen
                if (gattCharacteristic
                        .getUuid()
                        .equals(BluetoothLeService.TEMPERATURE_MEASUREMENT_CHARACTERISTIC)) {
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
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
//            if (device.getName().equals("NGE76")) {
                Log.e("device", device.getAddress());
                listDevice.add(device.getAddress());
                mDeviceAddress = device.getAddress();
                Intent gattServiceIntent = new Intent(MainActivity.this,
                        BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection,
                        BIND_AUTO_CREATE);
                // if (device.getAddress().equals(listDevice.get(0))) {
                // connectDevice(0);
                // }
            }
        }
    };

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this,
                    AlertDialog.THEME_HOLO_LIGHT);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage("Loading data...");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
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

    // Create repeat scan
    Runnable mScanRequest = new Runnable() {
        @Override
        public void run() {
            listDevice = new ArrayList<String>();
            mDeviceNumber = 0;
            if (listTemperature1.size() > 3000) {
                listTemperature1 = new ArrayList<Integer>();
                listHumidity1 = new ArrayList<Integer>();
                listTemperature2 = new ArrayList<Integer>();
                listHumidity2 = new ArrayList<Integer>();
                listTemperature3 = new ArrayList<Integer>();
                listHumidity3 = new ArrayList<Integer>();
            }
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
}
