package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

import java.util.Arrays;

public class AvgHiLo implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        return Arrays.stream(chartCandles).mapToDouble(chartCandle -> Math.abs(chartCandle.getHigh()-chartCandle.getLow())).average().getAsDouble();
    }
}
