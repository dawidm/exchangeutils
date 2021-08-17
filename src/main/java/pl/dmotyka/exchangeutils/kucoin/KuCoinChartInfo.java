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

package pl.dmotyka.exchangeutils.kucoin;

import java.io.IOException;
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
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

/**
 * Created by dawid on 12/16/17.
 */
public class KuCoinChartInfo implements ExchangeChartInfo {

    private static final Logger logger = Logger.getLogger(KuCoinChartInfo.class.getName());

    // startAt and endAt are in seconds
    private static final String CANDLES_API_URL = "https://api.kucoin.com/api/v1/market/candles?type=%s&symbol=%s&startAt=%d&endAt=%d";
    public static final long OK_SYSTEM_CODE = 200000;

    private final PairSymbolConverter pairSymbolConverter = new KuCoinPairSymbolConverter();

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws ExchangeCommunicationException, ConnectionProblemException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String kuCoinApiPeriod = periodSecondsToKucoinPeriodType((int)timePeriodSeconds);
            String apiUrl = String.format(CANDLES_API_URL, kuCoinApiPeriod, symbol, beginTimestampSeconds, endTimestampSeconds);
            JsonNode responseNode = objectMapper.readValue(new URL(apiUrl), JsonNode.class);
            int responseCode = responseNode.get("code").asInt();
            if (responseCode != OK_SYSTEM_CODE) {
                throw new ExchangeCommunicationException("Wrong response code: " + responseCode);
            }
            JsonNode candlesNode = responseNode.get("data");
            if (!candlesNode.isArray()) {
                throw new ExchangeCommunicationException("Invalid json format: candles response isn't an array");
            }
            List<ChartCandle> candles = new ArrayList<>(candlesNode.size());
            for (JsonNode candleNode : candlesNode) {
                candles.add(new ChartCandle(candleNode.get(3).asDouble(), candleNode.get(4).asDouble(), candleNode.get(1).asDouble(), candleNode.get(2).asDouble(), candleNode.get(0).asInt()));
            }
            return candles.toArray(ChartCandle[]::new);
        } catch (UnknownHostException e) {
            throw new ConnectionProblemException("when getting poloniex candles for: "+symbol);
        }
        catch (IOException e) {
            throw new ExchangeCommunicationException("when getting poloniex candles for: "+symbol);
        }
    }

    private String periodSecondsToKucoinPeriodType(int periodSeconds) {
        return Arrays.stream(getAvailablePeriods()).
                filter(period -> period.getPeriodLengthSeconds() == periodSeconds).
                             findFirst().
                             orElseThrow(() -> new IllegalArgumentException("No such time period: " + periodSeconds)).
                             getExchangePeriodStringId();
    };

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[] {
                new ChartTimePeriod("1m",60,"1min"),
                new ChartTimePeriod("3m",3*60,"3min"),
                new ChartTimePeriod("5m",5*60,"5min"),
                new ChartTimePeriod("15m",15*60,"15min"),
                new ChartTimePeriod("30m",30*60,"30min"),
                new ChartTimePeriod("1h",60*60,"1hour"),
                new ChartTimePeriod("2h",2*60*60,"2hour"),
                new ChartTimePeriod("4h",4*60*60,"4hour"),
                new ChartTimePeriod("6h",6*60*60,"6hour"),
                new ChartTimePeriod("8h",8*60*60,"8hour"),
                new ChartTimePeriod("12h",12*60*60,"12hour"),
                new ChartTimePeriod("1d",24*60*60,"1day"),
                new ChartTimePeriod("1w",7*24*60*60,"1week")
        };
    }

}
