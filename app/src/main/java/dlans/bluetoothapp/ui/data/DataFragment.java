package dlans.bluetoothapp.ui.data;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dlans.bluetoothapp.databinding.FragmentDataBinding;
import dlans.bluetoothapp.utils.BTManager;
import dlans.bluetoothapp.utils.LogUtil;

public class DataFragment extends Fragment {

    private static final String TAG = "DataFragment";
    private DataViewModel dataViewModel;
    private FragmentDataBinding binding;
    private LineChartManager lineChartManager = null;

    public static final int GET_DATA = 1;

    public static Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_DATA:
                    LogUtil.d(TAG, "Get data");
                    break;
                default:
                    break;
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lineChartManager = new LineChartManager(binding.lineChart);
        BTManager.getInstance().setReadHandler(handler, GET_DATA);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
