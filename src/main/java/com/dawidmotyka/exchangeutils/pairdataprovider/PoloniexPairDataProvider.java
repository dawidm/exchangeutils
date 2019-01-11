package com.dawidmotyka.exchangeutils.pairdataprovider;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService;

import java.io.IOException;

public class PoloniexPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(double minVolume, String counterCurrencySymbol) throws IOException {
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                filter(entry -> entry.getValue().getBaseVolume().doubleValue()>minVolume).
                map((entry)->entry.getKey()).
                filter(pair -> pair.split("_")[0].equals(counterCurrencySymbol)).
                toArray(String[]::new);
    }
    public String[] getPairsApiSymbols() throws IOException {
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                map((entry)->entry.getKey()).
                toArray(String[]::new);
    }
}
