package com.dawidmotyka.exchangeutils.tickerprovider.generic;

import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;

import javax.annotation.Nullable;

public interface GenericTickerWebsocketExchangeMethods {
    String getWsUrl(@Nullable String[] pairsSymbols);
    boolean isMakingSubscriptions();
    String[] subscriptionsMessages(String[] pairsSymbols);
    Ticker handleMessage(String message);
}