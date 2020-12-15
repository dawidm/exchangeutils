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

package pl.dmotyka.exchangeutils.bitfinex;

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class BitfinexPairSymbolConverter implements PairSymbolConverter {

    @Override
    public String toFormattedString(String apiSymbol) {
        return String.format("%s/%s",apiSymbolToBaseCurrencySymbol(apiSymbol), apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    @Override
    public String toApiSymbol(CurrencyPair currencyPair) {
        if (currencyPair.base.getCurrencyCode().length() + currencyPair.counter.getCurrencyCode().length() > 6)
            return String.format("t"+currencyPair.base.getCurrencyCode()+":"+currencyPair.counter.getCurrencyCode());
        else
            return String.format("t"+currencyPair.base.getCurrencyCode()+currencyPair.counter.getCurrencyCode());
    }

    @Override
    public String apiSymbolToChartUrlSymbol(String apiSymbol) {
        return String.format("%s:%s",apiSymbolToBaseCurrencySymbol(apiSymbol), apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    @Override
    public String apiSymbolToCounterCurrencySymbol(String apiSymbol) {
        if (apiSymbol.contains(":"))
            return apiSymbol.split(":")[1];
        return apiSymbol.substring(apiSymbol.length()-3);
    }

    @Override
    public String apiSymbolToBaseCurrencySymbol(String apiSymbol) {
        if (apiSymbol.contains(":"))
            return apiSymbol.substring(1,apiSymbol.length()-1).split(":")[0];
        return apiSymbol.substring(1,apiSymbol.length()-3);
    }

    @Override
    public CurrencyPair apiSymbolToXchangeCurrencyPair(String apiSymbol) {
        return new CurrencyPair(
                apiSymbolToBaseCurrencySymbol(apiSymbol),
                apiSymbolToCounterCurrencySymbol(apiSymbol)
        );
    }
}
