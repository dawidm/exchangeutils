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

public class Uniswap3TokenSwapsQuery extends TheGraphQuery {

    private static final String QUERY_STRING_TOKEN0 = "{ swaps(where:{timestamp_gt: %d, timestamp_lt: %d, token0_in:[%s]}, orderBy: timestamp, orderDirection: asc) { id, timestamp, amount0, amount1, amountUSD, token0 {id,symbol}, token1 {id,symbol}}}";
    private static final String QUERY_STRING_TOKEN1 = "{ swaps(where:{timestamp_gt: %d, timestamp_lt: %d, token1_in:[%s]}, orderBy: timestamp, orderDirection: asc) { id, timestamp, amount0, amount1, amountUSD, token0 {id,symbol}, token1 {id,symbol}}}";
    private static final String QUERY_STRING_TOKEN0_PAGINATED = "{ swaps(where:{timestamp_gt: %d, timestamp_lt: %d, token0_in:[%s],id_gt: \"%s\"}, orderBy: timestamp, orderDirection: asc) { id, timestamp, amount0, amount1, amountUSD, token0 {id,symbol}, token1 {id,symbol}}}";
    private static final String QUERY_STRING_TOKEN1_PAGINATED = "{ swaps(where:{timestamp_gt: %d, timestamp_lt: %d, token1_in:[%s],id_gt: \"%s\"}, orderBy: timestamp, orderDirection: asc) { id, timestamp, amount0, amount1, amountUSD, token0 {id,symbol}, token1 {id,symbol}}}";

    private static final String SWAPS_FIELD = "swaps";
    private static final String ID_FIELD = "id";

    private final WhichToken whichToken;
    private final String[] tokenAddress;
    private final int beginTimestampSeconds;
    private final int endTimestampSeconds;

    public enum WhichToken {
        TOKEN0,
        TOKEN1
    }

    private String formatTokensAddresses(String[] tokensAddresses) {
        String[] tokensAddressesCopy = Arrays.copyOf(tokensAddresses, tokensAddresses.length);
        for(int i=0; i<tokensAddressesCopy.length; i++) {
            tokensAddressesCopy[i] = String.format("\"%s\"",tokensAddressesCopy[i]);
        }
        return String.join(",", tokensAddressesCopy);
    }


    public Uniswap3TokenSwapsQuery(WhichToken whichToken, String[] tokenAddress, int beginTimestampSeconds, int endTimestampSeconds) {
        this.whichToken = whichToken;
        this.tokenAddress = tokenAddress;
        this.beginTimestampSeconds = beginTimestampSeconds;
        this.endTimestampSeconds = endTimestampSeconds;
    }

    @Override
    protected String getGraphQLQuery() {
        switch (whichToken) {
            case TOKEN0:
                return String.format(QUERY_STRING_TOKEN0, beginTimestampSeconds, endTimestampSeconds, formatTokensAddresses(tokenAddress));
            case TOKEN1:
                return String.format(QUERY_STRING_TOKEN1, beginTimestampSeconds, endTimestampSeconds, formatTokensAddresses(tokenAddress));
            default:
                throw new IllegalArgumentException("wrong token specified");
        }
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
        switch (whichToken) {
            case TOKEN0:
                return String.format(QUERY_STRING_TOKEN0_PAGINATED, beginTimestampSeconds, endTimestampSeconds, formatTokensAddresses(tokenAddress), lastPaginationId);
            case TOKEN1:
                return String.format(QUERY_STRING_TOKEN1_PAGINATED, beginTimestampSeconds, endTimestampSeconds, formatTokensAddresses(tokenAddress), lastPaginationId);
            default:
                throw new IllegalArgumentException("wrong token specified");
        }
    }
}
