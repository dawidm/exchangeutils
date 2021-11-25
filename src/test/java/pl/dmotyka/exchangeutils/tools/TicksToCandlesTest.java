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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;
import pl.dmotyka.exchangeutils.thegraphuniswapv3.Uniswap3ExchangeSpecs;
import pl.dmotyka.exchangeutils.thegraphuniswapv3.Uniswap3SwapsQuery;
import pl.dmotyka.exchangeutils.thegraphuniswapv3.Uniswap3SwapsToTickers;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

import static org.junit.jupiter.api.Assertions.*;

class TicksToCandlesTest {

    @Test
    void generateCandles() throws ExchangeCommunicationException {
        long periodSeconds = 3600;
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        PairSymbolConverter pairSymbolConverter = new Uniswap3ExchangeSpecs().getPairSymbolConverter();
        // USDC/WETH and WETH/USDT pools
        String[] pools = new String[] {"0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8", "0x11b815efb8f581194ae79006d24e0d814b7697f6"};
        int currentTimestamp = (int) Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        Uniswap3SwapsQuery swapsQuery = new Uniswap3SwapsQuery(pools, dayAgoTimestamp, currentTimestamp);
        List<JsonNode> resultNodes = req.send(swapsQuery);
        List<Ticker> tickersList = new LinkedList<>();
        for(JsonNode resultNode : resultNodes) {
            Ticker[] tickers = Uniswap3SwapsToTickers.generateTickers(resultNode);
            tickersList.addAll(Arrays.asList(tickers));
        }
        ChartCandle[] candles = TicksToCandles.generateCandles(tickersList.stream().filter(t -> t.getPair().startsWith("WETH_")).toArray(Ticker[]::new), periodSeconds);
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