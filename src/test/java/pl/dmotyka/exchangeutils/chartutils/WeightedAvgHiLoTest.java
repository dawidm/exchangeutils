/*
 * Cryptonose
 *
 * Copyright © 2019-2022 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.chartutils;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

import static org.junit.jupiter.api.Assertions.*;

class WeightedAvgHiLoTest {

    @Test
    void calcValue() {
        ChartCandle[] candles = new ChartCandle[] {
                        new ChartCandle(10, 5, 7, 7, 0), //avg hi-lo = 5
                        new ChartCandle(15, 5, 7, 7, 0), //avg hi-lo = 10
                        new ChartCandle(12, 5, 7, 7, 0), //avg hi-lo = 7
                        new ChartCandle(14, 6, 7, 7, 0), //avg hi-lo = 8
        }; // weights would be 1,2,3,4
        double result = new WeightedAvgHiLo().calcValue(candles, 4);
        // 5*1 + 10*2 + 7*3 + 8*4 / (1+2+3+4) = 7.8
        assertEquals(7.8, result);
    }
}