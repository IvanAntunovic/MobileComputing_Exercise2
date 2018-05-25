package com.stuttgart.uni.mc_exe2;

import android.bluetooth.BluetoothDevice;

public class BluetoothLeDevice {

    private BluetoothDevice bluetoothDevice;
    private String rssi;


    public BluetoothLeDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public void setRSSI(int rssi) {
        this.rssi = Integer.toString(rssi);
    }

    public String getRSSI() {
        return rssi;
    }
}
