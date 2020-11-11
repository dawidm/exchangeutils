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

package pl.dmotyka.exchangeutils.poloniex;

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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionState;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionStateReceiver;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

public class PoloniexWebSocket implements TickerProvider {

    private static final Logger logger = Logger.getLogger(PoloniexWebSocket.class.getName());

    private static final String WS_API_URL = "wss://api2.poloniex.com";

    private static final int WS_TIMEOUT_SEC = 10;

    private final String[] pairs;
    private final HashMap<String, String> wsCurrencyPairMap;
    private TransactionReceiver transactionReceiver;
    private TickerReceiver tickerReceiver;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;

    private WebSocketClient webSocketClient;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> reconnectScheduledFuture;
    private final AtomicBoolean isConnectedAtomicBoolean = new AtomicBoolean(false);

    public PoloniexWebSocket(TransactionReceiver transactionReceiver,String[] pairs) {
        this.transactionReceiver = transactionReceiver;
        this.pairs = pairs;
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
        this.connectionStateReceiver=connectionStateReceiver;
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
                    logger.info("onOpen");
                    makeWsSubscriptions();
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                    if(transactionReceiver!=null)
                        transactionReceiver.wsConnected();
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info(String.format("onClose, code: %d, reason: %s, remote: %b", code, reason, remote));
                    if (transactionReceiver != null)
                        transactionReceiver.wsDisconnected(code, reason, remote);
                    if (isConnectedAtomicBoolean.get()) {
                        if (reconnectScheduledFuture == null || reconnectScheduledFuture.isDone()) {
                            if (code != -1)
                                connectionStateReceiver.connectionState(TickerProviderConnectionState.RECONNECTING);
                            logger.info("reconnecting in 1 second...");
                            reconnectScheduledFuture = scheduledExecutorService.schedule(this::reconnect, 1, TimeUnit.SECONDS);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    logger.log(Level.WARNING, "websocket error", e);
                    if(tickerReceiver!=null)
                        tickerReceiver.error(e);
                    if(transactionReceiver!=null)
                        transactionReceiver.wsException(e);
                }
            };
            webSocketClient.setConnectionLostTimeout(WS_TIMEOUT_SEC);
            webSocketClient.setSocketFactory(factory);
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
        isConnectedAtomicBoolean.set(false);
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
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
    }

    public boolean isConnected() {
        if (webSocketClient==null)
            return false;
        else
            return webSocketClient.isOpen();
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
        if(currentPairTickersList!=null && tickerReceiver!=null)
            tickerReceiver.receiveTickers(currentPairTickersList.toArray(Ticker[]::new));
    }
}
