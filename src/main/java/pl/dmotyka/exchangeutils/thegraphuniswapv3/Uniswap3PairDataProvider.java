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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.MarketQuoteVolume;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;

public class Uniswap3PairDataProvider implements PairDataProvider {

    private final Map<String, String> tokenAddressesMap = new HashMap<>();
    private final String COUNTER_CURRENCY_SYMBOL = Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0];

    @Override
    public synchronized String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        if (pairSelectionCriteria.length > 1 || !pairSelectionCriteria[0].getCounterCurrencySymbol().equals(COUNTER_CURRENCY_SYMBOL)) {
            throw new IllegalArgumentException(String.format("Only %s counter currency symbol and single pairs criteria are supported", COUNTER_CURRENCY_SYMBOL));
        }
        return Arrays.stream(getQuoteVolumes24h()).filter(q -> q.getQuoteVolume() > pairSelectionCriteria[0].getMinVolume()).map(MarketQuoteVolume::getPairApiSymbol).toArray(String[]::new);
    }

    @Override
    public synchronized String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        return Arrays.stream(getQuoteVolumes24h()).map(MarketQuoteVolume::getPairApiSymbol).toArray(String[]::new);
    }

    private MarketQuoteVolume[] getQuoteVolumes24h() throws ExchangeCommunicationException {
        TheGraphHttpRequest theGraphHttpRequest = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        List<JsonNode> tokensNodes = theGraphHttpRequest.send(new Uniswap3TokensQuery());
        Set<MarketQuoteVolume> volumes = new HashSet<>();
        for(JsonNode poolsNode : tokensNodes) {
            for (JsonNode tokenNode : poolsNode.get("tokens")) {
                String symbol = Uniswap3PairSymbolConverter.formatApiSymbol(tokenNode.get("id").textValue(),COUNTER_CURRENCY_SYMBOL);
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

    public synchronized String getTokenSymbol(String tokenAddress) {
        if (!tokenAddressesMap.containsKey(tokenAddress)) {
            throw new IllegalStateException("No such address. Use this method only for tokens acquired by the same instance of this object.");
        }
        return tokenAddressesMap.get(tokenAddress);
    }

    private synchronized void updatePairsMapWithNewData(List<JsonNode> tokensJsonNodes) {
        for (JsonNode tokensJsonNode : tokensJsonNodes) {
            for(JsonNode tokenJsonNode : tokensJsonNode.get("tokens")) {
                String tokenAddress = tokenJsonNode.get("id").textValue();
                String tokenSymbol = tokenJsonNode.get("symbol").textValue();
                tokenAddressesMap.put(tokenAddress, tokenSymbol);
            }
        }
    }
}
