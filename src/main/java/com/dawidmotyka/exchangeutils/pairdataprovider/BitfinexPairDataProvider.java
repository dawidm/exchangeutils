package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BitfinexPairDataProvider implements PairDataProvider {

    public static final String SYMBOLS_API_V2_URL = "https://api.bitfinex.com/v2/tickers?symbols=";

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium ->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        return Arrays.stream(getQuoteVolumes()).filter(marketQuoteVolume -> {
                Double minVol=criteriaMap.get(symbolToCounterSymbol(marketQuoteVolume.getPairApiSymbol()));
                return minVol!=null && marketQuoteVolume.getQuoteVolume()>minVol;
        }).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    @Override
    public String[] getPairsApiSymbols() throws IOException {
        return Arrays.stream(getQuoteVolumes()).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    private MarketQuoteVolume[] getQuoteVolumes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode symbolsJsonNode = objectMapper.readValue(new URL(SYMBOLS_API_V2_URL+"ALL"), JsonNode.class);
        List<MarketQuoteVolume> marketQuoteVolumes = new ArrayList<>(symbolsJsonNode.size());
        for(JsonNode symbolJsonNode : symbolsJsonNode) {
            String marketSymbol = symbolJsonNode.get(0).asText();
            double volume = symbolJsonNode.get(8).asDouble(0);
            double lastPrice = symbolJsonNode.get(7).asDouble(0);
            marketQuoteVolumes.add(new MarketQuoteVolume(marketSymbol,volume*lastPrice));
        }
        return marketQuoteVolumes.toArray(new MarketQuoteVolume[marketQuoteVolumes.size()]);
    }

    private String symbolToCounterSymbol(String symbol) {
        return symbol.substring(symbol.length()-3);
    }
}
