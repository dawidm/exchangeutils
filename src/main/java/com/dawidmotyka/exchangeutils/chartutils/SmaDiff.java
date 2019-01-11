package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

public class SmaDiff implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        if(numCandles<2)
            throw new IllegalArgumentException("numCandles is to low");
        double closePriceSum=0;
        for(int i=chartCandles.length-numCandles;i<=chartCandles.length-1;i++) {
            closePriceSum+=chartCandles[i].getClose();
        }
        double smaWithoutLastCandle=closePriceSum/(numCandles-1);
        closePriceSum+=chartCandles[chartCandles.length-1].getClose();
        double smaWithLastCandle=closePriceSum/numCandles;
        return smaWithLastCandle-smaWithoutLastCandle;
    }
}
