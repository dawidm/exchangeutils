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

package pl.dmotyka.exchangeutils.huobi;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuobiPairDataProviderTest {

    @Test
    void getPairsApiSymbols() throws IOException {
        String COUNTER_CURRENCY = "btc";
        String[] symbols = new HuobiPairDataProvider().getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria(COUNTER_CURRENCY, 1)});
        assertNotNull(symbols);
        assertTrue(symbols.length > 0);
        for (String symbol : symbols)
            assertTrue(symbol.endsWith(COUNTER_CURRENCY));
        String[] symbols2 = new HuobiPairDataProvider().getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria(COUNTER_CURRENCY, 500)});
        assertTrue(symbols2.length < symbols.length);
        String[] symbolsAll = new HuobiPairDataProvider().getPairsApiSymbols();
        assertTrue(symbolsAll.length > symbols.length);
    }
}