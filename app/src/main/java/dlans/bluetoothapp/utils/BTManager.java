package dlans.bluetoothapp.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;

import java.util.List;
import java.util.UUID;

public class BTManager {

    private static final String TAG = "BTManager";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID READ_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket bluetoothSocket = null;
    private List<BluetoothGattService> serviceList = null;
    private BluetoothGatt bluetoothGatt = null; // BLE Gatt
    private BluetoothGattService bluetoothGattService = null; // BLE读写服务
    private BluetoothGattCharacteristic writeCharacteristic = null; // 写特征
    private BluetoothGattCharacteristic readCharacteristic = null; // 读特征
    private static volatile BTManager btManager = new BTManager();

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            LogUtil.d(TAG, "status = " + status);
            LogUtil.d(TAG, "newStatus = " + newState);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    LogUtil.d(TAG, "GATT_SUCCESS");
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    LogUtil.d(TAG, "GATT_FAILURE");
                    break;
                default:
                    break;
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                LogUtil.d(TAG, "STATE_CONNECTED");
                // 连接成功后开始发现服务
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.d(TAG, "onServicesDiscovered");
            // TODO setService
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtil.d(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            LogUtil.d(TAG, "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private BTManager() {}

    public static BTManager getInstance() {
        if (btManager == null) {
            synchronized (BTManager.class) {
                if (btManager == null) {
                    btManager = new BTManager();
                }
            }
        }
        return btManager;
    }
}
