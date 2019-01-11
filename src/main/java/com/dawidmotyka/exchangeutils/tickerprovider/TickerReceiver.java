package com.dawidmotyka.exchangeutils.tickerprovider;

import java.util.List;

/**
 * Created by dawid on 8/13/17.
 */
public interface TickerReceiver {
    void receiveTicker(Ticker ticker);
    //receive number of tickers but FOR SINGLE CURRENCY PAIR
    void receiveTickers(List<Ticker> tickers);
    void error(Throwable error);
}
