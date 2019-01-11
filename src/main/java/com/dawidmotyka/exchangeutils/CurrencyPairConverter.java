package com.dawidmotyka.exchangeutils;

import com.dawidmotyka.exchangeutils.exchangespecs.*;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

/**
 * Created by dawid on 8/23/17.
 */
public class CurrencyPairConverter {

    public static CurrencyPair stringToCurrencyPair(ExchangeSpecs exchangeSpecs, String pair) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs)
            return poloPairToCurrencyPair(pair);
        if(exchangeSpecs instanceof BittrexExchangeSpecs)
            return bittrexPairToCurrencyPair(pair);
        if(exchangeSpecs instanceof HitBtcExchangeSpecs)
            return hitbtcPairToCurrencyPair(pair);
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            return binancePairToCurrencyPair(pair);
        return null;
    }

    public static CurrencyPair poloPairToCurrencyPair(String pair) {
        String[] splitPair = pair.split("_");
        return new CurrencyPair(new Currency(splitPair[1]), new Currency(splitPair[0]));
    }

    public static CurrencyPair bittrexPairToCurrencyPair(String pair) {
        String[] splitPair = pair.split("-");
        return new CurrencyPair(new Currency(splitPair[1]), new Currency(splitPair[0]));
    }

    public static CurrencyPair hitbtcPairToCurrencyPair(String pair) {
        String[] splitPair = pair.split("/");
        return new CurrencyPair(new Currency(splitPair[0]), new Currency(splitPair[1]));
    }

    public static CurrencyPair binancePairToCurrencyPair(String pair) {
        String counterCurrency = pair.substring(pair.length()-3,pair.length());
        String baseCurrency;
        if(counterCurrency.equals("SDT")) {
            counterCurrency="USDT";
            baseCurrency=pair.substring(0,pair.length()-4);
        } else
            baseCurrency=pair.substring(0,pair.length()-3);
        return new CurrencyPair(new Currency(baseCurrency),new Currency(counterCurrency));
    }

    public static String binanceApiPairToUrlPair(String pair) {
        return pair.replaceFirst("BTC$","_BTC")
                .replaceFirst("BNB$","_BNB")
                .replaceFirst("USDT$","_USDT")
                .replaceFirst("ETH$","_ETH");
    }

    public static String currencyPairToBinanceCurrencyPair(CurrencyPair currencyPair) {
        return String.format("%s%s",currencyPair.base.getCurrencyCode(),currencyPair.counter.getCurrencyCode());
    }

}
