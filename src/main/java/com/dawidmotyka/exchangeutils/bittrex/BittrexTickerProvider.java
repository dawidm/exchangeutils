/*
 * Copyright 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dawidmotyka.exchangeutils.bittrex;

import com.dawidmotyka.exchangeutils.tickerprovider.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dawid on 8/13/17.
 */
public class BittrexTickerProvider implements TickerProvider {

    private static final Logger logger = Logger.getLogger(BittrexTickerProvider.class.getName());
    private static final long DEFAULT_REFRESH_INTERVAL_MILLISECONDS = 1000;

    private final TickerReceiver tickerReceiver;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;
    private final ScheduledExecutorService scheduledExecutorService;
    private long refreshIntervalMillis;
    private final Set<String> pairsSet = new HashSet<>();

    public BittrexTickerProvider(long refreshIntervalMillis, TickerReceiver tickerReceiver) {
        this.refreshIntervalMillis =refreshIntervalMillis;
        this.tickerReceiver=tickerReceiver;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }
    public BittrexTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        this(DEFAULT_REFRESH_INTERVAL_MILLISECONDS,tickerReceiver);
        pairsSet.addAll(Arrays.asList(pairs));
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) {
        this.connectionStateReceiver=connectionStateReceiver;
        connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                logger.finest("getting tickers...");
                getAllTickers();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"",e);
            }
        },0, refreshIntervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {
        logger.fine("shutting down ScheduledExecutorService");
        scheduledExecutorService.shutdown();
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
    }

    private void getAllTickers() {
        try {
            for (Ticker ticker : BittrexTicker.getAllFromSummaries()) {
                if(pairsSet.contains(ticker.getPair()))
                    tickerReceiver.receiveTicker(ticker);
            }
        }
        catch (IOException e) {
            logger.severe("When receiving summaries: " + e.getLocalizedMessage());
            tickerReceiver.error(e);
        }
    }


}
