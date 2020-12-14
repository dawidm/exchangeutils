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

package pl.dmotyka.exchangeutils.exchangespecs;

import java.util.List;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import pl.dmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExchangesPluginsLoaderTest {

    @Test
    void getAllExchangeSpecs() {
        List<ExchangeSpecs> es = new ExchangesPluginsLoader().getAllExchangeSpecs();
        assertTrue(es.size() > 0);
        assertTrue(containExchangeSpecs(es, BinanceExchangeSpecs.class));
        assertTrue(containExchangeSpecs(es, PoloniexExchangeSpecs.class));
        assertTrue(containExchangeSpecs(es, BitfinexExchangeSpecs.class));
    }

    private boolean containExchangeSpecs(List<ExchangeSpecs> list, Class<? extends ExchangeSpecs> specsClass) {
        for (ExchangeSpecs es : list)
            if (es.getClass().equals(specsClass))
                return true;
        return false;
    }
}