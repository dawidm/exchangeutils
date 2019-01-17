package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.XtbChartInfo;

public class XtbExchangeSpecs extends ExchangeSpecs {
    private static final String EXCHANGE_NAME = "Xtb";
    private static final String MARKET_URL = "";
    private static final String COLOR_HEX = "000000";

    public XtbExchangeSpecs() {
        super(EXCHANGE_NAME,MARKET_URL, null,0, COLOR_HEX,200);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new XtbChartInfo();
    }

}
