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

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphQuery;

public class Uniswap3PoolSwapsQuery extends TheGraphQuery {

    private static final String QUERY_STRING = "{swaps(where:{timestamp_gt: %d, timestamp_lt: %d,pool_in:[%s]},orderBy: timestamp, orderDirection: asc){id,timestamp,token0{symbol},token1{symbol},amount0,amount1,amountUSD}}";
    private static final String QUERY_STRING_PAGINATED = "{swaps(where:{timestamp_gt: %d, timestamp_lt: %d,pool_in:[%s],id_gt: \"%s\"},orderBy: timestamp, orderDirection: asc){id,timestamp,token0{symbol},token1{symbol},amount0,amount1,amountUSD}}";

    private static final String SWAPS_FIELD = "swaps";
    private static final String ID_FIELD = "id";

    private final String[] poolsAddresses;
    private final int beginTimestampSeconds;
    private final int endTimestampSeconds;

    public Uniswap3PoolSwapsQuery(String[] poolsAddresses, int beginTimestampSeconds, int endTimestampSeconds) {
        this.poolsAddresses = poolsAddresses;
        this.beginTimestampSeconds = beginTimestampSeconds;
        this.endTimestampSeconds = endTimestampSeconds;
    }

    private String formatPoolsAddresses(String[] poolsAddresses) {
        String[] poolsAddressesCopy = Arrays.copyOf(poolsAddresses, poolsAddresses.length);
        for(int i=0; i<poolsAddressesCopy.length; i++) {
            poolsAddressesCopy[i] = String.format("\"%s\"",poolsAddressesCopy[i]);
        }
        return String.join(",", poolsAddressesCopy);
    }

    @Override
    protected String getGraphQLQuery() {
        return String.format(QUERY_STRING, beginTimestampSeconds, endTimestampSeconds, formatPoolsAddresses(poolsAddresses));
    }

    @Override
    protected boolean isPaginated() {
        return true;
    }

    @Override
    protected int numReturnedElements(JsonNode jsonNode) {
        return jsonNode.get(SWAPS_FIELD).size();
    }

    @Override
    protected String lastElemendId(JsonNode jsonNode) {
        JsonNode swapsNode = jsonNode.get(SWAPS_FIELD);
        return swapsNode.get(swapsNode.size()-1).get(ID_FIELD).asText();
    }

    @Override
    protected String getGraphQLQuery(String lastPaginationId) {
        return String.format(QUERY_STRING_PAGINATED, beginTimestampSeconds, endTimestampSeconds, formatPoolsAddresses(poolsAddresses), lastPaginationId);
    }
}
