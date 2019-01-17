package com.dawidmotyka.exchangeutils.chartdataprovider;

import com.dawidmotyka.dmutils.RepeatTillSuccess;
import com.dawidmotyka.dmutils.ThreadPause;
import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;
import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import com.dawidmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.tickerprovider.Ticker;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO option to provide pairs update
public class ChartDataProvider {

    private static final Logger logger = Logger.getLogger(ChartDataProvider.class.getName());

    //delay between getting candles for consecutive currencies
    public static final int GET_CHART_DATA_DELAY_MS = 500;
    public static final int NUM_RETRIES_FOR_PAIR=2;
    public static final int DEFAULT_NUM_CANDLES =50;

    private String[] pairs;
    private int[] timePeriodsSeconds;
    private ExchangeChartInfo exchangeChartInfo;
    private final Map<String,ChartCandle[]> chartCandlesMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String,List<Ticker>> tickersMap = new HashMap<>();
    private long lastCandlesGenerationTimestampSeconds=System.currentTimeMillis()/1000;
    private AtomicBoolean abortAtomicBoolean=new AtomicBoolean(false);
    private final Set<ChartDataReceiver> chartDataReceivers = new HashSet<>();
    private final AtomicInteger numCandlesAtomicInteger = new AtomicInteger(DEFAULT_NUM_CANDLES);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isRefreshingChartData=new AtomicBoolean(false);

    public ChartDataProvider(ExchangeSpecs exchangeSpecs, String[] pairs, int[] timePeriodsSeconds) {
        this.pairs = pairs;
        this.timePeriodsSeconds = timePeriodsSeconds;
        Arrays.sort(this.timePeriodsSeconds);
        exchangeChartInfo = ExchangeChartInfo.forExchange(exchangeSpecs);
    }

    public synchronized void refreshData() {
        logger.info("refreshing ChartDataProvider data");
        isRefreshingChartData.set(true);
        for(int currentPeriod : timePeriodsSeconds) {
            for (String currentPair : pairs) {
                if(abortAtomicBoolean.get()==true)
                    return;
                logger.fine("getting data for " + currentPair);
                RepeatTillSuccess.planTask(() -> {
                            if (abortAtomicBoolean.get() == true)
                                return;
                            getChartData(currentPair, numCandlesAtomicInteger.get(), currentPeriod);
                        },
                        (e) -> logger.log(Level.WARNING, "when getting data for " + currentPair, e),
                        GET_CHART_DATA_DELAY_MS,
                        NUM_RETRIES_FOR_PAIR,
                        () -> logger.log(Level.WARNING, String.format("reached maximum retires for getting data for %s", currentPair)));
                //delay before next api call
                ThreadPause.millis(GET_CHART_DATA_DELAY_MS);
            }
        }
        notifyChartDataReceivers();
        lastCandlesGenerationTimestampSeconds= System.currentTimeMillis()/1000-(System.currentTimeMillis()/1000)%timePeriodsSeconds[0];
        isRefreshingChartData.set(false);
        logger.info("finished refreshing data");
    }

    public void abort() {
        abortAtomicBoolean.set(true);
        scheduledExecutorService.shutdown();
    }

    public void subscribeChartCandles(ChartDataReceiver chartDataReceiver,int minimumNumCandles){
        chartDataReceivers.add(chartDataReceiver);
        numCandlesAtomicInteger.set(minimumNumCandles);
    }

    public ChartCandle[] requestCandlesGeneration(String symbol, int periodSeconds) throws IllegalArgumentException {
        if(!chartCandlesMap.containsKey(chartCandlesMapKey(symbol,periodSeconds)))
            throw new IllegalArgumentException(chartCandlesMapKey(symbol,periodSeconds)+" not available");
        generateCandles(symbol,periodSeconds);
        return chartCandlesMap.get(chartCandlesMapKey(symbol,periodSeconds));
    }

    //generate subsequent candles from tickers that should be provided using insertTicker method
    public void enableCandlesGenerator() {
        //todo period as parameter
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

    public Map<String, ChartCandle[]> getAllCandleData() {
        return Collections.unmodifiableMap(chartCandlesMap);
    }

    public ChartCandle[] getCandleData(String pair, int periodSeconds) {
        return chartCandlesMap.get(chartCandlesMapKey(pair,periodSeconds));
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
    private void chartCandlesGenerator() {
        if(isRefreshingChartData.get())
            return;
        long currentTimestampSnapshot=System.currentTimeMillis()/1000;
        List<Integer> updatedPeriodsList=null;
        for(int currentPeriod : timePeriodsSeconds) {
            if(currentTimestampSnapshot-currentTimestampSnapshot%currentPeriod > lastCandlesGenerationTimestampSeconds) {
                logger.fine("generating candles for period " + currentPeriod);
                if(updatedPeriodsList==null)
                    updatedPeriodsList=new ArrayList<>(timePeriodsSeconds.length);
                updatedPeriodsList.add(currentPeriod);
                for(String currentPair : pairs) {
                    logger.fine(String.format("generating candles for period %d for %s",currentPeriod,currentPair));
                    generateCandles(currentPair, currentPeriod);
                }
            }
        }
        if(updatedPeriodsList!=null) {
            int shortestUpdatedPeriod=updatedPeriodsList.get(0);
            lastCandlesGenerationTimestampSeconds=currentTimestampSnapshot-currentTimestampSnapshot%shortestUpdatedPeriod;
            //clean tickers
            long maxNotUpdatedTimePeriod=0;
            for(int timePeriod : timePeriodsSeconds) {
                if(!updatedPeriodsList.contains(new Integer(timePeriod))) {
                    maxNotUpdatedTimePeriod=Math.max(maxNotUpdatedTimePeriod,timePeriod);
                }
            }
            if(maxNotUpdatedTimePeriod!=0) {
                for(List<Ticker> tickerList : tickersMap.values()) {
                    Iterator<Ticker> tickersListIterator = tickerList.iterator();
                    while (tickersListIterator.hasNext() && tickersListIterator.next().getTimestampSeconds()<currentTimestampSnapshot-maxNotUpdatedTimePeriod)
                        tickersListIterator.remove();
                }
            }
            notifyChartDataReceivers();
        }
    }
    
    private void generateCandles(String pair, int timePeriodSeconds) {
        long currentTimestampSnapshot=System.currentTimeMillis()/1000;
        int newCandleTimestamp = (int)(currentTimestampSnapshot-currentTimestampSnapshot%timePeriodSeconds) - timePeriodSeconds;
        ChartCandle newChartCandle = null;
        List<Ticker> tickerList = tickersMap.get(pair);
        ChartCandle[] oldChartCandles = chartCandlesMap.get(chartCandlesMapKey(pair,timePeriodSeconds));
        if(tickerList==null) {
            if(oldChartCandles==null) {
                logger.fine("no ticker data and no candles from exchange for: " + pair);
                return;
            }
            double lastPrice = oldChartCandles[oldChartCandles.length-1].getClose();
            newChartCandle = new ChartCandle(lastPrice,lastPrice,lastPrice,lastPrice,newCandleTimestamp);
        } else {
            Ticker[] filteredTickers;
            synchronized (tickersMap.get(pair)) {
                filteredTickers = tickerList.stream().
                        filter(ticker -> ticker.getTimestampSeconds() >= newCandleTimestamp && ticker.getTimestampSeconds() < newCandleTimestamp + timePeriodSeconds).
                        toArray(Ticker[]::new);
            }
            if(filteredTickers.length==0) {
                double lastPrice = oldChartCandles[oldChartCandles.length-1].getClose();
                newChartCandle = new ChartCandle(lastPrice,lastPrice,lastPrice,lastPrice,newCandleTimestamp);
            } else {
                newChartCandle = new ChartCandle(Arrays.stream(filteredTickers).max(Comparator.comparingDouble(Ticker::getValue)).get().getValue(),
                        Arrays.stream(filteredTickers).min(Comparator.comparingDouble(Ticker::getValue)).get().getValue(),
                        filteredTickers[0].getValue(),
                        filteredTickers[filteredTickers.length - 1].getValue(),
                        newCandleTimestamp);
            }
        }
        ChartCandle[] newChartCandles=null;
        if(oldChartCandles[oldChartCandles.length-1].getTimestampSeconds()==newCandleTimestamp) {
            logger.finer("modifying last candle with new data for "+pair+timePeriodSeconds);
            ChartCandle oldLastChartCandle=oldChartCandles[oldChartCandles.length-1];
            newChartCandles=Arrays.copyOf(oldChartCandles,oldChartCandles.length);
            newChartCandles[newChartCandles.length-1]=new ChartCandle(Math.max(newChartCandle.getHigh(),oldLastChartCandle.getHigh()),
                    Math.min(newChartCandle.getLow(),oldLastChartCandle.getLow()),
                    oldLastChartCandle.getOpen(),
                    newChartCandle.getClose(),
                    newCandleTimestamp);
        } else {
            logger.finer("inserting new last candle, removing outdated candles for "+pair+timePeriodSeconds);
            int candlesToSkip = Math.max(0, oldChartCandles.length - numCandlesAtomicInteger.get());
            newChartCandles = Arrays.stream(oldChartCandles).skip(candlesToSkip).toArray(ChartCandle[]::new);
            newChartCandles = Arrays.copyOf(newChartCandles, newChartCandles.length + 1);
            newChartCandles[newChartCandles.length - 1] = newChartCandle;
        }
        chartCandlesMap.put(chartCandlesMapKey(pair,timePeriodSeconds),newChartCandles);
    }

    private void getChartData(String pair, long numCandles, int periodSeconds) throws ExchangeCommunicationException {
        try {
            long startTime = (System.currentTimeMillis() / 1000) - (numCandles * periodSeconds);
            long endTime = (System.currentTimeMillis() / 1000);
            ChartCandle[] chartCandles = exchangeChartInfo.getCandles(pair,periodSeconds,startTime,endTime);
            if(chartCandles.length==0)
                throw new ExchangeCommunicationException("got 0 candles");
            logger.fine(String.format("got %d chart candles for %s",chartCandles.length,pair));
            chartCandlesMap.put(chartCandlesMapKey(pair,periodSeconds),chartCandles);
        }
        catch (NoSuchTimePeriodException e) {
            logger.log(Level.WARNING,String.format("when getting data for %s, period: %s",pair,periodSeconds),e);
        }
    }

    public static String chartCandlesMapKey(String pair, int timePeriodSeconds) {
        return pair+","+timePeriodSeconds;
    }
 }
