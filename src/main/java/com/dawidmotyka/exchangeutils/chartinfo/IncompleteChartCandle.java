package com.dawidmotyka.exchangeutils.chartinfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dawid
 */
public class IncompleteChartCandle extends ChartCandle {

    public IncompleteChartCandle(double open, long timestamp) {
        super(open, timestamp);
    }

    @Override
    public void setHigh(double high) {
        super.setHigh(high);
    }

    @Override
    public void setLow(double low) {
        super.setLow(low);
    }

    @Override
    public void setOpen(double open) {
        super.setOpen(open);
    }

    @Override
    public void setClose(double close) {
        super.setClose(close);
    }

    public void setTimestampSeconds(long timestampSeconds) {
        super.timestampSeconds=timestampSeconds;

    }
}
