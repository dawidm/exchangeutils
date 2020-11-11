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

package pl.dmotyka.exchangeutils.tickerprovider;

import java.io.IOException;

import pl.dmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import pl.dmotyka.exchangeutils.binance.BinanceTransactionsWebSocket;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeMethods;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import pl.dmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import pl.dmotyka.exchangeutils.bittrex.BittrexWebsocketTickerProvider;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import pl.dmotyka.exchangeutils.poloniex.PoloniexWebSocket;
import pl.dmotyka.exchangeutils.xtb.XtbExchangeSpecs;
import pl.dmotyka.exchangeutils.xtb.XtbTickerProvider;

public interface TickerProvider {
    static TickerProvider forExchange(ExchangeSpecs exchangeSpecs, TickerReceiver tickerReceiver, String[] pairs) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs) {
            return new PoloniexWebSocket(tickerReceiver,pairs);
        }
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            return new BinanceTransactionsWebSocket(tickerReceiver, pairs);
        }
        if(exchangeSpecs instanceof BittrexExchangeSpecs) {
            return new BittrexWebsocketTickerProvider(tickerReceiver,pairs);
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs)
            return new XtbTickerProvider(tickerReceiver,pairs);
        if(exchangeSpecs instanceof BitfinexExchangeSpecs)
            return new GenericWebsocketTickerProvider(tickerReceiver,pairs,new BitfinexExchangeMethods());
        throw new Error("not implemented for "+exchangeSpecs.getName());
    };

    void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException;
    void disconnect();
}
