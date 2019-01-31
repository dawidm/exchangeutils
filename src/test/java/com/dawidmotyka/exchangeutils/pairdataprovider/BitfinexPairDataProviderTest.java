package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.dawidmotyka.exchangeutils.bitfinex.BitfinexPairDataProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BitfinexPairDataProviderTest {

    @Test
    void getPairsApiSymbolsFiltered() throws IOException {
        PairSelectionCriteria[] pairSelectionCriteria = new PairSelectionCriteria[] {
                new PairSelectionCriteria("BTC",100),
                new PairSelectionCriteria("USD",10000)
        };
        String[] symbols = new BitfinexPairDataProvider().getPairsApiSymbols(pairSelectionCriteria);
        assertTrue(symbols.length>0);
        for(String symbol : symbols) {
            String counter = symbol.substring(symbol.length()-3);
            assertTrue(counter.equals("BTC") ||
                    counter.equals("USD"));
        }
    }

    @Test
    void getPairsApiSymbolsAll() throws IOException {
        assertTrue(new BitfinexPairDataProvider().getPairsApiSymbols().length>0);
    }
}