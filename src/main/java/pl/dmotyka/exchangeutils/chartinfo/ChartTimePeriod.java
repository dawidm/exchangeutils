/*
 * Cryptonose
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

package pl.dmotyka.exchangeutils.chartinfo;

/**
 * Created by dawid on 12/4/17.
 */
public class ChartTimePeriod {

    public static final long PERIOD_1M = 60;
    public static final long PERIOD_5M = 5*60;
    public static final long PERIOD_15M = 15*60;
    public static final long PERIOD_30M = 30*60;
    public static final long PERIOD_1H = 60*60;
    public static final long PERIOD_3H = 3*60*60;
    public static final long PERIOD_1D = 24*60*60;

    private final String periodName;
    private final long periodLengthSeconds;
    private final String exchangePeriodStringId;

    public ChartTimePeriod(String periodName, long periodLengthSeconds, String exchangePeriodStringId) {
        this.periodName = periodName;
        this.periodLengthSeconds = periodLengthSeconds;
        this.exchangePeriodStringId = exchangePeriodStringId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public long getPeriodLengthSeconds() {
        return periodLengthSeconds;
    }

    public String getExchangePeriodStringId() {
        return exchangePeriodStringId;
    }

    @Override
    public String toString() {
        return periodName;
    }
}
