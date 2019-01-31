package com.dawidmotyka.exchangeutils.bittrex;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
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
