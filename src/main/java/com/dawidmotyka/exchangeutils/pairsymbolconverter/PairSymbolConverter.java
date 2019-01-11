package com.dawidmotyka.exchangeutils.pairsymbolconverter;

import com.dawidmotyka.exchangeutils.NotImplementedException;
import com.dawidmotyka.exchangeutils.exchangespecs.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.BittrexExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.PoloniexExchangeSpecs;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.bittrex.BittrexAdapters;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.PoloniexAdapters;

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
        return apiSymbol;
    }
    public static String toApiSymbol(ExchangeSpecs exchangeSpecs, CurrencyPair currencyPair) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return currencyPair.counter.getSymbol()+"_"+currencyPair.base.getSymbol();
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return currencyPair.counter.getSymbol()+"-"+currencyPair.base.getSymbol();
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            return BinanceAdapters.toSymbol(currencyPair);
        logger.severe(("cannot convert currency pairs to api symbol for: " + exchangeSpecs.getName()));
        return currencyPair.toString();
    }

}
