/*
 * Cryptonose
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.chartutils;

import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

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
