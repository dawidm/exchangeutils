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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tools.TicksToCandles;

class Uniswap3ChartInfo implements ExchangeChartInfo {

    private Uniswap3PairDataProvider pairDataProvider;

    public Uniswap3ChartInfo(Uniswap3PairDataProvider pairDataProvider) {
        this.pairDataProvider = pairDataProvider;
    }

    @Override
    public ChartCandle[] getCandles(String apiSymbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException, ConnectionProblemException {
        if (timePeriodSeconds > (long)Integer.MAX_VALUE || beginTimestampSeconds > (long)Integer.MAX_VALUE || endTimestampSeconds > (long)Integer.MAX_VALUE) {
            throw new IllegalArgumentException("At least one of time values is too big (they should be in seconds)");
        }
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        String[] pools = pairDataProvider.getPools(new String[] {apiSymbol});
        Uniswap3SwapsQuery swapsQuery = new Uniswap3SwapsQuery(pools, (int)beginTimestampSeconds, (int)endTimestampSeconds);
        List<JsonNode> swapsJsonNodes = req.send(swapsQuery);
        List<Ticker> tickerList = new LinkedList<>();
        for (JsonNode swapsJsonNode : swapsJsonNodes) {
            tickerList.addAll(Arrays.asList(Uniswap3SwapsToTickers.generateTickers(swapsJsonNode)));
        }
        Ticker[] tickers = tickerList.stream().filter(t -> t.getPair().equals(apiSymbol)).toArray(Ticker[]::new);
        return TicksToCandles.generateCandles(tickers, timePeriodSeconds);
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("m5", 300, "m5"),
                new ChartTimePeriod("m30", 1800, "m30"),
                new ChartTimePeriod("h1", 3600, "h1"),
                new ChartTimePeriod("d1", 24*3600, "d1")
        };
    }
}
