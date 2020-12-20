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

package pl.dmotyka.exchangeutils.bitfinex;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.MarketQuoteVolume;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class BitfinexPairDataProvider implements PairDataProvider {

    public static final String SYMBOLS_API_V2_URL = "https://api.bitfinex.com/v2/tickers?symbols=";
    private static final PairSymbolConverter pairSymbolConverter = new BitfinexPairSymbolConverter();

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ExchangeCommunicationException, ConnectionProblemException {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium ->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        return Arrays.stream(getQuoteVolumes()).filter(marketQuoteVolume -> {
                Double minVol=criteriaMap.get(symbolToCounterSymbol(marketQuoteVolume.getPairApiSymbol()));
                return minVol!=null && marketQuoteVolume.getQuoteVolume()>minVol;
        }).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    @Override
    public String[] getPairsApiSymbols() throws ExchangeCommunicationException, ConnectionProblemException {
        return Arrays.stream(getQuoteVolumes()).map(marketQuoteVolume -> marketQuoteVolume.getPairApiSymbol()).toArray(String[]::new);
    }

    private MarketQuoteVolume[] getQuoteVolumes() throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode symbolsJsonNode = objectMapper.readValue(new URL(SYMBOLS_API_V2_URL + "ALL"), JsonNode.class);
            List<MarketQuoteVolume> marketQuoteVolumes = new ArrayList<>(symbolsJsonNode.size());
            for (JsonNode symbolJsonNode : symbolsJsonNode) {
                String marketSymbol = symbolJsonNode.get(0).asText();
                if (marketSymbol.startsWith("f"))
                    continue;
                double volume = symbolJsonNode.get(8).asDouble(0);
                double lastPrice = symbolJsonNode.get(7).asDouble(0);
                marketQuoteVolumes.add(new MarketQuoteVolume(marketSymbol, volume * lastPrice));
            }
            return marketQuoteVolumes.toArray(new MarketQuoteVolume[marketQuoteVolumes.size()]);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting quote volumes", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting quote volumes", e);
        }
    }

    private String symbolToCounterSymbol(String symbol) {
        return pairSymbolConverter.apiSymbolToCounterCurrencySymbol(symbol);
    }
}
