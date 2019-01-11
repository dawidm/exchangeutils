package com.dawidmotyka.exchangeutils;

/**
 * Created by dawid on 8/13/17.
 */
public class SimplePairInfo {
    private final String pairApiSymbol;
    private final double volume;

    public SimplePairInfo(String pairName, double volume) {
        this.pairApiSymbol = pairName;
        this.volume = volume;
    }

    public String getPairApiSymbol() {
        return pairApiSymbol;
    }

    public double getVolume() {
        return volume;
    }
}
