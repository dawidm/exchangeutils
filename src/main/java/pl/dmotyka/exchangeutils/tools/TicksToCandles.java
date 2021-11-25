/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.tools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class TicksToCandles {

    private static class MutableChartCandle {
        long timestampSeconds;
        double open;
        double high;
        double low;
        double close;
    }

    public static ChartCandle[] generateCandles(Ticker[] tickers, long periodSeconds) {
        Ticker[] sortedTickers = Arrays.stream(tickers).sorted(Comparator.comparingLong(Ticker::getTimestampSeconds)).toArray(Ticker[]::new);
        long firstCandleTimestamp = sortedTickers[0].getTimestampSeconds() - sortedTickers[0].getTimestampSeconds() % periodSeconds;
        MutableChartCandle tmpMutableCandle = new MutableChartCandle();
        tmpMutableCandle.timestampSeconds = firstCandleTimestamp;
        tmpMutableCandle.open = sortedTickers[0].getValue();
        tmpMutableCandle.low = sortedTickers[0].getValue();
        tmpMutableCandle.high = sortedTickers[0].getValue();
        tmpMutableCandle.close = sortedTickers[0].getValue();
        List<ChartCandle> chartCandles = new LinkedList<>();
        for (int i=1; i< sortedTickers.length;i++) {
            Ticker currentTicker = sortedTickers[i];
            if (currentTicker.getTimestampSeconds() - tmpMutableCandle.timestampSeconds >= periodSeconds) {
                tmpMutableCandle.close = sortedTickers[i-1].getValue();
                chartCandles.add(new ChartCandle(tmpMutableCandle.high, tmpMutableCandle.low, tmpMutableCandle.open, tmpMutableCandle.close, tmpMutableCandle.timestampSeconds));
                tmpMutableCandle = new MutableChartCandle();
                tmpMutableCandle.open = currentTicker.getValue();
                tmpMutableCandle.high = currentTicker.getValue();
                tmpMutableCandle.low = currentTicker.getValue();
                tmpMutableCandle.timestampSeconds = currentTicker.getTimestampSeconds() - currentTicker.getTimestampSeconds() % periodSeconds;
            } else {
                if (currentTicker.getValue() < tmpMutableCandle.low) {
                    tmpMutableCandle.low = currentTicker.getValue();
                }
                if (currentTicker.getValue() > tmpMutableCandle.high) {
                    tmpMutableCandle.high = currentTicker.getValue();
                }
            }
        }
        return chartCandles.toArray(new ChartCandle[0]);
    }

}
