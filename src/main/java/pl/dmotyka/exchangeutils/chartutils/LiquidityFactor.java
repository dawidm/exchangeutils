package pl.dmotyka.exchangeutils.chartutils;

import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

public class LiquidityFactor implements SingleValueIndicator {
    @Override
    public double calcValue(ChartCandle[] chartCandles, int numCandles) {
        if(numCandles>chartCandles.length)
            throw new IllegalArgumentException("number candles to use is bigger than the number of candles in provided array");
        int emptyCandles = 0;
        for (int i=0; i<numCandles; i++) {
            if (chartCandles[0].getHigh() == chartCandles[0].getLow())
                emptyCandles++;
        }
        return (double)(numCandles-emptyCandles)/numCandles;
    }
}
