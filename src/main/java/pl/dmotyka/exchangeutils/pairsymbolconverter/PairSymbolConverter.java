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

package pl.dmotyka.exchangeutils.pairsymbolconverter;

import java.util.logging.Logger;

import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.exchangeutils.binance.BinanceExchangeSpecs;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeSpecs;
import pl.dmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.poloniex.PoloniexExchangeSpecs;
import pl.dmotyka.exchangeutils.xtb.XtbExchangeSpecs;

public class PairSymbolConverter { // TODO implementations should be in exchange-specific packages and it's need refactoring

    public static final Logger logger = Logger.getLogger(PairSymbolConverter.class.getName());

    public static String toFormattedString(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return new CurrencyPair(apiSymbol.split("_")[1],apiSymbol.split("_")[0]).toString();
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return new CurrencyPair(apiSymbol.split("-")[1],apiSymbol.split("-")[0]).toString();
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            return String.format("%s/%s",apiSymbolToBaseCurrencySymbol(exchangeSpecs,apiSymbol),apiSymbolToCounterCurrencySymbol(exchangeSpecs,apiSymbol));
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            return String.format("%s/%s",apiSymbolToBaseCurrencySymbol(exchangeSpecs, apiSymbol), apiSymbolToCounterCurrencySymbol(exchangeSpecs, apiSymbol));
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs) {
            String[] symbolSplit = apiSymbol.split("__");
            if (symbolSplit.length>1)
                return String.format("%s/%s", symbolSplit[0], symbolSplit[1]);
            else
                return apiSymbol;
        }
        logger.severe(("cannot api symbol to string: " + exchangeSpecs.getName()));
        return apiSymbol;
    }
    public static String toApiSymbol(ExchangeSpecs exchangeSpecs, CurrencyPair currencyPair) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return currencyPair.counter.getSymbol()+"_"+currencyPair.base.getSymbol();
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return currencyPair.counter.getSymbol()+"-"+currencyPair.base.getSymbol();
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            return currencyPair.base.getCurrencyCode() + currencyPair.counter.getCurrencyCode();
        if(exchangeSpecs instanceof  BitfinexExchangeSpecs)
            if (currencyPair.base.getCurrencyCode().length() + currencyPair.counter.getCurrencyCode().length() > 6)
                return String.format("t"+currencyPair.base.getCurrencyCode()+":"+currencyPair.counter.getCurrencyCode());
            else
                return String.format("t"+currencyPair.base.getCurrencyCode()+currencyPair.counter.getCurrencyCode());
        if(exchangeSpecs instanceof XtbExchangeSpecs) {
            return currencyPair.base.getSymbol();
        }
        logger.severe(("cannot convert currency pairs to api symbol for: " + exchangeSpecs.getName()));
        return null;
    }
    public static String apiSymbolToChartUrlSymbol(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return apiSymbol;
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return apiSymbol;
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            return apiSymbolToBaseCurrencySymbol(exchangeSpecs,apiSymbol)+"_"+apiSymbolToCounterCurrencySymbol(exchangeSpecs,apiSymbol);
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            return String.format("%s:%s",apiSymbolToBaseCurrencySymbol(exchangeSpecs, apiSymbol), apiSymbolToCounterCurrencySymbol(exchangeSpecs, apiSymbol));
        }
        logger.severe(("cannot api symbol to char url symbol: " + exchangeSpecs.getName()));
        return null;
    }

    public static String apiSymbolToCounterCurrencySymbol(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return apiSymbol.split("_")[0];
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return apiSymbol.split("-")[0];
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            if (apiSymbol.endsWith("USDT") || apiSymbol.endsWith("TUSD") || apiSymbol.endsWith("USDC")
                    || apiSymbol.endsWith("BKRW") || apiSymbol.endsWith("BUSD")
                    || apiSymbol.endsWith("IDRT"))
                return apiSymbol.substring(apiSymbol.length() - 4);
            return apiSymbol.substring(apiSymbol.length() - 3);
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            if (apiSymbol.contains(":"))
                return apiSymbol.split(":")[1];
            return apiSymbol.substring(apiSymbol.length()-3);
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs) {
            return apiSymbol.split("__")[1];
        }
        logger.severe(("cannot api symbol to counter currency symbol: " + exchangeSpecs.getName()));
        return apiSymbol;
    }
    public static String apiSymbolToBaseCurrencySymbol(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return apiSymbol.split("_")[1];
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return apiSymbol.split("-")[1];
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            if (apiSymbol.endsWith("USDT") || apiSymbol.endsWith("TUSD") || apiSymbol.endsWith("USDC")
            || apiSymbol.endsWith("BKRW") || apiSymbol.endsWith("BUSD")
                    || apiSymbol.endsWith("IDRT"))
                return apiSymbol.substring(0,apiSymbol.length()-4);
            return apiSymbol.substring(0,apiSymbol.length()-3);
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            if (apiSymbol.contains(":"))
                return apiSymbol.substring(1,apiSymbol.length()-1).split(":")[0];
            return apiSymbol.substring(1,apiSymbol.length()-3);
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs) {
            return apiSymbol.split("__")[0];
        }
        logger.severe(("cannot api symbol to base currency symbol: " + exchangeSpecs.getName()));
        return apiSymbol;
    }

    public static CurrencyPair apiSymbolToXchangeCurrencyPair(ExchangeSpecs exchangeSpecs,String apiSymbol) {
        return new CurrencyPair(
                apiSymbolToBaseCurrencySymbol(exchangeSpecs,apiSymbol),
                apiSymbolToCounterCurrencySymbol(exchangeSpecs,apiSymbol)
        );
    }

}
