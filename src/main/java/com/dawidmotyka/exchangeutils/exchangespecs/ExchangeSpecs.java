package com.dawidmotyka.exchangeutils.exchangespecs;

import com.dawidmotyka.exchangeutils.chartinfo.ExchangeChartInfo;
import org.knowm.xchange.Exchange;

import javax.annotation.Nullable;

/**
 * Created by dawid on 8/20/17.
 */
public abstract class ExchangeSpecs {

    public final double MIN_TOTAL_ORDER_VALUE;

    private final String name;
    private final String marketUrl;
    private final Class<? extends Exchange> xchangeExchange;
    private final String colorHex;

    public ExchangeSpecs(String name, String marketUrl, @Nullable Class<? extends Exchange> xchangeExchange, double MIN_TOTAL_ORDER_VALUE, String colorHex) {
        this.name = name;
        this.marketUrl = marketUrl;
        this.xchangeExchange=xchangeExchange;
        this.MIN_TOTAL_ORDER_VALUE=MIN_TOTAL_ORDER_VALUE;
        this.colorHex=colorHex;
    }

    public abstract ExchangeChartInfo getChartInfo();

    public String getName() {
        return name;
    }

    public String getMarketUrl() {
        return marketUrl;
    }

    public Class<? extends Exchange> getXchangeExchange() {
        return xchangeExchange;
    }

    public String getColorHash() {
        return colorHex;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExchangeSpecs)
            return name.equals(((ExchangeSpecs)obj).getName());
        return false;
    }

    //get instance of ExchangeSpecs subclass for exchange string name, not case-sensitive
    public static ExchangeSpecs fromStringName(String exchangeName) throws NoSuchExchangeException{
        switch (exchangeName.toLowerCase()) {
            case "poloniex": return new PoloniexExchangeSpecs();
            case "bittrex": return new BittrexExchangeSpecs();
            case "binance": return new BinanceExchangeSpecs();
            case "xtb": return new XtbExchangeSpecs();
            case "hitbtc": return new HitBtcExchangeSpecs();
            default: throw new NoSuchExchangeException("when getting exchange from string: " + exchangeName);
        }

    }
}
