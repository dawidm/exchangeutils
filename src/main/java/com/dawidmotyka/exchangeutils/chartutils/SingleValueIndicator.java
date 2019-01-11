package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

interface SingleValueIndicator {
    double calcValue(ChartCandle[] chartCandles, int numCandles);
}
