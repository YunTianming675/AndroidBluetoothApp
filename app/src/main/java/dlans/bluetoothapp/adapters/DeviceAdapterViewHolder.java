package dlans.bluetoothapp.adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private static final int CONNECT_SUCCESS = 0;
    private static final int CONNECT_FAILURE = 1;
    private BluetoothSocket bluetoothSocket = null;
    private ImageView deviceIcon;
    private TextView deviceName;
    private TextView deviceAddress;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    String name = (String) msg.obj;
                    Toast.makeText(GlobalContext.getContext(), "连接至：" + name, Toast.LENGTH_LONG).show();
                    break;
                case CONNECT_FAILURE:
                    Toast.makeText(GlobalContext.getContext(), "连接失败！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    public DeviceAdapterViewHolder(@NonNull View itemView) {
        super(itemView);
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
                    Message message = Message.obtain();
                    message.what = CONNECT_SUCCESS;
                    if (bluetoothDevice.getName() == null) {
                        message.obj = bluetoothDevice.getAddress();
                    } else {
                        message.obj = bluetoothDevice.getName();
                    }
                    handler.sendMessage(message);
                }).start();
            } catch (IOException e) {
                LogUtil.e(TAG, "Can't create insecure RF comm socket");
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
