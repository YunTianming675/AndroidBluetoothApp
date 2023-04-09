package dlans.bluetoothapp.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dlans.bluetoothapp.R;
import dlans.bluetoothapp.utils.LogUtil;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapterViewHolder>{

    private static final String TAG = "DeviceAdapter";
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private final static DeviceAdapter deviceAdapter = new DeviceAdapter();
    private int positionStart = 0;

    private DeviceAdapter() {}

    @NonNull
    @Override
    public DeviceAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /* 设置RecycleView中item的布局 */
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapterViewHolder holder, int position) {
        /* 获取item并设置内容 */
        BluetoothDevice bluetoothDevice = deviceList.get(position);
        if (bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            holder.getDeviceIcon().setImageResource(R.drawable.bluetooth_le);
        }
        else {
            holder.getDeviceIcon().setImageResource(R.drawable.bluetooth);
        }
        holder.getDeviceName().setText(bluetoothDevice.getName());
        holder.getDeviceAddress().setText(bluetoothDevice.getAddress());
    }

    @Override
    public int getItemCount() {
        if (deviceList == null) {
            return 0;
        } else {
            return deviceList.size();
        }
    }

    public static DeviceAdapter getDeviceAdapter() {
        return deviceAdapter;
    }

    public void setDeviceList(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public List<BluetoothDevice> getDeviceList() {
        return deviceList;
    }

    public void addDevice(BluetoothDevice device) {
        LogUtil.d(TAG, "add device");
        LogUtil.d(TAG, "name = " + device.getName() + ", address = " + device.getAddress());
        if (deviceList.contains(device)) {
            return;
        }
        deviceList.add(device);
        notifyItemRangeChanged(positionStart, deviceList.size());
        positionStart = deviceList.size();
    }
}
