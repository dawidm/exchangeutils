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

package pl.dmotyka.exchangeutils.bitfinex;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

public class BitfinexChartInfo  implements ExchangeChartInfo {

    //parameters: period,symbol,begin_timestamp_ms,end_timestamp_ms
    public static final String CANDLES_API_V2_URL = "https://api.bitfinex.com/v2/candles/trade:%s:%s/hist?start=%d&end=%d&limit=5000&sorted=1";

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException, ConnectionProblemException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode candlesJsonNode = objectMapper.readValue(new URL(buildQueryUrl(symbol, (int) timePeriodSeconds, (int) beginTimestampSeconds, (int) endTimestampSeconds)), JsonNode.class);
            if(candlesJsonNode.size()>0) {
                List<ChartCandle> chartCandles = new ArrayList<>(candlesJsonNode.size());
                for(JsonNode singleCandleNode : candlesJsonNode) {
                    //t,o,c,h,l,v
                    chartCandles.add(new ChartCandle(
                            singleCandleNode.get(3).asDouble(),
                            singleCandleNode.get(4).asDouble(),
                            singleCandleNode.get(1).asDouble(),
                            singleCandleNode.get(2).asDouble(),
                            singleCandleNode.get(0).asLong(0)/1000));
                }
                return chartCandles.toArray(new ChartCandle[chartCandles.size()]);
            } else {
                return new ChartCandle[0];
            }
        } catch (UnknownHostException e) {
            throw new ConnectionProblemException("when getting chart data for " + symbol,e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting chart data for " + symbol,e);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("1m",60,"1m"),
                new ChartTimePeriod("5m", 300, "5m"),
                new ChartTimePeriod("15m", 900, "15m"),
                new ChartTimePeriod("30m", 1800, "30m"),
                new ChartTimePeriod("1h",60*60, "1h"),
                new ChartTimePeriod("3h",3*60*60,"3h"),
                new ChartTimePeriod("6h",6*60*60,"6h"),
                new ChartTimePeriod("12h",12*60*60,"12h"),
                new ChartTimePeriod("1D",24*60*60,"1D"),
                new ChartTimePeriod("7D",7*24*60*60,"7D"),
                new ChartTimePeriod("14D",14*24*60*60,"14D"),
                new ChartTimePeriod("1M",30*24*60*60,"1M")
        };
    }

    private String buildQueryUrl(String symbol, int timePeriodSeconds, int beginSeconds, int endSeconds) throws NoSuchTimePeriodException {
        for(ChartTimePeriod chartTimePeriod : getAvailablePeriods()) {
            if(chartTimePeriod.getPeriodLengthSeconds()==timePeriodSeconds) {
                String periodSymbol=chartTimePeriod.getExchangePeriodStringId();
                return String.format(CANDLES_API_V2_URL,periodSymbol,symbol,(long)beginSeconds*1000,(long)endSeconds*1000);
            }
        }
        throw new NoSuchTimePeriodException(symbol+":"+timePeriodSeconds+"s");
    }

}
