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
                filter(simplePairInfo->criteriaMap.containsKey(apiSymbolToCounterCurrency(simplePairInfo.getPairApiSymbol()))).
                filter(simplePairInfo -> simplePairInfo.getVolume()>criteriaMap.get(apiSymbolToCounterCurrency(simplePairInfo.getPairApiSymbol()))).
                map(simplePairInfo -> simplePairInfo.getPairApiSymbol()).
                toArray(String[]::new);
    }
    public String[] getPairsApiSymbols() throws IOException {
        return Arrays.stream(BittrexPairInfoProvider.getAllPairsInfos()).
                map(simplePairInfo -> simplePairInfo.getPairApiSymbol()).
                toArray(String[]::new);
    }
    private static String apiSymbolToCounterCurrency(String apiSymbol) {
        return apiSymbol.split("-")[0];
    }
}
