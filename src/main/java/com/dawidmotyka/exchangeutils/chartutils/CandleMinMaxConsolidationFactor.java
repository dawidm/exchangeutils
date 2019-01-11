package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

import java.util.Arrays;

public class CandleMinMaxConsolidationFactor implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        double globalMin=Arrays.stream(chartCandles).
                skip(chartCandles.length-numCandles).
                mapToDouble(chartCandle -> chartCandle.getLow()).
                min().getAsDouble();
        double globalMax=Arrays.stream(chartCandles).
                skip(chartCandles.length-numCandles).
                mapToDouble(chartCandle -> chartCandle.getHigh()).
                max().getAsDouble();
        double candleLengthSum=Arrays.stream(chartCandles).
                skip(chartCandles.length-numCandles).
                mapToDouble(chartCandle -> Math.abs(chartCandle.getHigh()-chartCandle.getLow())).
                sum();
        return (candleLengthSum/numCandles)/Math.abs(globalMax-globalMin);
    }
}
