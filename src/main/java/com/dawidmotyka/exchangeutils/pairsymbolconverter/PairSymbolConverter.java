package com.dawidmotyka.exchangeutils.pairsymbolconverter;

import com.dawidmotyka.exchangeutils.exchangespecs.*;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.bitfinex.v1.BitfinexAdapters;
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
            return BinanceAdapters.adaptSymbol(apiSymbol).toString();
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
            return BinanceAdapters.toSymbol(currencyPair);
        if(exchangeSpecs instanceof  BitfinexExchangeSpecs)
            return String.format("t"+currencyPair.base.getSymbol()+currencyPair.counter.getSymbol());
        logger.severe(("cannot convert currency pairs to api symbol for: " + exchangeSpecs.getName()));
        return null;
    }
    public static String apiSymbolToChartUrlSymbol(ExchangeSpecs exchangeSpecs, String apiSymbol) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return apiSymbol;
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return apiSymbol;
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            CurrencyPair adaptedSymbol = BinanceAdapters.adaptSymbol(apiSymbol);
            return adaptedSymbol.base.getSymbol()+"_"+adaptedSymbol.counter.getSymbol();
        }
        if(exchangeSpecs instanceof BitfinexExchangeSpecs) {
            CurrencyPair adaptedSymbol = BitfinexAdapters.adaptCurrencyPair(apiSymbol);
            return adaptedSymbol.base.getSymbol()+":"+adaptedSymbol.counter.getSymbol();
        }
        logger.severe(("cannot api symbol to char url symbol: " + exchangeSpecs.getName()));
        return null;
    }

}
