package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.exchangeutils.exchangespecs.*;
import com.dawidmotyka.exchangeutils.poloniex.PoloniexWebSocket;
import com.dawidmotyka.exchangeutils.tickerprovider.generic.BitfinexExchangeMethods;
import com.dawidmotyka.exchangeutils.tickerprovider.generic.GenericTickerWebsocket;

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
        if(exchangeSpecs instanceof BitfinexExchangeSpecs)
            return new GenericTickerWebsocket(tickerReceiver,pairs,new BitfinexExchangeMethods());
        throw new Error("not implemented for "+exchangeSpecs.getName());
    };

    void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException;
    void disconnect();
}
