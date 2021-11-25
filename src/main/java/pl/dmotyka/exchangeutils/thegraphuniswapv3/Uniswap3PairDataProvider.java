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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.thegraphdex.DexCurrencyPair;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;

public class Uniswap3PairDataProvider implements PairDataProvider {

    private final Map<String, DexCurrencyPair> dexCurrencyPairMap = new HashMap<>();
    private final String COUNTER_CURRENCY_SYMBOL = Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0];

    @Override
    public synchronized String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        Set<String> symbols = new HashSet<>();
        for (PairSelectionCriteria currentCriteria : pairSelectionCriteria) {
            TheGraphHttpRequest theGraphHttpRequest = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
            Uniswap3PairsQuery uniswap3PairsQuery = new Uniswap3PairsQuery();
            uniswap3PairsQuery.setPairSelectionCriteria(currentCriteria);
            List<JsonNode> poolsNodes = theGraphHttpRequest.send(uniswap3PairsQuery);
            for (JsonNode poolsNode : poolsNodes) {
                for (JsonNode poolNode : poolsNode.get("pools")) {
                    symbols.add(Uniswap3PairSymbolConverter.formatApiSymbol(poolNode.get("token0").get("symbol").textValue(),COUNTER_CURRENCY_SYMBOL));
                    symbols.add(Uniswap3PairSymbolConverter.formatApiSymbol(poolNode.get("token1").get("symbol").textValue(),COUNTER_CURRENCY_SYMBOL));
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
        Set<String> symbols = new HashSet<>();
        for(JsonNode poolsNode : poolsNodes) {
            for (JsonNode poolNode : poolsNode.get("pools")) {
                symbols.add(Uniswap3PairSymbolConverter.formatApiSymbol(poolNode.get("token0").get("symbol").textValue(),COUNTER_CURRENCY_SYMBOL));
                symbols.add(Uniswap3PairSymbolConverter.formatApiSymbol(poolNode.get("token1").get("symbol").textValue(),COUNTER_CURRENCY_SYMBOL));
            }
        }
        updatePairsMapWithNewData(poolsNodes);
        return symbols.toArray(String[]::new);
    }

    // Returns a map with dex currency pairs. All pairs that were returned at any time by current instance of this class are stored in this map.
    public Map<String, DexCurrencyPair> getDexCurrencyPairMap() {
        return Collections.unmodifiableMap(dexCurrencyPairMap);
    }

    private synchronized void updatePairsMapWithNewData(List<JsonNode> poolsJsonNodes) {
        for(JsonNode poolsNode : poolsJsonNodes) {
            for (JsonNode poolNode : poolsNode.get("pools")) {
                String token0Symbol = poolNode.get("token0").get("symbol").textValue();
                String token0ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token0Symbol, COUNTER_CURRENCY_SYMBOL);
                String token0address = poolNode.get("token0").get("id").textValue();
                String token1Symbol = poolNode.get("token1").get("symbol").textValue();
                String token1ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token1Symbol, COUNTER_CURRENCY_SYMBOL);
                String token1address = poolNode.get("token1").get("id").textValue();
                String poolAddress = poolNode.get("id").textValue();
                if (dexCurrencyPairMap.containsKey(token0ApiSymbol)) {
                    dexCurrencyPairMap.get(token0ApiSymbol).addPool(poolAddress);
                } else {
                    dexCurrencyPairMap.put(token0ApiSymbol, new DexCurrencyPair(token0Symbol, token0address, COUNTER_CURRENCY_SYMBOL, poolAddress));
                }
                if (dexCurrencyPairMap.containsKey(token1ApiSymbol)) {
                    dexCurrencyPairMap.get(token1ApiSymbol).addPool(poolAddress);
                } else {
                    dexCurrencyPairMap.put(token1ApiSymbol, new DexCurrencyPair(token1Symbol, token1address, COUNTER_CURRENCY_SYMBOL, poolAddress));
                }
            }
        }
    }
}
