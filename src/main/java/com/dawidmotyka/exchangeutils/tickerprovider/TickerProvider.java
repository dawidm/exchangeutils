package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.exchangeutils.exchangespecs.*;

import java.io.IOException;

public interface TickerProvider {
    static TickerProvider forExchange(ExchangeSpecs exchangeSpecs, TickerReceiver tickerReceiver, String[] pairs) {
        if(exchangeSpecs instanceof PoloniexExchangeSpecs) {
            return new PoloniexWebSocket(tickerReceiver,pairs);
        }
        if(exchangeSpecs instanceof BinanceExchangeSpecs) {
            return new BinanceTransactionsWebSocket(tickerReceiver, pairs);
        }
        if(exchangeSpecs instanceof BittrexExchangeSpecs) {
            return new BittrexWebsocketTickerProvider(tickerReceiver,pairs);
        }
        if(exchangeSpecs instanceof XtbExchangeSpecs)
            return new XtbTickerProvider(tickerReceiver,pairs);
        throw new Error("not implemented for "+exchangeSpecs.getName());
    };

    void connect() throws IOException;
    void disconnect();
}
