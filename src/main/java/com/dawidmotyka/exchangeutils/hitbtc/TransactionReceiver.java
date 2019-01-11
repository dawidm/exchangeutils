package com.dawidmotyka.exchangeutils.hitbtc;

import com.dawidmotyka.exchangeutils.poloniex.Transaction;

/**
 * Created by dawid on 6/2/17.
 */
public interface TransactionReceiver {

    //send transaction with tradeId -1 to notify to force check changes
    void receive(Transaction transaction);

    void notifyCheckChanges(String pair);

    void heartbeat();
    void wsConnected();
    void wsDisconnected(int code, String reason, boolean remote);
    void wsException(Exception e);
}
