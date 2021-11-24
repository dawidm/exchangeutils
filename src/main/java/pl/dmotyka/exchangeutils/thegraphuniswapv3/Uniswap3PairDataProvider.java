/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.thegraphdex.DexCurrencyPair;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;

public class Uniswap3PairDataProvider implements PairDataProvider {

    private final Map<String, DexCurrencyPair> dexCurrencyPairMap = new HashMap<>();

    // api symbol is a pool address
    // minVolume in PairSelectionCriteria is total locked volume in counter currency (token1)
    @Override
    public synchronized String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        List<String> symbols = new LinkedList<>();
        for (PairSelectionCriteria currentCriteria : pairSelectionCriteria) {
            TheGraphHttpRequest theGraphHttpRequest = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
            Uniswap3PairsQuery uniswap3PairsQuery = new Uniswap3PairsQuery();
            uniswap3PairsQuery.setPairSelectionCriteria(currentCriteria);
            List<JsonNode> poolsNodes = theGraphHttpRequest.send(uniswap3PairsQuery);
            for (JsonNode poolsNode : poolsNodes) {
                for (JsonNode poolNode : poolsNode.get("pools")) {
                    symbols.add(poolNode.get("id").textValue());
                }
            }
            updatePairsMapWithNewData(poolsNodes);
        }
        return symbols.toArray(String[]::new);
    }

    // api symbol is a pool address
    @Override
    public synchronized String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        TheGraphHttpRequest theGraphHttpRequest = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        List<JsonNode> poolsNodes = theGraphHttpRequest.send(new Uniswap3PairsQuery());
        List<String> symbols = new LinkedList<>();
        for(JsonNode poolsNode : poolsNodes) {
            for (JsonNode poolNode : poolsNode.get("pools")) {
                symbols.add(poolNode.get("id").textValue());
            }
        }
        updatePairsMapWithNewData(poolsNodes);
        return symbols.toArray(String[]::new);
    }

    // Returns a map with dex currency pairs. All pairs that were returned at any time by current instance of this class are stored in this map.
    public Map<String, DexCurrencyPair> getDexCurrencyPairMap() {
        return Collections.unmodifiableMap(dexCurrencyPairMap);
    }

    private void updatePairsMapWithNewData(List<JsonNode> poolsJsonNodes) {
        for(JsonNode poolsNode : poolsJsonNodes) {
            for (JsonNode poolNode : poolsNode.get("pools")) {
                String id = poolNode.get("id").textValue();
                if (!dexCurrencyPairMap.containsKey(id)) {
                    DexCurrencyPair pair = new DexCurrencyPair(poolNode.get("token0").get("symbol").textValue().toUpperCase(Locale.ROOT),
                            poolNode.get("token0").get("id").textValue(),
                            poolNode.get("token1").get("symbol").textValue().toUpperCase(Locale.ROOT),
                            poolNode.get("token1").get("id").textValue(),
                            poolNode.get("id").textValue());
                    dexCurrencyPairMap.put(pair.getPoolAddress(), pair);
                }
            }
        }
    }
}
