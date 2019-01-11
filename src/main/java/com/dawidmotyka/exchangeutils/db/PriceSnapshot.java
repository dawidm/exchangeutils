package com.dawidmotyka.exchangeutils.db;

/**
 * Created by dawid on 8/31/17.
 */
public class PriceSnapshot {

    private int timestamp;
    private String pair;
    private double price;

    public PriceSnapshot(int timestamp, String pair, double price) {
        this.timestamp = timestamp;
        this.pair = pair;
        this.price = price;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getPair() {
        return pair;
    }

    public double getPrice() {
        return price;
    }
}
