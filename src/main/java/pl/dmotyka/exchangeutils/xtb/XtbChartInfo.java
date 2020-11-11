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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartinfo.ChartTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.sync.ServerData;

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
