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

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

import static org.junit.jupiter.api.Assertions.*;

class Uniswap3ChartInfoTest {

    @Test
    void getCandles() throws ConnectionProblemException, ExchangeCommunicationException, NoSuchTimePeriodException {
        Uniswap3ExchangeSpecs exchangeSpecs = new Uniswap3ExchangeSpecs();
        String[] symbols = exchangeSpecs.getPairDataProvider().getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("USD", 100000)});
        int currentTimestampSec = (int) Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestampSec - 24 * 3600;
        Optional<String> optionalSymbol = Arrays.stream(symbols).filter(s -> s.equals("WETH_USD")).findAny();
        String symbol;
        symbol = optionalSymbol.orElseGet(() -> symbols[0]);
        ChartCandle[] candles = exchangeSpecs.getChartInfo().getCandles(symbol,3600, dayAgoTimestamp, currentTimestampSec);
        assertNotNull(candles);
        assertTrue(candles.length > 0);
        for (ChartCandle candle : candles) {
            assertTrue(candle.getTimestampSeconds() > 0);
            assertTrue(candle.getOpen() > 0);
            assertTrue(candle.getHigh() > 0);
            assertTrue(candle.getLow() > 0);
            assertTrue(candle.getClose() > 0);
        }
    }
}