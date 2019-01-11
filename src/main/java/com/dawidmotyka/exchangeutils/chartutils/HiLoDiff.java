package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

public class HiLoDiff implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        double high=0;
        int hiIndex=0;
        double low=Double.MAX_VALUE;
        int lowIndex=0;
        for(int i=chartCandles.length-numCandles;i<chartCandles.length;i++) {
            if(chartCandles[i].getHigh()>high) {
                hiIndex=i;
                high=chartCandles[i].getHigh();
            }
            if(chartCandles[i].getLow()<low) {
                lowIndex=i;
                low=chartCandles[i].getLow();
            }
            if(hiIndex==lowIndex)
                return Math.abs(high-low)*Math.signum(chartCandles[i].getClose()-chartCandles[i].getOpen());
            if(hiIndex>lowIndex)
                return Math.abs(high-low);
            else
                return -Math.abs(high-low);
        }
        throw new Error("should return value before getting there");
    }
}
