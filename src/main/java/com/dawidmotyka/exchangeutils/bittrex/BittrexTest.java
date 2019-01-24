package com.dawidmotyka.exchangeutils.bittrex;

import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsNotAvailableException;
import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsProvider;
import com.dawidmotyka.exchangeutils.credentialsprovider.ExchangeCredentials;
import com.dawidmotyka.exchangeutils.exchangespecs.BittrexExchangeSpecs;
import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;
import com.github.ccob.bittrex4j.BittrexExchange;
import com.github.signalr4j.client.ConnectionState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BittrexTest {
    public static final Logger logger = Logger.getLogger(BittrexTest.class.getName());
    public static void main(String[] args) throws IOException {
        ExchangeCredentials exchangeCredentials;
        try {
            exchangeCredentials= CredentialsProvider.getCredentials(BittrexExchangeSpecs.class);
        } catch (CredentialsNotAvailableException e) {
            logger.severe(e.getLocalizedMessage());
            throw new IOException(e.getLocalizedMessage());
        }
        BittrexExchange bittrexExchange = new BittrexExchange(exchangeCredentials.getLogin(), exchangeCredentials.getPass());
        bittrexExchange.onUpdateExchangeState(updateExchangeState -> {
            logger.info("update");
        });
        bittrexExchange.onWebsocketError(e -> logger.log(Level.WARNING, "websocket error", e));
        bittrexExchange.onWebsocketStateChange(connectionStateChange -> {
            logger.info("websocket connection state: " + connectionStateChange.getNewState().name());
        });
        logger.info("connecting ws");
        bittrexExchange.connectToWebSocket(() -> {
            logger.info("connected ws");
            //bittrexExchange.subscribeToExchangeDeltas("BTC-ETH", aBoolean -> logger.fine(String.format("subscribing, result: %b",aBoolean)));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    bittrexExchange.disconnectFromWebSocket();
                }
            },1000);
        });
    }
}
