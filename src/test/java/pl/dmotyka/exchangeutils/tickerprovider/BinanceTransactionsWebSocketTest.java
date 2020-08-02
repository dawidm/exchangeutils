/*
 * Copyright 2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pl.dmotyka.exchangeutils.tickerprovider;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.binance.BinancePairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BinanceTransactionsWebSocketTest {
    @Test
    public synchronized void testWebSocketConnection() throws IOException, InterruptedException {
        BinancePairDataProvider binancePairDataProvider=new BinancePairDataProvider();
        String[] pairs = binancePairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",1),new PairSelectionCriteria("BNB",1)});
        assertNotNull(pairs);
        assertTrue(pairs.length>0);
        TickerReceiver tickerReceiver = new TickerReceiver() {
            @Override
            public void receiveTicker(Ticker ticker) {
                assertTrue(ticker != null);
                assertTrue(ticker.getValue() != 0);
                assertTrue(ticker.getPair() != null && !ticker.getPair().equals(""));
                assertTrue(ticker.getTimestampSeconds() != 0);
                synchronized (BinanceTransactionsWebSocketTest.this) {
                    BinanceTransactionsWebSocketTest.this.notify();
                }
            }

            @Override
            public void receiveTickers(List<Ticker> tickers) {
                assertTrue(tickers.size()>0);
                Ticker ticker = tickers.get(0);
                assertNotNull(ticker);
                assertTrue(ticker.getValue() != 0);
                assertTrue(ticker.getPair() != null && !ticker.getPair().equals(""));
                assertTrue(ticker.getTimestampSeconds() != 0);
            }

            @Override
            public void error(Throwable error) {
                fail();
            }
        };
        AtomicReference<TickerProviderConnectionState> connectionStateAtomicReference = new AtomicReference<>();
        BinanceTransactionsWebSocket binanceTransactionsWebSocket = new BinanceTransactionsWebSocket(tickerReceiver, pairs);
        binanceTransactionsWebSocket.connect(state -> {
            connectionStateAtomicReference.set(state);
        });
        wait();
        assertSame(connectionStateAtomicReference.get(), TickerProviderConnectionState.CONNECTED);
    }
}