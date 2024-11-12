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

package pl.dmotyka.exchangeutils.binance;

import java.util.Set;

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

class BinancePairSymbolConverter implements PairSymbolConverter {

    private static final Set<String> COUNTER_CURRENCY_EXCEPTIONS_SET = Set.of("USDT", "TUSD", "USDC", "BKRW", "BUSD", "IDRT", "FDUSD");

    @Override
    public String toFormattedString(String apiSymbol) {
        return String.format("%s/%s",apiSymbolToBaseCurrencySymbol(apiSymbol),apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    @Override
    public String toApiSymbol(CurrencyPair currencyPair) {
        return currencyPair.base.getCurrencyCode() + currencyPair.counter.getCurrencyCode();
    }

    @Override
    public String apiSymbolToChartUrlSymbol(String apiSymbol) {
        return apiSymbolToBaseCurrencySymbol(apiSymbol)+"_"+apiSymbolToCounterCurrencySymbol(apiSymbol);
    }

    @Override
    public String apiSymbolToCounterCurrencySymbol(String apiSymbol) {
        int endsWithExceptionCounterResult = endsWithExceptionCounter(apiSymbol);
        if (endsWithExceptionCounterResult > 0)
            return apiSymbol.substring(apiSymbol.length()-endsWithExceptionCounterResult);
        return apiSymbol.substring(apiSymbol.length() - 3);
    }

    @Override
    public String apiSymbolToBaseCurrencySymbol(String apiSymbol) {
        int endsWithExceptionCounterResult = endsWithExceptionCounter(apiSymbol);
        if (endsWithExceptionCounterResult > 0)
            return apiSymbol.substring(0,apiSymbol.length()-endsWithExceptionCounterResult);
        return apiSymbol.substring(0,apiSymbol.length()-3);
    }

    @Override
    public CurrencyPair apiSymbolToXchangeCurrencyPair(String apiSymbol) {
        return new CurrencyPair(
                apiSymbolToBaseCurrencySymbol(apiSymbol),
                apiSymbolToCounterCurrencySymbol(apiSymbol)
        );
    }

    private int endsWithExceptionCounter(String pairSymbol) {
        for(String counter : COUNTER_CURRENCY_EXCEPTIONS_SET) {
            if (pairSymbol.endsWith(counter))
                return counter.length();
        }
        return 0;
    }
}
