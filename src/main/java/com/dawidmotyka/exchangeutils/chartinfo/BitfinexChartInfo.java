package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BitfinexChartInfo  implements ExchangeChartInfo {

    //parameters: period,symbol,begin_timestamp_ms,end_timestamp_ms
    public static final String CANDLES_API_V2_URL = "https://api.bitfinex.com/v2/candles/trade:%s:%s/hist?start=%d&end=%d&limit=5000&sorted=1";

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException {
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
                throw new ExchangeCommunicationException("got 0 candles");
            }
        } catch (IOException e) {
            throw new ExchangeCommunicationException(e.getClass().getName() + " " + e.getMessage());
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
