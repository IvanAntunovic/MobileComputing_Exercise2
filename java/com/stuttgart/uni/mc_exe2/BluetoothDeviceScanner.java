package com.stuttgart.uni.mc_exe2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.activities.ClientActivity;

import java.util.Map;

import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.Constants;


public class BluetoothDeviceScanner {

    private static final String TAG = "ClientActivity";

    private boolean mScanning;
    private Handler mHandler;
    private Handler mLogHandler;
    private Map<String, BluetoothLeDevice> mScanResults;

    private ClientActivity mClientActivity;

    private BluetoothGatt mGatt;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback mScanCallback;
    //####################################################################
    // If you want to scan for only specific types of peripherals,
    // you can instead call startLeScan(UUID[], BluetoothAdapter.LeScanCallback),
    // providing an array of UUID objects that specify the GATT services your app supports.

    public BluetoothDeviceScanner(ClientActivity clientActivity, BluetoothAdapter bluetoothAdapter) {

        mScanning = false;
        mBluetoothAdapter = bluetoothAdapter;
        mClientActivity = clientActivity;

    }

    public boolean isScanning() {
        return mScanning;
    }

    public void start(){

        this.scanLeDevice(true);
    }

    public void stop() {

        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() ) {

            this.scanLeDevice(false);

        }

        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        mLeScanCallback = null;
        mScanning = false;
        mHandler = null;

    }

    private void scanLeDevice(final boolean enable) {

        if (mHandler == null) {

            mHandler = new Handler();

        }

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(
                    this::stop, // Callback method that will be called after SCAN_PERIOD expires
                    Constants.SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    mClientActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mClientActivity.addDevice(device, rssi);
                            mClientActivity.updateScanButton(R.string.ble_device_scan_start);
                        }
                    });
                }
            };


    public void log(String msg) {
        Log.d(TAG, msg);
    }

    public void logError(String msg) {
        log("Error: " + msg);
    }

    // ###########################################################################################

    private void requestLocationPermission() {
        mClientActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ClientActivity.REQUEST_FINE_LOCATION);
        log("Requested user enable Location. Try starting the scan again.");
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mClientActivity.startActivityForResult(enableBtIntent, ClientActivity.REQUEST_ENABLE_BT);
        log("Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return mClientActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasPermissions() {

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;

        } /*else if (!hasLocationPermissions()) {

            requestLocationPermission();
            return false;
        }*/
        return true;
    }

}
