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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;

class Uniswap3TickerProviderTest {

    @Test
    void tickerProviderTest() throws ConnectionProblemException, ExchangeCommunicationException, IOException, InterruptedException {
        Uniswap3ExchangeSpecs exchangeSpecs = new Uniswap3ExchangeSpecs();
        String[] symbols = exchangeSpecs.getPairDataProvider().getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("USD", 100000)});
        // WETH
        Optional<String> optionalSymbol = Arrays.stream(symbols).filter(s -> s.equals("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2_USD")).findAny();
        // USDC
        Optional<String> optional1Symbol = Arrays.stream(symbols).filter(s -> s.equals("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48_USD")).findAny();
        String symbol = optionalSymbol.orElseGet(() -> symbols[0]);
        String symbol1 = optional1Symbol.orElseGet(() -> symbols[1]);
        Set<String> resultsSymbolsSet = new HashSet<>();
        TickerProvider tickerProvider = exchangeSpecs.getTickerProvider(new TickerReceiver() {
            @Override
            public void receiveTicker(Ticker ticker) {
                System.out.println(exchangeSpecs.getPairSymbolConverter().toFormattedString(ticker.getPair()) + " " + ticker.getValue());
                resultsSymbolsSet.add(ticker.getPair());
                if (resultsSymbolsSet.size() == 2) {
                    synchronized (Uniswap3TickerProviderTest.this) {
                        Uniswap3TickerProviderTest.this.notify();
                    }
                }
            }

            @Override
            public void receiveTickers(Ticker[] tickers) {
                for (Ticker ticker : tickers) {
                    System.out.println(exchangeSpecs.getPairSymbolConverter().toFormattedString(ticker.getPair()) + " " + ticker.getValue());
                    resultsSymbolsSet.add(ticker.getPair());
                    if (resultsSymbolsSet.size() == 2) {
                        synchronized (Uniswap3TickerProviderTest.this) {
                            Uniswap3TickerProviderTest.this.notify();
                        }
                    }
                }
            }

            @Override
            public void error(Throwable error) {
                throw new RuntimeException("", error);
            }
        }, new String[] {symbol, symbol1});
        tickerProvider.connect(System.out::println);
        synchronized(this) {
            // waiting to get tickers for 2 symbols
            wait();
        }
    }
}