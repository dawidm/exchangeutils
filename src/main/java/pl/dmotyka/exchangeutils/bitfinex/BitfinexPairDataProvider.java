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

package pl.dmotyka.exchangeutils.bitfinex;

import pl.dmotyka.exchangeutils.pairdataprovider.MarketQuoteVolume;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BitfinexPairDataProvider implements PairDataProvider {

    public static final String SYMBOLS_API_V2_URL = "https://api.bitfinex.com/v2/tickers?symbols=";

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium ->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        return Arrays.stream(getQuoteVolumes()).filter(marketQuoteVolume -> {
                Double minVol=criteriaMap.get(symbolToCounterSymbol(marketQuoteVolume.getPairApiSymbol()));
                return minVol!=null && marketQuoteVolume.getQuoteVolume()>minVol;
        }).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    @Override
    public String[] getPairsApiSymbols() throws IOException {
        return Arrays.stream(getQuoteVolumes()).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    private MarketQuoteVolume[] getQuoteVolumes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode symbolsJsonNode = objectMapper.readValue(new URL(SYMBOLS_API_V2_URL+"ALL"), JsonNode.class);
        List<MarketQuoteVolume> marketQuoteVolumes = new ArrayList<>(symbolsJsonNode.size());
        for(JsonNode symbolJsonNode : symbolsJsonNode) {
            String marketSymbol = symbolJsonNode.get(0).asText();
            if(marketSymbol.startsWith("f"))
                continue;
            double volume = symbolJsonNode.get(8).asDouble(0);
            double lastPrice = symbolJsonNode.get(7).asDouble(0);
            marketQuoteVolumes.add(new MarketQuoteVolume(marketSymbol,volume*lastPrice));
        }
        return marketQuoteVolumes.toArray(new MarketQuoteVolume[marketQuoteVolumes.size()]);
    }

    private String symbolToCounterSymbol(String symbol) {
        return symbol.substring(symbol.length()-3);
    }
}
