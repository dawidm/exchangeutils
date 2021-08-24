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

package pl.dmotyka.exchangeutils.pairdataprovider;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import pl.dmotyka.exchangeutils.bittrex.BittrexPairDataProvider;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.poloniex.PoloniexPairDataProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class PairDataProviderTest {

    PairDataProvider binancePairDataProvider=new BinanceExchangeSpecs().getPairDataProvider();
    PoloniexPairDataProvider poloniexPairDataProvider=new PoloniexPairDataProvider();
    BittrexPairDataProvider bittrexPairDataProvider=new BittrexPairDataProvider();

    @Test
    void getPairsApiSymbolsBinance() throws ExchangeCommunicationException, ConnectionProblemException {
        String[] symbols = binancePairDataProvider.getPairsApiSymbols();
        assertNotNull(symbols);
        assertTrue(symbols.length > 0);
    }

    @Test
    void getPairsApiSymbolsWithCriteriaBinance() throws ExchangeCommunicationException, ConnectionProblemException {
        String[] pairs = binancePairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("BNB",1)});
        assertTrue(pairs!=null && pairs.length > 0);
        boolean hasBtcPair = false;
        boolean hasBnbPair = false;
        for (String pair : pairs) {
            if (pair.toUpperCase().endsWith("BTC")) {
                hasBtcPair = true;
            }
            if (pair.toUpperCase().endsWith("BNB")) {
                hasBnbPair = true;
            }
            assertTrue(pair.toUpperCase().endsWith("BTC") || pair.toUpperCase().endsWith("BNB"));
        }
        assertTrue(hasBtcPair && hasBnbPair);
    }

    @Test
    void getPairsApiSymbolsPoloniex() throws ExchangeCommunicationException, ConnectionProblemException {
        String[] pairs = poloniexPairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("USDT",1)});
        assertTrue(pairs!=null);
        String allPairs = Arrays.stream(pairs).collect(Collectors.joining());
        assertTrue(allPairs.toUpperCase().contains("BTC"));
        assertTrue(allPairs.toUpperCase().contains("USDT"));
        pairs = poloniexPairDataProvider.getPairsApiSymbols();
        assertTrue(pairs!=null &pairs.length>0);
    }

    @Test
    void getPairsApiSymbolsBittrex() throws ExchangeCommunicationException, ConnectionProblemException {
        String[] pairs = bittrexPairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("ETH",1)});
        assertTrue(pairs!=null);
        String allPairs = Arrays.stream(pairs).collect(Collectors.joining());
        assertTrue(allPairs.toUpperCase().contains("BTC"));
        assertTrue(allPairs.toUpperCase().contains("ETH"));
        pairs = bittrexPairDataProvider.getPairsApiSymbols();
        assertTrue(pairs!=null &pairs.length>0);
    }
}