/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.tools;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicksToCandlesTest {

    @Test
    void generateCandles() throws ExchangeCommunicationException {
        long periodSeconds = 3600;
        int currentTimestamp = (int) Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        double tmpPrice = 100;
        List<Ticker> tickersList = new LinkedList<>();
        for(int tmpTimestamp = dayAgoTimestamp; tmpTimestamp < currentTimestamp; tmpTimestamp += 1 + (int)(Math.random()*10)) {
            tmpPrice = tmpPrice + Math.random()*2-1;
            tickersList.add(new Ticker("TEST", tmpPrice, tmpTimestamp));
        }
        ChartCandle[] candles = TicksToCandles.generateCandles(tickersList.toArray(Ticker[]::new), periodSeconds);
        assertNotNull(candles);
        assertTrue(candles.length > 0);
        for (ChartCandle candle : candles) {
            assertTrue(candle.getOpen() > 0);
            assertTrue(candle.getClose() > 0);
            assertTrue(candle.getHigh() > 0);
            assertTrue(candle.getLow() > 0);
            assertTrue(candle.getTimestampSeconds() >= dayAgoTimestamp - periodSeconds);
        }

    }
}