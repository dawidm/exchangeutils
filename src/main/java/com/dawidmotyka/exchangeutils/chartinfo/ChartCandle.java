/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.chartinfo;

public class ChartCandle {

    private double high;
    private double low;
    private double open;
    private double close;
    private long timestampSeconds;

    public ChartCandle(double high, double low, double open, double close, long timestampSeconds) {
        this.timestampSeconds=timestampSeconds;
        this.high=high;
        this.low=low;
        this.open=open;
        this.close=close;
    }

    public ChartCandle(double open, long timestamp) {
        this.open=open;
        this.timestampSeconds=timestamp;
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

    protected void setHigh(double high) {
        this.high = high;
    }

    protected void setLow(double low) {
        this.low = low;
    }

    protected void setOpen(double open) {
        this.open = open;
    }

    protected void setClose(double close) {
        this.close = close;
    }
}
