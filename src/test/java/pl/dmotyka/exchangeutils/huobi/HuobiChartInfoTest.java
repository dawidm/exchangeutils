/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.huobi;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HuobiChartInfoTest {

    @Test
    void getCandles() throws NoSuchTimePeriodException, ExchangeCommunicationException {
        HuobiChartInfo info = new HuobiChartInfo();
        long period = info.getAvailablePeriods()[1].getPeriodLengthSeconds();
        long timestampSec = System.currentTimeMillis()/1000;
        int minNumCandles = 10;
        long beginTimestamp = timestampSec - period * 10 - timestampSec % period;
        ChartCandle[] candles = info.getCandles("ethbtc", period, beginTimestamp, timestampSec);
        assertTrue(candles.length >= minNumCandles);
        assertTrue(candles[0].getTimestampSeconds() > 946684800);
        assertTrue(timestampSec - candles[0].getTimestampSeconds() > 0);
        assertTrue(candles[0].getOpen() != 0);
    }
}