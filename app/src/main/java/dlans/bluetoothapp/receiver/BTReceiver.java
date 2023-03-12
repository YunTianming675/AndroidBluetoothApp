package dlans.bluetoothapp.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dlans.bluetoothapp.adapters.DeviceAdapter;
import dlans.bluetoothapp.utils.LogUtil;

public class BTReceiver extends BroadcastReceiver {

    private static final String TAG = "BTReceiver ==>";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "--------------------onReceive--------------------");
        String action = intent.getAction();
        LogUtil.d(TAG, "action = " + action);
        DeviceAdapter deviceAdapter = DeviceAdapter.getDeviceAdapter();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            LogUtil.d(TAG, "name = " + device.getName() + ", address = " + device.getAddress());
        }
        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                LogUtil.d(TAG, "start found");
                break;
            case BluetoothDevice.ACTION_FOUND:
                deviceAdapter.addDevice(device);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                LogUtil.d(TAG, "EXTRA_RSSI = " + rssi);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                LogUtil.d(TAG, "finish found");
            default:
                break;
        }
    }
}
