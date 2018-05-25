package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util;

import java.util.UUID;

public class Constants {

    public final static String TAG = "ClientActivity";

    public static String SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    public static String CHARACTERISTIC_ECHO_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING);

    public static String CHARACTERISTIC_TIME_STRING = "7D2EDEAD-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID CHARACTERISTIC_TIME_UUID = UUID.fromString(CHARACTERISTIC_TIME_STRING);
    public static String CLIENT_CONFIGURATION_DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb";
    public static UUID CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CONFIGURATION_DESCRIPTOR_STRING);

    public static final String CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "2902";

    public static final long SCAN_PERIOD = 5000;

    public static final String DEVICE_NAME_EXTRA = "DEVICE_NAME_EXTRA";
    public static final String DEVICE_ADDRESS_EXTRA = "DEVICE_ADDRESS_EXTRA";

    public final static String NEWLINE = System.getProperty("line.separator");

}
