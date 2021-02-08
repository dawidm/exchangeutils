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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidmotyka.dmutils.runtime.RepeatTillSuccess;
import jakarta.websocket.DeploymentException;
import org.glassfish.tyrus.client.ClientManager;

public class JSRWebsocketTickerProvider implements TickerProvider {

    private static final Logger logger = Logger.getLogger(JSRWebsocketTickerProvider.class.getName());

    public static final int RECONNECT_INTERVAL_SECONDS = 1;

    private final TickerReceiver tickerReceiver;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;
    private final String[] pairs;
    private final GenericTickerWebsocketExchangeMethods exchangeMethods;

    private ClientManager client;
    private final ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> reconnectScheduledFuture;

    private final AtomicBoolean connected = new AtomicBoolean(false);

    public JSRWebsocketTickerProvider(TickerReceiver tickerReceiver, String[] pairs, GenericTickerWebsocketExchangeMethods exchangeMethods) {
        this.tickerReceiver = tickerReceiver;
        this.pairs = pairs;
        this.exchangeMethods = exchangeMethods;
    }

    @Override
    public synchronized void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {
        if (connected.get()) {
            throw new IllegalStateException("already connected");
        }
        this.connectionStateReceiver = connectionStateReceiver;
        client = ClientManager.createClient();
        try {
            client.connectToServer(new JSRWebsocketClientEndpoint(exchangeMethods, tickerReceiver, this::JSRWsebsocketConnectionState, pairs), new URI(exchangeMethods.getWsUrl(null)));
            connected.set(true);
        } catch (DeploymentException | URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public synchronized void disconnect() {
        if (!connected.get()) {
            throw new IllegalStateException("not connected");
        }
        if (client != null)
            client.shutdown();
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
    }

    private void reconnect() throws IOException {
        connect(connectionStateReceiver);
    }

    private void JSRWsebsocketConnectionState(JSRWebsocketConnectionState state) {
        switch (state) {
            case CONNECTED:
                connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
                break;
            case DISCONNECTED:
                connectionStateReceiver.connectionState(TickerProviderConnectionState.RECONNECTING);
                logger.fine("websocket closed, reconnecting...");
                if (reconnectScheduledFuture == null || reconnectScheduledFuture.isDone()) {
                    reconnectScheduledFuture = scheduledExecutorService.schedule(() -> {
                        RepeatTillSuccess.planTask(this::reconnect, e -> logger.log(Level.WARNING, "when reconnecting", e), RECONNECT_INTERVAL_SECONDS * 100);
                    }, RECONNECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
                }
                break;
        }
    }
}

