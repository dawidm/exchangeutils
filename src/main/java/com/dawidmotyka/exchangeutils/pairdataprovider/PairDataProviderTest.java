package com.dawidmotyka.exchangeutils.pairdataprovider;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PairDataProviderTest {

    BinancePairDataProvider binancePairDataProvider=new BinancePairDataProvider();
    PoloniexPairDataProvider poloniexPairDataProvider=new PoloniexPairDataProvider();
    BittrexPairDataProvider bittrexPairDataProvider=new BittrexPairDataProvider();

    @org.junit.jupiter.api.Test
    void getPairsApiSymbolsBinance() throws IOException {
        String[] pairs = binancePairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("BNB",1)});
        assertTrue(pairs!=null);
        String allPairs = Arrays.stream(pairs).collect(Collectors.joining());
        assertTrue(allPairs.toUpperCase().contains("BTC"));
        assertTrue(allPairs.toUpperCase().contains("BNB"));
        pairs = binancePairDataProvider.getPairsApiSymbols();
        assertTrue(pairs!=null &pairs.length>0);
    }

    @org.junit.jupiter.api.Test
    void getPairsApiSymbolsPoloniex() throws IOException {
        String[] pairs = poloniexPairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("USDT",1)});
        assertTrue(pairs!=null);
        String allPairs = Arrays.stream(pairs).collect(Collectors.joining());
        assertTrue(allPairs.toUpperCase().contains("BTC"));
        assertTrue(allPairs.toUpperCase().contains("USDT"));
        pairs = poloniexPairDataProvider.getPairsApiSymbols();
        assertTrue(pairs!=null &pairs.length>0);
    }

    @org.junit.jupiter.api.Test
    void getPairsApiSymbolsBittrex() throws IOException {
        String[] pairs = bittrexPairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("ETH",1)});
        assertTrue(pairs!=null);
        String allPairs = Arrays.stream(pairs).collect(Collectors.joining());
        assertTrue(allPairs.toUpperCase().contains("BTC"));
        assertTrue(allPairs.toUpperCase().contains("ETH"));
        pairs = bittrexPairDataProvider.getPairsApiSymbols();
        assertTrue(pairs!=null &pairs.length>0);
    }
}