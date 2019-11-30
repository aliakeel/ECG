package com.ba.ecgreader;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class ChartUtils {
    private static final int MAX_ENTRIES = 200;
    // (more code, irrelevant for the issue...)
    public static void removeOutdatedEntries(DataSet... dataSets) {
        for (DataSet ds : dataSets) {
            while (ds.getEntryCount() > MAX_ENTRIES) {
                ds.removeFirst();
            }
        }
    }



}