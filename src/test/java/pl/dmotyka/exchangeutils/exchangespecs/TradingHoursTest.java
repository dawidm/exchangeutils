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

package pl.dmotyka.exchangeutils.exchangespecs;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradingHoursTest {
    @Test
    void testTradingDaysArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TradingHours.TradingDay(DayOfWeek.MONDAY,60, 25*60*60);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new TradingHours.TradingDay(DayOfWeek.MONDAY,60, 0);
        });
    }

    @Test
    void testTradingHours() {
        // monday
        OffsetDateTime firstDateTime = OffsetDateTime.of(2020,11,30,0,1, 10, 0, ZoneOffset.UTC);
        OffsetDateTime firstDateTimeDay = OffsetDateTime.of(2020,11,30,0,0, 0, 0, ZoneOffset.UTC);
        // thursday
        OffsetDateTime secondDateTime = OffsetDateTime.of(2020,12,3,0,10, 50, 0, ZoneOffset.UTC);
        OffsetDateTime secondDateTimeDay = OffsetDateTime.of(2020,12,3,0,0, 0, 0, ZoneOffset.UTC);
        // thursday
        OffsetDateTime thirdDateTime = OffsetDateTime.of(2020,12,3,0,20, 0, 0, ZoneOffset.UTC);
        // friday
        OffsetDateTime fourthDateTime = OffsetDateTime.of(2020,12,4,0,1, 10, 0, ZoneOffset.UTC);
        TradingHours.TradingDay[] tradingDays = new TradingHours.TradingDay[] {
                new TradingHours.TradingDay(DayOfWeek.MONDAY,60, 600),
                new TradingHours.TradingDay(DayOfWeek.THURSDAY,120, 1000)
        };
        TradingHours tradingHours = new TradingHours(tradingDays);
        assertTrue(tradingHours.isInTradingHours(firstDateTime.toEpochSecond()));
        assertTrue(tradingHours.isInTradingHours(secondDateTime.toEpochSecond()));
        assertFalse(tradingHours.isInTradingHours(thirdDateTime.toEpochSecond()));
        assertFalse(tradingHours.isInTradingHours(fourthDateTime.toEpochSecond()));
        var optFirstSession = tradingHours.getTradingSession(firstDateTime.toEpochSecond());
        assertTrue(optFirstSession.isPresent());
        assertEquals(optFirstSession.get().getTimestampStartSeconds(), firstDateTimeDay.toEpochSecond() + tradingDays[0].getTradingStartSeconds());
        assertEquals(optFirstSession.get().getTimestampEndSeconds(), firstDateTimeDay.toEpochSecond() + tradingDays[0].getTradingEndSeconds());
        var optSecondSession = tradingHours.getTradingSession(secondDateTime.toEpochSecond());
        assertTrue(optSecondSession.isPresent());
        assertEquals(optSecondSession.get().getTimestampStartSeconds(), secondDateTimeDay.toEpochSecond() + tradingDays[1].getTradingStartSeconds());
        assertEquals(optSecondSession.get().getTimestampEndSeconds(), secondDateTimeDay.toEpochSecond() + tradingDays[1].getTradingEndSeconds());
        var optThirdSession = tradingHours.getTradingSession(thirdDateTime.toEpochSecond());
        assertTrue(optThirdSession.isEmpty());
    }
}