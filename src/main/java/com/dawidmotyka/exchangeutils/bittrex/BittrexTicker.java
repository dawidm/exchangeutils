/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.bittrex;

import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dawid on 8/13/17.
 */
public class BittrexTicker {

    private static final String TICKER_API_URL = "https://bittrex.com/api/v1.1/public/getticker?market=";
    private static final String SUMMARIES_API_URL = "https://bittrex.com/api/v1.1/public/getmarketsummaries";

    public static Ticker get(String pair) throws IOException {
        String stringUrl=TICKER_API_URL+pair;
        URL url = new URL(stringUrl);
        JsonNode jsonNode = new ObjectMapper().readValue(url, JsonNode.class);
        JsonNode success = jsonNode.get("success");
        if (success != null && success.asBoolean() == true && jsonNode.get("result") != null) {
            return new Ticker(pair,jsonNode.get("result").get("Last").asDouble(),System.currentTimeMillis()/1000);
        } else
            throw new IOException("Unexpected server response");
    }

    public static Ticker[] getAllFromSummaries() throws IOException {
        List<Ticker> tickerList = new LinkedList<>();
        String stringUrl=SUMMARIES_API_URL;
        URL url = new URL(stringUrl);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(url, JsonNode.class);
        JsonNode success = jsonNode.get("success");
        if (success != null && success.asBoolean() == true && jsonNode.get("result") != null) {
            ArrayNode resultArrayNode = (ArrayNode) jsonNode.get("result");
            for(JsonNode summaryNode : resultArrayNode) {
                tickerList.add(
                        new Ticker(
                                summaryNode.get("MarketName").asText(),
                                summaryNode.get("Last").asDouble(),
                                System.currentTimeMillis()/1000
                        )
                );
            }
        return tickerList.toArray(new Ticker[tickerList.size()]);
        } else
            throw new IOException("Unexpected server response");
    }
}
