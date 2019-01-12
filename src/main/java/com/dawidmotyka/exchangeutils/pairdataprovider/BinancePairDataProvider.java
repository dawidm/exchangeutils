package com.dawidmotyka.exchangeutils.pairdataprovider;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class BinancePairDataProvider implements PairDataProvider {

    private static final Logger logger = Logger.getLogger(BinancePairDataProvider.class.getName());

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        BinanceMarketDataService binanceMarketDataService = new BinanceMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(BinanceExchange.class)));
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        CurrencyPair[] pairs = binanceMarketDataService.tickerAllPrices().stream().
                map(binanceSymbolPrice -> binanceSymbolPrice.getCurrencyPair()).
                filter(currencyPair -> criteriaMap.containsKey(currencyPair.counter.getSymbol())).toArray(CurrencyPair[]::new);
        ArrayList<String> filteredPairs = new ArrayList<>(pairs.length);
        logger.fine("filtering currency pairs...");
        for(CurrencyPair currentCurrencyPair : pairs) {
            BinanceTicker24h binanceTicker24h = binanceMarketDataService.ticker24h(currentCurrencyPair);
            if(binanceTicker24h==null) {
                logger.warning("binanceTicker24h for " + currentCurrencyPair.toString() + " is null");
                continue;
            }
            if (binanceTicker24h.getQuoteVolume().doubleValue() > criteriaMap.get(currentCurrencyPair.counter.getSymbol()))
                filteredPairs.add(BinanceAdapters.toSymbol(currentCurrencyPair));
        }
        return filteredPairs.toArray(new String[filteredPairs.size()]);
    }

    @Override
    public String[] getPairsApiSymbols() throws IOException {
        BinanceMarketDataService binanceMarketDataService = new BinanceMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(BinanceExchange.class)));
        return binanceMarketDataService.tickerAllPrices().stream().
                map(binanceSymbolPrice -> binanceSymbolPrice.getCurrencyPair()).
                map(currencyPair -> BinanceAdapters.toSymbol(currencyPair)).
                toArray(String[]::new);
    }
}
