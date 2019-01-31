/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
