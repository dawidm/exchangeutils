package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import org.knowm.xchange.hitbtc.v2.HitbtcExchange;

/**
 * Created by dawid on 9/29/17.
 */
public class HitBtcExchangeSpecs extends ExchangeSpecs {

        private static final String EXCHANGE_NAME = "HitBtc";
        private static final String MARKET_URL = "https://hitbtc.com/exchange/";
        private static final String COLOR_HEX = "000000";

        public HitBtcExchangeSpecs() {
            super(EXCHANGE_NAME,MARKET_URL, HitbtcExchange.class,0.0, COLOR_HEX);
        }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return null;
    }
}
