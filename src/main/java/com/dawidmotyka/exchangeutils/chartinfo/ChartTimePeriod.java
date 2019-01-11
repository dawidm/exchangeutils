package com.dawidmotyka.exchangeutils.chartinfo;

/**
 * Created by dawid on 12/4/17.
 */
public class ChartTimePeriod {

    public static final long PERIOD_1M = 60;
    public static final long PERIOD_5M = 5*60;
    public static final long PERIOD_15M = 15*60;
    public static final long PERIOD_30M = 30*60;
    public static final long PERIOD_1H = 60*60;
    public static final long PERIOD_3H = 3*60*60;
    public static final long PERIOD_1D = 24*60*60;

    private final String periodName;
    private final long periodLengthSeconds;
    private final String exchangePeriodStringId;

    public ChartTimePeriod(String periodName, long periodLengthSeconds, String exchangePeriodStringId) {
        this.periodName = periodName;
        this.periodLengthSeconds = periodLengthSeconds;
        this.exchangePeriodStringId = exchangePeriodStringId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public long getPeriodLengthSeconds() {
        return periodLengthSeconds;
    }

    public String getExchangePeriodStringId() {
        return exchangePeriodStringId;
    }

    @Override
    public String toString() {
        return periodName;
    }
}
