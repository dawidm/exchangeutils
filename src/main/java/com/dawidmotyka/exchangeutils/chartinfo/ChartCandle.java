package com.dawidmotyka.exchangeutils.chartinfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dawid
 */
public class ChartCandle {

    protected double high;
    protected double low;
    protected double open;
    protected double close;
    protected long timestampSeconds;

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

    void setHigh(double high) {
        this.high = high;
    }

    void setLow(double low) {
        this.low = low;
    }

    void setOpen(double open) {
        this.open = open;
    }

    void setClose(double close) {
        this.close = close;
    }
}
