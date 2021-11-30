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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class Uniswap3SwapsToTickers {

    private static final BigInteger Q192 = new BigInteger("2").pow(192);

    public static Ticker[] generateTickers(JsonNode swapsNode, Uniswap3ExchangeSpecs uniswap3ExchangeSpecs) {

        List<Ticker> tickers = new LinkedList<>();
        for (JsonNode swapNode : swapsNode.get("swaps")) {
            double amountUsd = swapNode.get("amountUSD").asDouble();
            if (amountUsd > 0) {
                double amountToken0 = swapNode.get("amount0").asDouble();
                double amountToken1 = swapNode.get("amount1").asDouble();
                long timestampSec = swapNode.get("timestamp").asLong();
                String token0Address = swapNode.get("token0").get("id").textValue();
                String token1Address = swapNode.get("token1").get("id").textValue();
                String token0ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token0Address, Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0]);
                String token1ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token1Address, Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0]);
                String poolAddress = swapNode.get("pool").get("id").textValue();
                int token0Decimals = swapNode.get("token0").get("decimals").asInt();
                int token1Decimals = swapNode.get("token1").get("decimals").asInt();
                BigInteger sqrtPriceX96 = new BigInteger(swapNode.get("sqrtPriceX96").asText());
                double doublePrice = getPriceFromSqrtPriceX96(sqrtPriceX96, token0Decimals, token1Decimals);
                try {
                    DexTokenInfo token0Info = ((Uniswap3PairDataProvider)uniswap3ExchangeSpecs.getPairDataProvider()).getTokenInfo(token0Address);
                    if (token0Info.checkIsPoolWhitelisted(poolAddress)) {
                        double token0SwapPrice = -amountToken1 / amountToken0;
                        double token0PoolPrice = doublePrice;
                        double token0SwapUSDPrice = amountUsd / Math.abs(amountToken0);
                        double token0PoolUSDPrice = token0SwapUSDPrice * token0PoolPrice / token0SwapPrice;
                        tickers.add(new Ticker(token0ApiSymbol, token0PoolUSDPrice, timestampSec));
                    }
                } catch (IllegalStateException ignored) {}
                try {
                    DexTokenInfo token1Info = ((Uniswap3PairDataProvider)uniswap3ExchangeSpecs.getPairDataProvider()).getTokenInfo(token1Address);
                    if (token1Info.checkIsPoolWhitelisted(poolAddress)) {
                        double token1SwapPrice = -amountToken0 / amountToken1;
                        double token1PoolPrice = 1/doublePrice;
                        double token1SwapUSDPrice = amountUsd / Math.abs(amountToken1);
                        double token1PoolUSDPrice = token1SwapUSDPrice * token1PoolPrice / token1SwapPrice;
                        tickers.add(new Ticker(token1ApiSymbol, token1PoolUSDPrice, timestampSec));
                    }
                } catch (IllegalStateException ignored) {}
            }
        }
        return tickers.toArray(Ticker[]::new);

    }

    private static double getPriceFromSqrtPriceX96(BigInteger sqrtPriceX96, int token0Decimals, int token1Decimals) {
        int scaleFactor = token0Decimals - token1Decimals;
        BigInteger priceNumerator = sqrtPriceX96.pow(2);
        BigInteger priceDenominator = Q192;
        if (scaleFactor > 0) {
            priceNumerator = priceNumerator.multiply(new BigInteger("10").pow(scaleFactor));
        }
        if (scaleFactor < 0 ) {
            priceDenominator = priceDenominator.multiply(new BigInteger("10").pow(-scaleFactor));
        }
        return new BigDecimal(priceNumerator).divide(new BigDecimal(priceDenominator), token0Decimals, RoundingMode.HALF_UP).doubleValue();
    }

}
