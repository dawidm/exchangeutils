package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dawid on 12/4/17.
 */
public class BittrexChartInfo implements ExchangeChartInfo {

    private final Logger logger = Logger.getLogger(BittrexChartInfo.class.getName());

    private static final String BITTREX_URL = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=%s&tickInterval=%s&_=%d";

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException {
        ChartTimePeriod timePeriod = getChartTimePeriodForSeconds(timePeriodSeconds);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode mainNode = objectMapper.readValue(new URL(String.format(BITTREX_URL, symbol, timePeriod.getExchangePeriodStringId(), beginTimestampSeconds*1000)), JsonNode.class);
            if (!mainNode.get("success").asText().equals("true"))
                throw new ExchangeCommunicationException("response \"success\" was not \"true\"");
            JsonNode resultNode = mainNode.get("result");
            List<ChartCandle> candlesArrayList = new ArrayList<>(resultNode.size());
            if (!resultNode.isArray())
                throw new ExchangeCommunicationException(String.format("retrieved empty array for: %s, period: %s", symbol, timePeriod));
            for (JsonNode currentCandleJsonNode : resultNode) {
                //date format: 2017-10-10T21:00:00
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    long timestamp = simpleDateFormat.parse(currentCandleJsonNode.get("T").asText()).getTime() / 1000;
                    if (timestamp > beginTimestampSeconds)
                        candlesArrayList.add(new ChartCandle(
                                        currentCandleJsonNode.get("H").asDouble(),
                                        currentCandleJsonNode.get("L").asDouble(),
                                        currentCandleJsonNode.get("O").asDouble(),
                                        currentCandleJsonNode.get("C").asDouble(),
                                        timestamp
                                )
                        );
                } catch (ParseException e) {
                    logger.log(Level.SEVERE, String.format("when parsing date and time for chart data %s", symbol), e);
                }
            }
            ChartCandle[] chartCandles = candlesArrayList.toArray(new ChartCandle[0]);
            if(timePeriodSeconds==900) {
                return mapToHigherTimePeriod(chartCandles,3);
            }
            return chartCandles;
        } catch (IOException e) {
            logger.log(Level.WARNING,"when getting bittrex candles for: "+symbol);
            throw new ExchangeCommunicationException(e.getClass().getSimpleName() + " when getting bittrex candles for: "+symbol);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        //sorted by time in seconds!
        return new ChartTimePeriod[] {
                new ChartTimePeriod("1m",60,"oneMin"),
                new ChartTimePeriod("5m",300,"fiveMin"),
                new ChartTimePeriod("15m",900,"fiveMin"),
                new ChartTimePeriod("30m",1800,"thirtyMin"),
                new ChartTimePeriod("1h",3600,"hour"),
                new ChartTimePeriod("1d",86400,"day")
        };
    }

    private ChartCandle[] mapToHigherTimePeriod(ChartCandle inputCandles[], int multiplier) {
        int numOutputCandles=inputCandles.length/multiplier;
        if(inputCandles.length%multiplier>0)
            numOutputCandles++;
        ChartCandle[] outputCandles = new ChartCandle[numOutputCandles];
        for(int i=0;i<inputCandles.length;i++) {
            if(i%multiplier==0) {
                outputCandles[i/multiplier]=new ChartCandle(inputCandles[i].getHigh(),inputCandles[i].getLow(),inputCandles[i].getOpen(),inputCandles[i].getClose(),inputCandles[i].getTimestampSeconds());
            } else {
                if (i%multiplier==multiplier-1)
                    outputCandles[i/multiplier].setClose(inputCandles[i].getClose());
                outputCandles[i/multiplier].setHigh(Math.max(outputCandles[i/multiplier].getHigh(),inputCandles[i].getHigh()));
                outputCandles[i/multiplier].setLow(Math.min(outputCandles[i/multiplier].getLow(),inputCandles[i].getLow()));
            }
        }

//        for(int i=0;i<numOutputCandles;i++) {
//            double open = inputCandles[i*multiplier].getOpen();
//            //different for last output candle
//            double close = inputCandles[i*multiplier+multiplier-1].getClose();
//            double high=Double.MAX_VALUE;
//            double low=0;
//            for (int j=i*multiplier;j<i*multiplier+multiplier;j++) {
//                if(inputCandles[j].getHigh()>high)
//                    high=inputCandles[j].getHigh();
//                if(inputCandles[j].getLow()<low)
//                    low=inputCandles[j].getLow();
//            }
//            outputCandles[i]=new ChartCandle(high,low,open,close,inputCandles[i*multiplier].getTimestampSeconds());
//        }
        return outputCandles;
    }


    private ChartTimePeriod getChartTimePeriodForSeconds(long timePeriodSeconds) throws NoSuchTimePeriodException {
        for(ChartTimePeriod chartTimePeriod : getAvailablePeriods()) {
            if(chartTimePeriod.getPeriodLengthSeconds()==timePeriodSeconds)
                return chartTimePeriod;
        }
        throw new NoSuchTimePeriodException(String.format("requested period in seconds: %d",timePeriodSeconds));
    }
}
