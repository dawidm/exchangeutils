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
