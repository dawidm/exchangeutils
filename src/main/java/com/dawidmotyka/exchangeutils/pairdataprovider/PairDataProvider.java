/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.dawidmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.binance.BinancePairDataProvider;
import com.dawidmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import com.dawidmotyka.exchangeutils.bitfinex.BitfinexPairDataProvider;
import com.dawidmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import com.dawidmotyka.exchangeutils.bittrex.BittrexPairDataProvider;
import com.dawidmotyka.exchangeutils.exceptions.NotImplementedException;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import com.dawidmotyka.exchangeutils.poloniex.PoloniexPairDataProvider;
import com.dawidmotyka.exchangeutils.xtb.XtbExchangeSpecs;
import com.dawidmotyka.exchangeutils.xtb.XtbPairDataProvider;

import java.io.IOException;

public interface PairDataProvider {
    static PairDataProvider forExchange(ExchangeSpecs exchangeSpecs) throws NotImplementedException {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs) {
            return new PoloniexPairDataProvider();
        }
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            return new BinancePairDataProvider();
        }
        if(exchangeSpecs instanceof BittrexExchangeSpecs) {
            return new BittrexPairDataProvider();
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs) {
            return new XtbPairDataProvider();
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs)
            return new BitfinexPairDataProvider();
        throw new NotImplementedException();
    }

    String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException;
    String[] getPairsApiSymbols() throws IOException;

}
