package dlans.bluetoothapp.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dlans.bluetoothapp.R;
import dlans.bluetoothapp.utils.LogUtil;

public class DeviceAdapterViewHolder extends RecyclerView.ViewHolder{

    private static final String TAG = "DeviceAdapterViewHolder";
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
