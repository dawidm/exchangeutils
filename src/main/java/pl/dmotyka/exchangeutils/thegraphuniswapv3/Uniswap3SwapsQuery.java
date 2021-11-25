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

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphQuery;

public class Uniswap3SwapsQuery extends TheGraphQuery {

    private static final String QUERY_STRING = "";

    private final String apiSymbol;
    private final int beginTimestampSeconds;
    private final int endTimestampSeconds;

    public Uniswap3SwapsQuery(String apiSymbol, int beginTimestampSeconds, int endTimestampSeconds) {
        this.apiSymbol = apiSymbol;
        this.beginTimestampSeconds = beginTimestampSeconds;
        this.endTimestampSeconds = endTimestampSeconds;
    }

    @Override
    protected String getGraphQLQuery() {
        return null;
    }

    @Override
    protected boolean isPaginated() {
        return true;
    }

    @Override
    protected int numReturnedElements(JsonNode jsonNode) {
        return 0;
    }

    @Override
    protected String lastElemendId(JsonNode jsonNode) {
        return null;
    }

    @Override
    protected String getGraphQLQuery(String lastPaginationId) {
        return null;
    }
}
