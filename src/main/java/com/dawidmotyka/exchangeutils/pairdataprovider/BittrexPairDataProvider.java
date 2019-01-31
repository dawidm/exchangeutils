package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.dawidmotyka.exchangeutils.bittrex.BittrexPairInfoProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BittrexPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        return Arrays.stream(BittrexPairInfoProvider.getAllPairsInfos()).
                filter(marketQuoteVolume->criteriaMap.containsKey(apiSymbolToCounterCurrency(marketQuoteVolume.getPairApiSymbol()))).
                filter(marketQuoteVolume -> marketQuoteVolume.getQuoteVolume()>criteriaMap.get(apiSymbolToCounterCurrency(marketQuoteVolume.getPairApiSymbol()))).
                map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).
                toArray(String[]::new);
    }
    public String[] getPairsApiSymbols() throws IOException {
        return Arrays.stream(BittrexPairInfoProvider.getAllPairsInfos()).
                map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).
                toArray(String[]::new);
    }
    private static String apiSymbolToCounterCurrency(String apiSymbol) {
        return apiSymbol.split("-")[0];
    }
}
