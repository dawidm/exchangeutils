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

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionState;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionStateReceiver;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

public class Uniswap3TickerProvider implements TickerProvider {

    public static final Logger logger = Logger.getLogger(Uniswap3PairDataProvider.class.getName());

    private static final int TICKER_REFRESH_RATE_SEC = 6;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private TickerReceiver tickerReceiver;
    private String[] pairsApiSymbols;
    private Set<String> pairsSet;
    private TickerProviderConnectionStateReceiver connectionStateReceiver;
    private Uniswap3ExchangeSpecs exchangeSpecs;
    TheGraphHttpRequest theGraphHttpRequest;

    private int lastRefreshTimestampSeconds;

    public Uniswap3TickerProvider(TickerReceiver tickerReceiver, String[] pairsApiSymbols, Uniswap3ExchangeSpecs exchangeSpecs) {
        this.tickerReceiver = tickerReceiver;
        this.pairsApiSymbols = pairsApiSymbols;
        this.pairsSet = Set.of(pairsApiSymbols);
        this.exchangeSpecs = exchangeSpecs;
        theGraphHttpRequest = new TheGraphHttpRequest(exchangeSpecs);
        lastRefreshTimestampSeconds = (int)Instant.now().getEpochSecond() - TICKER_REFRESH_RATE_SEC;
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver tickerProviderConnectionStateReceiver) throws IOException, ExchangeCommunicationException {
        scheduledExecutorService.scheduleWithFixedDelay(this::executorTask, 0, TICKER_REFRESH_RATE_SEC, TimeUnit.SECONDS);
        this.connectionStateReceiver = tickerProviderConnectionStateReceiver;
        connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
    }

    @Override
    public void disconnect() {
        scheduledExecutorService.shutdown();
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
    }

    private void executorTask() {
        try {
            String[] pools = ((Uniswap3PairDataProvider)exchangeSpecs.getPairDataProvider()).getPools(pairsApiSymbols);
            Uniswap3SwapsQuery swapsQuery = new Uniswap3SwapsQuery(pools, lastRefreshTimestampSeconds, Integer.MAX_VALUE);
            List<JsonNode> swapsNodes = theGraphHttpRequest.send(swapsQuery);
            List<Ticker> tickerList = new LinkedList<>();
            for (JsonNode swapsNode : swapsNodes) {
                Ticker[] tickers = Uniswap3SwapsToTickers.generateTickers(swapsNode);
                tickerList.addAll(Arrays.asList(tickers).stream().filter(t -> pairsSet.contains(t.getPair())).collect(Collectors.toList()));
            }
            if (tickerList.isEmpty()) {
                return;
            }
            lastRefreshTimestampSeconds = (int)tickerList.get(tickerList.size()-1).getTimestampSeconds();
            tickerReceiver.receiveTickers(tickerList.toArray(Ticker[]::new));
        } catch (Exception e) {
            logger.log(Level.WARNING, "When refreshing ticker", e);
        }

    }
}
