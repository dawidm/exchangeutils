package com.dawidmotyka.exchangeutils.tickerprovider;

/**
 * Created by dawid on 8/13/17.
 */
public class Ticker {
    private final String pair;
    private final double value;
    private final long timestampSeconds;

    public Ticker(String pair, double value, long timestampSeconds) {
        this.pair = pair;
        this.value = value;
        this.timestampSeconds=timestampSeconds;
    }

    public String getPair() {
        return pair;
    }

    public double getValue() {
        return value;
    }

    public long getTimestampSeconds() {
        return timestampSeconds;
    }
}
