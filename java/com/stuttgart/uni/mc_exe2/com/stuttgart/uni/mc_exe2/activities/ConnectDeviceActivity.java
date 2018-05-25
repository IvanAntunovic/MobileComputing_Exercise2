package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.activities;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stuttgart.uni.mc_exe2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.BluetoothUtils;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.Constants;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.SampleGattAttributes;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.StringUtils;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.Utils;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.WeatherStation;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.WeatherStationUtils;

public class ConnectDeviceActivity extends AppCompatActivity {

    private final static String TAG = Constants.TAG;

    private Button mSendMessageButton;
    private Button mDisconnectButton;
    private Button mNotificationsButton;

    private static BluetoothGattCharacteristic mHearthRateCharacteristic;

    private TextView mConnectedDeviceTextView;
    private static TextView mServiceConnectedTextView;
    private static TextView mGattConnectedTextView;
    private TextView mServicesTextView;
    private static EditText mMessageEditText;

    private static TextView mHeartRateTextView;

    private String mBluetoothDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private static WeatherStation mWeatherStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        this.initActivity();
        this.initialize();

        Intent clientIntent = getIntent();
        String deviceName = clientIntent.getStringExtra(Constants.DEVICE_NAME_EXTRA);
        String deviceAddress = clientIntent.getStringExtra(Constants.DEVICE_ADDRESS_EXTRA);
        StringBuilder deviceInfo = new StringBuilder();
        deviceInfo.append("Device Info:").append("\nName: ").append(deviceName).append("\nAddress:").append(deviceAddress);

        mBluetoothDeviceAddress = deviceAddress;

        mConnectedDeviceTextView.setText(deviceInfo.toString());

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Create an IntentFilter and register the receiver by calling registerReceiver(BroadcastReceiver, IntentFilter):
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        this.registerReceiver(mGattUpdateReceiver, intentFilter);

        // Bind to LocalService
        // Intent that explicitly names the service to bind.
        Intent bleServiceIntent = new Intent(this, BluetoothLeService.class);
        //if the system is in the process of bringing up a service that your client has permission to bind to
        boolean clientHasPermissionToBound = bindService(
                        bleServiceIntent,
                        mConnection,
                        Context.BIND_AUTO_CREATE); // It should usually be BIND_AUTO_CREATE in order to create the service if it's not already alive

        if ( clientHasPermissionToBound ) {

            Log.i(TAG, "Client HAS permission to bind to service");

        } else {

            Log.i(TAG, "Client DOESN'T HAVE permission to bind to service");

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister receiver to stop receiving broadcasts,
        this.unregisterReceiver( mGattUpdateReceiver );
        unbindService(mConnection);
        mBound = false;
    }


    private void initActivity() {

        mMessageEditText = findViewById(R.id.message_edit_text);

        mNotificationsButton =  findViewById(R.id.notifications_button);

        mHeartRateTextView = findViewById(R.id.hearth_rate_text_view);

        mServiceConnectedTextView = findViewById(R.id.service_connected_text_view);
        mGattConnectedTextView = findViewById(R.id.gatt_connected_text_view);
        mConnectedDeviceTextView = findViewById(R.id.connected_device_info);
        mSendMessageButton =  findViewById(R.id.send_message_button);
        mDisconnectButton = findViewById(R.id.disconnect_button);

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.sendMessage();
            }
        });

        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //disconnectGattServer();
                Log.i(TAG, "Disconnect button pressed");

                mBluetoothLeService.disconnect();

            }
        });

        mNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Enable notifications

            }
        });

    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    public static class BluetoothLeService extends Service {

        private BluetoothManager mBluetoothManager;
        private BluetoothAdapter mBluetoothAdapter;
        private String mBluetoothDeviceAddress;
        private BluetoothGatt mBluetoothGatt;
        private int mConnectionState = STATE_DISCONNECTED;

        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTING = 1;
        private static final int STATE_CONNECTED = 2;

        public final static String ACTION_GATT_CONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
        public final static String ACTION_GATT_DISCONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
        public final static String ACTION_GATT_SERVICES_DISCOVERED =
                "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
        public final static String ACTION_DATA_AVAILABLE =
                "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
        public final static String EXTRA_DATA =
                "com.example.bluetooth.le.EXTRA_DATA";

        public final UUID UUID_HEART_RATE_MEASUREMENT =
                UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

        public BluetoothLeService() {
            super();
        }

        // Various callback methods defined by the BLE API.
        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    String intentAction;

                    if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {

                        Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange(): STATE_CONNECTED Connected to GATT server.");
                        Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange():  Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());
                        mGattConnectedTextView.setText(R.string.gatt_connected);

                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);

                    } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {

                        Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange(): STATE_DISCONNECTED Disconnected from GATT server.");
                        mGattConnectedTextView.setText(R.string.gatt_disconnected);

                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        broadcastUpdate(intentAction);

                    } else if (status != BluetoothGatt.GATT) {

                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        gatt.disconnect();
                        broadcastUpdate(intentAction);

                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "BluetoothGattCallback.onServicesDiscovered(): GATT_SUCCESS");

                        BluetoothGattService hearthRateService = null;
                        if (gatt != null && (hearthRateService = gatt.getService(SampleGattAttributes.HEARTH_RATE_SERVICE)) != null ) {
                            Log.i(TAG, "HEARTH_RATE_SERVICE discovered!");

                            // Enable notifications
                            BluetoothGattCharacteristic hearthRateCharacteristic =
                                    hearthRateService.getCharacteristic(UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT));

                            mBluetoothGatt.setCharacteristicNotification(hearthRateCharacteristic , true);

                            BluetoothGattDescriptor descriptor = hearthRateCharacteristic.getDescriptor(
                                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);

                            mWeatherStation = new WeatherStation(gatt);
                            mWeatherStation.enableNotifications(gatt);
                            //gatt.readCharacteristic(hearthRateCharacteristic);

                        }

                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                    } else {
                        Log.w(TAG, "BluetoothGattCallback.onServicesDiscovered(): received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        Log.i(TAG, "BluetoothGattCallback.onCharactertisticRead(): BluetoothGatt.GATT_SUCCESS and ACTION_DATA_AVAILABLE");

                        /*
                        if (characteristic.getUuid().equals( UUID.fromString( SampleGattAttributes.TEMPERATURE_MEASUREMENT) )) {

                            WeatherStationUtils.parseTemperatureValue(characteristic);
                            Log.i(TAG, "Try to read humidity sensor");
                            gatt.readCharacteristic(mHumidityCharacteristic);

                        }
                        else if (characteristic.getUuid().equals( UUID.fromString( SampleGattAttributes.HUMIDITY_MEASUREMENT ) ) ) {

                            WeatherStationUtils.parseHumidityValue(characteristic);

                            Log.i(TAG, "Try to enable notification for humidity sensor");

                            gatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor hDescriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.HUMIDITY_SENSOR_CONFI_UUID));
                            hDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(hDescriptor);

                        }
                        */
                        /*
                            ConnectDeviceActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTextView.setText(stringBuilder.toString());
                                }
                            });
                        */

                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                    }

                }

                // After notifications have been enabled, regular callbacks are issued for this method,
                // As all the notifications that we've enabled start brining data into us
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {

                    Log.w(TAG, "BluetoothGattCallback.onCharacteristicChanged(): ACTION_DATA_AVAILABLE");

                    /*
                     * After Notifications are enabled, all updates from the device on cahracteristic
                     * value changes will be posted here. Similar to read, we hand these up to the
                     * UI thread to update the display, because all the callbacks inside GATT callback object
                     * are not going to happen on the Main Thread, they are going to be called up into our
                     * application on background threads so if we want to update the display from this data
                     * we have to use handler or some other mechanism to synchronize the inofrmation background
                     * to the main thread so that you can do your updates
                     */
                    if ( characteristic.getUuid().equals(SampleGattAttributes.TEMPERATURE_MEASUREMENT_UUID) ) {
                        Log.i(TAG, "Try to read humidity sensor");

                        mWeatherServiceCharacteristicHandler.sendMessage(Message.obtain( null, TEMPERATURE_MESSAGE, characteristic ) );

                    }
                    else if ( characteristic.getUuid().equals(SampleGattAttributes.HUMIDITY_MEASUREMENT_UUID) ) {

                        mWeatherServiceCharacteristicHandler.sendMessage(Message.obtain( null, HUMIDITY_MESSAGE, characteristic ) );

                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                    }
                    else {

                        mWeatherServiceCharacteristicHandler.sendMessage(Message.obtain(null, HEARTH_RATE_MESSAGE, characteristic));
                    }
                }

                private void broadcastUpdate(final String action) {

                    final Intent intent = new Intent(action);
                    sendBroadcast(intent);

                }

                private void broadcastUpdate(final String action,
                                             final BluetoothGattCharacteristic characteristic) {
                    Log.i(TAG, "BluetoothGattCallback.broadcastUpdate() called.");

                    final Intent intent = new Intent(action);

                    // This is special handling for the Heart Rate Measurement profile. Data
                    // parsing is carried out as per profile specifications.
                    if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                        int flag = characteristic.getProperties();
                        int format = -1;
                        if ((flag & 0x01) != 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                            Log.d(TAG, "Heart rate format UINT16.");
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                            Log.d(TAG, "Heart rate format UINT8.");
                        }
                        final int heartRate = characteristic.getIntValue(format, 1);
                        Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                        intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
                    } else {
                        // For all other profiles, writes the data formatted in HEX.
                        final byte[] data = characteristic.getValue();
                        if (data != null && data.length > 0) {
                            final StringBuilder stringBuilder = new StringBuilder(data.length);
                            for(byte byteChar : data)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                                    stringBuilder.toString());
                        }
                    }
                    sendBroadcast(intent);
                }

            };

        private final IBinder mBinder = new LocalBinder();

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public boolean onUnbind(Intent intent) {
            // After using a given device, you should make sure that BluetoothGatt.close() is called
            // such that resources are cleaned up properly.  In this particular example, close() is
            // invoked when the UI is disconnected from the Service.
            //close();
            if (mBluetoothGatt == null) {
                return false;
            }

            mBluetoothGatt.close();
            mBluetoothGatt = null;

            return super.onUnbind(intent);
        }

        public void connect(final BluetoothDevice device) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            mConnectionState = STATE_CONNECTING;
        }

        /**
         * Connects to the GATT server hosted on the Bluetooth LE device.
         *
         * @param address The device address of the destination device.
         *
         * @return Return true if the connection is initiated successfully. The connection result
         *         is reported asynchronously through the
         *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
         *         callback.
         */
        public boolean connect(final String address) {
            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }

            // Previously connected device.  Try to reconnect.
            if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                    && mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            mBluetoothDeviceAddress = address;
            mConnectionState = STATE_CONNECTING;

            return true;
        }

        /**
         * Disconnects an existing connection or cancel a pending connection. The disconnection result
         * is reported asynchronously through the
         * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
         * callback.
         */
        public void disconnect() {

            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            Log.i(TAG, "Closing Gatt connection");

            //mConnected = false;

            if (mBluetoothGatt != null) {

                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();

            }

        }

        // Messaging
        private void sendMessage() {

            /*
            if ( !mConnected ) {
                return;
            } */

            if ( mConnectionState != STATE_CONNECTED ) {
                return;
            }

            BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mBluetoothGatt);
            if (characteristic == null) {
                Log.e(TAG, "Unable to find echo characteristic.");
                this.disconnect();
                return;
            }

            String message = mMessageEditText.getText().toString();
            Log.i(TAG, "Sending message: " + message);

            byte[] messageBytes = StringUtils.bytesFromString(message);
            if (messageBytes.length == 0) {
                Log.e(TAG, "Unable to convert message to bytes");
                return;
            }

            characteristic.setValue(messageBytes);
            boolean success = mBluetoothGatt.writeCharacteristic(characteristic);
            if (success) {

                Log.i(TAG, "Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes));

            } else {

                Log.e(TAG, "Failed to write data");

            }
        }


        /**
         * Initializes a reference to the local Bluetooth adapter.
         *
         * @return Return true if the initialization is successful.
         */
        public boolean initialize() {
            // For API level 18 and above, get a reference to BluetoothAdapter through
            // BluetoothManager.
            if (mBluetoothManager == null) {
                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (mBluetoothManager == null) {
                    Log.e(TAG, "Unable to initialize BluetoothManager.");
                    return false;
                }
            }

            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }

            return true;
        }

        /**
         * Class used for the client Binder.  Because we know this service always
         * runs in the same process as its clients, we don't need to deal with IPC.
         */
        public class LocalBinder extends Binder {

            BluetoothLeService  getService() {
                // Return this instance of LocalService so clients can call public methods
                return BluetoothLeService.this;
            }
        }


        /**
         * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
         * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
         * callback.
         *
         * @param characteristic The characteristic to read from.
         */
        public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "Service_BTLE_GATT.readCharacteristic(): ");
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {

                Log.w(TAG, "Service_BTLE_GATT.readCharacteristic(): BluetoothAdapter not initialized");
                return;

            }

            Log.d(TAG, "Service_BTLE_GATT.readCharacteristic(): " +
                    "\nCharacteristic UUID: " + characteristic.getUuid().toString() +
                    "\nData: + " + Utils.hexToString(characteristic.getValue()) );

            mBluetoothGatt.readCharacteristic(characteristic);
        }

        /**
         * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
         * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
         * callback.
         *
         * @param characteristic The characteristic to read from.
         */
        public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "Service_BTLE_GATT.writeCharacteristic(): " +
                    "\nCharacteristic UUID: " + characteristic.getUuid().toString() +
                    "\nData: + " + Utils.hexToString(characteristic.getValue()) );

            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            mBluetoothGatt.writeCharacteristic(characteristic);
        }

        /**
         * Enables or disables notification on a give characteristic.
         *
         * @param characteristic Characteristic to act on.
         * @param enabled If true, enable notification.  False otherwise.
         */
        public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

            Log.d(TAG, "Service_BTLE_GATT.setCharacteristicNotification(): " +
                    "\nCharacteristic UUID: " + characteristic.getUuid().toString() +
                    "\nData: + " + Utils.hexToString(characteristic.getValue()) );

            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            if (enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }

            mBluetoothGatt.writeDescriptor(descriptor);
        }

        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
         *
         * @return A {@code List} of supported services.
         */
        public List<BluetoothGattService> getSupportedGattServices() {

            if (mBluetoothGatt == null) {
                return null;
            }

            return mBluetoothGatt.getServices();
        }
    }


    private BluetoothLeService mBluetoothLeService;
    private boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        // Callback Called when the connection with the service is established
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "ServiceConnection.onServiceConnected(): Connection with the service is established");
            mServiceConnectedTextView.setText(R.string.service_connected);
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = binder.getService();
            mBound = true;

            Log.i(TAG, "ServiceConnection.onServiceConnected(): Connecting to the GATT device with address: " + mBluetoothDeviceAddress +  "...");

            mBluetoothLeService.initialize();
            mBluetoothLeService.connect(mBluetoothDeviceAddress);

        }

        // Callback Called when the connection with the service disconnects unexpectedly
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "ServiceConnection.onServiceDisconnected()");
            mServiceConnectedTextView.setText(R.string.service_disconnected);

            mBound = false;
            mBluetoothLeService.disconnect();
        }
    };

    private boolean mConnected = false;

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                Log.i(TAG, "BroadCastReceiver.onReceive(): ACTION_GATT_CONNECTED ");

                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                mConnected = false;

                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
                Log.i(TAG, "BroadCastReceiver.onReceive(): ACTION_GATT_DISCONNECTED ");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                Log.i(TAG, "BroadCastReceiver.onReceive(): ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the
                // user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                //updateServices();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.i(TAG, "BroadCastReceiver.onReceive(): ACTION_DATA_AVAILABLE ");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //updateCharacteristic();
            }
            Log.i(TAG, "BroadCastReceiver.onReceive(): No Service Action found: " + "Action is: " + action);
        }
    };


    /** Updating UI with Services and Characteristics
     * ##################################################################################################################################
     */
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            //currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            //currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                //currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                //currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                mServicesTextView.setText(uuid);
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static final int HUMIDITY_MESSAGE = 101;
    private static final int TEMPERATURE_MESSAGE = 102;
    private static final int HEARTH_RATE_MESSAGE = 201;

    private static Handler mWeatherServiceCharacteristicHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            //TODO: Handle different types of messages
            //TODO: handle cast exception

            BluetoothGattCharacteristic characteristic;

            switch (msg.what) {

                case TEMPERATURE_MESSAGE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining temperature value");
                        return;
                    }
                    mWeatherStation.updateTemperatureValue(characteristic);
                    break;

                case HUMIDITY_MESSAGE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                    mWeatherStation.updateHumidityValue(characteristic);
                    break;

                case HEARTH_RATE_MESSAGE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining heart rate value");
                        return;
                    }
                    // Update Heart Rate value
                    parseHearthRateData(characteristic);
                    break;

                default:
                    Log.w(TAG, "Error while handling message in mWeatherServiceCharacteristicHandler.handleMessage() method");

            }
        }
    };

    public static final UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public static void parseHearthRateData(BluetoothGattCharacteristic characteristic) {
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));

            mHeartRateTextView.setText(String.format("Received heart rate: %d", heartRate) );

        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
            }
        }
    }
}
