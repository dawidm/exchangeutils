package com.dawidmotyka.exchangeutils.tickerprovider;

import com.dawidmotyka.dmutils.RepeatTillSuccess;
import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.xtb.XtbConnectionManager;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.records.SKeepAliveRecord;
import pro.xstore.api.message.records.STickRecord;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.ServerData;

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

    public XtbTickerProvider(TickerReceiver tickerReceiver, String[] pairs) {
        this.pairs=pairs;
        this.tickerReceiver=tickerReceiver;
    }

    @Override
    public void connect() {
        RepeatTillSuccess.planTask(this::connectXtbConnectionManager, (e) -> logger.log(Level.WARNING, "", e), CONNECT_RETRY_PERIOD_SECONDS * 1000);
        RepeatTillSuccess.planTask(this::makeSubscriptions,(e)->logger.log(Level.WARNING,"when making subscriptions",e),CONNECT_RETRY_PERIOD_SECONDS*1000);
    }

    @Override
    public void disconnect() {
        if(keepAliveScheduledFuture!=null && !keepAliveScheduledFuture.isCancelled())
            keepAliveScheduledFuture.cancel(false);
        xtbConnectionManager.disconnect();
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
            connect();
        }
    }

    public void SubscribeAskBid(AskBidTickerReceiver askBidTickerReceiver) {
        this.askBidTickerReceiver=askBidTickerReceiver;
    }

}
