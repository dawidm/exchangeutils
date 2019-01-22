package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BitfinexChartInfoTest {

    @Test
    void getCandles() throws ExchangeCommunicationException,NoSuchTimePeriodException {
        int beginTimestamp = (int)(System.currentTimeMillis()/1000) - 50*60;
        int endTimestamp = (int)(System.currentTimeMillis()/1000);
        ChartCandle[] chartCandles = new BitfinexChartInfo().getCandles("tBTCUSD",60,beginTimestamp,endTimestamp);
        assertTrue(chartCandles.length>0);
        for(ChartCandle chartCandle : chartCandles) {
            assertTrue(chartCandle.timestampSeconds!=0);
            assertTrue(chartCandle.timestampSeconds>beginTimestamp);
            assertTrue(chartCandle.timestampSeconds<endTimestamp);
        }
        chartCandles = new BitfinexChartInfo().getCandles("tETHUSD",300,beginTimestamp,endTimestamp);
        assertTrue(chartCandles.length>0);
        for(ChartCandle chartCandle : chartCandles) {
            assertTrue(chartCandle.timestampSeconds!=0);
            assertTrue(chartCandle.timestampSeconds>beginTimestamp);
            assertTrue(chartCandle.timestampSeconds<endTimestamp);
        }
    }
}