package com.dawidmotyka.exchangeutils.pairdataprovider;

public class MarketQuoteVolume {
    private final String pairApiSymbol;
    private final double quoteVolume;

    public MarketQuoteVolume(String pairApiSymbol, double quoteVolume) {
        this.pairApiSymbol = pairApiSymbol;
        this.quoteVolume = quoteVolume;
    }

    public String getPairApiSymbol() {
        return pairApiSymbol;
    }

    public double getQuoteVolume() {
        return quoteVolume;
    }
}
