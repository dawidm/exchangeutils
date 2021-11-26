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

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import org.knowm.xchange.Exchange;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphExchangeSpecs;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

public class Uniswap3ExchangeSpecs extends TheGraphExchangeSpecs {

    private static final String THE_GRAPH_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v3";
    private static final String THE_GRAPH_HOSTNAME = "api.thegraph.com";
    private static final int API_PORT = 443;
    private static final String MARKET_URL = "https://info.uniswap.org/#/tokens/";
    private static final String COLOR_HEX = "B10DC9";
    static final String[] SUPPORTED_COUNTER_CURR = new String[] {"USD"};

    private static final int TICKER_PROVIDER_PAST_TICKS_SECONDS_AGO = 300;

    private final Uniswap3PairDataProvider uniswap3PairDataProvider = new Uniswap3PairDataProvider();

    public Uniswap3ExchangeSpecs() {
        super("UniswapV3");
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new Uniswap3ChartInfo(uniswap3PairDataProvider);
    }

    @Override
    public PairDataProvider getPairDataProvider() {
        return uniswap3PairDataProvider;
    }

    @Override
    public PairSymbolConverter getPairSymbolConverter() {
        return new Uniswap3PairSymbolConverter(this);
    }

    @Override
    public String getMarketUrl() {
        return MARKET_URL;
    }

    @Override
    public Exchange getXchangeExchange() {
        throw  new RuntimeException("not supported");
    }

    @Override
    public String getColorHash() {
        return COLOR_HEX;
    }

    @Override
    public int getDelayBetweenChartDataRequestsMs() {
        return 0;
    }

    @Override
    public TickerProvider getTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        return new Uniswap3TickerProvider(tickerReceiver, pairs, this, TICKER_PROVIDER_PAST_TICKS_SECONDS_AGO);
    }

    @Override
    public String getApiHostname() {
        return THE_GRAPH_HOSTNAME;
    }

    @Override
    public int getApiPort() {
        return API_PORT;
    }

    @Override
    public String getTheGraphApiURL() {
        return THE_GRAPH_URL;
    }

    @Override
    public String[] getSupportedCounterCurrencies() {
        return SUPPORTED_COUNTER_CURR;
    }
}
