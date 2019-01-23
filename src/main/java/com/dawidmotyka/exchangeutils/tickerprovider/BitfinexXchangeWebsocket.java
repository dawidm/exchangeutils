package com.dawidmotyka.exchangeutils.tickerprovider;

import java.io.IOException;

public class BitfinexXchangeWebsocket implements TickerProvider {

    private final TickerReceiver tickerReceiver;

    public BitfinexXchangeWebsocket(TickerReceiver tickerReceiver, String[] pairs) {
        this.tickerReceiver=tickerReceiver;
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) throws IOException {

    }

    @Override
    public void disconnect() {

    }
}
