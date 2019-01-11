package com.dawidmotyka.exchangeutils.chartdataprovider;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

import java.util.Map;

public interface ChartDataReceiver {
    void onChartCandlesUpdate(Map<String,ChartCandle[]> chartCandlesMap);
}
