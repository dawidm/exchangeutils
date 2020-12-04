/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.xtb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidmotyka.dmutils.runtime.RepeatTillSuccess;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.tickerprovider.AskBidTicker;
import pl.dmotyka.exchangeutils.tickerprovider.AskBidTickerReceiver;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProvider;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionState;
import pl.dmotyka.exchangeutils.tickerprovider.TickerProviderConnectionStateReceiver;
import pl.dmotyka.exchangeutils.tickerprovider.TickerReceiver;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.records.SKeepAliveRecord;
import pro.xstore.api.message.records.STickRecord;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.ServerData;

public class XtbTickerProvider implements TickerProvider {

    private static final Logger logger = Logger.getLogger(XtbTickerProvider.class.getName());
    
    public static final int CONNECT_RETRY_PERIOD_SECONDS=60;
    public static final int MAX_KEEP_ALIVE_DELAY_SECONDS=60;

    private final XtbConnectionManager xtbConnectionManager=new XtbConnectionManager(ServerData.ServerEnum.REAL);
    private final AtomicReference<Long> keepAliveAtomicReference = new AtomicReference<>();
    private String[] pairs;
    private TickerReceiver tickerReceiver;
    private AskBidTickerReceiver askBidTickerReceiver;
    private ScheduledFuture keepAliveScheduledFuture;
    private ScheduledExecutorService scheduledExecutorService=Executors.newScheduledThreadPool(1);
    private TickerProviderConnectionStateReceiver connectionStateReceiver;

    public XtbTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        this.pairs=pairs;
        this.tickerReceiver=tickerReceiver;
    }

    @Override
    public void connect(TickerProviderConnectionStateReceiver connectionStateReceiver) {
        this.connectionStateReceiver=connectionStateReceiver;
        RepeatTillSuccess.planTask(this::connectXtbConnectionManager, (e) -> logger.log(Level.WARNING, "", e), CONNECT_RETRY_PERIOD_SECONDS * 1000);
        RepeatTillSuccess.planTask(this::makeSubscriptions,(e)->logger.log(Level.WARNING,"when making subscriptions",e),CONNECT_RETRY_PERIOD_SECONDS*1000);
        connectionStateReceiver.connectionState(TickerProviderConnectionState.CONNECTED);
    }

    @Override
    public void disconnect() {
        if(keepAliveScheduledFuture!=null && !keepAliveScheduledFuture.isCancelled())
            keepAliveScheduledFuture.cancel(false);
        connectionStateReceiver.connectionState(TickerProviderConnectionState.DISCONNECTED);
        try {
            xtbConnectionManager.disconnect();
        } catch (ExchangeCommunicationException e) {
            logger.warning("when disconnecting xtbConnectionManager: " + e.getClass().getName() + e.getMessage());
        }
    }

    private void connectXtbConnectionManager() throws ExchangeCommunicationException {
        xtbConnectionManager.connect();
        if(!xtbConnectionManager.isConnected())
            throw new ExchangeCommunicationException("");
    }

    private void makeSubscriptions() throws IOException, APICommunicationException {
        synchronized (xtbConnectionManager.getConnectorLock()) {
            logger.info("making subscribtions for pairs");
            StreamingListener streamingListener = new StreamingListener() {
                @Override
                public void receiveTickRecord(STickRecord tickRecord) {
                    if(tickRecord.getLevel()!=0)
                        return;
                    logger.finest(String.format("%s: ask: %.5f (vol: %d), bid: %.5f (vol: %d)",tickRecord.getSymbol(),tickRecord.getAsk(),tickRecord.getAskVolume(),tickRecord.getBid(),tickRecord.getBidVolume()));
                    List<Ticker> tickerList = new ArrayList<>(2);
                    double askBidAverage=0;
                    long askBidTimestamp=0;
                    if(tickRecord.getAsk()!=null && tickRecord.getAsk().doubleValue()!=0) {
                        if(askBidTickerReceiver!=null) {
                            askBidTickerReceiver.receiveAskBidTicker(new AskBidTicker(tickRecord.getSymbol(),tickRecord.getAsk(),null));
                        }
                        askBidAverage+=tickRecord.getAsk().doubleValue();
                        askBidTimestamp=tickRecord.getTimestamp();
                    }
                    if(tickRecord.getBid()!=null && tickRecord.getBid().doubleValue()!=0) {
                        if(askBidTickerReceiver!=null) {
                            askBidTickerReceiver.receiveAskBidTicker(new AskBidTicker(tickRecord.getSymbol(),null,tickRecord.getBid()));
                        }
                        if(askBidAverage!=0 || askBidTimestamp!=0) {
                            askBidAverage+=tickRecord.getBid().doubleValue();
                            askBidAverage/=2;
                        }
                    }
                    tickerReceiver.receiveTicker(new Ticker(tickRecord.getSymbol(),askBidAverage,askBidTimestamp/1000));
                }
                @Override
                public void receiveKeepAliveRecord(SKeepAliveRecord keepAliveRecord) {
                    keepAliveAtomicReference.set(System.currentTimeMillis());
                }
            };
            xtbConnectionManager.getConnector().connectStream(streamingListener);
            xtbConnectionManager.getConnector().subscribeKeepAlive();
            for (String pair : pairs) {
                logger.fine("making subscribtion for: " + pair);
                xtbConnectionManager.getConnector().subscribePrice(pair);
            }
            keepAliveAtomicReference.set(System.currentTimeMillis());
            logger.info("finished making subscribtions for pairs");
            keepAliveScheduledFuture=scheduledExecutorService.scheduleAtFixedRate(this::keepAliveWatcher,1,1,TimeUnit.SECONDS);
        }
    }

    private void keepAliveWatcher() {
        if ((int)((System.currentTimeMillis()-keepAliveAtomicReference.get())/1000)>MAX_KEEP_ALIVE_DELAY_SECONDS) {
            logger.warning("keep alive not received restarting connection");
            disconnect();
            connect(connectionStateReceiver);
        }
    }

    public void SubscribeAskBid(AskBidTickerReceiver askBidTickerReceiver) {
        this.askBidTickerReceiver=askBidTickerReceiver;
    }

}
