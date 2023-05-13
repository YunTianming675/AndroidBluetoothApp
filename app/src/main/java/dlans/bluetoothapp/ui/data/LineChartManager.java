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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import dlans.bluetoothapp.utils.GlobalContext;
import dlans.bluetoothapp.utils.LogUtil;

public class LineChartManager implements OnChartValueSelectedListener {

    private static final String TAG = "LineChartManager";
    private int MIN_DATA = 0;
    private int MAX_DATA = 100;
    private LineChart lineChart;
    private Typeface typeface;

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setDrawCircles(false);
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

    private void addEntry(byte[] data) {
        LineData lineData = lineChart.getData();
        if (lineData != null) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                lineData.addDataSet(set);
            }
            for (byte b : data) {
                lineData.addEntry(new Entry(set.getEntryCount(), b), 0);
                lineData.notifyDataChanged();
            }
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(300);
            lineChart.moveViewToX(lineData.getEntryCount());
        } else {
            throw new NullPointerException("lineChart.getData() return null");
        }
    }

    private void addEntry(ArrayList<Integer> list) {
        LineData lineData = lineChart.getData();
        if (lineData != null) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                lineData.addDataSet(set);
            }
            for (int i : list) {
                lineData.addEntry(new Entry(set.getEntryCount(), i), 0);
                lineData.notifyDataChanged();
            }
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(300);
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
        this.lineChart.setBackgroundColor(Color.WHITE);

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
        leftAxis.setAxisMaximum(70f);
        leftAxis.setAxisMinimum(40f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = this.lineChart.getAxisRight();
        rightAxis.setEnabled(true);
    }

    public void addData(byte[] data) {
        LogUtil.d(TAG, "addData");
        byte[] d = Arrays.copyOf(data, data.length);
        Arrays.sort(d);
        int minData = d[0];
        int maxData = d[d.length-1];
        if (MIN_DATA < minData) {
            MIN_DATA = minData;
            lineChart.getAxisLeft().setAxisMinimum((float) MIN_DATA);
        }
        if (MAX_DATA > maxData) {
            MAX_DATA = maxData;
            lineChart.getAxisLeft().setAxisMaximum((float) MAX_DATA);
        }
        addEntry(data);
    }

    public void addData(ArrayList<Integer> list) {
        LogUtil.d(TAG, "addData");

        ArrayList<Integer> list1 = new ArrayList();
        for (Integer i : list) {
            list1.add(i);
        }
        Collections.sort(list1);

        int minData = list1.get(0);
        int maxData = list1.get(list1.size()-1);
        if (MIN_DATA > minData) {
            MIN_DATA = minData;
            lineChart.getAxisLeft().setAxisMinimum((float) MIN_DATA);
        }
        if (MAX_DATA < maxData) {
            MAX_DATA = maxData;
            lineChart.getAxisLeft().setAxisMaximum((float) MAX_DATA);
        }
        addEntry(list);
    }
}
