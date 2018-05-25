package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

public class WeatherStation {

    private static final String TAG = Constants.TAG;
    private static BluetoothGattService mWeatherStationService;
    private static BluetoothGattCharacteristic mTemperatureCharacteristic, mHumidityCharacteristic;

    private TextView mHumidityTextView;
    private TextView mTemperatureTextView;

    public WeatherStation(BluetoothGatt bleGatt) {

        if (bleGatt == null) {
            return;
        }

        mWeatherStationService = bleGatt.getService(SampleGattAttributes.WEATHER_STATION_SERVICE_UUID);
        mTemperatureCharacteristic = mWeatherStationService.getCharacteristic(SampleGattAttributes.TEMPERATURE_MEASUREMENT_UUID);
        mHumidityCharacteristic = mWeatherStationService.getCharacteristic(SampleGattAttributes.HUMIDITY_MEASUREMENT_UUID);

    }

    public boolean enableNotifications(BluetoothGatt gatt) {

        if (gatt == null) {
            Log.i(TAG, "Notification enable failed");
            return false;
        }

        Log.i(TAG, "Try to enable notification for humidity and temperature sensor");

        BluetoothGattService weatherStationService = gatt.getService(SampleGattAttributes.WEATHER_STATION_SERVICE_UUID);

        BluetoothGattCharacteristic temperatureCharacteristic =
                weatherStationService.getCharacteristic(SampleGattAttributes.TEMPERATURE_MEASUREMENT_UUID);
        BluetoothGattCharacteristic humidityCharactesrics =
                weatherStationService.getCharacteristic(SampleGattAttributes.HUMIDITY_MEASUREMENT_UUID);

        gatt.setCharacteristicNotification(temperatureCharacteristic , true);
        gatt.setCharacteristicNotification(humidityCharactesrics, true);

        BluetoothGattDescriptor temperatureDescriptor = temperatureCharacteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        temperatureDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(temperatureDescriptor);

        BluetoothGattDescriptor humidityDescriptor = temperatureCharacteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        humidityDescriptor .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(humidityDescriptor );

        return true;
    }

    public void updateHumidityValue(BluetoothGattCharacteristic characteristic) {

        if (characteristic == null) return;

        mHumidityTextView.setText( String.format("%.0f%%", WeatherStationUtils.parseHumidityValue(characteristic) ));

    }

    public void updateTemperatureValue(BluetoothGattCharacteristic characteristic) {

        if (characteristic == null) return;

        mTemperatureTextView.setText( String.format("%.1f\u00B0C", WeatherStationUtils.parseTemperatureValue(characteristic) ));

    }

}
