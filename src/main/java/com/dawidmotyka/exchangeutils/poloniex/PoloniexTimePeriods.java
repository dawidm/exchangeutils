package com.dawidmotyka.exchangeutils.poloniex;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.service.PoloniexChartDataPeriodType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dawid on 8/1/17.
 */
public class PoloniexTimePeriods {

    private static Map<Long,PoloniexChartDataPeriodType> periodsMap = initPeriodsMap();

    public static CurrencyPair poloPairToCurrencyPair(String pair) {
        String[] splitPair = pair.split("_");
        return new CurrencyPair(new Currency(splitPair[1]), new Currency(splitPair[0]));
    }
    public static PoloniexChartDataPeriodType mapPeriodSecondsToPoloniexPeriodType(Long periodSeconds) {
        if (periodsMap.containsKey(periodSeconds)) {
            return periodsMap.get(periodSeconds);
        }
        else return null;
    }

    public static Map<Long,PoloniexChartDataPeriodType> initPeriodsMap() {
        Map<Long,PoloniexChartDataPeriodType> periodsMap = new HashMap<>();
        periodsMap.put(new Long(300),PoloniexChartDataPeriodType.PERIOD_300);
        periodsMap.put(new Long(900),PoloniexChartDataPeriodType.PERIOD_900);
        periodsMap.put(new Long(1800),PoloniexChartDataPeriodType.PERIOD_1800);
        periodsMap.put(new Long(7200),PoloniexChartDataPeriodType.PERIOD_7200);
        periodsMap.put(new Long(14400),PoloniexChartDataPeriodType.PERIOD_14400);
        periodsMap.put(new Long(86400),PoloniexChartDataPeriodType.PERIOD_86400);
        return periodsMap;
    }
}
