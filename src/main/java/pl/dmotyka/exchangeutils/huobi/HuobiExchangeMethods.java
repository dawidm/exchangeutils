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

package pl.dmotyka.exchangeutils.huobi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.tickerprovider.GenericTickerWebsocketExchangeMethods;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class HuobiExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    private static final Logger logger = Logger.getLogger(HuobiExchangeMethods.class.getName());

    private static final String WS_API_URL = "wss://api.huobi.pro/ws";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWsUrl(String[] pairsSymbols) {
        return WS_API_URL;
    }

    @Override
    public boolean makesSubscriptions() {
        return true;
    }

    @Override
    public boolean clientSendsPingMessages() {
        return false;
    }

    @Override
    public long clientPingMessageIntervalMs() {
        throw new RuntimeException("not applicable for this implementation (check with clientSendsPingMessages)");
    }

    @Override
    public String[] subscriptionsMessages(String[] pairsSymbols) {
        return Arrays.stream(pairsSymbols).map(symbol -> String.format("{ \"sub\": \"market.%s.trade.detail\", \"id\": \"1\" }", symbol)).toArray(String[]::new);
    }

    @Override
    public Ticker[] handleMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode chanNode = node.get("ch");
            if (chanNode == null)
                return null;
            String pair = chanNode.asText().split("\\.")[1];
            JsonNode tickNode = node.get("tick");
            JsonNode dataNode = tickNode.get("data");
            List<Ticker> tickers = new LinkedList<>();
            for (JsonNode tradeNode : dataNode) {
                long time = tradeNode.get("ts").asLong();
                double price = tradeNode.get("price").asDouble();
                tickers.add(new Ticker(pair, price, time / 1000));
            }
            return tickers.toArray(Ticker[]::new);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String handleBinaryMessage(ByteBuffer buffer) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
            GZIPInputStream gis = new GZIPInputStream(bis);
            BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            logger.log(Level.WARNING, "when handling binary message", e);
            return null;
        }
    }

    @Override
    public String checkIfPingMessage(String msg) {
        if (msg.startsWith("{\"ping\":")) {
            String number = msg.split(":")[1];
            number = number.substring(0, number.length()-1);
            return String.format("{\"pong\":%s}", number);
        }
        return null;
    }
}
