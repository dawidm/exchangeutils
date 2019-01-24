package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.BinanceChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import org.knowm.xchange.binance.BinanceExchange;

/**
 * Created by dawid on 8/20/17.
 */
public class BinanceExchangeSpecs extends ExchangeSpecs {

    private static final String EXCHANGE_NAME = "Binance";
    private static final String MARKET_URL = "https://www.binance.com/tradeDetail.html?symbol=";
    private static final String COLOR_HEX = "cc9900";

    public BinanceExchangeSpecs() {
        super(EXCHANGE_NAME,MARKET_URL, BinanceExchange.class,0.0005, COLOR_HEX,200);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new BinanceChartInfo();
    }
}
