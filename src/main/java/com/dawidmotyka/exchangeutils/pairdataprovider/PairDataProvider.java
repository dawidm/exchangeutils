package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.dawidmotyka.exchangeutils.NotImplementedException;
import com.dawidmotyka.exchangeutils.exchangespecs.*;

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
        throw new NotImplementedException();
    }

    String[] getPairsApiSymbols(double minVolume, String counterCurrencySymbol) throws IOException;
    String[] getPairsApiSymbols() throws IOException;

}
