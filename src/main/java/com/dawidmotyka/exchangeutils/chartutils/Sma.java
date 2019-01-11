package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

import java.util.Arrays;

public class Sma implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        if(numCandles<2)
            throw new IllegalArgumentException("numCandles is to low");
        int numCandlesToSkip = chartCandles.length-numCandles;
        return Arrays.stream(chartCandles).skip(numCandlesToSkip).mapToDouble(chartCandle -> chartCandle.getClose()).average().getAsDouble();
    }
}
