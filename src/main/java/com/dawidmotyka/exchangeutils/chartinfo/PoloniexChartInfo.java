package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.CurrencyPairConverter;
import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
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
    private  PoloniexMarketDataService poloniexMarketDataService;

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
            PoloniexChartData poloniexChartData[] = poloniexMarketDataService.getPoloniexChartData(CurrencyPairConverter.poloPairToCurrencyPair(symbol),
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
