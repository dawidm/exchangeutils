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

package pl.dmotyka.exchangeutils.bittrex;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
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
            return chartCandles;
        } catch (IOException e) {
            logger.log(Level.WARNING,"when getting bittrex candles for: "+symbol);
            throw new ExchangeCommunicationException(e.getClass().getSimpleName() + " when getting bittrex candles for: "+symbol);
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("1m",60,"oneMin"),
                new ChartTimePeriod("5m",300,"fiveMin"),
                new ChartTimePeriod("30m",1800,"thirtyMin"),
                new ChartTimePeriod("1h",3600,"hour"),
                new ChartTimePeriod("1d",86400,"day")
        };
    }

    private ChartTimePeriod getChartTimePeriodForSeconds(long timePeriodSeconds) throws NoSuchTimePeriodException {
        for(ChartTimePeriod chartTimePeriod : getAvailablePeriods()) {
            if(chartTimePeriod.getPeriodLengthSeconds()==timePeriodSeconds)
                return chartTimePeriod;
        }
        throw new NoSuchTimePeriodException(String.format("requested period in seconds: %d",timePeriodSeconds));
    }
}
