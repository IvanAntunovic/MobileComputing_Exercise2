package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util;

import java.util.HashMap;
import java.util.UUID;

public class SampleGattAttributes {

    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String BODY_SENSOR_LOCATION = "00002A38-0000-1000-8000-00805F9B34FB";
    public static String HEART_RATE_CONTROL_POINT = "00002A39-0000-1000-8000-00805F9B34FB";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String TEMPERATURE_MEASURE = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String HUMIDITY_MEASURE = "00002a6f-0000-1000-8000-00805f9b34fb";
    public static String INTENSITY_MEASURE = "10000001-0000-0000-fdfd-fdfdfdfdfdfd";

    public static String HUMIDITY_SENSOR_CONFI_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static UUID WEATHER_STATION_SERVICE_UUID = UUID.fromString("00000002-0000-0000-fdfd-fdfdfdfdfdfd");
    public static UUID FAN_CONTROL_SERVICE_UUID = UUID.fromString("00000001-0000-0000-fdfd-fdfdfdfdfdfd");

    public static UUID TEMPERATURE_MEASUREMENT_UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
    public static UUID HUMIDITY_MEASUREMENT_UUID = UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb");
    public static String SPEED_FAN = "1000-0001-0000-0000-fdfd-fdfd-fdfd-fdfd";

    public static String TEMPERATURE_MEASUREMENT = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String HUMIDITY_MEASUREMENT = "00002a6f-0000-1000-8000-00805f9b34fb";

    /* ################################################################################## */

    public static final UUID HEARTH_RATE_SERVICE = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB");

    static {
        // Sample Services.
        attributes.put("00000002-0000-0000-fdfd-fdfdfdfdfdfd", "Weather Service");
        attributes.put("00000001-0000-0000-fdfd-fdfdfdfdfdfd", "Fan Control Service");

        // Sample Characteristics.
        attributes.put(TEMPERATURE_MEASUREMENT, "Temperature Measurement");
        attributes.put(HUMIDITY_MEASUREMENT, "Humidity Measurement");
        attributes.put(SPEED_FAN, "Intensity of Speed");

        /* ######################################################################################## */

        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic attribute");
        attributes.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate alert");
        attributes.put("00001803-0000-1000-8000-00805f9b34fb", "Link loss");
        attributes.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");
        attributes.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");


        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002A06-0000-1000-8000-00805f9b34fb", "Alert level");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put(TEMPERATURE_MEASURE, "Temperature Measurement");
        attributes.put(HUMIDITY_MEASURE, "Humidity Measurement");
        attributes.put(INTENSITY_MEASURE, "Intensity Measurement");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
