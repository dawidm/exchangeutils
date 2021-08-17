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
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.binance.BinanceExchangeMethods;
import pl.dmotyka.exchangeutils.bitfinex.BitfinexExchangeMethods;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.kucoin.KuCoinExchangeMethods;
import pl.dmotyka.exchangeutils.poloniex.PoloniexExchangeMethods;

class GenericWebsocketTickerProviderTest {

    @Test
    public synchronized void connectPoloniex() throws IOException, InterruptedException, ExchangeCommunicationException {
        testConnect(new PoloniexExchangeMethods(), new String[] {"USDT_BTC", "USDT_ETH"});
    }

    @Test
    public synchronized void connectBitfinex() throws IOException, InterruptedException, ExchangeCommunicationException {
        testConnect(new BitfinexExchangeMethods(), new String[] {"tBTCUSD","tETHUSD"});
    }

    @Test
    public synchronized void connectBinance() throws IOException, InterruptedException, ExchangeCommunicationException {
        testConnect(new BinanceExchangeMethods(), new String[] {"BTCUSDT","ETHBTC"});
    }

    @Test
    public synchronized void connectKuCoin() throws IOException, InterruptedException, ExchangeCommunicationException {
        testConnect(new KuCoinExchangeMethods(), new String[] {"BTC-USDT","ETH-USDT"});
    }

    private void testConnect(GenericTickerWebsocketExchangeMethods exchangeMethods, String[] pairs) throws IOException, InterruptedException, ExchangeCommunicationException {
        Set<String> pairsSet= new HashSet<>();
        GenericWebsocketTickerProvider genericWebsocketTickerProvider = new GenericWebsocketTickerProvider(new TickerReceiver() {
            @Override
            public void receiveTicker(Ticker ticker) {
                System.out.println(ticker.getPair()+": "+ticker.getValue());
                pairsSet.add(ticker.getPair());
                if(pairsSet.size()==pairs.length)
                    synchronized (GenericWebsocketTickerProviderTest.this) {
                        GenericWebsocketTickerProviderTest.this.notify();
                    }
            }

            @Override
            public void receiveTickers(Ticker[] tickers) {
                for (Ticker ticker : tickers)
                    receiveTicker(ticker);
            }

            @Override
            public void error(Throwable error) {

            }
        }, pairs, exchangeMethods);
        genericWebsocketTickerProvider.connect(System.out::println);
        wait();
        genericWebsocketTickerProvider.disconnect();
    }

}