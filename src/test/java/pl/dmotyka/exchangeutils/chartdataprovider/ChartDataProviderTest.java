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

package pl.dmotyka.exchangeutils.chartdataprovider;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartDataProviderTest {

    @Test
    void insertMissingCandles() {
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
}