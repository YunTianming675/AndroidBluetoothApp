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
        byte[] b = Arrays.copyOfRange(data, 0, 3);
        byte[] b1 = Arrays.copyOfRange(data, 0, 5);
        byte[] b2 = {'h', 'r', ':'};
        byte[] b3 = {'s', 'p', 'o', '2', ':'};
        try {
            if (Arrays.equals(b, b2)) {
                byte[] b4 = Arrays.copyOfRange(data,3, data.length);
                hrData.setText(new String(b4));
            } else if (Arrays.equals(b1, b3)) {
                byte[] b4 = Arrays.copyOfRange(data, 5, data.length);
                spo2Data.setText(new String(b4));
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

        hrData.setText("---");
        spo2Data.setText("---");

        BTManager.getInstance().setReadHandler(handler, GET_DATA);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hrData.setText("---");
        spo2Data.setText("---");
        binding = null;
    }
}
