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

package com.dawidmotyka.exchangeutils.tools;

public class TimeConverter {

    public static final int SECONDS_IN_MINUTE=60;
    public static final int SECONDS_IN_HOUR=SECONDS_IN_MINUTE*60;
    public static final int SECONDS_IN_DAY=SECONDS_IN_HOUR*24;

    public static String secondsToFullMinutesHoursDays(int seconds) {
        if(seconds>SECONDS_IN_DAY && seconds%SECONDS_IN_DAY==0)
            return seconds/SECONDS_IN_DAY+"d";
        if(seconds>SECONDS_IN_HOUR && seconds%SECONDS_IN_HOUR==0)
            return seconds/SECONDS_IN_HOUR+"h";
        if(seconds>SECONDS_IN_MINUTE && seconds%SECONDS_IN_MINUTE==0)
            return seconds/SECONDS_IN_MINUTE+"m";
        return seconds+"s";
    }
}