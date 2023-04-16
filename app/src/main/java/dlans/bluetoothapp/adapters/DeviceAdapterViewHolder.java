package dlans.bluetoothapp.adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.UUID;

import dlans.bluetoothapp.R;
import dlans.bluetoothapp.utils.BTManager;
import dlans.bluetoothapp.utils.GlobalContext;
import dlans.bluetoothapp.utils.LogUtil;

public class DeviceAdapterViewHolder extends RecyclerView.ViewHolder{

    private static final String TAG = "DeviceAdapterViewHolder";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket = null;
    private ImageView deviceIcon;
    private TextView deviceName;
    private TextView deviceAddress;

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

    private void onItemClick(int position) {
        BluetoothDevice bluetoothDevice = DeviceAdapter.getDeviceAdapter().getDeviceList().get(position);

        LogUtil.d(TAG, "device name = " + bluetoothDevice.getName());
        LogUtil.d(TAG, "device address = " + bluetoothDevice.getAddress());

        /* 启动蓝牙连接线程，不同类型的蓝牙连接方式不同 */
        if (bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            BTManager.getInstance().connect(GlobalContext.getContext(), bluetoothDevice);
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
