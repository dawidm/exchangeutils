/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.chartinfo;

import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
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