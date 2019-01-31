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

import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.pairdataprovider.MarketQuoteVolume;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by dawid on 8/13/17.
 */
public class BittrexPairInfoProvider {

    private static final String MARKET_SUMMARIES_URL = "https://bittrex.com/api/v1.1/public/getmarketsummaries";
    private static final String MARKES_URL = "https://bittrex.com/api/v1.1/public/getmarkets";

    public static MarketQuoteVolume[] getAllPairsInfos() throws IOException {
        URL url = new URL(MARKET_SUMMARIES_URL);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(url,JsonNode.class);
        JsonNode success = jsonNode.get("success");
        if (success != null && success.asBoolean() == true && jsonNode.get("result") != null) {
            JsonNode[] marketsJsonNode = objectMapper.readValue(jsonNode.get("result").toString(),JsonNode[].class);
            MarketQuoteVolume[] marketQuoteVolumes = Arrays.stream(marketsJsonNode).
                    map(marketJsonNode ->
                            new MarketQuoteVolume(marketJsonNode.get("MarketName").asText(),
                                    marketJsonNode.get("Volume").asDouble()*marketJsonNode.get("Last").asDouble())).
                    toArray(MarketQuoteVolume[]::new);
            return marketQuoteVolumes;
        } else
            throw new IOException("Unexpected server response when getting market summaries");
    }

    public static String[] getActivePairs() throws IOException, ExchangeCommunicationException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode marketsJsonNode = objectMapper.readValue(new URL(MARKES_URL),JsonNode.class);
        if(marketsJsonNode.get("success").asText().equals("true")) {
            return Arrays.stream(objectMapper.readValue(marketsJsonNode.get("result").toString(), JsonNode[].class)).
                    filter((jsonNode -> jsonNode.get("IsActive").asText().equals("true"))).
                    map((jsonNode)->jsonNode.get("BaseCurrency").asText()+"-"+jsonNode.get("MarketCurrency").asText()).
                    toArray(String[]::new);
        } else {
            throw new ExchangeCommunicationException("success=false when getting bittrex markets");
        }
    }
}
