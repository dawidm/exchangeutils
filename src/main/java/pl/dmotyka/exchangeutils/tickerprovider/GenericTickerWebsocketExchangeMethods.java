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

package pl.dmotyka.exchangeutils.tickerprovider;

import java.nio.ByteBuffer;

public interface GenericTickerWebsocketExchangeMethods {
    // get websocket sddress
    // pairsSymbols - arrays of symbols in a format correct for API
    String getWsUrl(String[] pairsSymbols);
    // whether websocket for exchange needs making subscriptions for pairs
    boolean isMakingSubscriptions();
    // generate message sent to websocket to subscribe tickers data
    String[] subscriptionsMessages(String[] pairsSymbols);
    // read websocket message and return Tickers when message contains any, otherwise null
    Ticker[] handleMessage(String message);
    // read binary websocket message and converts it to string
    String handleBinaryMessage(ByteBuffer buffer);
    // check if provided message is a ping message and return pong massage if it is
    String checkIfPingMessage(String msg);
}