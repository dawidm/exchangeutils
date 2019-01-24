package com.dawidmotyka.exchangeutils.tickerprovider.generic;

import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;
import com.dawidmotyka.exchangeutils.tickerprovider.TickerReceiver;
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