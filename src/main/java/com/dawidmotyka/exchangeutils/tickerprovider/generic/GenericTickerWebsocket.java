/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.tickerprovider.generic;

import com.dawidmotyka.exchangeutils.tickerprovider.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericTickerWebsocket implements TickerProvider {

    private static final Logger logger = Logger.getLogger(BinanceTransactionsWebSocket.class.getName());
    public static final int CONNECTION_LOST_TIMEOUT_SECONDS=60;
    public static final int PAUSE_BETWEEN_SUBSCRIPTIONS_MILLIS=200;

    private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
    private ScheduledFuture reconnectScheduledFuture;
    private AtomicBoolean isConnectedAtomicBoolean=new AtomicBoolean(false);
    private WebSocketClient webSocketClient;
    private TickerReceiver tickerReceiver;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;
    private GenericTickerWebsocketExchangeMethods genericTickerWebsocketExchangeMethods;
    private String[] pairs;

    public GenericTickerWebsocket(TickerReceiver tickerReceiver, String[] pairs, GenericTickerWebsocketExchangeMethods genericTickerWebsocketExchangeMethods) {
        this.tickerReceiver = tickerReceiver;
        this.pairs=pairs;
        this.genericTickerWebsocketExchangeMethods = genericTickerWebsocketExchangeMethods;
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {
        this.connectionStateReceiver=connectionStateReceiver;
        isConnectedAtomicBoolean.set(true);
        logger.fine(String.format("connecting websocket, url: %s", genericTickerWebsocketExchangeMethods.getWsUrl(pairs)));
        try {
            webSocketClient = new WebSocketClient(new URI(genericTickerWebsocketExchangeMethods.getWsUrl(pairs))) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.fine(String.format("connected websocket, handshake: %s",serverHandshake.getHttpStatusMessage()));
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                    if(genericTickerWebsocketExchangeMethods.isMakingSubscriptions()) {
                        logger.fine(String.format("making subscriptions"));
                        String[] messages = genericTickerWebsocketExchangeMethods.subscriptionsMessages(pairs);
                        for(String message : messages) {
                            try { Thread.sleep(PAUSE_BETWEEN_SUBSCRIPTIONS_MILLIS); } catch (InterruptedException e) {break;}
                            logger.finer("sending to ws: " + message);
                            webSocketClient.send(message);
                        }
                    }
                }

                @Override
                public void onMessage(String message) {
                    Ticker ticker = genericTickerWebsocketExchangeMethods.handleMessage(message);
                    if(ticker!=null)
                        tickerReceiver.receiveTicker(ticker);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (isConnectedAtomicBoolean.get() == false)
                        return;
                    if(code!=-1)
                        connectionStateReceiver.connectionState(TickerProviderConnectionState.RECONNECTING);
                    logger.fine("websocket closed, reconnecting...");
                    if (reconnectScheduledFuture == null || reconnectScheduledFuture.isDone()) {
                        reconnectScheduledFuture = scheduledExecutorService.schedule(this::reconnect, 1, TimeUnit.SECONDS);
                    }
                }
                @Override
                public void onError(Exception e) {
                    logger.log(Level.SEVERE, "WebSocket exception", e);
                }
            };
            SSLContext sslContext;
            sslContext = SSLContext.getDefault();
            SSLSocketFactory factory = sslContext.getSocketFactory();
            webSocketClient.setSocket(factory.createSocket());
            webSocketClient.setConnectionLostTimeout(CONNECTION_LOST_TIMEOUT_SECONDS);
            webSocketClient.connect();
        } catch (URISyntaxException | NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, "", e);
            throw new IOException(e);
        }
    }

    public void disconnect() {
        logger.fine("disconnecting websocket");
        if(reconnectScheduledFuture!=null)
            reconnectScheduledFuture.cancel(true);
        if(webSocketClient==null)
            return;
        if(webSocketClient.isClosing()) {
            logger.warning("unexpected websocketclient state: isClosing");
            return;
        }
        try {
            isConnectedAtomicBoolean.set(false);
            webSocketClient.closeBlocking();
            connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
        } catch (InterruptedException e) {
            logger.warning("interruptedException when closeBlocking webSocketClient");
        }
    }

}
