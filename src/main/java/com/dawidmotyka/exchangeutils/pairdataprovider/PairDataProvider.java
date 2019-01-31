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
