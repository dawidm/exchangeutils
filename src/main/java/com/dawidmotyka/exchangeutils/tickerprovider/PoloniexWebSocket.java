package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.dmutils.RepeatTillSuccess;
import com.dawidmotyka.exchangeutils.hitbtc.TransactionReceiver;
import com.dawidmotyka.exchangeutils.poloniex.Transaction;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PoloniexWebSocket implements TickerProvider {

    private static final Logger logger = Logger.getLogger(PoloniexWebSocket.class.getName());

    private static final String WS_API_URL = "wss://api2.poloniex.com";

    private String[] pairs;
    private WebSocketClient webSocketClient;
    private HashMap<String, String> wsCurrencyPairMap;
    private TransactionReceiver transactionReceiver;
    private TickerReceiver tickerReceiver;
    private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
    private ScheduledFuture reconnectScheduledFuture;
    private AtomicBoolean isConnectedAtomicBoolean=new AtomicBoolean(false);

    public PoloniexWebSocket(TransactionReceiver transactionReceiver,String[] pairs) {
        this.transactionReceiver = transactionReceiver;
        this.pairs=pairs;
        wsCurrencyPairMap = new HashMap<>();
    }

    public PoloniexWebSocket(TickerReceiver tickerReceiver,String[] pairs) {
        this.tickerReceiver = tickerReceiver;
        this.pairs=pairs;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        wsCurrencyPairMap = new HashMap<>();
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {
        try {
            isConnectedAtomicBoolean.set(true);
            SSLContext sslContext;
            sslContext = SSLContext.getDefault();
            SSLSocketFactory factory = sslContext.getSocketFactory();
            if (webSocketClient != null)
                webSocketClient.close();
            webSocketClient = new WebSocketClient(new URI(WS_API_URL)) {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    makeWsSubscriptions();
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                    if(transactionReceiver!=null)
                        transactionReceiver.wsConnected();
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
                    if(transactionReceiver!=null)
                        transactionReceiver.wsDisconnected(code, reason, remote);
                    if(reconnectScheduledFuture==null || reconnectScheduledFuture.isDone()) {
                        reconnectScheduledFuture=scheduledExecutorService.schedule(this::reconnect,1,TimeUnit.SECONDS);
                    }
                }

                @Override
                public void onError(Exception e) {
                    if(transactionReceiver!=null)
                        transactionReceiver.wsException(e);
                }
            };
            webSocketClient.setSocket(factory.createSocket());
            webSocketClient.connect();
        } catch (NoSuchAlgorithmException|URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void makeWsSubscriptions() {
        for (String currentPair : pairs) {
            webSocketClient.send(String.format("{\"command\": \"subscribe\",\"channel\": \"%s\"}", currentPair));
        }
    }

    @Override
    public void disconnect() {
        if(reconnectScheduledFuture!=null)
            reconnectScheduledFuture.cancel(true);
        if (webSocketClient != null) {
            try {
                webSocketClient.closeBlocking();
            } catch (InterruptedException e) {
                logger.warning(e.getLocalizedMessage());
            }
            webSocketClient=null;
        }
    }

    public boolean isConnected() {
        if (webSocketClient==null)
            return false;
        else
            return webSocketClient.isOpen() || webSocketClient.isConnecting();
    }

    private void handleMessage(String message) {
        if (transactionReceiver != null)
            transactionReceiver.heartbeat();
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
                                int tradeId = currentDetailsJSON.getInt(1);
                                Date date = new Date((long) currentDetailsJSON.getInt(5) * 1000);
                                String type = currentDetailsJSON.getInt(2) == 1 ? "buy" : "sell";
                                double rate = Double.parseDouble(currentDetailsJSON.getString(3));
                                if(tickerReceiver!=null) {
                                    if(currentPairTickersList==null)
                                        currentPairTickersList=new ArrayList<>(detailsJsonArray.length());
                                    currentPairTickersList.add(new Ticker(wsCurrencyPairMap.get(currentPairSymbol),rate,date.getTime()/1000));
                                }
                                if (transactionReceiver != null) {
                                    double amount = Double.parseDouble(currentDetailsJSON.getString(4));
                                    double total = rate * amount;
                                    Transaction transaction = new Transaction(
                                            wsCurrencyPairMap.get(currentPairSymbol),
                                            tradeId,
                                            type,
                                            rate,
                                            amount,
                                            total,
                                            date);
                                    transactionReceiver.receive(transaction);
                                }
                            }
                            break;
                    }
                }
            }
        }
        if(currentPairTickersList!=null)
            tickerReceiver.receiveTickers(currentPairTickersList);
    }
}
