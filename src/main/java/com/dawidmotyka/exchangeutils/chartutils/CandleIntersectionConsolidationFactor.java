package com.dawidmotyka.exchangeutils.chartutils;

import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;

//for all candles except the first one calculates intersection of candle min-max with previous candle min-max divided by candle length
//returns average of all calculated values
public class CandleIntersectionConsolidationFactor implements SingleValueIndicator {

    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        double intersectionSum=0;
        for(int i=chartCandles.length-numCandles+1;i<chartCandles.length;i++) {
            intersectionSum+=calcCandleIntersectionLength(chartCandles[i-1],chartCandles[i]);
        }
        return intersectionSum/numCandles;
    }

    private double calcCandleIntersectionLength(ChartCandle previousCandle, ChartCandle currentCandle) {
        if(currentCandle.getHigh()==currentCandle.getLow() || previousCandle.getHigh()==previousCandle.getLow())
            return 0;
        //current candle is "inside" previous
        if(currentCandle.getHigh()<previousCandle.getHigh() && currentCandle.getLow()>previousCandle.getLow())
            return 1;
        double previousCandleLength=Math.abs(previousCandle.getHigh()-previousCandle.getLow());
        double currentCandleLength=Math.abs(currentCandle.getHigh()-currentCandle.getLow());
        //previous candle is "inside" current
        if(currentCandle.getHigh()>previousCandle.getHigh() && currentCandle.getLow()<previousCandle.getLow())
            return previousCandleLength/currentCandleLength;
        double jointLength=Math.max(
                Math.abs(previousCandle.getLow()-currentCandle.getHigh()),
                Math.abs(previousCandle.getHigh()-currentCandle.getLow())
        );
        double intersectionValue = Math.max(0,previousCandleLength+currentCandleLength-jointLength);
        return intersectionValue/currentCandleLength;
    }

}
