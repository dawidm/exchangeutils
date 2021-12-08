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

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Uniswap3TokenSwapsQueryTest {

    @Test
    void testQuerySwapsToken0() throws ExchangeCommunicationException {
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        // WETH and USDT
        String[] tokensAddr = new String[] {"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2", "0xdac17f958d2ee523a2206206994597c13d831ec7"};
        int currentTimestamp = (int)Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        Uniswap3TokenSwapsQuery swapsQuery = new Uniswap3TokenSwapsQuery(Uniswap3TokenSwapsQuery.WhichToken.TOKEN0, tokensAddr, dayAgoTimestamp, currentTimestamp);
        List<JsonNode> resultNodes = req.send(swapsQuery);
        for(JsonNode resultNode : resultNodes) {
            for (JsonNode swapNode : resultNode.get("swaps")) {
                assertTrue(swapNode.get("token0").get("symbol").asText().equals("WETH") || swapNode.get("token0").get("symbol").asText().equals("USDT"));
                assertTrue(swapNode.has("amountUSD"));
                assertTrue(swapNode.has("amount0"));
                assertTrue(swapNode.has("amount1"));
                assertTrue(swapNode.has("token0"));
                assertTrue(swapNode.has("token1"));
            }
        }
    }

    @Test
    void testQuerySwapsToken1() throws ExchangeCommunicationException {
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        // WETH and USDT
        String[] tokensAddr = new String[] {"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2", "0xdac17f958d2ee523a2206206994597c13d831ec7"};
        int currentTimestamp = (int)Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        Uniswap3TokenSwapsQuery swapsQuery = new Uniswap3TokenSwapsQuery(Uniswap3TokenSwapsQuery.WhichToken.TOKEN1, tokensAddr, dayAgoTimestamp, currentTimestamp);
        List<JsonNode> resultNodes = req.send(swapsQuery);
        for(JsonNode resultNode : resultNodes) {
            for (JsonNode swapNode : resultNode.get("swaps")) {
                assertTrue(swapNode.get("token1").get("symbol").asText().equals("WETH") || swapNode.get("token1").get("symbol").asText().equals("USDT"));
                assertTrue(swapNode.has("amountUSD"));
                assertTrue(swapNode.has("amount0"));
                assertTrue(swapNode.has("amount1"));
                assertTrue(swapNode.has("token1"));
            }
        }
    }
}