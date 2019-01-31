package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.binance.BinanceChartInfo;
import com.dawidmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import com.dawidmotyka.exchangeutils.bittrex.BittrexChartInfo;
import com.dawidmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import com.dawidmotyka.exchangeutils.xtb.XtbExchangeSpecs;

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
