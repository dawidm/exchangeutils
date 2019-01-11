package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.dmutils.RepeatTillSuccess;
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
    private AtomicReference<Set<String>> pairsAtomicReference = new AtomicReference<>();

    public BinanceTransactionsWebSocket(TickerReceiver tickerReceiver, String[] pairs) {
        this.tickerReceiver = tickerReceiver;
        pairsAtomicReference.set(new HashSet<>(Arrays.asList(pairs)));
    }

    @Override
    public void connect() throws IOException {
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
        } catch (URISyntaxException |NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, "", e);
            throw new IOException(e);
        }
    }

    private void reconnect() {
        disconnect();
        RepeatTillSuccess.planTask(this::connect,e->logger.log(Level.WARNING,"when connecting websocket",e),1000);
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
