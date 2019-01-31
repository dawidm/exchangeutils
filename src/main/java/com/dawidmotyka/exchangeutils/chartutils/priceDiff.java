package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

public class priceDiff implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        if(numCandles<2)
            throw new IllegalArgumentException("numCandles is to low");
        return chartCandles[chartCandles.length-1].getClose()-chartCandles[chartCandles.length-numCandles].getClose();
    }
}
