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

import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import com.dawidmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexChartData;
import org.knowm.xchange.poloniex.service.PoloniexChartDataPeriodType;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dawid on 12/16/17.
 */
public class PoloniexChartInfo implements ExchangeChartInfo {

    private static final Logger logger = Logger.getLogger(PoloniexChartInfo.class.getName());

    private Exchange poloniex;
    private PoloniexMarketDataService poloniexMarketDataService;
    private final ExchangeSpecs exchangeSpecs = new PoloniexExchangeSpecs();

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws ExchangeCommunicationException {
        if(poloniex==null | poloniexMarketDataService==null) {
            poloniex = ExchangeFactory.INSTANCE.createExchange(PoloniexExchange.class.getName());
            poloniexMarketDataService = (PoloniexMarketDataService) poloniex.getMarketDataService();
        }
        try {
            PoloniexChartDataPeriodType poloniexChartDataPeriodType = periodSecondsToPoloniexPeriodType((int)timePeriodSeconds);
            if(poloniexChartDataPeriodType==null)
                throw new ExchangeCommunicationException("error getting PoloniexChartDataPeriodType for " + timePeriodSeconds);

            PoloniexChartData poloniexChartData[] = poloniexMarketDataService.getPoloniexChartData(
                    new CurrencyPair(PairSymbolConverter.apiSymbolToBaseCurrencySymbol(exchangeSpecs,symbol),PairSymbolConverter.apiSymbolToCounterCurrencySymbol(exchangeSpecs,symbol)),
                    new Long(beginTimestampSeconds),
                    new Long(endTimestampSeconds),
                    poloniexChartDataPeriodType);
            return Arrays.stream(poloniexChartData).map(poloniexChartCandle -> new ChartCandle(poloniexChartCandle.getHigh().doubleValue(),
                    poloniexChartCandle.getLow().doubleValue(),
                    poloniexChartCandle.getOpen().doubleValue(),
                    poloniexChartCandle.getClose().doubleValue(),
                    poloniexChartCandle.getDate().getTime()/1000)).toArray(ChartCandle[]::new);
        } catch (IOException e) {
            logger.log(Level.WARNING,"when getting poloniex candles for: "+symbol);
            throw new ExchangeCommunicationException(e.getClass().getSimpleName() + " when getting poloniex candles for: "+symbol);
        }
    }

    private PoloniexChartDataPeriodType periodSecondsToPoloniexPeriodType(int periodSeconds) {
        for (PoloniexChartDataPeriodType poloniexChartDataPeriodType : PoloniexChartDataPeriodType.values()) {
            if(poloniexChartDataPeriodType.getPeriod()==periodSeconds)
                return poloniexChartDataPeriodType;
        }
        return null;
    };

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("5m",5*60,null),
                new ChartTimePeriod("15m",15*60,null),
                new ChartTimePeriod("30m",30*60,null),
                new ChartTimePeriod("2h",2*60*60,null),
                new ChartTimePeriod("4h",4*60*60,null),
                new ChartTimePeriod("1d",24*60*60,null),
        };
    }
}
