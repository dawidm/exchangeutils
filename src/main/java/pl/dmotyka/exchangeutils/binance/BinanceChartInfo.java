/*
 * Cryptonose
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.binance;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceKline;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

/**
 * Created by dawid on 12/4/17.
 */
public class BinanceChartInfo implements ExchangeChartInfo {

    private final Logger logger = Logger.getLogger(BinanceChartInfo.class.getName());
    private Exchange exchange;
    private BinanceMarketDataService binanceMarketDataService;

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws ExchangeCommunicationException, NoSuchTimePeriodException, ConnectionProblemException {
        if (exchange==null || binanceMarketDataService==null) {
            exchange=ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class);
            binanceMarketDataService = (BinanceMarketDataService) exchange.getMarketDataService();
        }
        try {
            List<BinanceKline> binanceKlineList = binanceMarketDataService.klines(BinanceAdapters.adaptSymbol(symbol), periodsSecondsToKlineInterval((int) timePeriodSeconds), 500, beginTimestampSeconds * 1000, endTimestampSeconds * 1000);
            return binanceKlineList.stream().
                    map(binanceKline -> new ChartCandle(
                            binanceKline.getHighPrice().doubleValue(),
                            binanceKline.getLowPrice().doubleValue(),
                            binanceKline.getOpenPrice().doubleValue(),
                            binanceKline.getClosePrice().doubleValue(),
                            binanceKline.getOpenTime() / 1000)).
                    toArray(ChartCandle[]::new);
        } catch (UnknownHostException e) {
            throw new ConnectionProblemException("when getting binance klines for " + symbol);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting binance klines for " + symbol);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("m1",60,null),
                new ChartTimePeriod("m5",5*60,null),
                new ChartTimePeriod("m15",15*60,null),
                new ChartTimePeriod("m30",30*60,null),
                new ChartTimePeriod("h1",60*60,null),
                new ChartTimePeriod("h2",2*60*60,null),
                new ChartTimePeriod("h4",4*60*60,null),
                new ChartTimePeriod("h6",6*60*60,null),
                new ChartTimePeriod("h12",12*60*60,null),
                new ChartTimePeriod("d1",24*60*60,null),
                new ChartTimePeriod("d3",3*24*60*60,null),
                new ChartTimePeriod("w1",7*24*60*60,null),
                new ChartTimePeriod("M1",30*24*60*60,null),
        };
    }

    private KlineInterval periodsSecondsToKlineInterval(int periodSeconds) throws NoSuchTimePeriodException {
        switch (periodSeconds) {
            case 60: return KlineInterval.m1;
            case 300: return KlineInterval.m5;
            case 900: return KlineInterval.m15;
            case 1800: return KlineInterval.m30;
            case 3600: return KlineInterval.h1;
            case 7200: return KlineInterval.h2;
            case 14400: return KlineInterval.h4;
            case 21600: return KlineInterval.h6;
            case 43200: return KlineInterval.h12;
            case 86400: return KlineInterval.d1;
            case 259200: return KlineInterval.d3;
            case 604800: return KlineInterval.w1;
        }
        throw new NoSuchTimePeriodException("for time period (seconds): " + periodSeconds);
    }

}
