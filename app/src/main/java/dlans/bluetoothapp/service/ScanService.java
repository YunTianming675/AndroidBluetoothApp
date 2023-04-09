package dlans.bluetoothapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import dlans.bluetoothapp.R;
import dlans.bluetoothapp.adapters.DeviceAdapter;
import dlans.bluetoothapp.utils.LogUtil;

public class ScanService extends Service {

    private static final String TAG = "ScanService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ScanService";
    private boolean isScanning = false;
    private BluetoothLeScanner scanner;
    private final DeviceAdapter deviceAdapter = DeviceAdapter.getDeviceAdapter();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            LogUtil.d(TAG, "Thread: " + Thread.currentThread().getName() + "id = " + Thread.currentThread().getId());
            BluetoothDevice bluetoothDevice = result.getDevice();
            deviceAdapter.addDevice(bluetoothDevice);
            String name = bluetoothDevice.getName();
            String address = bluetoothDevice.getAddress();
            int rssi = result.getRssi();
            if (name != null) {
                LogUtil.d(TAG, "Found device ==> name: " + name + ", address: " + address + ", rssi: " + rssi);
            }
            else {
                LogUtil.d(TAG, "Found device ==> name: null" + ", address: " + address + ", rssi: " + rssi);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.e(TAG, "Scan Error, Error Code: " + errorCode);
        }
    };

    // 使用定时器来控制BLE的扫描时间
    private final CountDownTimer timer = new CountDownTimer(9000, 3000) {
        @Override
        public void onTick(long l) {
            LogUtil.d(TAG, "onTick");
            if (!isScanning) {
                isScanning = true;
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                scanner.startScan(scanCallback);
            }
        }

        @Override
        public void onFinish() {
            scanner.stopScan(scanCallback);
            stopSelf();
        }
    };

    public ScanService() {
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Scan Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BLE Scan Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.drawable.bluetooth_le)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android O以上版本需要在服务启动后的5s内设为前台服务，否则将被系统终止
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");
        timer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}