/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.tickerprovider.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class BitfinexExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    public static final Logger logger = Logger.getLogger(BitfinexExchangeMethods.class.getName());

    private static final String BITFINEX_WS_API_V2_URL="wss://api.bitfinex.com/ws/2";
    private static final String BITFINEX_WS_TRADES_SUBSCRIBE_MSG="{\"event\":\"subscribe\",\"channel\":\"trades\",\"symbol\":\"%s\"}";

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String,String> subscriptionsSymbols = new HashMap<>();

    @Override
    public String getWsUrl(String[] pairsSymbols) {
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
