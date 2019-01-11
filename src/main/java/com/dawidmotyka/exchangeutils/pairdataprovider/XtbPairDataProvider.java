package com.dawidmotyka.exchangeutils.pairdataprovider;

import java.io.IOException;

public class XtbPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(double minVolume, String counterCurrencySymbol) throws IOException {
        return new String[0];
    }
    public String[] getPairsApiSymbols() throws IOException {
        return new String[0];
    }
}
