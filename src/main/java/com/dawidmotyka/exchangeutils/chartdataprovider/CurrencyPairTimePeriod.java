package com.dawidmotyka.exchangeutils.chartdataprovider;

public class CurrencyPairTimePeriod {
    private final String currencyPairSymbol;
    private final int timePeriodSeconds;

    public CurrencyPairTimePeriod(String currencyPairSymbol, int timePeriodSeconds) {
        this.currencyPairSymbol = currencyPairSymbol;
        this.timePeriodSeconds = timePeriodSeconds;
    }

    public String getCurrencyPairSymbol() {
        return currencyPairSymbol;
    }

    public int getTimePeriodSeconds() {
        return timePeriodSeconds;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return currencyPairSymbol+","+timePeriodSeconds;
    }
}
