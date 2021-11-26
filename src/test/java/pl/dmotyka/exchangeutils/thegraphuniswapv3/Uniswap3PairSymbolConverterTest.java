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

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Uniswap3PairSymbolConverterTest {

    @Test
    void testConvertingPairs() throws ConnectionProblemException, ExchangeCommunicationException {
        String ethAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
        String apiSymbol = ethAddress + "_USD";
        Uniswap3ExchangeSpecs exchangeSpecs = new Uniswap3ExchangeSpecs();
        PairDataProvider pairDataProvider = exchangeSpecs.getPairDataProvider();
        //String[] pools = pairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria(ethAddress, 100)});
        PairSymbolConverter converter = exchangeSpecs.getPairSymbolConverter();
        CurrencyPair cp = converter.apiSymbolToXchangeCurrencyPair(apiSymbol);
        assertEquals(converter.toApiSymbol(cp), apiSymbol);
        assertEquals(ethAddress, converter.apiSymbolToBaseCurrencySymbol(apiSymbol));
        assertEquals("USD", converter.apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

}