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

import java.util.Locale;

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

class Uniswap3PairSymbolConverter implements PairSymbolConverter {

    private Uniswap3ExchangeSpecs exchangeSpecs;
    private Uniswap3PairDataProvider pairDataProvider;

    public Uniswap3PairSymbolConverter(Uniswap3ExchangeSpecs exchangeSpecs) {
        this.exchangeSpecs = exchangeSpecs;
        pairDataProvider = (Uniswap3PairDataProvider) exchangeSpecs.getPairDataProvider();
    }

    @Override
    public String toFormattedString(String apiSymbol) {
        String address = apiSymbolToBaseCurrencySymbol(apiSymbol);
        String symbol = pairDataProvider.getTokenInfo(address).getTokenSymbol();
        if (symbol.length() > 10) {
            symbol = symbol.substring(0,10) + "...";
        }
        return String.format("%s/%s", symbol, apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    @Override
    public String toApiSymbol(CurrencyPair currencyPair) {
        return formatApiSymbol(currencyPair.base.getCurrencyCode().toLowerCase(Locale.ROOT), currencyPair.counter.getCurrencyCode());
    }

    @Override
    public String apiSymbolToChartUrlSymbol(String apiSymbol) {
        return apiSymbolToBaseCurrencySymbol(apiSymbol);
    }

    @Override
    public String apiSymbolToCounterCurrencySymbol(String apiSymbol) {
        return apiSymbol.split("_")[1];
    }

    @Override
    public String apiSymbolToBaseCurrencySymbol(String apiSymbol) {
        return apiSymbol.split("_")[0];
    }

    @Override
    public CurrencyPair apiSymbolToXchangeCurrencyPair(String apiSymbol) {
        return new CurrencyPair(apiSymbolToBaseCurrencySymbol(apiSymbol), apiSymbolToCounterCurrencySymbol(apiSymbol));
    }

    public static String formatApiSymbol(String tokenAddress, String counterSymbol) {
        return String.format("%s_%s", tokenAddress, counterSymbol);
    }
}
