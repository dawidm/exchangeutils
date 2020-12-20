/*
 * Cryptonose
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

package pl.dmotyka.exchangeutils.chartdataprovider;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.tradinghoursprovider.TradingHours;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartDataProviderTest {

    @Test
    void insertMissingCandlesNoHours() {
        long periodSec = 60;
        long startTimeSec = 400; // first expected candle time would be 120
        long endTimeSec = 910;
        ChartCandle[] chartCandles = new ChartCandle[] {
                new ChartCandle(1, 2, 1, 2, 600),
                new ChartCandle(1, 3, 1, 3, 660),
                new ChartCandle(1, 4, 1, 4, 720),
                // (780 is missing)
                new ChartCandle(1, 2, 1, 2, 840),
                // (900 is missing)
        };
        ChartCandle[] newChartCandles = ChartDataProvider.insertMissingCandles(chartCandles, startTimeSec, endTimeSec, periodSec);
        assertEquals(newChartCandles.length, 9);
        assertTrue(newChartCandles[0].isNoPriceChangeCandle());
        assertEquals(newChartCandles[0].getClose(), chartCandles[0].getOpen());
        assertTrue(newChartCandles[1].isNoPriceChangeCandle());
        assertEquals(newChartCandles[1].getClose(), chartCandles[0].getOpen());
        assertTrue(newChartCandles[2].isNoPriceChangeCandle());
        assertEquals(newChartCandles[2].getClose(), chartCandles[0].getOpen());
        assertEquals(newChartCandles[3], chartCandles[0]);
        assertEquals(newChartCandles[4], chartCandles[1]);
        assertEquals(newChartCandles[5], chartCandles[2]);
        assertTrue(newChartCandles[6].isNoPriceChangeCandle());
        assertEquals(newChartCandles[6].getOpen(), chartCandles[2].getClose());
        assertEquals(newChartCandles[7], chartCandles[3]);
        assertTrue(newChartCandles[8].isNoPriceChangeCandle());
        assertEquals(newChartCandles[8].getOpen(), chartCandles[3].getClose());
    }

    @Test
    void insertMissingCandlesWithHours() {
        int periodSec = 60;
        int numCandles = 30;
        int tradingEndSeconds = 1500;
        TradingHours tradingHours = new TradingHours(new TradingHours.TradingDay[]{
                new TradingHours.TradingDay(DayOfWeek.SUNDAY, 0, tradingEndSeconds),
                new TradingHours.TradingDay(DayOfWeek.MONDAY, 0, tradingEndSeconds),
                new TradingHours.TradingDay(DayOfWeek.TUESDAY, 0, tradingEndSeconds)
        });
        OffsetDateTime sunday = OffsetDateTime.of(2020,11,29,0,0,0,0, ZoneOffset.UTC);
        OffsetDateTime monday = OffsetDateTime.of(2020,11,30,0,0,0,0, ZoneOffset.UTC);
        OffsetDateTime tuesday = OffsetDateTime.of(2020,12,1,0,0,0,0, ZoneOffset.UTC);
        ChartCandle[] candles = new ChartCandle[] {
                new ChartCandle(0.1,0.2,0.3,0.4, sunday.toEpochSecond()),
                new ChartCandle(2,3,4,5, monday.toEpochSecond() + 1440),
                new ChartCandle(10,20,30,40, tuesday.toEpochSecond() + 60)
        };

        ChartCandle[] newCandles = ChartDataProvider.insertMissingCandles(candles, numCandles, periodSec, tradingHours);
        assertEquals(numCandles, newCandles.length);
        for (ChartCandle candle : newCandles)
            assertTrue(tradingHours.isInTradingHours(candle.getTimestampSeconds()));
        assertEquals(tuesday.toEpochSecond() + tradingEndSeconds - periodSec, newCandles[newCandles.length-1].getTimestampSeconds());
        assertEquals(monday.toEpochSecond() + tradingEndSeconds - 5*60, newCandles[0].getTimestampSeconds());
        assertEquals(candles[0].getClose(), newCandles[0].getOpen());
        assertEquals(candles[0].getClose(), newCandles[0].getClose());
        assertEquals(candles[1].getClose(), newCandles[5].getOpen());
        assertEquals(candles[1].getClose(), newCandles[5].getClose());
        assertEquals(candles[2].getClose(), newCandles[7].getOpen());
        assertEquals(candles[2].getClose(), newCandles[7].getClose());

    }
}