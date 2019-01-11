package com.dawidmotyka.exchangeutils.tickerprovider;

public class AskBidTicker {
    private String symbol;
    private Double ask;
    private Double bid;

    public AskBidTicker(String symbol, Double ask, Double bid) {
        this.symbol = symbol;
        this.ask = ask;
        this.bid = bid;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getAsk() {
        return ask;
    }

    public Double getBid() {
        return bid;
    }
}
