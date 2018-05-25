package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.stuttgart.uni.mc_exe2.BluetoothDeviceScanner;
import com.stuttgart.uni.mc_exe2.BluetoothLeDevice;
import com.stuttgart.uni.mc_exe2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapter.BluetoothLeDeviceListAdapter;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.Utils;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_FINE_LOCATION = 2;
    public static final int BTLE_SERVICES = 4;
    private static final int MY_PERMISSIONS_LOCATION = 3;

    private Map<String, BluetoothDevice> mScanResults;

    private BluetoothAdapter mBluetoothAdapter;

    //############################################################
    private BluetoothDeviceScanner mBluetoothDeviceScanner;
    private BluetoothLeDeviceListAdapter mBluetoothLeDeviceListAdapter;

    private HashMap<String, BluetoothLeDevice> mBTDevicesHashMap;
    private ArrayList<BluetoothLeDevice> mBTDevicesArrayList;

    private TextView mClientDeviceInfoTextView;
    private ListView mDiscoveredDevicesListView;
    private Button mStartScanningButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        this.initActivity();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothDeviceScanner = new BluetoothDeviceScanner(this, mBluetoothAdapter);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();
        mBluetoothLeDeviceListAdapter = new BluetoothLeDeviceListAdapter(ClientActivity.this, R.layout.adapter_view_layout, mBTDevicesArrayList);
        mDiscoveredDevicesListView.setAdapter(mBluetoothLeDeviceListAdapter);

        this.initializeBluetoothLE();

        String deviceInfo = "Device Info:"
                + "\nName: " + mBluetoothAdapter.getName()
                + "\nAddress: " + mBluetoothAdapter.getAddress();
        mClientDeviceInfoTextView.setText(deviceInfo);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            log("BLE is not supported. Exiting application...");
            finish();
        }
    }

    private void initActivity() {

        mDiscoveredDevicesListView = findViewById(R.id.discovered_devices_list_view);
        mClientDeviceInfoTextView = findViewById(R.id.client_device_info_text_view);
        mStartScanningButton = findViewById(R.id.start_scanning_button);

        mStartScanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mBluetoothDeviceScanner.isScanning()) {

                    Log.i(TAG, "BLE device scan started");
                    mStartScanningButton.setText(R.string.ble_device_scan_stop);

                    mBTDevicesArrayList.clear();
                    mBTDevicesHashMap.clear();

                    mBluetoothDeviceScanner.start();

                } else {

                    Log.i(TAG, "BLE device scan stopped");
                    mStartScanningButton.setText(R.string.ble_device_scan_start);

                    mBluetoothDeviceScanner.stop();

                }
            }
        });
    }

    private void initializeBluetoothLE() {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            Log.i(TAG, super.getString(R.string.ble_not_supported));
            super.finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.i(TAG, "Bluetooth not enabled, requesting BLE enable");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
        else if (requestCode == BTLE_SERVICES) {
            // Do something
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(ClientActivity.this, "permission was granted", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(ClientActivity.this, "permission was denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void updateScanButton(int buttonText) {

        mStartScanningButton.setText( super.getString( buttonText ) );

    }

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!mBTDevicesHashMap.containsKey(address)) {
            BluetoothLeDevice btleDevice = new BluetoothLeDevice(device);
            btleDevice.setRSSI(rssi);

            mBTDevicesHashMap.put(address, btleDevice);
            mBTDevicesArrayList.add(btleDevice);
            Log.i(TAG, "BLE device found with address: " + btleDevice.getAddress());
        }
        else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }

        mBluetoothLeDeviceListAdapter.notifyDataSetChanged();
    }

    public void log(String msg) {
        Log.d(TAG, msg);
    }

    public void logError(String msg) {
        log("Error: " + msg);
    }
}
