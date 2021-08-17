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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.tickerprovider.GenericTickerWebsocketExchangeMethods;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class KuCoinExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    private static class KuCoinWsData {

        private final String wsURL;
        private final Long clientPingMessageInterval;
        private final String token;

        public KuCoinWsData(String wsURL, Long clientPingMessageInterval, String token) {
            this.wsURL = wsURL;
            this.clientPingMessageInterval = clientPingMessageInterval;
            this.token = token;
        }

        public String getWsURL() {
            return wsURL;
        }

        public Long getClientPingMessageInterval() {
            return clientPingMessageInterval;
        }

        public String getToken() {
            return token;
        }
    }

    private static class MatchSubscriptionPOJO {

        private final long id;
        private final String type = "subscribe";
        private final String topic;
        private final boolean response = true;

        public MatchSubscriptionPOJO(long id, String pairApiSymbols[]) {
            this.id = id;
            this.topic = String.format(WS_SUB_TEMPLATE, Arrays.stream(pairApiSymbols).collect(Collectors.joining(",")));
        }

        public long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getTopic() {
            return topic;
        }

        public boolean isResponse() {
            return response;
        }
    }

    private static final Logger logger = Logger.getLogger(KuCoinExchangeMethods.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String WS_INFO_POST_URL = "https://api.kucoin.com/api/v1/bullet-public";
    public static final long OK_SYSTEM_CODE = 200000;
    public static final String WS_URL_ENDING = "?token=%s&[connectId=%s]";
    // template for subscription of market match events, %s is symbols list
    public static final String WS_SUB_TEMPLATE = "/market/match:%s";

    private final long connectionId = System.currentTimeMillis();
    private KuCoinWsData kuCoinWsData = null;

    @Override
    public String getWsUrl(String[] pairsSymbols) throws ExchangeCommunicationException {
        if (kuCoinWsData == null) {
            kuCoinWsData = getWsData();
        }
        return kuCoinWsData.wsURL + String.format(WS_URL_ENDING, kuCoinWsData.getToken(), connectionId);
    }

    @Override
    public boolean makesSubscriptions() {
        return true;
    }

    @Override
    public boolean clientSendsPingMessages() {
        return true;
    }

    @Override
    public long clientPingMessageIntervalMs() throws ExchangeCommunicationException {
        if (kuCoinWsData == null) {
            kuCoinWsData = getWsData();
        }
        return kuCoinWsData.clientPingMessageInterval;
    }

    @Override
    public String[] subscriptionsMessages(String[] pairsSymbols) {
        if (kuCoinWsData == null) {
            throw new IllegalStateException("websocket data not initialized (call getWsUrl first?)");
        }
        return new String[] {(objectMapper.convertValue(new MatchSubscriptionPOJO(System.currentTimeMillis(), pairsSymbols), JsonNode.class)).toString()};
    }

    @Override
    public Ticker[] handleMessage(String message) {
        return new Ticker[0];
    }

    @Override
    public String handleBinaryMessage(ByteBuffer buffer) {
        logger.warning("Handling binary messages not supported");
        return null;
    }

    @Override
    public String checkIfPingMessage(String msg) {
        throw new RuntimeException("not implemented");
    }

    private KuCoinWsData getWsData() throws ExchangeCommunicationException {
        try {
            URL url = new URL(WS_INFO_POST_URL);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            JsonNode wsDataNode = objectMapper.readValue(http.getInputStream(), JsonNode.class);
            long responseSystemCode = wsDataNode.get("code").asLong();
            logger.fine("Ws data message response code: " + responseSystemCode);
            if (responseSystemCode != OK_SYSTEM_CODE) {
                throw new ExchangeCommunicationException("Wrong response code: " + responseSystemCode);
            }
            JsonNode dataNode = wsDataNode.get("data");
            String token = dataNode.get("token").asText();
            JsonNode serversArray = dataNode.get("instanceServers");
            if (!serversArray.isArray()) {
                throw new ExchangeCommunicationException("Unexpected JSON format");
            }
            JsonNode serverArray = serversArray.get(0);
            String endpoint = serverArray.get("endpoint").asText();
            Long pingInterval = serverArray.get("pingInterval").asLong();
            return new KuCoinWsData(endpoint, pingInterval, token);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("When getting websocket data from " + WS_INFO_POST_URL);
        }
    }
}
