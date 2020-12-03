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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

// trading hours for an instrument
public class TradingHours {

    public static class TradingDay {

        private final DayOfWeek dayOfWeek;
        private final int tradingStartSeconds;
        private final int tradingEndSeconds;

        // tradingStartSeconds - start of trading (inclusive), seconds from the beginning of a day, UTC
        // tradingEndSeconds - end of trading (exclusive), seconds from the beginning of a day, UTC
        public TradingDay(DayOfWeek dayOfWeek, int tradingStartSeconds, int tradingEndSeconds) {
            if (tradingStartSeconds < 0 || tradingStartSeconds > 24*60*60 || tradingEndSeconds < 0 || tradingEndSeconds > 24*60*60)
                throw new IllegalArgumentException("start and and values should be in a range from 0 to 24*60*60");
            if (tradingEndSeconds <= tradingStartSeconds)
                throw new IllegalArgumentException("trading end should be greater than starting start");
            this.dayOfWeek = dayOfWeek;
            this.tradingStartSeconds = tradingStartSeconds;
            this.tradingEndSeconds = tradingEndSeconds;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public int getTradingStartSeconds() {
            return tradingStartSeconds;
        }

        public int getTradingEndSeconds() {
            return tradingEndSeconds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TradingDay that = (TradingDay) o;
            return tradingStartSeconds == that.tradingStartSeconds &&
                    tradingEndSeconds == that.tradingEndSeconds &&
                    dayOfWeek == that.dayOfWeek;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dayOfWeek, tradingStartSeconds, tradingEndSeconds);
        }
    }

    public static class TradingSession {

        private final long timestampStartSeconds;
        private final long timestampEndSeconds;

        public TradingSession(long timestampStartSeconds, long timestampEndSeconds) {
            this.timestampStartSeconds = timestampStartSeconds;
            this.timestampEndSeconds = timestampEndSeconds;
        }

        public long getTimestampStartSeconds() {
            return timestampStartSeconds;
        }

        public long getTimestampEndSeconds() {
            return timestampEndSeconds;
        }
    }

    private final Set<TradingDay> tradingDays;

    // tradingDays - specify all days when trading is active, for each day trading hour are specified inside TradingDay class
    public TradingHours(TradingDay[] tradingDays) {
        this.tradingDays = Collections.unmodifiableSet(Set.of(tradingDays));
    }

    public boolean isInTradingHours(long timestampSeconds) {
        Optional<TradingDay> optTradingDay = getTradingDay(timestampSeconds);
        if (optTradingDay.isEmpty())
            return false;
        TradingDay tradingDay = optTradingDay.get();
        long secondOfDay = timestampSeconds % (24*60*60);
        return secondOfDay >= tradingDay.tradingStartSeconds && secondOfDay < tradingDay.tradingEndSeconds;
    }

    public Optional<TradingSession> getTradingSession(long timestampSeconds) {
        Optional<TradingDay> optTradingDay = getTradingDay(timestampSeconds);
        if (optTradingDay.isEmpty())
            return Optional.empty();
        TradingDay tradingDay = optTradingDay.get();
        long secondOfDay = timestampSeconds % (24*60*60);
        if (secondOfDay >= tradingDay.tradingStartSeconds && secondOfDay < tradingDay.tradingEndSeconds) {
            long dayBeginSeconds = timestampSeconds - secondOfDay;
            TradingSession ts = new TradingSession(dayBeginSeconds + tradingDay.tradingStartSeconds,
                    dayBeginSeconds + tradingDay.tradingEndSeconds);
            return Optional.of(ts);
        }
        return Optional.empty();
    }

    // get trading day for timestamp if timestamp is within trading hours
    private Optional<TradingDay> getTradingDay(long timestampSeconds) {
        OffsetDateTime dateTime = Instant.ofEpochSecond(timestampSeconds).atOffset(ZoneOffset.UTC);
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return tradingDays.stream().filter(tradingDay -> tradingDay.dayOfWeek == dayOfWeek).findAny();
    }
}
