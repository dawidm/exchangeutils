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

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.thegraphdex.DexCurrencyPair;

class Uniswap3PairSymbolConverter implements PairSymbolConverter {

    private final Uniswap3PairDataProvider uniswap3PairDataProvider;

    public Uniswap3PairSymbolConverter(Uniswap3PairDataProvider uniswap3PairDataProvider) {
        this.uniswap3PairDataProvider = uniswap3PairDataProvider;
    }

    @Override
    public String toFormattedString(String apiSymbol) {
        return String.format("%s/%s", apiSymbolToBaseCurrencySymbol(apiSymbol), apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    @Override
    public String toApiSymbol(CurrencyPair currencyPair) {
        for (DexCurrencyPair dexCurrencyPair : uniswap3PairDataProvider.getDexCurrencyPairMap().values()) {
            if (dexCurrencyPair.getToken0Symbol().equals(currencyPair.base.getSymbol()) && dexCurrencyPair.getToken1Symbol().equals(currencyPair.counter.getSymbol())) {
                return dexCurrencyPair.getPoolAddress();
            }
        }
        throw new IllegalStateException("No such currency pair (could happen when currency pair wasn't acquired using PairDataProvider)");
    }

    @Override
    public String apiSymbolToChartUrlSymbol(String apiSymbol) {
        throw new RuntimeException("not applicable");
    }

    @Override
    public String apiSymbolToCounterCurrencySymbol(String apiSymbol) {
        DexCurrencyPair cp = uniswap3PairDataProvider.getDexCurrencyPairMap().get(apiSymbol);
        if (cp == null) {
            throw new IllegalStateException("No such symbol (pool) (could happen when currency pair wasn't acquired using PairDataProvider)");
        }
        return cp.getToken1Symbol();
    }

    @Override
    public String apiSymbolToBaseCurrencySymbol(String apiSymbol) {
        DexCurrencyPair cp = uniswap3PairDataProvider.getDexCurrencyPairMap().get(apiSymbol);
        if (cp == null) {
            throw new IllegalStateException("No such symbol (pool) (could happen when currency pair wasn't acquired using PairDataProvider)");
        }
        return cp.getToken0Symbol();
    }

    @Override
    public CurrencyPair apiSymbolToXchangeCurrencyPair(String apiSymbol) {
        return new CurrencyPair(apiSymbolToBaseCurrencySymbol(apiSymbol), apiSymbolToCounterCurrencySymbol(apiSymbol));
    }
}
