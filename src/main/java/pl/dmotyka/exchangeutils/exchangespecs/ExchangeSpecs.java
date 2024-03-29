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

package pl.dmotyka.exchangeutils.exchangespecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.knowm.xchange.Exchange;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

/**
 * Created by dawid on 8/20/17.
 */
public abstract class ExchangeSpecs {

    public static final Logger logger = Logger.getLogger(ExchangeSpecs.class.getName());

    private static final int CHECK_CONNECTION_TIMEOUT_MILLIS = 60000;

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
    public static ExchangeSpecs fromStringName(String exchangeName) throws NoSuchExchangeException{
        ExchangeSpecs[] availableExchanges = getAll();
        for (ExchangeSpecs currentExchange : availableExchanges) {
            if (exchangeName.toLowerCase().equals(currentExchange.getName().toLowerCase())) {
                return currentExchange;
            }
        }
        throw new NoSuchExchangeException("when getting exchange from string: " + exchangeName);
    }

    public abstract String getApiHostname();

    public abstract int getApiPort();

    // check if exchange api is reachable (using java.net.Socket connect() method), throws exception if not (connection problems)
    public void checkConnection() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(getApiHostname(), getApiPort()), CHECK_CONNECTION_TIMEOUT_MILLIS);
        } catch (IOException e) {
            logger.log(Level.WARNING, name + ": no connection", e);
            throw e;
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

    public static ExchangeSpecs[] getAll() {
        ServiceLoader<ExchangeSpecsProvider> loader = ServiceLoader.load(ExchangeSpecsProvider.class);
        return loader.stream().map(prov -> prov.get().create()).toArray(ExchangeSpecs[]::new);
    }

}
