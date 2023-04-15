package dlans.bluetoothapp.ui.data;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import dlans.bluetoothapp.utils.GlobalContext;
import dlans.bluetoothapp.utils.LogUtil;

public class LineChartManager implements OnChartValueSelectedListener {

    private static final String TAG = "LineChartManager";
    private LineChart lineChart;
    private Typeface typeface;
    private Thread thread;

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);

        return set;
    }

    private void addEntry(int[] data) {
        LineData lineData = lineChart.getData();
        int i;
        if (lineData != null) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
            }
//            for (i = 1; i <= data.length; i++) {
//                lineData.addEntry(new Entry(set.getEntryCount(), (float)data[i-1]), 0);
//                if ((i % 10) == 0) {
//                    lineData.notifyDataChanged();
//                    lineChart.notifyDataSetChanged();
//                    lineChart.setVisibleXRangeMaximum(2000);
//                    lineChart.moveViewToX(lineData.getEntryCount());
//                }
//            }
            lineData.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40 + 30)), 0);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(120);
            lineChart.moveViewToX(lineData.getEntryCount());
        } else {
            throw new NullPointerException("lineChart.getData() return null");
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public LineChartManager(LineChart lineChart) {
        if (lineChart == null) {
            throw new NullPointerException("lineChart is null");
        }
        this.lineChart = lineChart;
        this.lineChart.setOnChartValueSelectedListener(this);
        // enable description text
        this.lineChart.getDescription().setEnabled(true);
        // enable touch gestures
        this.lineChart.setTouchEnabled(true);
        this.lineChart.setPinchZoom(true);
        this.lineChart.setBackgroundColor(Color.LTGRAY);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);
        // set empty data
        this.lineChart.setData(lineData);
        // get legend, only possible after setting data
        Legend legend = this.lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        this.typeface = Typeface.createFromAsset(GlobalContext.getContext().getAssets(), "OpenSans-Light.ttf");
        legend.setTypeface(this.typeface);
        legend.setTextColor(Color.WHITE);

        XAxis xAxis = this.lineChart.getXAxis();
        xAxis.setTypeface(typeface);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setEnabled(true);

        YAxis leftAxis = this.lineChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = this.lineChart.getAxisRight();
        rightAxis.setEnabled(true);
    }

    public void addData(int[] data) {
        if (thread != null) {
            thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry(data);
            }
        };

        thread = new Thread(()->{
        });
        addEntry(data);
    }
}
