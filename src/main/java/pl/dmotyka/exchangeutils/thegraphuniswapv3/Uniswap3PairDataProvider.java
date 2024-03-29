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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.MarketQuoteVolume;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphExchangeSpecs;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;

public class Uniswap3PairDataProvider implements PairDataProvider {

    private final TheGraphExchangeSpecs theGraphExchangeSpecs;
    private final Map<String, DexTokenInfo> tokenInfoMap = new HashMap<>();
    private static final double MIN_WHITELIST_POOL_TO_BIGGEST_POOL_USD_VOL_PROPORTION = 0.1;
    private static final int MAX_WHITELIST_POOLS = 10;

    public Uniswap3PairDataProvider(TheGraphExchangeSpecs theGraphExchangeSpecs) {
        this.theGraphExchangeSpecs = theGraphExchangeSpecs;
    }

    @Override
    public synchronized String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        if (pairSelectionCriteria.length > 1 || !pairSelectionCriteria[0].getCounterCurrencySymbol().equals(theGraphExchangeSpecs.getSupportedCounterCurrencies()[0])) {
            throw new IllegalArgumentException(String.format("Only %s counter currency symbol and single pairs criteria are supported", theGraphExchangeSpecs.getSupportedCounterCurrencies()[0]));
        }
        return Arrays.stream(getQuoteVolumes24h()).filter(q -> q.getQuoteVolume() > pairSelectionCriteria[0].getMinVolume()).map(MarketQuoteVolume::getPairApiSymbol).toArray(String[]::new);
    }

    @Override
    public synchronized String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        return Arrays.stream(getQuoteVolumes24h()).map(MarketQuoteVolume::getPairApiSymbol).toArray(String[]::new);
    }

    private MarketQuoteVolume[] getQuoteVolumes24h() throws ExchangeCommunicationException {
        TheGraphHttpRequest theGraphHttpRequest = new TheGraphHttpRequest(theGraphExchangeSpecs);
        List<JsonNode> tokensNodes = theGraphHttpRequest.send(new Uniswap3TokensQuery());
        Set<MarketQuoteVolume> volumes = new HashSet<>();
        for(JsonNode poolsNode : tokensNodes) {
            for (JsonNode tokenNode : poolsNode.get("tokens")) {
                if (tokenNode.get("whitelistPools").size() == 0) {
                    continue;
                }
                String symbol = Uniswap3PairSymbolConverter.formatApiSymbol(tokenNode.get("id").textValue(), theGraphExchangeSpecs.getSupportedCounterCurrencies()[0]);
                long currentTimestampSec = System.currentTimeMillis() / 1000;
                long todayTimestampSec = currentTimestampSec - currentTimestampSec % (24*3600);
                long yesterdayTimestampSec = todayTimestampSec - 24*3600;
                double todayVol = 0.0;
                double yesterdayVol = 0.0;
                for (JsonNode dayDataNode : tokenNode.get("tokenDayData")) {
                    if (dayDataNode.get("date").longValue() == todayTimestampSec) {
                        todayVol = dayDataNode.get("volumeUSD").asDouble(0.0);
                    }
                    if (dayDataNode.get("date").longValue() == yesterdayTimestampSec) {
                        yesterdayVol = dayDataNode.get("volumeUSD").asDouble(0.0);
                    }
                }
                double currentDayHours = (double)((System.currentTimeMillis() / 1000) % (24*3600)) / 3600;
                double lastDayHoursToTake = 24 - currentDayHours;
                double proportionallyReducedYesterdaysVolume = yesterdayVol * (lastDayHoursToTake / 24);
                volumes.add(new MarketQuoteVolume(symbol, todayVol + proportionallyReducedYesterdaysVolume));
            }
        }
        updatePairsMapWithNewData(tokensNodes);
        return volumes.toArray(MarketQuoteVolume[]::new);
    }

    public synchronized DexTokenInfo getTokenInfo(String tokenAddress) {
        if (!tokenInfoMap.containsKey(tokenAddress)) {
            throw new IllegalStateException("No such address. Use this method only for tokens acquired by the same instance of this object.");
        }
        return tokenInfoMap.get(tokenAddress);
    }

    private synchronized void updatePairsMapWithNewData(List<JsonNode> tokensJsonNodes) {
        for (JsonNode tokensJsonNode : tokensJsonNodes) {
            for(JsonNode tokenJsonNode : tokensJsonNode.get("tokens")) {
                String tokenAddress = tokenJsonNode.get("id").textValue();
                String tokenSymbol = tokenJsonNode.get("symbol").textValue();
                List<String> poolsAddresses = new LinkedList<>();
                JsonNode poolsNode = tokenJsonNode.get("whitelistPools");
                if (poolsNode.size() > 0) {
                    double firstUSDVolume = poolsNode.get(0).get("totalValueLockedUSD").asDouble();
                    for (JsonNode poolNode : poolsNode) {
                        if (poolNode.get("totalValueLockedUSD").asDouble() > firstUSDVolume * MIN_WHITELIST_POOL_TO_BIGGEST_POOL_USD_VOL_PROPORTION) {
                            poolsAddresses.add(poolNode.get("id").textValue());
                        }
                        if (poolsAddresses.size() >= MAX_WHITELIST_POOLS) {
                            break;
                        }
                    }
                }
                tokenInfoMap.put(tokenAddress, new DexTokenInfo(tokenAddress, tokenSymbol, poolsAddresses.toArray(String[]::new)));
            }
        }
    }
}
