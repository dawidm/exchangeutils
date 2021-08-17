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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class JSRWebsocketClientEndpoint {

    private static final Logger logger = Logger.getLogger(JSRWebsocketClientEndpoint.class.getName());

    private final GenericTickerWebsocketExchangeMethods exchangeMethods;
    private final TickerReceiver tickerReceiver;
    private final JSRWebsocketConnectionStateReceiver connectionStateReceiver;
    private final String[] pairs;

    public JSRWebsocketClientEndpoint(GenericTickerWebsocketExchangeMethods exchangeMethods, TickerReceiver tickerReceiver, JSRWebsocketConnectionStateReceiver connectionStateReceiver, String[] pairs) {
        this.exchangeMethods = exchangeMethods;
        this.tickerReceiver = tickerReceiver;
        this.connectionStateReceiver = connectionStateReceiver;
        this.pairs = pairs;
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.fine("connection opened");
        connectionStateReceiver.connectionState(JSRWebsocketConnectionState.CONNECTED);
        try {
            if (exchangeMethods.makesSubscriptions()) {
                String[] messages = exchangeMethods.subscriptionsMessages(pairs);
                for (String msg : messages)
                    session.getBasicRemote().sendText(msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        String pongMsg = exchangeMethods.checkIfPingMessage(message);
        if (pongMsg != null) {
            try {
                session.getBasicRemote().sendText(pongMsg);
            } catch (IOException e) {
                logger.log(Level.WARNING, "cannot send pong message", e);
            }
        }
        Ticker[] tickers = exchangeMethods.handleMessage(message);
        if (tickers != null)
            tickerReceiver.receiveTickers(exchangeMethods.handleMessage(message));
        return null;
    }

    @OnMessage
    public void handleBytes(ByteBuffer buffer, Session session) {
        onMessage(exchangeMethods.handleBinaryMessage(buffer), session);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.fine("connection closed");
        if (closeReason.getCloseCode() == CloseReason.CloseCodes.NORMAL_CLOSURE)
            connectionStateReceiver.connectionState(JSRWebsocketConnectionState.REQUESTED_DISCONNECT);
        else
            connectionStateReceiver.connectionState(JSRWebsocketConnectionState.DISCONNECTED);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.log(Level.WARNING, "websocket error", t);
    }
}
