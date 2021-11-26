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

import static org.junit.jupiter.api.Assertions.*;

class Uniswap3PoolSwapsQueryTest {

    @Test
    void testQuerySwaps() throws ExchangeCommunicationException {
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        // USDC/WETH and WETH/USDT pools
        String[] pools = new String[] {"0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8", "0x11b815efb8f581194ae79006d24e0d814b7697f6"};
        int currentTimestamp = (int)Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        Uniswap3PoolSwapsQuery swapsQuery = new Uniswap3PoolSwapsQuery(pools, dayAgoTimestamp, currentTimestamp);
        List<JsonNode> resultNodes = req.send(swapsQuery);
        for(JsonNode resultNode : resultNodes) {
            for (JsonNode swapNode : resultNode.get("swaps")) {
                assertTrue(swapNode.get("token1").get("symbol").asText().equals("WETH") || swapNode.get("token1").get("symbol").asText().equals("USDT"));
            }
        }
    }
}