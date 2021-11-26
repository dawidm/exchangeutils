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

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

public class Uniswap3SwapsToTickers {

    public static Ticker[] generateTickers(JsonNode swapsNode) {

        List<Ticker> tickers = new LinkedList<>();
        for (JsonNode swapNode : swapsNode.get("swaps")) {
            double amountUsd = swapNode.get("amountUSD").asDouble();
            double amountToken0 = swapNode.get("amount0").asDouble();
            double amountToken1 = swapNode.get("amount1").asDouble();
            long timestampSec = swapNode.get("timestamp").asLong();
            String token0Address = swapNode.get("token0").get("id").textValue();
            String token1Address = swapNode.get("token1").get("id").textValue();
            String token0ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token0Address, Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0]);
            String token1ApiSymbol = Uniswap3PairSymbolConverter.formatApiSymbol(token1Address, Uniswap3ExchangeSpecs.SUPPORTED_COUNTER_CURR[0]);
            tickers.add(new Ticker(token0ApiSymbol, amountUsd/Math.abs(amountToken0), timestampSec));
            tickers.add(new Ticker(token1ApiSymbol, amountUsd/Math.abs(amountToken1), timestampSec));
        }
        return tickers.toArray(Ticker[]::new);

    }

}
