/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dawidmotyka.exchangeutils.poloniex;

import java.util.Date;

public class Transaction {

    private String pairName;

    private int globalTradeID;
    private int tradeID;
    private double rate;
    private double amount;

    //@JsonDeserialize(using = CustomJsonDateDeserializer.class)
    private Date date;
    private double total;
    private String type;

    public Transaction() {}

    public Transaction(String pairName, int tradeId, String type, double rate, double amount, double total, Date date) {
        this.pairName = pairName;
        this.tradeID = tradeId;
        this.type = type;
        this.rate = rate;
        this.amount = amount;
        this.total = total;
        this.date = date;
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public int getTradeID() {
        return tradeID;
    }

    public void setTradeID(int tradeID) {
        this.tradeID = tradeID;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getGlobalTradeID() {
        return globalTradeID;
    }

    public void setGlobalTradeID(int globalTradeID) {
        this.globalTradeID = globalTradeID;
    }
}

