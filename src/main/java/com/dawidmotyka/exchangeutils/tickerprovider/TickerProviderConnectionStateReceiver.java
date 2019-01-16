package com.dawidmotyka.exchangeutils.tickerprovider;

public interface TickerProviderConnectionStateReceiver {
    void connectionState(TickerProviderConnectionState state);
}
