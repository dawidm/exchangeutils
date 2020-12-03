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

package pl.dmotyka.exchangeutils.chartdataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidmotyka.dmutils.runtime.RepeatTillSuccess;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeWithTradingHours;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;
import pl.dmotyka.exchangeutils.tradinghoursprovider.TradingHours;
import pl.dmotyka.exchangeutils.tradinghoursprovider.TradingHoursProvider;

public class ChartDataProvider {

    private static final Logger logger = Logger.getLogger(ChartDataProvider.class.getName());

    public static final int NUM_RETRIES_FOR_PAIR=2;

    private final ExchangeSpecs exchangeSpecs;
    private final String[] pairs;
    private final PeriodNumCandles[] periodsNumCandles;
    private final ExchangeChartInfo exchangeChartInfo;
    private final Map<CurrencyPairTimePeriod, ChartCandle[]> chartCandlesMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String,List<Ticker>> tickersMap = new HashMap<>();
    private final Map<Long,Long> lastGeneratedCandlesTimestamps = new HashMap<>();
    private final AtomicBoolean abortAtomicBoolean=new AtomicBoolean(false);
    private final Set<ChartDataReceiver> chartDataReceivers = new HashSet<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isRefreshingChartData=new AtomicBoolean(false);

    public ChartDataProvider(ExchangeSpecs exchangeSpecs, String[] pairs, PeriodNumCandles[] periodsNumCandles) {
        this.exchangeSpecs=exchangeSpecs;
        this.pairs=pairs;
        this.periodsNumCandles = periodsNumCandles;
        Arrays.sort(this.periodsNumCandles);
        exchangeChartInfo = exchangeSpecs.getChartInfo();
        for(PeriodNumCandles periodNumCandles : periodsNumCandles) {
            long periodSeconds = periodNumCandles.getPeriodSeconds();
            long timestampSnapshot = System.currentTimeMillis()/1000;
            lastGeneratedCandlesTimestamps.put(periodNumCandles.getPeriodSeconds(),timestampSnapshot-timestampSnapshot%periodSeconds-periodSeconds);
        }
    }

    public static ChartTimePeriod[] getAvailableTimePeriods(ExchangeSpecs exchangeSpecs) {
        return exchangeSpecs.getChartInfo().getAvailablePeriods();
    };

    public synchronized void refreshData() {
        logger.info("refreshing ChartDataProvider data");
        isRefreshingChartData.set(true);
        for (String currentPair : pairs) {
            for(PeriodNumCandles currentPeriodNumCandles : periodsNumCandles) {
                if(abortAtomicBoolean.get())
                    break;
                logger.fine("getting data for " + currentPair);
                RepeatTillSuccess.planTask(() -> {
                            if (abortAtomicBoolean.get())
                                return;
                            getChartData(currentPair, currentPeriodNumCandles.getNumCandles(), currentPeriodNumCandles.getPeriodSeconds());
                        },
                        (e) -> logger.log(Level.WARNING, "when getting data for " + currentPair, e),
                        exchangeSpecs.getDelayBetweenChartDataRequestsMs(),
                        NUM_RETRIES_FOR_PAIR,
                        () -> logger.log(Level.WARNING, String.format("reached maximum retires for getting data for %s", currentPair)));
                //delay before next api call
                try {Thread.sleep(exchangeSpecs.getDelayBetweenChartDataRequestsMs());} catch (InterruptedException e) {return;};
            }
        }
        notifyChartDataReceivers();
        isRefreshingChartData.set(false);
        logger.info("finished refreshing data");
    }

    public void abort() {
        abortAtomicBoolean.set(true);
        scheduledExecutorService.shutdown();
    }

    public void subscribeChartCandles(ChartDataReceiver chartDataReceiver){
        chartDataReceivers.add(chartDataReceiver);
    }

    public ChartCandle[] requestCandlesGeneration(CurrencyPairTimePeriod currencyPairTimePeriod) throws IllegalArgumentException {
        if(!chartCandlesMap.containsKey(currencyPairTimePeriod))
            throw new IllegalArgumentException(currencyPairTimePeriod+" not available");
        generateCandles(currencyPairTimePeriod.getCurrencyPairSymbol(),currencyPairTimePeriod.getTimePeriodSeconds());
        ChartCandle[] oldArray = chartCandlesMap.get(currencyPairTimePeriod);
        return Arrays.copyOf(oldArray,oldArray.length);
    }

    //generate subsequent candles from tickers that should be provided using insertTicker method
    //automatic candle generation occurs at the end of every time period
    public void enableCandlesGenerator() {
        logger.fine("enabling candles generator");
        scheduledExecutorService.scheduleAtFixedRate(this::chartCandlesGeneratorExecutor,1,1,TimeUnit.SECONDS);
    }

    public void insertTicker(Ticker ticker) {
        List<Ticker> tickerList = tickersMap.get(ticker.getPair());
        if (tickerList == null) {
            tickerList = Collections.synchronizedList(new LinkedList<>());
            tickersMap.put(ticker.getPair(), tickerList);
        }
        synchronized (tickerList) {
            tickerList.add(ticker);
        }
    }

    public Map<CurrencyPairTimePeriod, ChartCandle[]> getAllCandleData() {
        Map<CurrencyPairTimePeriod,ChartCandle[]> allDataMap = new HashMap<>();
        for(Map.Entry<CurrencyPairTimePeriod,ChartCandle[]> oldMapEntry : chartCandlesMap.entrySet()) {
            allDataMap.put(oldMapEntry.getKey(),Arrays.copyOf(oldMapEntry.getValue(),oldMapEntry.getValue().length));
        }
        return allDataMap;
    }

    public Map<CurrencyPairTimePeriod, ChartCandle[]> getAllCandleDataForPeriod(long timePeriodSeconds) {
        Map<CurrencyPairTimePeriod,ChartCandle[]> allDataMap = new HashMap<>();
        for(Map.Entry<CurrencyPairTimePeriod,ChartCandle[]> oldMapEntry : chartCandlesMap.entrySet()) {
            if(oldMapEntry.getKey().getTimePeriodSeconds()==timePeriodSeconds)
                allDataMap.put(oldMapEntry.getKey(),Arrays.copyOf(oldMapEntry.getValue(),oldMapEntry.getValue().length));
        }
        return allDataMap;
    }

    public synchronized ChartCandle[] getCandleData(CurrencyPairTimePeriod currencyPairTimePeriod) {
        ChartCandle[] oldArray = chartCandlesMap.get(currencyPairTimePeriod);
        return Arrays.copyOf(oldArray,oldArray.length);
    }

    private void notifyChartDataReceivers() {
        if(!chartDataReceivers.isEmpty()) {
            logger.fine("sending update notification to ChartDataReceivers");
            for (ChartDataReceiver chartDataReceiver:chartDataReceivers)
                chartDataReceiver.onChartCandlesUpdate(Collections.unmodifiableMap(chartCandlesMap));
        }
    }

    private void chartCandlesGeneratorExecutor() {
        try { chartCandlesGenerator(); }
        catch (Exception e) {
            logger.log(Level.WARNING,"when generating chart candles",e);
        }
    }

    //generates candles from tickersMap
    private synchronized void chartCandlesGenerator() {
        if(isRefreshingChartData.get())
            return;
        int currentTimestampSnapshot=(int)(System.currentTimeMillis()/1000);
        boolean longestPeriodUpdated=false;
        boolean anyPeriodUpdated=false;
        long longestTimePeriod=periodsNumCandles[periodsNumCandles.length-1].getPeriodSeconds();
        for(PeriodNumCandles periodNumCandles : periodsNumCandles) {
            long periodSeconds = periodNumCandles.getPeriodSeconds();
            if(currentTimestampSnapshot-currentTimestampSnapshot%periodSeconds-periodSeconds > lastGeneratedCandlesTimestamps.get(periodSeconds)) {
                logger.fine("generating candles for period " + periodSeconds + "s");
                lastGeneratedCandlesTimestamps.put(periodSeconds,currentTimestampSnapshot-currentTimestampSnapshot%periodSeconds-periodSeconds);
                for(String currentPair : pairs) {
                    logger.finer(String.format("generating candles for period %d for %s",periodSeconds,currentPair));
                    generateCandles(currentPair, periodSeconds);
                }
                anyPeriodUpdated=true;
                if(periodSeconds==longestTimePeriod)
                    longestPeriodUpdated=true;
            }
        }
        if(longestPeriodUpdated) {
            for(List<Ticker> tickerList : tickersMap.values()) {
                Iterator<Ticker> tickersListIterator = tickerList.iterator();
                while (tickersListIterator.hasNext() && tickersListIterator.next().getTimestampSeconds()<currentTimestampSnapshot-longestTimePeriod)
                    tickersListIterator.remove();
            }
        }
        if(anyPeriodUpdated)
            notifyChartDataReceivers();
    }

    private synchronized void generateCandles(String pair, long timePeriodSeconds) {
        long currentTimestampSnapshot=(System.currentTimeMillis()/1000);
        long newCandleTimestamp = (currentTimestampSnapshot-currentTimestampSnapshot%timePeriodSeconds) - timePeriodSeconds;
        ChartCandle newChartCandle;
        List<Ticker> tickerList = tickersMap.get(pair);
        ChartCandle[] oldChartCandles = chartCandlesMap.get(new CurrencyPairTimePeriod(pair,timePeriodSeconds));
        if(oldChartCandles==null || oldChartCandles.length==0) {
            logger.severe("no previous candles for " + pair + "," + timePeriodSeconds + "candle not generated");
            return;
        }
        double lastClose = oldChartCandles[oldChartCandles.length-1].getClose();
        double maxTicker = lastClose;
        double minTicker = lastClose;
        double firstTicker = lastClose;
        double lastTicker = lastClose;
        if(tickerList==null) {
            tickerList = new ArrayList<Ticker>();
        }
        Ticker[] filteredTickers;
        synchronized (tickerList) {
            filteredTickers = tickerList.stream().
                    filter(ticker -> ticker.getTimestampSeconds() >= newCandleTimestamp && ticker.getTimestampSeconds() < newCandleTimestamp + timePeriodSeconds).
                    toArray(Ticker[]::new);
        }
        if(filteredTickers.length==0) {
            logger.finer(String.format("no new tickers for %s, generating candle with previous close price", pair));
        } else {
            maxTicker = Arrays.stream(filteredTickers).max(Comparator.comparingDouble(Ticker::getValue)).get().getValue();
            minTicker = Arrays.stream(filteredTickers).min(Comparator.comparingDouble(Ticker::getValue)).get().getValue();
            firstTicker = filteredTickers[0].getValue();
            lastTicker = filteredTickers[filteredTickers.length-1].getValue();
        }
        if(oldChartCandles[oldChartCandles.length-1].getTimestampSeconds()==newCandleTimestamp && filteredTickers.length>0) {
            logger.finer("modifying last candle with new data for "+pair+timePeriodSeconds);
            ChartCandle oldLastChartCandle=oldChartCandles[oldChartCandles.length-1];
            oldChartCandles[oldChartCandles.length-1]=new ChartCandle(Math.max(maxTicker,oldLastChartCandle.getHigh()),
                    Math.min(minTicker,oldLastChartCandle.getLow()),
                    oldLastChartCandle.getOpen(),
                    filteredTickers[filteredTickers.length-1].getValue(),
                    newCandleTimestamp);
        } else {
            newChartCandle = new ChartCandle(maxTicker, minTicker, firstTicker, lastTicker, newCandleTimestamp);
            logger.finer("inserting new last candle, removing outdated candle for "+pair+timePeriodSeconds);
            PeriodNumCandles periodNumCandles=Arrays.stream(periodsNumCandles).filter(o -> o.getPeriodSeconds()==timePeriodSeconds).findAny().get();
            long minValidTimestampSeconds=currentTimestampSnapshot-timePeriodSeconds*periodNumCandles.getNumCandles()-currentTimestampSnapshot%periodNumCandles.getPeriodSeconds();
            List<ChartCandle> newChartCandles = new ArrayList<>(oldChartCandles.length+1);
            newChartCandles.addAll(Arrays.asList(oldChartCandles));
            newChartCandles.add(newChartCandle);
            logger.finer(String.format("Number of candles in array: %d.", newChartCandles.size()));
            chartCandlesMap.put(new CurrencyPairTimePeriod(pair,timePeriodSeconds),
                    newChartCandles.stream().filter(chartCandle -> chartCandle.getTimestampSeconds()>=minValidTimestampSeconds).toArray(ChartCandle[]::new));
        }
    }

    private void getChartData(String pair, int numCandles, long periodSeconds) throws ExchangeCommunicationException {
        try {
            long startTime;
            // TODO add methods (ChartInfo) for getting specified number of candles (e.g. xtb has api method to get N last candles instead of specifying timeframe)
            // now it tries to take enough candles but it could fail for longer time periods
            if (exchangeSpecs instanceof ExchangeWithTradingHours)
                startTime = Math.min(System.currentTimeMillis() / 1000 - 7*24*60*60, (System.currentTimeMillis() / 1000) - (numCandles * periodSeconds));
            else
                startTime = (System.currentTimeMillis() / 1000) - (numCandles * periodSeconds);
            long endTime = (System.currentTimeMillis() / 1000);
            ChartCandle[] chartCandles = exchangeChartInfo.getCandles(pair,periodSeconds,startTime,endTime);
            if(chartCandles.length==0)
                throw new ExchangeCommunicationException("got 0 candles");
            logger.fine(String.format("got %d chart candles for %s,%d",chartCandles.length,pair,periodSeconds));
            if (exchangeSpecs instanceof ExchangeWithTradingHours) {
                TradingHoursProvider tradingHoursProvider = ((ExchangeWithTradingHours) exchangeSpecs).getTradingHoursProvider();
                chartCandles = insertMissingCandles(chartCandles, numCandles, periodSeconds, tradingHoursProvider.getTradingHours(pair));
            }
            else
                chartCandles = insertMissingCandles(chartCandles, startTime, endTime, periodSeconds);
            chartCandlesMap.put(new CurrencyPairTimePeriod(pair,periodSeconds),chartCandles);
        }
        catch (NoSuchTimePeriodException e) {
            logger.log(Level.WARNING,String.format("when getting data for %s, period: %s",pair,periodSeconds),e);
            throw new ExchangeCommunicationException("no such time period: " + e.getMessage());
        }
    }

    public static ChartCandle[] insertMissingCandles(ChartCandle[] candles, long startTimeSec, long endTimeSec, long periodSec) {
        if (candles.length == 0)
            throw new IllegalArgumentException("candles array must not be empty");
        if (endTimeSec < startTimeSec)
            throw new IllegalArgumentException("end time should be later than start time");
        Map<Long, ChartCandle> candlesMap = new HashMap<>();
        for (ChartCandle candle : candles)
            candlesMap.put(candle.getTimestampSeconds(), candle);
        long firstCandleTime = startTimeSec + (periodSec - startTimeSec % periodSec);
        long lastCandleTime = endTimeSec - endTimeSec % periodSec;
        long currentCandleTime = firstCandleTime;
        long numNewCandles = (lastCandleTime - firstCandleTime)/periodSec + 1;
        List<ChartCandle> newCandles = new ArrayList<>((int)numNewCandles);
        while (currentCandleTime <= lastCandleTime) {
            if (candlesMap.containsKey(currentCandleTime))
                newCandles.add(candlesMap.get(currentCandleTime));
            else {
                if (newCandles.size() > 0) {
                    double lastClose = newCandles.get(newCandles.size()-1).getClose();
                    newCandles.add(new ChartCandle(lastClose, lastClose, lastClose, lastClose, currentCandleTime));
                } else {
                    double nextOpen = candles[0].getOpen();
                    newCandles.add(new ChartCandle(nextOpen, nextOpen, nextOpen, nextOpen, currentCandleTime));
                }
            }
            currentCandleTime += periodSec;
        }
        return newCandles.toArray(new ChartCandle[0]);
    }

    public static ChartCandle[] insertMissingCandles(ChartCandle[] candles, int numCandles, long periodSec, TradingHours tradingHours) {
        List<Long> newCandlesTimestamps = new LinkedList<>();
        int i;
        for (i = candles.length-1; i>=0; i--) {
            ChartCandle currentCandle = candles[i];
            if (!newCandlesTimestamps.isEmpty() && currentCandle.getTimestampSeconds() > newCandlesTimestamps.get(0))
                continue;
            Optional<TradingHours.TradingSession> optCurrentSession = tradingHours.getTradingSession(currentCandle.getTimestampSeconds());
            if(optCurrentSession.isEmpty())
                logger.warning("got chart candle which is not within trading hours, aborting inserting missing candles for trading hours exchange");
            TradingHours.TradingSession currentSession = optCurrentSession.get();
            LinkedList<Long> currentSessionTimestamps = new LinkedList<>();
            long currentTimeSec = System.currentTimeMillis()/1000;
            if (currentCandle.getTimestampSeconds() > currentTimeSec) {
                logger.severe("chart candle from a future, timestamp: " + currentCandle.getTimestampSeconds() + ", aborting inserting missing candles for trading hours exchange");
                return candles;
            }
            long maxSessionEnd = Math.min(currentSession.getTimestampEndSeconds(), currentTimeSec - currentTimeSec % periodSec);
            for (long currentTimestamp=currentSession.getTimestampStartSeconds(); currentTimestamp<maxSessionEnd; currentTimestamp+=periodSec) {
                currentSessionTimestamps.add(currentTimestamp);
            }
            currentSessionTimestamps.addAll(newCandlesTimestamps);
            newCandlesTimestamps = currentSessionTimestamps;
            if (newCandlesTimestamps.size() >= numCandles)
                break;
        }
        if (i==0)
            logger.warning("not enough candles with change to correctly generate missing (no change) candles for trading hours exchange");
        newCandlesTimestamps = newCandlesTimestamps.subList(newCandlesTimestamps.size()-numCandles, newCandlesTimestamps.size());
        Map<Long, ChartCandle> oldCandlesMap = new HashMap<>();
        Arrays.stream(candles).forEach(candle -> oldCandlesMap.put(candle.getTimestampSeconds(), candle));
        List<ChartCandle> newChartCandles = new ArrayList<>(numCandles);
        ChartCandle lastChangeCandle = candles[Math.max(0, i-1)];
        for (long timestamp : newCandlesTimestamps) {
            if(oldCandlesMap.containsKey(timestamp)) {
                newChartCandles.add(oldCandlesMap.get(timestamp));
                lastChangeCandle = oldCandlesMap.get(timestamp);
            } else {
                double lastClose = lastChangeCandle.getClose();
                newChartCandles.add(new ChartCandle(lastClose, lastClose, lastClose, lastClose, timestamp));
            }
        }
        return newChartCandles.toArray(new ChartCandle[0]);
    }

 }
