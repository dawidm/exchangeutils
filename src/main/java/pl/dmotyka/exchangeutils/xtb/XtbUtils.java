/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.xtb;

import pl.dmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
import pro.xstore.api.message.codes.PERIOD_CODE;

public class XtbUtils {
    public static PERIOD_CODE periodSecondsToPeriodCode(int seconds) throws NoSuchTimePeriodException {
        switch(seconds) {
            case 60:
                return PERIOD_CODE.PERIOD_M1;
            case 300:
                return PERIOD_CODE.PERIOD_M5;
            case 900:
                return PERIOD_CODE.PERIOD_M15;
            case 1800:
                return PERIOD_CODE.PERIOD_M30;
            case 3600:
                return PERIOD_CODE.PERIOD_H1;
            case 14400:
                return PERIOD_CODE.PERIOD_H4;
            case 24*60*60:
                return  PERIOD_CODE.PERIOD_D1;
            case 7*24*60*60:
                return PERIOD_CODE.PERIOD_W1;
                
        }
        throw new NoSuchTimePeriodException("");
    }
}
