package dlans.bluetoothapp.ui.data;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;

import dlans.bluetoothapp.databinding.FragmentDataBinding;
import dlans.bluetoothapp.utils.BTManager;
import dlans.bluetoothapp.utils.LogUtil;

public class DataFragment extends Fragment {

    private static final String TAG = "DataFragment";
    private DataViewModel dataViewModel;
    private FragmentDataBinding binding;
    private LineChartManager lineChartManager = null;
    private TextView hrData = null;
    private TextView spo2Data = null;
    private TextView tempData = null;

    public static final int GET_DATA = 1;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_DATA:
                    LogUtil.d(TAG, "Get data");
                    onGetData(msg);
                    break;
                default:
                    break;
            }
        }
    };

    private void onGetData(@NonNull Message msg) {
        byte[] data = (byte[]) msg.obj;
        byte[] b = Arrays.copyOfRange(data, 0, 6);
        byte[] b2 = {11, 12, 13, 11, 12, 13};
        byte[] b3 = {21, 22, 23, 21, 22, 23};
        try {
            if (data.length < 10) {
                if (Arrays.equals(b, b2)) {
                    hrData.setText(String.valueOf(data[data.length-1]));
                } else if (Arrays.equals(b, b3)) {
                    spo2Data.setText(String.valueOf(data[data.length-1]));
                }
            } else {
                lineChartManager.addData(data);
            }
        }
        catch (Resources.NotFoundException e) {
            LogUtil.e(TAG, "onGetData ==> Resources.NotFoundException");
            binding.hrData.setText("90");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lineChartManager = new LineChartManager(binding.lineChart);
        hrData = binding.hrData;
        spo2Data = binding.spo2Data;
        tempData = binding.tempData;

        hrData.setText("---");
        spo2Data.setText("---");
        tempData.setText("---");

        BTManager.getInstance().setReadHandler(handler, GET_DATA);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hrData.setText("---");
        spo2Data.setText("---");
        tempData.setText("---");
        binding = null;
    }
}
