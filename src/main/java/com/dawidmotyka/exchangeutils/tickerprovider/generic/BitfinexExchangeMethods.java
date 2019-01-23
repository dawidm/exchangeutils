package com.dawidmotyka.exchangeutils.tickerprovider.generic;

import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BitfinexExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    public static final Logger logger = Logger.getLogger(BitfinexExchangeMethods.class.getName());

    private static final String BITFINEX_WS_API_V2_URL="wss://api.bitfinex.com/ws/2";
    private static final String BITFINEX_WS_TRADES_SUBSCRIBE_MSG="{\"event\":\"subscribe\",\"channel\":\"trades\",\"symbol\":\"%s\"}";

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String,String> subscriptionsSymbols = new HashMap<>();

    @Override
    public String getWsUrl(@Nullable String[] pairsSymbols) {
        return BITFINEX_WS_API_V2_URL;
    }

    @Override
    public boolean isMakingSubscriptions() {
        return true;
    }

    @Override
    public String[] subscriptionsMessages(String[] pairsSymbols) {
        List<String> messages = new ArrayList<>(pairsSymbols.length);
        for(String pair : pairsSymbols)  {
            messages.add(subscribeMessage(pair));
        }
        return messages.toArray(new String[messages.size()]);
    }

    //[3,"te","11943696-BTCUSD",1548198480,3641.2,0.00455376]
    @Override
    public Ticker handleMessage(String message) {
        try {
            JsonNode messageNode = objectMapper.readValue(message, JsonNode.class);
            JsonNode eventNode=messageNode.get("event");
            if(eventNode!=null && eventNode.asText().equals("subscribed")) {
                subscriptionsSymbols.put(messageNode.get("chanId").asText(),messageNode.get("symbol").asText());
                logger.finer("subscription for trade data succeed: " + messageNode.get("symbol").asText());
                return null;
            }
            JsonNode teNode = messageNode.get(1);
            if(teNode!=null && teNode.asText().equals("te")) {
                String channelId=messageNode.get(0).asText();
                JsonNode detailsNode = messageNode.get(2);
                int timestampSeconds=(int)(detailsNode.get(1).asLong()/1000);
                double price=detailsNode.get(3).asDouble();
                return new Ticker(subscriptionsSymbols.get(channelId),price,timestampSeconds);
            }
            return null;
        } catch (IOException e) {
            logger.finer("when handling ws message" + e.getMessage());
            return null;
        }
    }

    private String subscribeMessage(String pair) {
        return String.format(BITFINEX_WS_TRADES_SUBSCRIBE_MSG,pair);
    }
}
