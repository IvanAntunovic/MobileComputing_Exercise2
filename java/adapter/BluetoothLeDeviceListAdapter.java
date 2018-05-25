package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.stuttgart.uni.mc_exe2.BluetoothLeDevice;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.Util.Constants;
import com.stuttgart.uni.mc_exe2.R;
import com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.activities.ConnectDeviceActivity;

import java.util.ArrayList;

public class BluetoothLeDeviceListAdapter extends ArrayAdapter<BluetoothLeDevice> {

    private Context mContext;
    private int mResource;

    public BluetoothLeDeviceListAdapter(@NonNull Context context, int resource, ArrayList<BluetoothLeDevice> objects) {
        super(context, resource, objects);

        mContext  = context;
        mResource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        BluetoothLeDevice bluetoothDevice = super.getItem(position);

        String deviceAddress = null;
        String deviceName    = null;
        StringBuilder   deviceRssi = new StringBuilder("RSSI: ");

        if (bluetoothDevice != null) {
            deviceAddress = bluetoothDevice.getAddress();
            deviceName    = bluetoothDevice.getName();
            deviceRssi.append(bluetoothDevice.getRSSI());

            if (deviceName == null) {
                deviceName = "N/A";
            }
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView deviceAddressTextView = (TextView) convertView.findViewById(R.id.deviceAddressTextView);
        TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
        TextView deviceRssiTextView = (TextView) convertView.findViewById(R.id.rssiTextView);
        Button   deviceConnectButton = (Button) convertView.findViewById(R.id.connect_device_button);

        final String selectedDeviceAddress = deviceAddress;
        final String selectedDeviceName = deviceName;
        deviceConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent selectedDeviceIntent = new Intent(mContext, ConnectDeviceActivity.class);
                selectedDeviceIntent.putExtra( Constants.DEVICE_ADDRESS_EXTRA, selectedDeviceAddress );
                selectedDeviceIntent.putExtra( Constants.DEVICE_NAME_EXTRA, selectedDeviceName );
                mContext.startActivity(selectedDeviceIntent);

            }
        });

        deviceAddressTextView.setText(deviceAddress);
        deviceNameTextView.setText(deviceName);
        deviceRssiTextView.setText(deviceRssi.toString());

        return convertView;

    }
}
