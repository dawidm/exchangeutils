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

package pl.dmotyka.exchangeutils.chartinfo;

import java.util.Objects;

public class ChartCandle {

    private final double high;
    private final double low;
    private final double open;
    private final double close;
    private final long timestampSeconds;
    private final Double quoteVolume;

    public ChartCandle(double high, double low, double open, double close, long timestampSeconds) {
        this.timestampSeconds=timestampSeconds;
        this.high=high;
        this.low=low;
        this.open=open;
        this.close=close;
        this.quoteVolume = null;
    }

    public ChartCandle(double high, double low, double open, double close, long timestampSeconds, double quoteVolume) {
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.timestampSeconds = timestampSeconds;
        this.quoteVolume = quoteVolume;
    }

    /**
     * @return the high
     */
    public double getHigh() {
        return high;
    }

    /**
     * @return the low
     */
    public double getLow() {
        return low;
    }

    /**
     * @return the open
     */
    public double getOpen() {
        return open;
    }

    /**
     * @return the close
     */
    public double getClose() {
        return close;
    }

    /**
     * @return the timestamp
     */
    public long getTimestampSeconds() {
        return timestampSeconds;
    }

    /**
     * @return quote volume
     */
    public Double getQuoteVolume() {
        return quoteVolume;
    }

    // returns true when price didn't change "within" this candle
    public boolean isNoPriceChangeCandle() {
        return (open == close && close == high && high == low);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChartCandle that = (ChartCandle) o;
        return Double.compare(that.high, high) == 0 &&
                Double.compare(that.low, low) == 0 &&
                Double.compare(that.open, open) == 0 &&
                Double.compare(that.close, close) == 0 &&
                timestampSeconds == that.timestampSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, low, open, close, timestampSeconds);
    }
}
