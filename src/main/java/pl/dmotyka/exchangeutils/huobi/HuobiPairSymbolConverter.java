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

package pl.dmotyka.exchangeutils.huobi;

import java.util.Set;

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class HuobiPairSymbolConverter implements PairSymbolConverter {

    private static final Set<String> COUNTER_CURRENCY_SET = Set.of("btc", "usdt", "husd", "eth", "ht", "trx");

    @Override
    public String toFormattedString(String apiSymbol) {
        return new CurrencyPair(apiSymbolToBaseCurrencySymbol(apiSymbol),apiSymbolToCounterCurrencySymbol(apiSymbol)).toString();
    }

    @Override
    public String toApiSymbol(CurrencyPair currencyPair) {
        return currencyPair.base.getSymbol().toLowerCase()+currencyPair.counter.getSymbol().toLowerCase();
    }

    @Override
    public String apiSymbolToChartUrlSymbol(String apiSymbol) {
        return apiSymbolToBaseCurrencySymbol(apiSymbol) + "_" +apiSymbolToCounterCurrencySymbol(apiSymbol);
    }

    @Override
    public String apiSymbolToCounterCurrencySymbol(String apiSymbol) {
        for (String counter : COUNTER_CURRENCY_SET) {
            if (apiSymbol.endsWith(counter)) {
                return counter;
            }
        }
        throw new IllegalArgumentException(apiSymbol + "has an unknown counter currency");
    }

    @Override
    public String apiSymbolToBaseCurrencySymbol(String apiSymbol) {
        for (String counter : COUNTER_CURRENCY_SET) {
            if (apiSymbol.endsWith(counter)) {
                return apiSymbol.substring(0, apiSymbol.length()-counter.length());
            }
        }
        throw new IllegalArgumentException(apiSymbol + "has an unknown counter currency");
    }

    @Override
    public CurrencyPair apiSymbolToXchangeCurrencyPair(String apiSymbol) {
        return new CurrencyPair(
                apiSymbolToBaseCurrencySymbol(apiSymbol),
                apiSymbolToCounterCurrencySymbol(apiSymbol)
        );
    }
}
