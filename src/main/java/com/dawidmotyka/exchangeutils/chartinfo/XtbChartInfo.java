package com.dawidmotyka.exchangeutils.chartinfo;

import com.dawidmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.xtb.XtbConnectionManager;
import com.dawidmotyka.exchangeutils.xtb.XtbUtils;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.sync.ServerData;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XtbChartInfo implements ExchangeChartInfo {

    Logger logger = Logger.getLogger(XtbChartInfo.class.getName());

    private XtbConnectionManager xtbConnectionManager=new XtbConnectionManager(ServerData.ServerEnum.REAL);

    @Override
    public ChartCandle[] getCandles(String symbol, long timePeriodSeconds, long beginTimestampSeconds, long endTimestampSeconds) throws NoSuchTimePeriodException, ExchangeCommunicationException {
        try {
            if(!xtbConnectionManager.isConnected())
                xtbConnectionManager.connect();
            synchronized (xtbConnectionManager.getConnectorLock()) {
                ChartResponse chartResponse = APICommandFactory.executeChartLastCommand(xtbConnectionManager.getConnector(), symbol, XtbUtils.periodSecondsToPeriodCode((int) timePeriodSeconds), beginTimestampSeconds * 1000);
                List<RateInfoRecord> rateInfoRecords = chartResponse.getRateInfos();
                ChartCandle[] chartCandles = rateInfoRecords.stream().map((rateInfoRecord) -> new ChartCandle(
                        (rateInfoRecord.getOpen() + rateInfoRecord.getHigh()) / Math.pow(10, chartResponse.getDigits()),
                        (rateInfoRecord.getOpen() + rateInfoRecord.getLow()) / Math.pow(10, chartResponse.getDigits()),
                        rateInfoRecord.getOpen() / Math.pow(10, chartResponse.getDigits()),
                        (rateInfoRecord.getOpen() + rateInfoRecord.getClose()) / Math.pow(10, chartResponse.getDigits()),
                        rateInfoRecord.getCtm() / 1000)).
                        toArray(ChartCandle[]::new);
                logger.finer(String.format("got %d candles", chartCandles.length));
                return chartCandles;
            }
        } catch (APICommunicationException | APIReplyParseException | APIErrorResponse | APICommandConstructionException e) {
            logger.log(Level.SEVERE,"when getting candles: " + symbol + timePeriodSeconds,e);
            throw new ExchangeCommunicationException("when getting candles");
        }
    }

    @Override
    public ChartTimePeriod[] getAvailablePeriods() {
        return new ChartTimePeriod[0];
    }
}
