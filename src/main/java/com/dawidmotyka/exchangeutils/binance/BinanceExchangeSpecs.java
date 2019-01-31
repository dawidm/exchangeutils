/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.dawidmotyka.exchangeutils.binance;

import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
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
