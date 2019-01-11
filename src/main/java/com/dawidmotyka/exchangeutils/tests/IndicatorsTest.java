package com.dawidmotyka.exchangeutils.tests;

import com.dawidmotyka.exchangeutils.chartdataprovider.ChartDataProvider;
import com.dawidmotyka.exchangeutils.chartinfo.ChartCandle;
import com.dawidmotyka.exchangeutils.chartutils.CandleDistanceConsolidationFactor;
import com.dawidmotyka.exchangeutils.chartutils.CandleIntersectionConsolidationFactor;
import com.dawidmotyka.exchangeutils.chartutils.CandleMinMaxConsolidationFactor;
import com.dawidmotyka.exchangeutils.exchangespecs.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairdataprovider.PairDataProvider;

import java.util.Map;

public class IndicatorsTest {
    public static void main(String[] args) {
        try {
            ExchangeSpecs exchangeSpecs = new BinanceExchangeSpecs();
            PairDataProvider pairDataProvider = PairDataProvider.forExchange(exchangeSpecs);
            String[] pairs = pairDataProvider.getPairsApiSymbols(200,"BTC");
            ChartDataProvider chartDataProvider = new ChartDataProvider(exchangeSpecs, pairs,new int[] {300});
            chartDataProvider.subscribeChartCandles(chartCandlesMap -> {
                for(Map.Entry<String,ChartCandle[]> entry : chartCandlesMap.entrySet()) {
                    double overlapFactor = new CandleIntersectionConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    double minMaxSumFactor = new CandleMinMaxConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    double candleDistanceConsolidationFactor = new CandleDistanceConsolidationFactor().calcValue(entry.getValue(),entry.getValue().length);
                    System.out.println(entry.getKey()+","+entry.getValue().length+","+overlapFactor+","+minMaxSumFactor+","+candleDistanceConsolidationFactor);
                }
            },50);
            chartDataProvider.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
