package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.BittrexChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import org.knowm.xchange.bittrex.BittrexExchange;

/**
 * Created by dawid on 8/20/17.
 */
public class BittrexExchangeSpecs extends ExchangeSpecs {

    private static final String EXCHANGE_NAME = "Bittrex";
    private static final String MARKET_URL = "https://bittrex.com/Market/Index?MarketName=";
    private static final String COLOR_HEX = "2184ff";

    public BittrexExchangeSpecs() {
        super(EXCHANGE_NAME,MARKET_URL, BittrexExchange.class,0.0005, COLOR_HEX);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new BittrexChartInfo();
    }
}
