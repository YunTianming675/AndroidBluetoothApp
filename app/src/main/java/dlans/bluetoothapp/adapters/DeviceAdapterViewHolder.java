package dlans.bluetoothapp.adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import dlans.bluetoothapp.R;
import dlans.bluetoothapp.utils.GlobalContext;
import dlans.bluetoothapp.utils.LogUtil;

public class DeviceAdapterViewHolder extends RecyclerView.ViewHolder{

    private static final String TAG = "DeviceAdapterViewHolder";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID READ_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket bluetoothSocket = null;
    private List<BluetoothGattService> serviceList = null;
    private ImageView deviceIcon;
    private TextView deviceName;
    private TextView deviceAddress;

    public static BluetoothGatt bluetoothGatt = null; // BLE GATT
    public static BluetoothGattService bluetoothGattService = null; // BLE读写服务
    public static BluetoothGattCharacteristic writeCharacteristic = null; // 写特征
    public static BluetoothGattCharacteristic readCharacteristic = null; // 读特征

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
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
                default:
                    break;
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                LogUtil.d(TAG, "Connect success");
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
            }
            else {
                LogUtil.w(TAG, "Data send failure, status = " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            LogUtil.d(TAG, "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            LogUtil.d(TAG, "Get data: " + Arrays.toString(data));
        }
    };

    public DeviceAdapterViewHolder(@NonNull View itemView) {
        super(itemView);
        deviceIcon = itemView.findViewById(R.id.device_icon);
        deviceName = itemView.findViewById(R.id.device_name);
        deviceAddress = itemView.findViewById(R.id.device_address);

        /* 为RecycleView中的item设置监听 */
        itemView.setOnClickListener(view -> {
            int position = getAdapterPosition();
            LogUtil.d(TAG, "position = " + position);
            onItemClick(position);
        });
    }

    private void setService(BluetoothGatt gatt) {
        if (gatt == null) {
            LogUtil.e(TAG, "setService() -> gatt == null");
            return;
        }
        serviceList = gatt.getServices();
        if (serviceList.isEmpty()) {
            LogUtil.w(TAG, "Can't get any service");
            gatt.disconnect();
            return;
        }
        for (BluetoothGattService service : serviceList) {
            // 获取服务UUID
            String serviceUUID = service.getUuid().toString();
            LogUtil.d(TAG, "service UUID = " + serviceUUID);
            // 获取服务包含的所有特征
            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristicList) {
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
        byte[] data = "Android: OK".getBytes();
        writeCharacteristic.setValue(data);
        gatt.writeCharacteristic(writeCharacteristic);
        // 检查特征是否支持监听
        int properties = readCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            LogUtil.d(TAG, "read support notify");
            // 读特征支持监听，则设置监听
            gatt.setCharacteristicNotification(readCharacteristic, true);
            List<BluetoothGattDescriptor> descriptors = writeCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : descriptors) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
        else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            LogUtil.d(TAG, "read support indicate");
        }
        else {
            LogUtil.d(TAG, "read not support notify or indicate");
        }
    }

    private void onItemClick(int position) {
        BluetoothDevice bluetoothDevice = DeviceAdapter.getDeviceAdapter().getDeviceList().get(position);

        LogUtil.d(TAG, "device name = " + bluetoothDevice.getName());
        LogUtil.d(TAG, "device address = " + bluetoothDevice.getAddress());

        /* 启动蓝牙连接线程，不同类型的蓝牙连接方式不同 */
        if (bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            bluetoothGatt = bluetoothDevice.connectGatt(GlobalContext.getContext(), false, gattCallback);
        }
        else {
            try {
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                new Thread(() -> {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    try {
                        LogUtil.d(TAG, "try connect");
                        bluetoothSocket.connect();
                        LogUtil.d(TAG, "try connect finish");
                    } catch (IOException e) {
                        LogUtil.e(TAG, "Could not connect");
                        try {
                            LogUtil.d(TAG, "try to close");
                            bluetoothSocket.close();
                        } catch (IOException ioException) {
                            LogUtil.e(TAG, "Could not close");
                        }
                    }
                    LogUtil.d(TAG, "connect status: " + bluetoothSocket.isConnected());
                }).start();
            } catch (IOException e) {
                LogUtil.e(TAG, "Can't create insecure RF comm socket");
            }
            if (bluetoothSocket.isConnected()) {
                if (bluetoothDevice.getName() != null) {
                    Toast.makeText(GlobalContext.getContext(), "connect to " + bluetoothDevice.getName(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GlobalContext.getContext(), "Connect to " + bluetoothDevice.getAddress(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public ImageView getDeviceIcon() {
        return deviceIcon;
    }

    public TextView getDeviceName() {
        return deviceName;
    }

    public TextView getDeviceAddress() {
        return deviceAddress;
    }
}
