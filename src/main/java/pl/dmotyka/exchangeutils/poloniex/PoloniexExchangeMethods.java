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

package pl.dmotyka.exchangeutils.poloniex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.dmotyka.exchangeutils.tickerprovider.GenericTickerWebsocketExchangeMethods;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class PoloniexExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    private static final Logger logger = Logger.getLogger(PoloniexExchangeMethods.class.getName());

    private static final String WS_API_URL = "wss://api2.poloniex.com";

    private final HashMap<String, String> wsCurrencyPairMap;

    public PoloniexExchangeMethods() {
        wsCurrencyPairMap = new HashMap<>();
    }

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
        return Arrays.stream(pairsSymbols).map(symbol -> String.format("{\"command\": \"subscribe\",\"channel\": \"%s\"}", symbol)).toArray(String[]::new);
    }

    @Override
    public Ticker[] handleMessage(String message) {
        JSONArray msgJsonArray = new JSONArray(message);
        String currentPairSymbol = msgJsonArray.get(0).toString();
        JSONArray detailsJsonArray;
        List<Ticker> currentPairTickersList=null;
        if (msgJsonArray.length() > 2) {
            detailsJsonArray = msgJsonArray.getJSONArray(2);
            for (Object currentDetails : detailsJsonArray) {
                JSONArray currentDetailsJSON = (JSONArray) currentDetails;
                if (currentDetailsJSON.length() > 1) {
                    switch (currentDetailsJSON.getString(0)) {
                        case "i":
                            JSONObject currentPairInfo = currentDetailsJSON.getJSONObject(1);
                            wsCurrencyPairMap.put(currentPairSymbol, currentPairInfo.getString("currencyPair"));
                            break;
                        case "t":
                            if (currentDetailsJSON.length() > 5) {
                                Date date = new Date((long) currentDetailsJSON.getInt(5) * 1000);
                                double rate = Double.parseDouble(currentDetailsJSON.getString(3));
                                if(currentPairTickersList==null)
                                    currentPairTickersList=new ArrayList<>(detailsJsonArray.length());
                                currentPairTickersList.add(new Ticker(wsCurrencyPairMap.get(currentPairSymbol),rate,date.getTime()/1000));
                            }
                            break;
                    }
                }
            }
        }
        if (currentPairTickersList==null)
            return null;
        return currentPairTickersList.toArray(new Ticker[0]);
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
}
