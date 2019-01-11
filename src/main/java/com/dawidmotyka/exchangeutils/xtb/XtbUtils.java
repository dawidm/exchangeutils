package com.dawidmotyka.exchangeutils.xtb;

import com.dawidmotyka.exchangeutils.chartinfo.NoSuchTimePeriodException;
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
