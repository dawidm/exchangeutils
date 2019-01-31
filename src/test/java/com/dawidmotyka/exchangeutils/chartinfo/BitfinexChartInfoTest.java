package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BitfinexChartInfoTest {

    @Test
    void getCandles() throws ExchangeCommunicationException,NoSuchTimePeriodException {
        int beginTimestamp = (int)(System.currentTimeMillis()/1000) - 50*1800;
        int endTimestamp = (int)(System.currentTimeMillis()/1000);
        ChartCandle[] chartCandles = new BitfinexChartInfo().getCandles("tBTCUSD",1800,beginTimestamp,endTimestamp);
        assertTrue(chartCandles.length>0);
        for(ChartCandle chartCandle : chartCandles) {
            assertTrue(chartCandle.getTimestampSeconds()!=0);
            assertTrue(chartCandle.getTimestampSeconds()>beginTimestamp);
            assertTrue(chartCandle.getTimestampSeconds()<endTimestamp);
        }
        chartCandles = new BitfinexChartInfo().getCandles("tETHUSD",300,beginTimestamp,endTimestamp);
        assertTrue(chartCandles.length>0);
        for(ChartCandle chartCandle : chartCandles) {
            assertTrue(chartCandle.getTimestampSeconds()!=0);
            assertTrue(chartCandle.getTimestampSeconds()>beginTimestamp);
            assertTrue(chartCandle.getTimestampSeconds()<endTimestamp);
        }
    }
}