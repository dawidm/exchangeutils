package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

public class CandleDistanceConsolidationFactor implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        double open=chartCandles[chartCandles.length-numCandles].getOpen();
        double previousValue=open;
        double distance=0;
        for(int i=chartCandles.length-numCandles+1;i<chartCandles.length;i++) {
            //choose high or low from current candle to get longer distance
            double currentValue;
            if(Math.abs(previousValue-chartCandles[i].getHigh())>Math.abs(previousValue-chartCandles[i].getLow()))
                currentValue=chartCandles[i].getHigh();
            else
                currentValue=chartCandles[i].getLow();
            distance+=Math.abs(currentValue-previousValue);
            previousValue=currentValue;
        }
        double close = chartCandles[chartCandles.length-1].getClose();
        distance+=Math.abs(close-previousValue);
        return distance/Math.abs(open-close);
    }
}
