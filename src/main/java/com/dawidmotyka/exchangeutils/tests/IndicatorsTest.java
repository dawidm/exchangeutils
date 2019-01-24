package com.dawidmotyka.exchangeutils.tests;

import com.dawidmotyka.exchangeutils.chartdataprovider.ChartDataProvider;
import com.dawidmotyka.exchangeutils.chartdataprovider.CurrencyPairTimePeriod;
import com.dawidmotyka.exchangeutils.chartdataprovider.PeriodNumCandles;
import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;
import com.dawidmotyka.exchangeutils.chartutils.CandleDistanceConsolidationFactor;
import com.dawidmotyka.exchangeutils.chartutils.CandleIntersectionConsolidationFactor;
import com.dawidmotyka.exchangeutils.chartutils.CandleMinMaxConsolidationFactor;
import com.dawidmotyka.exchangeutils.exchangespecs.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import com.dawidmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

import java.util.Map;

public class IndicatorsTest {
    public static void main(String[] args) {
        try {
            ExchangeSpecs exchangeSpecs = new BinanceExchangeSpecs();
            PairDataProvider pairDataProvider = PairDataProvider.forExchange(exchangeSpecs);
            String[] pairs = pairDataProvider.getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("BTC",200)});
            PeriodNumCandles[] periodNumCandles = new PeriodNumCandles[] {new PeriodNumCandles(300,50)};
            ChartDataProvider chartDataProvider = new ChartDataProvider(exchangeSpecs, pairs, periodNumCandles);
            chartDataProvider.subscribeChartCandles(chartCandlesMap -> {
                for(Map.Entry<CurrencyPairTimePeriod,ChartCandle[]> entry : chartCandlesMap.entrySet()) {
                    double overlapFactor = new CandleIntersectionConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    double minMaxSumFactor = new CandleMinMaxConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    double candleDistanceConsolidationFactor = new CandleDistanceConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    System.out.println(entry.getKey()+","+entry.getValue().length+","+overlapFactor+","+minMaxSumFactor+","+candleDistanceConsolidationFactor);
                }
            });
            chartDataProvider.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
