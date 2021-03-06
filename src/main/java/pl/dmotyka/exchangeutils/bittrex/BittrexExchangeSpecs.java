/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.bittrex;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bittrex.BittrexExchange;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

/**
 * Created by dawid on 8/20/17.
 */
public class BittrexExchangeSpecs extends ExchangeSpecs {

    private static final String EXCHANGE_NAME = "Bittrex";
    private static final String MARKET_URL = "https://bittrex.com/Market/Index?MarketName=";
    private static final String COLOR_HEX = "2184ff";
    private static final int DELAY_BETWEEN_REQUESTS_MS=500;
    private static final String API_HOSTNAME = "api.bittrex.com/v3";
    private static final int API_PORT = 443;

    public BittrexExchangeSpecs() {
        super(EXCHANGE_NAME);
    }

    @Override
    public ExchangeChartInfo getChartInfo() { return new BittrexChartInfo(); }

    @Override
    public PairDataProvider getPairDataProvider() {
        return new BittrexPairDataProvider();
    }

    @Override
    public PairSymbolConverter getPairSymbolConverter() {
        return new BittrexPairSymbolConverter();
    }

    @Override
    public String getMarketUrl() { return MARKET_URL; }

    @Override
    public Exchange getXchangeExchange() { return ExchangeFactory.INSTANCE.createExchange(BittrexExchange.class); }

    @Override
    public String getColorHash() {
        return COLOR_HEX;
    }

    @Override
    public int getDelayBetweenChartDataRequestsMs() {
        return DELAY_BETWEEN_REQUESTS_MS;
    }

    @Override
    public TickerProvider getTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getApiHostname() {
        return API_HOSTNAME;
    }

    @Override
    public int getApiPort() {
        return API_PORT;
    }
}
