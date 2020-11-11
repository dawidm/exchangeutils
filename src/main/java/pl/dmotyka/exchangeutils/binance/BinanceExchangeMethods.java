/*
 * Cryptonose2
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

package pl.dmotyka.exchangeutils.binance;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.tickerprovider.GenericTickerWebsocketExchangeMethods;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class BinanceExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    private static final String WS_API_URL_COMBINED = "wss://stream.binance.com/stream?streams=";
    private static final String TRADE_STREAM_NAME = "%s@aggTrade";

    private static final Logger logger = Logger.getLogger(BinanceExchangeMethods.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWsUrl(String[] pairsSymbols) {
        return WS_API_URL_COMBINED + Arrays.stream(pairsSymbols).
                map(pair -> String.format(TRADE_STREAM_NAME,pair.toLowerCase())).
                collect(Collectors.joining("/"));
    }

    @Override
    public boolean isMakingSubscriptions() {
        return false;
    }

    @Override
    public String[] subscriptionsMessages(String[] pairsSymbols) {
        return new String[0];
    }

    @Override
    public Ticker handleMessage(String message) {
        try {
            message = objectMapper.readTree(message).get("data").toString();
            String[] splitMsg = message.split(",");
            String pair=splitMsg[2].split(":")[1].replace("\"","");
            double price = Double.parseDouble(splitMsg[4].split(":")[1].replace("\"",""));
            long timestampSeconds = Long.parseLong(splitMsg[1].split(":")[1].replace("\"",""))/1000;
            return  new Ticker(pair, price, timestampSeconds);
        } catch (IOException e) {
            logger.log(Level.WARNING,"when reading websocket aggTrade message",e);
        }
        return null;
    }
}
