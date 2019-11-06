package com.dxc.hassanalthaf.eventscheduler.graphs;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class ComparisonGraphIndexAxisValueFormatter extends ValueFormatter {
    private ArrayList<String> values;

    public ComparisonGraphIndexAxisValueFormatter(ArrayList<String> values) {
        this.values = values;
    }



    @Override
    public String getAxisLabel(float value, AxisBase base) {
        int index = Math.round(value);

        return this.values.get(index - 1);
    }
}
