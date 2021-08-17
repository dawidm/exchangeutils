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

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.huobi.HuobiExchangeMethods;

class JSRWebsocketTickerProviderTest {

    @Test
    void testConnectHuobi() throws IOException, InterruptedException, ExchangeCommunicationException {
        TickerProvider tp = new JSRWebsocketTickerProvider(new TickerReceiver() {
            @Override
            public void receiveTicker(Ticker ticker) {

            }

            @Override
            public void receiveTickers(Ticker[] tickers) {
                if (tickers != null)
                    for (Ticker ticker : tickers) {
                        System.out.print(ticker.getPair() + " ");
                        System.out.println(ticker.getValue());
                    }
            }

            @Override
            public void error(Throwable error) {
                error.printStackTrace();
            }
        }, new String[] {"btcusdt"}, new HuobiExchangeMethods());
        tp.connect(state -> System.out.println(state.toString()));
        Thread.sleep(5000);
        tp.disconnect();

    }
}