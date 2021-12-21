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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class Uniswap3SwapsToTickers {

    private static class AmountTokenUSD {
        private final double amountToken;
        private final double amountUSD;

        public AmountTokenUSD(double amountToken, double amountUSD) {
            this.amountToken = amountToken;
            this.amountUSD = amountUSD;
        }

        public double getAmountToken() {
            return amountToken;
        }

        public double getAmountUSD() {
            return amountUSD;
        }
    }

    // the key is timestampSec_tokenAddress (assuming that timestamp represents a block)
    // This map is used to use only a swap with the lowest token amount to calculate token price for given block.
    //  This is for better USD price approximation which is problematic with used api and big swaps produce
    //  more inaccurate USD prices
    private final Map<String, AmountTokenUSD> tmpAmountsMap = new HashMap<>();

    public Ticker[] generateTickers(List<JsonNode> swapsNodes, Uniswap3ExchangeSpecs uniswap3ExchangeSpecs) {
        List<Ticker> tickers = new LinkedList<>();
        for (JsonNode swapsNode : swapsNodes) {
            for (JsonNode swapNode : swapsNode.get("swaps")) {
                double amountUsd = swapNode.get("amountUSD").asDouble();
                if (amountUsd > 0) {
                    double amountToken0 = swapNode.get("amount0").asDouble();
                    double amountToken1 = swapNode.get("amount1").asDouble();
                    long timestampSec = swapNode.get("timestamp").asLong();
                    String token0Address = swapNode.get("token0").get("id").textValue();
                    String token1Address = swapNode.get("token1").get("id").textValue();
                    String poolAddress = swapNode.get("pool").get("id").textValue();
                    try {
                        DexTokenInfo token0Info = ((Uniswap3PairDataProvider) uniswap3ExchangeSpecs.getPairDataProvider()).getTokenInfo(token0Address);
                        if (token0Info.checkIsPoolWhitelisted(poolAddress)) {
                            AmountTokenUSD oldAmountTokenUSD = tmpAmountsMap.get(formatMapKey(timestampSec, token0Address));
                            if (oldAmountTokenUSD == null || oldAmountTokenUSD.amountToken > amountToken0) {
                                tmpAmountsMap.put(formatMapKey(timestampSec, token0Address), new AmountTokenUSD(amountToken0, amountUsd));
                            }
                        }
                    } catch (IllegalStateException ignored) {
                    }
                    try {
                        DexTokenInfo token1Info = ((Uniswap3PairDataProvider) uniswap3ExchangeSpecs.getPairDataProvider()).getTokenInfo(token1Address);
                        if (token1Info.checkIsPoolWhitelisted(poolAddress)) {
                            AmountTokenUSD oldAmountTokenUSD = tmpAmountsMap.get(formatMapKey(timestampSec, token1Address));
                            if (oldAmountTokenUSD == null || oldAmountTokenUSD.amountToken > amountToken1) {
                                tmpAmountsMap.put(formatMapKey(timestampSec, token1Address), new AmountTokenUSD(amountToken1, amountUsd));
                            }
                        }
                    } catch (IllegalStateException ignored) {
                    }
                }
            }
        }
        for (Map.Entry<String, AmountTokenUSD> amountEntry : tmpAmountsMap.entrySet()) {
            double approxUsdPrice = amountEntry.getValue().amountUSD / Math.abs(amountEntry.getValue().amountToken);
            String apiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(tokenAddressFromMapKey(amountEntry.getKey()), Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0]);
            tickers.add(new Ticker(apiSymbol, approxUsdPrice, timestampFromMapKey(amountEntry.getKey())));
        }
        tmpAmountsMap.clear();
        return tickers.toArray(Ticker[]::new);
    }

    private static String formatMapKey(long timestampSec, String tokenAddress) {
        return String.format("%d_%s", timestampSec, tokenAddress);
    }

    private static String tokenAddressFromMapKey(String key) {
        return key.split("_")[1];
    }

    private static long timestampFromMapKey(String key) {
        return Long.parseLong(key.split("_")[0]);
    }

}
