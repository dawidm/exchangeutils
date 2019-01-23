package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.exchangespecs.*;

/**
 * Created by dawid on 12/4/17.
 */
public interface ExchangeChartInfo {
    static ExchangeChartInfo forExchange(ExchangeSpecs exchangeSpecs) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs) {
            return new PoloniexChartInfo();
        }
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            return new BinanceChartInfo();
        }
        if(exchangeSpecs instanceof BittrexExchangeSpecs) {
            return new BittrexChartInfo();
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs)
            return new XtbChartInfo();
        if(exchangeSpecs instanceof BitfinexExchangeSpecs)
            return new BitfinexChartInfo();
        throw new Error("not implemented for "+exchangeSpecs.getName());
    };
    ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException;
    ChartTimePeriod[] getAvailablePeriods();
}
