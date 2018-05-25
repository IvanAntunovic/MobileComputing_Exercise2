package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public class WeatherStationUtils {

    private static final String TAG = Constants.TAG;
    private static StringBuilder stringBuilder = new StringBuilder();

    public static float parseTemperatureValue(BluetoothGattCharacteristic characteristic) {

        byte[] sensorValue = characteristic.getValue();
        byte flag = sensorValue[0];
        String unit = "\u00b0";
        String mString;

        if (flag%2 == 0) {
            // unit = "\u2103"; // celsius
            unit = unit + "C";
        } else {
            // unit = "\u2109"; // fahrenheit;
            unit = unit + "F";
        }

        Float value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
        Log.i(TAG, "Temperature is " + value.toString() + unit);

        return value;

    }

    public static String parseHumidityValue(BluetoothGattCharacteristic characteristic) {

        Integer value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

        Log.i(TAG, "Humidity is " + value.toString() + "%");

        return value.toString();

    }

    public static void readWeatherData(BluetoothGatt gatt, BluetoothGattCharacteristic temperatureCharacteristic){

        gatt.readCharacteristic(temperatureCharacteristic);

    }

}
