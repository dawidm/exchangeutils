/*
 * Cryptonose
 *
 * Copyright © 2019-2022 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.chartutils;

import java.util.Arrays;

import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

public class WeightedAvgHiLo implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        ChartCandle[] lastCandles = Arrays.copyOfRange(chartCandles, chartCandles.length-numCandles, chartCandles.length);
        double[] hiLos = Arrays.stream(lastCandles).mapToDouble(chartCandle -> Math.abs(chartCandle.getHigh()-chartCandle.getLow())).toArray();
        double weightsSum = 0;
        for(int i=1; i<hiLos.length+1; i++) {
            hiLos[i-1] = hiLos[i-1] * i;
            weightsSum += i;
        }
        return Arrays.stream(hiLos).sum() / weightsSum;
    }
}
