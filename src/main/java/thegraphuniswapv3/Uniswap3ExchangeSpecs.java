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

package thegraphuniswapv3;

import org.knowm.xchange.Exchange;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphExchangeSpecs;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

public class Uniswap3ExchangeSpecs extends TheGraphExchangeSpecs {

    private static final String THE_GRAPH_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v3";

    public Uniswap3ExchangeSpecs() {
        super("Uniswap V3");
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return null;
    }

    @Override
    public PairDataProvider getPairDataProvider() {
        return null;
    }

    @Override
    public PairSymbolConverter getPairSymbolConverter() {
        return null;
    }

    @Override
    public String getMarketUrl() {
        return null;
    }

    @Override
    public Exchange getXchangeExchange() {
        return null;
    }

    @Override
    public String getColorHash() {
        return null;
    }

    @Override
    public int getDelayBetweenChartDataRequestsMs() {
        return 0;
    }

    @Override
    public TickerProvider getTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        return null;
    }

    @Override
    public String getApiHostname() {
        return null;
    }

    @Override
    public int getApiPort() {
        return 0;
    }

    @Override
    public String getTheGraphApiURL() {
        return THE_GRAPH_URL;
    }
}
