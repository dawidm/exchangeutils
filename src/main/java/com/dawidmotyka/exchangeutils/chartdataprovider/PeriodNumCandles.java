package com.dawidmotyka.exchangeutils.chartdataprovider;

public class PeriodNumCandles implements Comparable<PeriodNumCandles> {
    private final int periodSeconds;
    private final int numCandles;

    public PeriodNumCandles(int periodSeconds, int numCandles) {
        this.periodSeconds = periodSeconds;
        this.numCandles = numCandles;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public int getNumCandles() {
        return numCandles;
    }

    @Override
    public int compareTo(PeriodNumCandles other) {
        return Integer.compare(this.periodSeconds,other.periodSeconds);
    }
}
