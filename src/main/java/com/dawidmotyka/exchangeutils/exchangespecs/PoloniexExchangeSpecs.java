package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.PoloniexChartInfo;
import org.knowm.xchange.poloniex.PoloniexExchange;

/**
 * Created by dawid on 8/20/17.
 */
public class PoloniexExchangeSpecs extends ExchangeSpecs {

    private static final String EXCHANGE_NAME = "Poloniex";
    private static final String MARKET_URL = "https://poloniex.com/exchange#";
    private static final String COLOR_HEX = "0a6970";

    public PoloniexExchangeSpecs() {
        super(EXCHANGE_NAME,MARKET_URL, PoloniexExchange.class,0.0001, COLOR_HEX,500);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new PoloniexChartInfo();
    }
}
