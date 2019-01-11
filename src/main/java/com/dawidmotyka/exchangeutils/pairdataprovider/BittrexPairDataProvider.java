package com.dawidmotyka.exchangeutils.pairdataprovider;

import com.dawidmotyka.exchangeutils.bittrex.BittrexPairInfoProvider;

import java.io.IOException;
import java.util.Arrays;

public class BittrexPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(double minVolume, String counterCurrencySymbol) throws IOException {
        return Arrays.stream(BittrexPairInfoProvider.getAllPairsInfos()).
                filter(simplePairInfo -> simplePairInfo.getVolume()>minVolume).
                map(simplePairInfo -> simplePairInfo.getPairApiSymbol()).
                filter((pair)->pair.split("-")[0].equals(counterCurrencySymbol)).
                toArray(String[]::new);
    }
    public String[] getPairsApiSymbols() throws IOException {
        return Arrays.stream(BittrexPairInfoProvider.getAllPairsInfos()).
                map(simplePairInfo -> simplePairInfo.getPairApiSymbol()).
                toArray(String[]::new);
    }
}
