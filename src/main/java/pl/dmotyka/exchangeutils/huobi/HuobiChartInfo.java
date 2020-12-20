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

package pl.dmotyka.exchangeutils.huobi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

/**
 * Created by dawid on 12/4/17.
 */
public class HuobiChartInfo implements ExchangeChartInfo {

    public static final String KLINE_API_URL = "https://api.huobi.pro/market/history/kline?symbol=%s&period=%s&size=%d";

    private final Logger logger = Logger.getLogger(HuobiChartInfo.class.getName());

    private Map<Long, String> periodSecToSymbolMap;
    private ObjectMapper objectMapper = new ObjectMapper();

    public HuobiChartInfo() {
        periodSecToSymbolMap = new HashMap<>();
        for (ChartTimePeriod chartTimePeriod : getAvailablePeriods()) {
            periodSecToSymbolMap.put(chartTimePeriod.getPeriodLengthSeconds(), chartTimePeriod.getExchangePeriodStringId());
        }
        logger.warning("exchange returns only last candles, endTimestampSeconds isn't supported");
    }

    // exchange returns only last candles, endTimestampSeconds isn't supported
    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException {
        long currentTimestampSec = System.currentTimeMillis()/1000;
        int numCandles = (int)Math.ceil((double)(currentTimestampSec - beginTimestampSeconds) / timePeriodSeconds);
        try {
            JsonNode klinesNode = objectMapper.readValue(new URL(getApiUrl(symbol, timePeriodSeconds, numCandles)), JsonNode.class);
            if (!klinesNode.get("status").textValue().equals("ok"))
                throw new ExchangeCommunicationException("Status returned by exchange was not \"ok\"");
            klinesNode = klinesNode.get("data");
            List<ChartCandle> candleList = new ArrayList<>(numCandles);
            for(JsonNode klineNode : klinesNode) {
                candleList.add(new ChartCandle(
                        klineNode.get("high").asDouble(),
                        klineNode.get("low").asDouble(),
                        klineNode.get("open").asDouble(),
                        klineNode.get("close").asDouble(),
                        klineNode.get("id").longValue()));
            }
            return candleList.toArray(ChartCandle[]::new);
        } catch (IOException e) {
            throw new ExchangeCommunicationException(e.getMessage());
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("m1",60,"1min"),
                new ChartTimePeriod("m5",5*60,"5min"),
                new ChartTimePeriod("m15",15*60,"15min"),
                new ChartTimePeriod("m30",30*60,"30min"),
                new ChartTimePeriod("h1",60*60,"60min"),
                new ChartTimePeriod("h4",4*60*60,"4hour"),
                new ChartTimePeriod("d1",24*60*60,"1day"),
                new ChartTimePeriod("w1",7*24*60*60,"1mon"),
                new ChartTimePeriod("M1",30*24*60*60,"1week"),
                new ChartTimePeriod("y1",365*24*60*60,"1year")
        };
    }

    private String getTimePeriodApiSymbol(long periodSec) throws NoSuchTimePeriodException {
        String periodName = periodSecToSymbolMap.get(periodSec);
        if (periodName==null)
            throw new NoSuchTimePeriodException(String.format("No such time period: %ds", periodSec));
        return periodName;
    }

    private String getApiUrl(String pair, long periodSec, int size) throws NoSuchTimePeriodException {
        return String.format(KLINE_API_URL, pair, getTimePeriodApiSymbol(periodSec), size);
    }

}
