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

package pl.dmotyka.exchangeutils.binance;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

/**
 * Created by dawid on 12/4/17.
 */
class BinanceChartInfoNew implements ExchangeChartInfo {

    private final Logger logger = Logger.getLogger(BinanceChartInfoNew.class.getName());

    private static final String KLINES_ENDPOINT = "/api/v3/klines";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws ExchangeCommunicationException, NoSuchTimePeriodException, ConnectionProblemException {
        try{
            String url = new URL(BinanceApiSpecs.getFullEndpointUrl(KLINES_ENDPOINT)) + createEndpointParameters(symbol, timePeriodSeconds, beginTimestampSeconds * 1000, endTimestampSeconds * 1000);
            JsonNode klinesNode = objectMapper.readValue(new URL(url), JsonNode.class);
            List<ChartCandle> candlesList = new ArrayList<>(klinesNode.size());
            for (JsonNode klineNode : klinesNode) {
                candlesList.add(new ChartCandle(
                        klineNode.get(2).asDouble(),
                        klineNode.get(3).asDouble(),
                        klineNode.get(1).asDouble(),
                        klineNode.get(4).asDouble(),
                        klineNode.get(0).asLong()/1000));
            }
            return candlesList.toArray(ChartCandle[]::new);
        } catch (SocketException | UnknownHostException e) {
            throw new ConnectionProblemException("when getting binance klines for " + symbol, e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting binance klines for " + symbol, e);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("m1",60,"1m"),
                new ChartTimePeriod("m1",5*60,"5m"),
                new ChartTimePeriod("m15",15*60,"15m"),
                new ChartTimePeriod("m30",30*60,"30m"),
                new ChartTimePeriod("h1",60*60,"1h"),
                new ChartTimePeriod("h2",2*60*60,"2h"),
                new ChartTimePeriod("h4",4*60*60,"4h"),
                new ChartTimePeriod("h6",6*60*60,"6h"),
                new ChartTimePeriod("h12",12*60*60,"12h"),
                new ChartTimePeriod("d1",24*60*60,"1d"),
                new ChartTimePeriod("d3",3*24*60*60,"3d"),
                new ChartTimePeriod("w1",7*24*60*60,"1e"),
                new ChartTimePeriod("M1",30*24*60*60,"1M"),
        };
    }

    private String createEndpointParameters(String symbol, long periodSeconds, long startTimeMs, long endTimeMs) throws NoSuchTimePeriodException{
        return String.format("?symbol=%s&interval=%s&startTime=%d&endTime=%d",
                symbol,
                Arrays.stream(getAvailablePeriods()).filter(ctp -> ctp.getPeriodLengthSeconds()==periodSeconds).findAny().orElseThrow(() -> new NoSuchTimePeriodException("for time: " + periodSeconds)).getExchangePeriodStringId(),
                startTimeMs, endTimeMs);
    }

}
