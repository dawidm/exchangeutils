package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceKline;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;
import org.knowm.xchange.binance.service.BinanceMarketDataService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dawid on 12/4/17.
 */
public class BinanceChartInfo implements ExchangeChartInfo {

    private final Logger logger = Logger.getLogger(BinanceChartInfo.class.getName());
    private Exchange exchange;
    private BinanceMarketDataService binanceMarketDataService;

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws ExchangeCommunicationException, NoSuchTimePeriodException {
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
        } catch (IOException e) {
            logger.log(Level.WARNING,"when getting binance klines for " + symbol,e);
            throw new ExchangeCommunicationException("when getting binance klines for " + symbol);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        //TODO add missing
        return new ChartTimePeriod[] {
                new ChartTimePeriod("m1",60,null),
                new ChartTimePeriod("m5",5*60,null),
                new ChartTimePeriod("m15",15*60,null),
                new ChartTimePeriod("m30",30*60,null),
                new ChartTimePeriod("h1",60*60,null),
                new ChartTimePeriod("d1",24*60*60,null),
                new ChartTimePeriod("w1",7*24*60*60,null),
        };
    }

    private KlineInterval periodsSecondsToKlineInterval(int periodSeconds) throws NoSuchTimePeriodException {
        //TODO add missing
        switch (periodSeconds) {
            case 60: return KlineInterval.m1;
            case 300: return KlineInterval.m5;
            case 900: return KlineInterval.m15;
            case 1800: return KlineInterval.m30;
            case 3600: return KlineInterval.h1;
            case 86400: return KlineInterval.d1;
            case 604800: return KlineInterval.w1;
        }
        throw new NoSuchTimePeriodException("for time period (seconds): " + periodSeconds);
    }

}
