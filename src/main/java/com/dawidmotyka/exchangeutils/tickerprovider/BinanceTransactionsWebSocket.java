/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.tickerprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinanceTransactionsWebSocket implements TickerProvider {

    private static final Logger logger = Logger.getLogger(BinanceTransactionsWebSocket.class.getName());

    private static final String WS_API_URL_COMBINED = "wss://stream.binance.com:9443/stream?streams=";
    private static final String TRADE_STREAM_NAME = "%s@aggTrade";
    private static final int CONNECTION_LOST_TIMEOUT_SECONDS=10;

    private ScheduledExecutorService scheduledExecutorService=Executors.newScheduledThreadPool(1);
    private ScheduledFuture reconnectScheduledFuture;
    private AtomicBoolean isConnectedAtomicBoolean=new AtomicBoolean(false);
    private WebSocketClient webSocketClient;
    private TickerReceiver tickerReceiver;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;
    private AtomicReference<Set<String>> pairsAtomicReference = new AtomicReference<>();

    public BinanceTransactionsWebSocket(TickerReceiver tickerReceiver, String[] pairs) {
        this.tickerReceiver = tickerReceiver;
        pairsAtomicReference.set(new HashSet<>(Arrays.asList(pairs)));
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {
        this.connectionStateReceiver=connectionStateReceiver;
        isConnectedAtomicBoolean.set(true);
        StringBuilder stringBuilder = new StringBuilder(WS_API_URL_COMBINED);
        for (String currentPair : pairsAtomicReference.get()) {
            stringBuilder.append(String.format(TRADE_STREAM_NAME+"/",currentPair.toLowerCase()));
        }
        String websocketUrl = stringBuilder.toString();
        logger.fine(String.format("connecting websocket, url: %s",websocketUrl));
        try {
            webSocketClient = new WebSocketClient(new URI(websocketUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.fine(String.format("connected websocket, handshake: %s",serverHandshake.getHttpStatusMessage()));
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (isConnectedAtomicBoolean.get() == false)
                        return;
                    logger.fine("websocket closed, reconnecting...");
                    if(code!=-1)
                        connectionStateReceiver.connectionState(TickerProviderConnectionState.RECONNECTING);
                    if (reconnectScheduledFuture == null || reconnectScheduledFuture.isDone()) {
                        reconnectScheduledFuture = scheduledExecutorService.schedule(this::reconnect, 5, TimeUnit.SECONDS);
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
        } catch (URISyntaxException |NoSuchAlgorithmException e) {
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
        } catch (InterruptedException e) {
            logger.warning("interruptedException when closeBlocking webSocketClient");
        }
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
    }

    private void handleMessage(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            message=objectMapper.readTree(message).get("data").toString();
            String[] splitMsg = message.split(",");
            String pair=splitMsg[2].split(":")[1].replace("\"","");
            double price = Double.parseDouble(splitMsg[4].split(":")[1].replace("\"",""));
            long timestampSeconds = Long.parseLong(splitMsg[1].split(":")[1].replace("\"",""))/1000;
            tickerReceiver.receiveTicker(new Ticker(pair,price,timestampSeconds));
        } catch (IOException e) {
            logger.log(Level.WARNING,"when reading websocket aggTrade message",e);
        }
    }

}
