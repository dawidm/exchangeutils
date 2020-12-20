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

package pl.dmotyka.exchangeutils.huobi;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

public class HuobiPairDataProvider implements PairDataProvider {

    public static final String TICKERS_URL = "https://api.huobi.pro/market/tickers";
    public static final String MARKETS_URL = "https://api.huobi.pro/v1/common/symbols";

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        Objects.requireNonNull(pairSelectionCriteria);
        return getPairsApiSymbolsRaw(pairSelectionCriteria);
    }

    @Override
    public String[] getPairsApiSymbols() throws IOException {
        return getPairsApiSymbolsRaw(null);
    }

    // pairSelectionCriteria - pass null to get all assets
    public String[] getPairsApiSymbolsRaw(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode marketsNode = objectMapper.readValue(new URL(MARKETS_URL), JsonNode.class);
        if (!marketsNode.get("status").textValue().equals("ok")) {
            throw new IOException("Status reported by api is not \"ok\" " + MARKETS_URL);
        }
        Set<String> quoteCurrencies = new HashSet<>();
        Set<String> onlineMarkets = new HashSet<>();
        marketsNode = marketsNode.get("data");
        for (JsonNode marketNode : marketsNode) {
            quoteCurrencies.add(marketNode.get("quote-currency").textValue());
            if (marketNode.get("state").textValue().equals("online")) {
                onlineMarkets.add(marketNode.get("base-currency").textValue() + marketNode.get("quote-currency").textValue());
            }
        }
        if (pairSelectionCriteria==null)
            return onlineMarkets.toArray(String[]::new);
        JsonNode tickersNode = objectMapper.readValue(new URL(TICKERS_URL), JsonNode.class);
        Map<String, Double> volumesMap = new HashMap<>();
        if (!tickersNode.get("status").textValue().equals("ok"))
            throw new IOException("Status reported by api is not \"ok\" " + TICKERS_URL);
        tickersNode = tickersNode.get("data");
        for (JsonNode tickerNode : tickersNode) {
            volumesMap.put(tickerNode.get("symbol").asText(), tickerNode.get("vol").asDouble(0));
        }
        tickersNode.size();
        String[] filteredPairs = volumesMap.entrySet().stream().filter(entry -> {
            for (var criteria : pairSelectionCriteria) {
                if(entry.getKey().endsWith(criteria.getCounterCurrencySymbol()))
                    if (entry.getValue() > criteria.getMinVolume())
                        return true;
            }
            return false;
        }).map(entry -> entry.getKey()).toArray(String[]::new);
        return filteredPairs;
    }
}
