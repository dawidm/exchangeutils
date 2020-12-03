/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.xtb;

import org.knowm.xchange.Exchange;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeWithTradingHours;
import pl.dmotyka.exchangeutils.exchangespecs.TradingHours;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

public class XtbExchangeSpecs extends ExchangeSpecs implements ExchangeWithTradingHours {
    private static final String EXCHANGE_NAME = "Xtb";
    private static final String MARKET_URL = "";
    private static final String COLOR_HEX = "000000";
    public static final int DELAY_BETWEEN_REQUESTS_MS=200;

    public XtbExchangeSpecs() {
        super(EXCHANGE_NAME);
    }

    @Override
    public ExchangeChartInfo getChartInfo() {
        return new XtbChartInfo();
    }

    @Override
    public PairDataProvider getPairDataProvider() {
        return new XtbPairDataProvider();
    }

    @Override
    public String getMarketUrl() { return MARKET_URL; }

    @Override
    public Exchange getXchangeExchange() { return null; }

    @Override
    public String getColorHash() { return COLOR_HEX; }

    @Override
    public int getDelayBetweenChartDataRequestsMs() { return DELAY_BETWEEN_REQUESTS_MS; };

    @Override
    public TickerProvider getTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        return new XtbTickerProvider(tickerReceiver,pairs);
    }

    @Override
    public TradingHours getTradingHours(String symbol) {
        throw new RuntimeException("not implemented");
    }
}
