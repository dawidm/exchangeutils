package com.dawidmotyka.exchangeutils.pairsymbolconverter;

import com.dawidmotyka.exchangeutils.exchangespecs.*;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.logging.Logger;

public class PairSymbolConverter {

    public static final Logger logger = Logger.getLogger(PairSymbolConverter.class.getName());

    public static String toFormattedString(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return new CurrencyPair(apiSymbol.split("_")[1],apiSymbol.split("_")[0]).toString();
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return new CurrencyPair(apiSymbol.split("-")[1],apiSymbol.split("-")[0]).toString();
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            return String.format("%s/%s",apiSymbolToBaseCurrencySymbol(exchangeSpecs,apiSymbol),apiSymbolToCounterCurrencySymbol(exchangeSpecs,apiSymbol));
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            return String.format("%s/%s",apiSymbol.substring(1,apiSymbol.length()-3),apiSymbol.substring(apiSymbol.length()-3));
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
            return String.format("t"+currencyPair.base.getCurrencyCode()+currencyPair.counter.getCurrencyCode());
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
            return String.format("%s:%s",apiSymbol.substring(1,apiSymbol.length()-3),apiSymbol.substring(apiSymbol.length()-3));
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
            if (apiSymbol.endsWith("USDT") || apiSymbol.endsWith("TUSD") || apiSymbol.endsWith("USDC"))
                return apiSymbol.substring(apiSymbol.length() - 4);
            return apiSymbol.substring(apiSymbol.length() - 3);
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            return apiSymbol.substring(apiSymbol.length()-3);
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
            if (apiSymbol.endsWith("USDT") || apiSymbol.endsWith("TUSD") || apiSymbol.endsWith("USDC"))
                return apiSymbol.substring(0,apiSymbol.length()-4);
            return apiSymbol.substring(0,apiSymbol.length()-3);
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            return apiSymbol.substring(1,apiSymbol.length()-3);
        }
        logger.severe(("cannot api symbol to base currency symbol: " + exchangeSpecs.getName()));
        return apiSymbol;
    }

}
