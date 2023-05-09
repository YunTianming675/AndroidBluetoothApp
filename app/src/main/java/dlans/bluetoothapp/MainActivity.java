package dlans.bluetoothapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import dlans.bluetoothapp.databinding.ActivityMainBinding;
import dlans.bluetoothapp.utils.LogUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean appOpenBT = false;
    private ActivityMainBinding mainBinding;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_device, R.id.nav_data).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mainBinding.navView, navController);

        actionBar = getSupportActionBar();
        actionBar.setTitle("生理参数监测");

        /* 获取蓝牙适配器 */
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "此设备上没有可用的蓝牙设备！", Toast.LENGTH_LONG).show();
        }
        /* 检查是否支持BLE */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持低功耗蓝牙", Toast.LENGTH_LONG).show();
        }
        /* 检查蓝牙是否打开，如果没有则申请打开 */
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            /* 注册一个Activity启动器 */
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                LogUtil.d(TAG, "On Activity: " + result);
                if (result != null) {
                    int resultCode = result.getResultCode();
                    if (resultCode != RESULT_OK) {
                        appOpenBT = false;
                        Toast.makeText(this, "此应用需要蓝牙功能", Toast.LENGTH_LONG).show();
                    } else {
                        appOpenBT = true;
                    }
                }
            });
            /* 声明Intent: 启动蓝牙 */
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            launcher.launch(intent);
        }

        /* 蓝牙相关权限申请，仅Android 6.0 以上版本需要，也仅有6.0以上版本具有这个接口 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            /* 如果没有权限就申请权限 */
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* 如果蓝牙是由应用开启的，则在应用退出后关闭蓝牙 */
        if (appOpenBT) {
            /* 获取蓝牙适配器 */
            BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            /* 关闭蓝牙 */
            bluetoothAdapter.disable();
        }
    }
}