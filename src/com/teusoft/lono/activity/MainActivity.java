package com.teusoft.lono.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

	private Handler mHandler;
	private static final long SCAN_PERIOD = 30000;
	private BluetoothAdapter mBluetoothAdapter;

	private static final int BUTTONS_SIZE = 3;
	private int[] arrayButtonId = { R.id.btn1, R.id.btn2, R.id.btn3 };
	private int[] arrayLineId = { R.id.line_btn1, R.id.line_btn2,
			R.id.line_btn3 };
	private Button[] arrayButton = new Button[BUTTONS_SIZE];
	private View[] arrayLine = new View[BUTTONS_SIZE];
	private RelativeLayout tempLayout;
	private RelativeLayout humidityLayout;
	private TextView currentTempTv, currentHumidityTv, minTempTv, maxTempTv,
			minHumidityTv, maxHumidityTv, lastUpdatedTv, lowTv, normalTv,
			hightTv;
	private ArrayList<Integer> listTemperature;
	private ArrayList<Integer> listHumidity;
	private int channel;
	private int type;
	private int indexArray;
	private MySharedPreferences sharedPreferences;
	private long lastUpdate;
	private RelativeLayout progressLayout;
	ProgressDialog mProgressDialog;
	private List<String> listDevice;
	private ServiceConnection mServiceConnection1;
	private ServiceConnection mServiceConnection2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
		mHandler = new Handler();
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
			scanLeDevice(true);
		}

	}

	private void initData() {
		listTemperature = new ArrayList<Integer>();
		listHumidity = new ArrayList<Integer>();
		sharedPreferences = new MySharedPreferences(this);
		listDevice = new ArrayList<String>();
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
		progressLayout.setVisibility(View.GONE);
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
			showLine(v.getId());
			onButton1Press();
			break;
		case R.id.btn2:
			showLine(v.getId());
			onButton2Press();
			break;
		case R.id.btn3:
			showLine(v.getId());
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

	}

	private void onButton3Press() {

	}

	private void onButton2Press() {

	}

	// Code to manage Service lifecycle.
	// private final ServiceConnection mServiceConnection = new
	// ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName componentName,
	// IBinder service) {
	// Log.e("onServiceConnected", "onServiceConnected");
	// mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
	// .getService();
	// if (!mBluetoothLeService.initialize()) {
	// Log.e(TAG, "Unable to initialize Bluetooth");
	// finish();
	// }
	// // Automatically connects to the device upon successful start-up
	// // initialization.
	// mBluetoothLeService.connect(mDeviceAddress);
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName componentName) {
	// mBluetoothLeService = null;
	// }
	// };

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				Log.e("Connected", "Connected");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				Log.e("Disconnected", "Disconnected");
				mConnected = false;
				mBluetoothLeService.disconnect();
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				Log.e("Discovered", "Discovered");
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				indexArray = intent.getIntExtra(BluetoothLeService.EXTRA_COUNT,
						0);
				listTemperature.add(intent.getIntExtra(
						BluetoothLeService.EXTRA_TEMPERATURE, 0));
				listHumidity.add(intent.getIntExtra(
						BluetoothLeService.EXTRA_HUMIDITY, 0));
				channel = intent.getIntExtra(BluetoothLeService.EXTRA_CHANNEL,
						0);
				type = intent.getIntExtra(BluetoothLeService.EXTRA_TYPE, 0);
				// Show dialog
				showProgressDialog();
				if (type == 0 && indexArray == 1) {
					progressLayout.setVisibility(View.GONE);
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
					displayData();
					putDataToSharedPreference();
					listTemperature = new ArrayList<Integer>();
					listHumidity = new ArrayList<Integer>();
				}
			}
		}
	};

	private void displayData() {
		currentTempTv.setText(listTemperature.get(listTemperature.size() - 1)
				+ " °C");
		currentHumidityTv.setText(listHumidity.get(listHumidity.size() - 1)
				+ " %");
		// disconnect
		// mBluetoothLeService.disconnect();
		Log.e("size", listTemperature.size() + "");
		showLine(arrayButtonId[channel - 1]);

		// Set min max textview
		minTempTv.setText(Collections.min(listTemperature) + " °C");
		maxTempTv.setText(Collections.max(listTemperature) + " °C");
		minHumidityTv.setText(Collections.min(listHumidity) + " %");
		maxHumidityTv.setText(Collections.max(listHumidity) + " %");
		setHumidityRange(listHumidity.get(listHumidity.size() - 1));
		lastUpdate = System.currentTimeMillis();
		lastUpdatedTv.setText("Last updated,"
				+ new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US)
						.format(new Date(lastUpdate)));
		lastUpdatedTv.setVisibility(View.VISIBLE);
	}

	public ServiceConnection getConnection(final String address) {
		if (mBluetoothLeService != null) {
			mBluetoothLeService.disconnect();
		}
		ServiceConnection serviceConnection = new ServiceConnection() {
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
				mBluetoothLeService.connect(address);
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
				// mBluetoothLeService = null;
			}
		};
		return serviceConnection;
	}

	public void onConnect1Press(View v) {
		mDeviceAddress = "D0:FF:50:7C:04:F1";
		mServiceConnection1 = getConnection(mDeviceAddress);
		Intent gattServiceIntent = new Intent(MainActivity.this,
				BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection1, BIND_AUTO_CREATE);
	}

	public void onConnect2Press(View v) {
		mDeviceAddress = "D0:FF:50:7B:F8:48";
		mServiceConnection2 = getConnection(mDeviceAddress);
		Intent gattServiceIntent = new Intent(MainActivity.this,
				BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection2, BIND_AUTO_CREATE);
	}

	/**
	 * Push data to shared preference
	 */
	private void putDataToSharedPreference() {
		sharedPreferences.putList(BluetoothLeService.EXTRA_TEMPERATURE,
				listTemperature);
		sharedPreferences.putList(BluetoothLeService.EXTRA_HUMIDITY,
				listHumidity);
		sharedPreferences.putLong(Utils.LAST_UPDATED, lastUpdate);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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
		unregisterReceiver(mGattUpdateReceiver);
		if (mBluetoothLeService != null) {
			if (mServiceConnection1 != null) {
				unbindService(mServiceConnection1);
			}
			if (mServiceConnection2 != null) {
				unbindService(mServiceConnection2);
			}
			mBluetoothLeService = null;
		}
		putDataToSharedPreference();

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
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.e("Stop", "Stop");
					progressLayout.setVisibility(View.GONE);
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);
			mBluetoothAdapter.startLeScan(mLeScanCallback);
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
			// runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			if (device != null && !listDevice.contains(device.getAddress())
					&& !device.getAddress().equals("D0:FF:50:7B:F8:48")) {
				Log.e("device", device.getAddress());
				mDeviceAddress = device.getAddress();
				// listDevice.add(device.getAddress());
				// mDeviceAddress = "D0:FF:50:7B:F8:48";
				// mDeviceAddress = "D0:FF:50:7C:04:F1";
				Intent gattServiceIntent = new Intent(MainActivity.this,
						BluetoothLeService.class);
				bindService(gattServiceIntent, getConnection(mDeviceAddress),
						BIND_AUTO_CREATE);
				return;
			}
			// }
			// });
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
}
