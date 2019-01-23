package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.BitfinexChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import org.knowm.xchange.binance.BinanceExchange;

/**
 * Created by dawid on 8/20/17.
 */
public class BitfinexExchangeSpecs extends ExchangeSpecs {

    private static final String EXCHANGE_NAME = "Bitfinex";
    private static final String MARKET_URL = "https://www.bitfinex.com/t/";
    private static final String COLOR_HEX = " 32CD32";

    public BitfinexExchangeSpecs() {
        super(EXCHANGE_NAME,MARKET_URL, BinanceExchange.class,0.0005, COLOR_HEX,200);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new BitfinexChartInfo();
    }
}
