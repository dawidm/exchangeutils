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
