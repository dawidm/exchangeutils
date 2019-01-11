package com.dawidmotyka.exchangeutils.hitbtc;

import com.dawidmotyka.exchangeutils.SimplePairInfo;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.hitbtc.v2.HitbtcExchange;
import org.knowm.xchange.hitbtc.v2.dto.HitbtcSymbol;
import org.knowm.xchange.hitbtc.v2.dto.HitbtcTicker;
import org.knowm.xchange.hitbtc.v2.service.HitbtcMarketDataService;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dawid on 9/30/17.
 */
public class HitBtcDataProvider {

    private Exchange exchange;
    public MarketDataService marketDataService;
    private HitbtcMarketDataService hitBtcMarketDataService;

    public HitBtcDataProvider() {
        exchange =ExchangeFactory.INSTANCE.createExchange(HitbtcExchange.class.getName());
        hitBtcMarketDataService =(HitbtcMarketDataService)exchange.getMarketDataService();
    }

    public StepLotInfo[] getStepLotInfo() throws IOException {
        List<HitbtcSymbol> hitbtcSymbolList = hitBtcMarketDataService.getHitbtcSymbols();
        return hitbtcSymbolList.stream().map((symbol)->new StepLotInfo(symbol.getBaseCurrency()+"/"+symbol.getQuoteCurrency(),symbol.getTickSize(),symbol.getQuantityIncrement())).toArray(StepLotInfo[]::new);
    }

    public SimplePairInfo[] getSimplePairInfos() throws IOException {
        Map<String,HitbtcTicker> hitbtcTickersMap= hitBtcMarketDataService.getHitbtcTickers();
        Set<Map.Entry<String, HitbtcTicker>> hitbtcTickers = hitbtcTickersMap.entrySet();
        return hitbtcTickers.stream().map((tickerEntry)->new SimplePairInfo(convertPairName(tickerEntry.getKey()), tickerEntry.getValue().getVolumeQuote().doubleValue())).toArray(SimplePairInfo[]::new);
    }

    private String stringSymbol(String commodity, String currency) {
        return commodity+"/"+currency;
    }

    private String convertPairName(String pair) {
        return pair.substring(0,pair.length()-3) + "/" + pair.substring(pair.length()-3,pair.length());
    }

}
