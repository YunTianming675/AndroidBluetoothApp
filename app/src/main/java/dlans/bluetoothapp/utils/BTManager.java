package dlans.bluetoothapp.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Arrays;
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
    private Handler readHandler = null;
    private Integer readHandlerWhat = null;

    private static boolean writeAvailable = false;
    private static boolean readNotifyAvailable = false;
    private static boolean readIndicateAvailable = false;

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
            setService(gatt);
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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(TAG, "Data send success");
                writeAvailable = true;
            } else {
                LogUtil.w(TAG, "Data send failure, status = " + status);
                writeAvailable = false;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            LogUtil.d(TAG, "onCharacteristicChanged");
            // TODO change read method, getValue() was deprecated in API level 33
            byte[] data = characteristic.getValue();
            if (data.length == 0) {
                LogUtil.w(TAG, "No data");
            } else if (data.length <= 10) {
                LogUtil.d(TAG, "Get data: " + Arrays.toString(data));
            } else {
                LogUtil.d(TAG, "Get data, length = " + data.length);
            }
            if ((readHandler != null) && (readHandlerWhat != null)) {
                Message message = Message.obtain();
                message.what = readHandlerWhat;
                message.obj = data;
                readHandler.sendMessage(message);
            }
        }
    };

    private BTManager() {}

    private void testWriteAvailable() {
        byte[] data = "Android: OK".getBytes();
        writeCharacteristic.setValue(data);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    private void testReadNotifyAndIndicate() {
        int properties = readCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            LogUtil.d(TAG, "Read characteristic support notify");
            readNotifyAvailable = true;
            // 读特征支持监听，则设置监听
            bluetoothGatt.setCharacteristicNotification(readCharacteristic, true);
            // 设置监听后需要再次写描述符以使设置生效
            List<BluetoothGattDescriptor> descriptors = writeCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : descriptors) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            LogUtil.d(TAG, "Read characteristic support indicate");
            readIndicateAvailable = true;
        } else {
            LogUtil.d(TAG, "Read characteristic not support notify or indicate");
            readNotifyAvailable = false;
            readIndicateAvailable = false;
        }
    }

    private void setService(BluetoothGatt gatt) {
        if (gatt == null) {
            LogUtil.e(TAG, "gatt is null");
            return;
        }
        serviceList = gatt.getServices();
        if (serviceList.isEmpty()) {
            LogUtil.e(TAG, "Can't get any service");
            return;
        }
        for (BluetoothGattService service : serviceList) {
            // 获取服务UUID
            String serviceUUID = service.getUuid().toString();
            LogUtil.d(TAG, "service UUID = " + serviceUUID);
            // 获取服务包含的所有特征
            List<BluetoothGattCharacteristic> chList = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : chList) {
                // 获取特征的UUID
                String charUUID = characteristic.getUuid().toString();
                LogUtil.d(TAG, "characteristic UUID = " + charUUID);
            }
            LogUtil.d(TAG, "---------------------------------------------------");
        }
        // 先尝试通过预设的UUID进行通信
        bluetoothGatt = gatt;
        bluetoothGattService = gatt.getService(SERVICE_UUID);
        writeCharacteristic = bluetoothGattService.getCharacteristic(WRITE_UUID);
        readCharacteristic = bluetoothGattService.getCharacteristic(READ_UUID);
        // 测试写特征是否可用
        testWriteAvailable();
        // 检查读特征是否支持监听
        testReadNotifyAndIndicate();
    }

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

    public boolean connect(Context context, BluetoothDevice device) {
        if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            device.connectGatt(context, false, btManager.bluetoothGattCallback);
        }
        return true;
    }

    public void setReadHandler(Handler readHandler, Integer what) {
        if (readHandler == null) {
            LogUtil.e(TAG, "readHandler is null");
        }
        if (what == null) {
            LogUtil.e(TAG, "what is null");
        }
        this.readHandler = readHandler;
        this.readHandlerWhat = what;
    }

    public void removeReadHandler() {
        this.readHandler = null;
        this.readHandlerWhat = null;
    }

    public boolean isWriteAvailable() {
        return writeAvailable;
    }

    public boolean isReadNotifyAvailable() {
        return readNotifyAvailable;
    }

    public boolean isReadIndicateAvailable() {
        return readIndicateAvailable;
    }
}
