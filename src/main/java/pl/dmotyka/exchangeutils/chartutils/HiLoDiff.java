/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
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
