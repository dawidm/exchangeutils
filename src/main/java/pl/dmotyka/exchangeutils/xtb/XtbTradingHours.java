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

import java.time.DayOfWeek;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.exchangespecs.TradingHours;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.HoursRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.TradingHoursResponse;
import pro.xstore.api.sync.ServerData;

public class XtbTradingHours {

    public static final Logger logger = Logger.getLogger(XtbTradingHours.class.getName());

    private final XtbConnectionManager xtbConnectionManager=new XtbConnectionManager(ServerData.ServerEnum.REAL);

    public TradingHours getTradingHours(String symbol) throws ExchangeCommunicationException {
        try {
            if(!xtbConnectionManager.isConnected())
                xtbConnectionManager.connect();
            synchronized (xtbConnectionManager.getConnectorLock()) {
                TradingHoursResponse resp = APICommandFactory.executeTradingHoursCommand(xtbConnectionManager.getConnector(), List.of(symbol));
                List<HoursRecord> hoursRecords = resp.getTrading().get(0);
                if (hoursRecords.isEmpty())
                    throw new ExchangeCommunicationException("trading hours are empty");
                logger.fine("got trading hours for "+ symbol);
                return new TradingHours(hoursRecords.stream().map(this::hoursRecordToTradingDay).toArray(TradingHours.TradingDay[]::new));
            }
        } catch (APICommunicationException | APIReplyParseException | APIErrorResponse | APICommandConstructionException e) {
            logger.log(Level.SEVERE,"when getting trading hours: " + symbol);
            throw new ExchangeCommunicationException("when getting trading hours");
        }
    }

    private TradingHours.TradingDay hoursRecordToTradingDay(HoursRecord hr) {
        return new TradingHours.TradingDay(xtbDayToDow(hr.getDay()), (int)(hr.getFromT()/1000), (int)(hr.getToT()/1000));
    }

    private DayOfWeek xtbDayToDow(long xtbDay) {
        switch ((int) xtbDay) {
            case 1:
                return DayOfWeek.MONDAY;
            case 2:
                return DayOfWeek.TUESDAY;
            case 3:
                return DayOfWeek.WEDNESDAY;
            case 4:
                return DayOfWeek.THURSDAY;
            case 5:
                return DayOfWeek.FRIDAY;
            case 6:
                return DayOfWeek.SATURDAY;
            case 7:
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("wrong xtb day: " + xtbDay);
        }
    }
}
