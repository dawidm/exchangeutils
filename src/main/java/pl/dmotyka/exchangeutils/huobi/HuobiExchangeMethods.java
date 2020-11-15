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

package pl.dmotyka.exchangeutils.huobi;

import java.util.Arrays;

import pl.dmotyka.exchangeutils.tickerprovider.GenericTickerWebsocketExchangeMethods;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class HuobiExchangeMethods implements GenericTickerWebsocketExchangeMethods {

    private static final String WS_API_URL = "wss://api.huobi.pro/ws";

    @Override
    public String getWsUrl(String[] pairsSymbols) {
        return WS_API_URL;
    }

    @Override
    public boolean isMakingSubscriptions() {
        return true;
    }

    @Override
    public String[] subscriptionsMessages(String[] pairsSymbols) {
        return Arrays.stream(pairsSymbols).map(symbol -> String.format("{ \"sub\": \"market.%s.trade.detail\", \"id\": \"1\" }", symbol)).toArray(String[]::new);
    }

    @Override
    public Ticker[] handleMessage(String message) {
        System.out.println(message);
        return null;
    }
}
