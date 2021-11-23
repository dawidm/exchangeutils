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

package thegraphuniswapv3;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

import static org.junit.jupiter.api.Assertions.*;

class Uniswap3PairSymbolConverterTest {

    @Test
    void testConvertingPairs() throws ConnectionProblemException, ExchangeCommunicationException {
        String ethAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
        Uniswap3ExchangeSpecs exchangeSpecs = new Uniswap3ExchangeSpecs();
        PairDataProvider pairDataProvider = exchangeSpecs.getPairDataProvider();
        String[] pools = pairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria(ethAddress, 100)});
        PairSymbolConverter converter = exchangeSpecs.getPairSymbolConverter();
        assertEquals("WETH", converter.apiSymbolToCounterCurrencySymbol(pools[0]));
        assertNotEquals("WETH", converter.apiSymbolToBaseCurrencySymbol(pools[0]));
        assertTrue(converter.toFormattedString(pools[0]).endsWith("/WETH"));
        String base = converter.apiSymbolToBaseCurrencySymbol(pools[0]);
        String counter = converter.apiSymbolToCounterCurrencySymbol(pools[0]);
        assertEquals(converter.toApiSymbol(new CurrencyPair(base, counter)), pools[0]);
    }

}