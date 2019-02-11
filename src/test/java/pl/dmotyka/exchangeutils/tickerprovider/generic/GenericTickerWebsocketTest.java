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

package pl.dmotyka.exchangeutils.tickerprovider.generic;

import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GenericTickerWebsocketTest {

    @Test
    public synchronized void connect() throws IOException, InterruptedException {
        String[] pairs = new String[] {"tBTCUSD","tETHUSD"};
        Set<String> pairsSet= new HashSet<>();
        GenericTickerWebsocket genericTickerWebsocket = new GenericTickerWebsocket(new TickerReceiver() {
            @Override
            public void receiveTicker(Ticker ticker) {
                System.out.println(ticker.getPair()+": "+ticker.getValue());
                pairsSet.add(ticker.getPair());
                if(pairsSet.size()==pairs.length)
                    synchronized (GenericTickerWebsocketTest.this) {
                        GenericTickerWebsocketTest.this.notify();
                    }
            }

            @Override
            public void receiveTickers(List<Ticker> tickers) {

            }

            @Override
            public void error(Throwable error) {

            }
        },pairs,new BitfinexExchangeMethods());
        genericTickerWebsocket.connect(state -> System.out.println(state));
        wait();
    }

}