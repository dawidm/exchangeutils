package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.dmutils.runtime.ThreadPause;
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;

class BitfinexXchangeWebsocketTest {
    @Test
    public void testWebsocket() {
        StreamingExchange streamingExchange = StreamingExchangeFactory.INSTANCE.createExchange(BitfinexStreamingExchange.class.getName());
        // Connect to the Exchange WebSocket API. Blocking wait for the connection.
        streamingExchange.connect().blockingAwait();
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
        exchange.getExchangeMetaData().getCurrencyPairs().entrySet().stream().limit(50).forEach(entry -> {
            CurrencyPair currencyPair = entry.getKey();
            ThreadPause.millis(50);
            streamingExchange.getStreamingMarketDataService().getTrades(currencyPair).subscribe(trade -> System.out.println(trade.getCurrencyPair()));
        });

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}