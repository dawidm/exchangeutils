package com.dawidmotyka.exchangeutils.pairdataprovider;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PoloniexPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                filter(entry -> criteriaMap.containsKey(apiSymbolToCounterCurrency(entry.getKey()))).
                filter(entry -> entry.getValue().getBaseVolume().doubleValue()>criteriaMap.get(apiSymbolToCounterCurrency(entry.getKey()))).
                map((entry)->entry.getKey()).
                toArray(String[]::new);
    }
    public String[] getPairsApiSymbols() throws IOException {
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                map((entry)->entry.getKey()).
                toArray(String[]::new);
    }
    private static String apiSymbolToCounterCurrency(String apiSymbol) {
        return apiSymbol.split("_")[0];
    }
}
