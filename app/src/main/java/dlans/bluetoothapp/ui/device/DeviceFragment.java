package dlans.bluetoothapp.ui.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dlans.bluetoothapp.adapters.DeviceAdapter;
import dlans.bluetoothapp.databinding.FragmentDeviceBinding;
import dlans.bluetoothapp.receiver.BTReceiver;
import dlans.bluetoothapp.utils.GlobalContext;
import dlans.bluetoothapp.utils.LogUtil;

public class DeviceFragment extends Fragment {

    private static final String TAG = "DeviceFragment";
    private DeviceViewModel deviceViewModel;
    private FragmentDeviceBinding binding;
    private ImageButton scanButton;
    private RecyclerView deviceList;
    private BTReceiver btReceiver;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        deviceList = binding.deviceList;
        /* 设置RecyclerView的布局管理器 */
        deviceList.setLayoutManager(new LinearLayoutManager(GlobalContext.getContext()));
        /* 获取蓝牙适配器 */
        BluetoothManager manager = (BluetoothManager)GlobalContext.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = manager.getAdapter();
        /* 获取已配对设备 */
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                LogUtil.d(TAG, "Device: " + device.getName() + " Address: " + device.getAddress());
                try {
                    bluetoothDevices.add(device);
                }
                catch (NullPointerException exception) {
                    LogUtil.e(TAG, "NullPointerException");
                }
            }
        } else {
            LogUtil.d(TAG, "No devices");
        }
        DeviceAdapter deviceAdapter = DeviceAdapter.getDeviceAdapter();
        deviceAdapter.setDeviceList(bluetoothDevices);
        deviceList.setAdapter(deviceAdapter);

        /* 注册广播接收蓝牙发现的结果 */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btReceiver = new BTReceiver();
        GlobalContext.getContext().registerReceiver(btReceiver, intentFilter);

        /* 从binding获取ImageButton对象并设置监听 */
        scanButton = binding.scanButton;
        scanButton.setOnClickListener(view -> {
            Toast.makeText(GlobalContext.getContext(), "start scan, please wait", Toast.LENGTH_LONG).show();
            final RotateAnimation animation = new RotateAnimation(0.0f, 180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(500);
            scanButton.startAnimation(animation);
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                LogUtil.d(TAG, "is discovering");
            }
            else {
                bluetoothAdapter.startDiscovery();
                LogUtil.d(TAG, "process start");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        GlobalContext.getContext().unregisterReceiver(btReceiver);
    }
}
