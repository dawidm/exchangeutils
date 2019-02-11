/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.bittrex;

import pl.dmotyka.exchangeutils.credentialsprovider.CredentialsNotAvailableException;
import pl.dmotyka.exchangeutils.credentialsprovider.CredentialsProvider;
import pl.dmotyka.exchangeutils.credentialsprovider.ExchangeCredentials;
import com.github.ccob.bittrex4j.BittrexExchange;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

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
