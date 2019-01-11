package com.dawidmotyka.exchangeutils.hitbtc;

import com.dawidmotyka.exchangeutils.poloniex.Transaction;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by dawid on 9/29/17.
 */
public class HitBtcWebSocket {

    private static final int WEBSOCKET_NO_TRADES_RECONNECT_DELAY_MILLIS = 10000;
    private static String WS_API_URL = "ws://api.hitbtc.com:80";

    private WebSocketClient webSocketClient;
    private Set<String> pairs;
    private TransactionReceiver transactionReceiver;

    ScheduledExecutorService scheduledExecutorService;
    ScheduledFuture connectionCheckerScheduledFuture;

    //TODO use atomic Long
    Object lastHartbeatLock;
    private long lastHartbeat = 0;

    public HitBtcWebSocket(String pairs[], TransactionReceiver transactionReceiver) {
        this.transactionReceiver = transactionReceiver;
        this.pairs = new HashSet<>(Arrays.asList(pairs));
        lastHartbeatLock = new Object();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    public void connect() throws URISyntaxException, IOException {
        if (webSocketClient != null)
            webSocketClient.close();
        webSocketClient = new WebSocketClient(new URI(WS_API_URL)) {
            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                if (connectionCheckerScheduledFuture != null)
                    connectionCheckerScheduledFuture.cancel(true);
                synchronized (lastHartbeatLock) {
                    lastHartbeat = System.currentTimeMillis();
                }
                connectionCheckerScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> connectionChecker(), 1, 1, TimeUnit.SECONDS);
                transactionReceiver.wsConnected();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                transactionReceiver.wsDisconnected(code, reason, remote);
            }

            @Override
            public void onError(Exception e) {
                transactionReceiver.wsException(e);
            }
        };
        webSocketClient.connect();
    }

    public void disconnect(String msg) {
        if (webSocketClient != null)
            webSocketClient.closeConnection(0, msg);
    }

    public boolean isConnected() {
        if (webSocketClient == null)
            return false;
        else
            return webSocketClient.isOpen() || webSocketClient.isConnecting();
    }

    private void connectionChecker() {
        synchronized (lastHartbeatLock) {
            if (lastHartbeat != 0 && (System.currentTimeMillis() - lastHartbeat) > WEBSOCKET_NO_TRADES_RECONNECT_DELAY_MILLIS) {
                lastHartbeat = 0;
                disconnect(String.format("no transactions in last %d seconds", WEBSOCKET_NO_TRADES_RECONNECT_DELAY_MILLIS / 1000));
            }
        }
    }

    private void handleMessage(String message) {
        if (transactionReceiver != null)
            transactionReceiver.heartbeat();
        synchronized (lastHartbeatLock) {
            lastHartbeat = System.currentTimeMillis();
        }
        try {
            JSONObject incrementalRefreshJsonObject = new JSONObject(message).getJSONObject("MarketDataIncrementalRefresh");
            String pairName = convertPairName(incrementalRefreshJsonObject.getString("symbol"));
            //not btc counter currency or not on the list
            if(!pairName.split("/")[1].equals("BTC") || !pairs.contains(pairName))
                return;
            long timestamp = incrementalRefreshJsonObject.getLong("timestamp");
            JSONArray tradesJsonArray = incrementalRefreshJsonObject.getJSONArray("trade");
            if (tradesJsonArray != null && tradesJsonArray.length() > 0) {
                Iterator<Object> tradesIterator = tradesJsonArray.iterator();
                while (tradesIterator.hasNext()) {
                    JSONObject currentTradeJson = (JSONObject) tradesIterator.next();
                    Transaction transaction = new Transaction(
                            pairName,
                            currentTradeJson.getInt("tradeId"),
                            currentTradeJson.getString("side"),
                            currentTradeJson.getDouble("price"),
                            currentTradeJson.getDouble("size"),
                            currentTradeJson.getDouble("price") * currentTradeJson.getDouble("size"),
                            new Date(timestamp));
                    if (transactionReceiver != null)
                        transactionReceiver.receive(transaction);
                }
            }
        }
        catch (JSONException e) {

        }
    }

    private String convertPairName(String pair) {
        return pair.substring(0,pair.length()-3) + "/" + pair.substring(pair.length()-3,pair.length());
    }
}
