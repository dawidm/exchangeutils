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

import com.dawidmotyka.dmutils.runtime.RepeatTillSuccess;
import com.dawidmotyka.exchangeutils.bittrex.BittrexExchangeSpecs;
import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsNotAvailableException;
import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsProvider;
import com.dawidmotyka.exchangeutils.credentialsprovider.ExchangeCredentials;
import com.github.ccob.bittrex4j.BittrexExchange;
import com.github.signalr4j.client.ConnectionState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BittrexWebsocketTickerProvider implements TickerProvider {

    Logger logger = Logger.getLogger(BittrexWebsocketTickerProvider.class.getName());

    private BittrexExchange bittrexExchange;
    private String[] pairs;
    private TickerReceiver tickerReceiver;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture reconnectScheduledFuture;
    private AtomicBoolean connectedAtomicBoolean=new AtomicBoolean(false);
    TickerProviderConnectionStateReceiver connectionStateReceiver;

    public BittrexWebsocketTickerProvider(TickerReceiver tickerReceiver,String pairs[]) {
        this.pairs=pairs;
        this.tickerReceiver=tickerReceiver;
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {
        connectedAtomicBoolean.set(true);
        startWebsocket();
        this.connectionStateReceiver=connectionStateReceiver;
    }

    @Override
    public void disconnect() {
        connectedAtomicBoolean.set(false);
        disconnectBittrexExchange();
    }

    private void disconnectBittrexExchange() {
        logger.info("disconnecting ws");
        if(reconnectScheduledFuture!=null)
            reconnectScheduledFuture.cancel(true);
        if(bittrexExchange!=null) {
            bittrexExchange.disconnectFromWebSocket();
        }
    }

    private void reconnect() {
        disconnectBittrexExchange();
        RepeatTillSuccess.planTask(this::startWebsocket, e->{logger.log(Level.WARNING,"when connecting websocket",e);},1000);
    }

    private void startWebsocket() throws IOException {
        if(bittrexExchange!=null)
            disconnectBittrexExchange();
        ExchangeCredentials exchangeCredentials;
        try {
            exchangeCredentials=CredentialsProvider.getCredentials(BittrexExchangeSpecs.class);
        } catch (CredentialsNotAvailableException e) {
            logger.severe(e.getLocalizedMessage());
            throw new IOException(e.getLocalizedMessage());
        }
        bittrexExchange = new BittrexExchange(exchangeCredentials.getLogin(), exchangeCredentials.getPass());
        bittrexExchange.onUpdateExchangeState(updateExchangeState -> {
            if(updateExchangeState.getFills().length>0) {
                logger.finest(String.format("%d ticker for %s", updateExchangeState.getFills().length, updateExchangeState.getMarketName()));
                List<Ticker> tickers = Arrays.stream(updateExchangeState.getFills()).map(fill -> new Ticker(updateExchangeState.getMarketName(), fill.getPrice(), System.currentTimeMillis() / 1000)).collect(Collectors.toList());
                tickerReceiver.receiveTickers(tickers);
            }
        });
        bittrexExchange.onWebsocketError(e -> logger.log(Level.WARNING, "websocket error", e));
        bittrexExchange.onWebsocketStateChange(connectionStateChange -> {
            logger.info("websocket connection state: " + connectionStateChange.getNewState().name());
            switch (connectionStateChange.getNewState()) {
                case Connected:
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                    break;
                case Disconnected:
                    connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
                    break;
            }
            if(connectionStateChange.getNewState()==ConnectionState.Disconnected && connectedAtomicBoolean.get()==true) {
                if(reconnectScheduledFuture==null || reconnectScheduledFuture.isDone() || reconnectScheduledFuture.isCancelled()) {
                    logger.info("lost connection, reconnecting");
                    reconnectScheduledFuture=scheduledExecutorService.schedule(this::reconnect,1,TimeUnit.SECONDS);
                }
            }
        });
        logger.info("connecting ws");
        bittrexExchange.connectToWebSocket(() -> {
            logger.info("connected ws");
            for(String pair : pairs) {
                bittrexExchange.subscribeToExchangeDeltas(pair, aBoolean -> logger.fine(String.format("subscribing %s, result: %b",pair,aBoolean)));
            }
        });
    }

}
