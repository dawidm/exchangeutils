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

package pl.dmotyka.exchangeutils.exchangespecs;

import org.knowm.xchange.Exchange;
import org.pf4j.ExtensionPoint;
import pl.dmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import pl.dmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

/**
 * Created by dawid on 8/20/17.
 */
public abstract class ExchangeSpecs implements ExtensionPoint {

    private final String name;

    public ExchangeSpecs(String name) {
        this.name = name;
    }

    public abstract ExchangeChartInfo getChartInfo();

    public abstract PairDataProvider getPairDataProvider();

    public abstract PairSymbolConverter getPairSymbolConverter();

    public String getName() {
        return name;
    }

    public abstract String getMarketUrl();

    public abstract Exchange getXchangeExchange();

    public abstract String getColorHash();

    public abstract int getDelayBetweenChartDataRequestsMs();

    public abstract TickerProvider getTickerProvider(TickerReceiver tickerReceiver, String[] pairs);

    // get instance of ExchangeSpecs subclass for exchange string name, not case-sensitive
    // available exchanges: poloniex, bittrex, bitfinex, binance, xtb
    public static ExchangeSpecs fromStringName(String exchangeName) throws NoSuchExchangeException{
        switch (exchangeName.toLowerCase()) {
            case "poloniex": return new PoloniexExchangeSpecs();
            case "bittrex": return new BittrexExchangeSpecs();
            case "bitfinex": return new BitfinexExchangeSpecs();
            case "binance": return new BinanceExchangeSpecs();
            default: throw new NoSuchExchangeException("when getting exchange from string: " + exchangeName);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExchangeSpecs)
            return name.equals(((ExchangeSpecs)obj).getName());
        return false;
    }

}
