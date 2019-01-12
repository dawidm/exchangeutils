package com.dawidmotyka.exchangeutils.pairdataprovider;

public class PairSelectionCriteria {
    private String counterCurrencySymbol;
    private double minVolume;

    public PairSelectionCriteria(String counterCurrencySymbol, double minVolume) {
        this.counterCurrencySymbol = counterCurrencySymbol;
        this.minVolume = minVolume;
    }

    public String getCounterCurrencySymbol() {
        return counterCurrencySymbol;
    }

    public void setCounterCurrencySymbol(String counterCurrencySymbol) {
        this.counterCurrencySymbol = counterCurrencySymbol;
    }

    public double getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(double minVolume) {
        this.minVolume = minVolume;
    }
}
