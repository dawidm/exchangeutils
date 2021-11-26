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

public class Uniswap3TokensQuery extends TheGraphQuery {

    private final String QUERY_STRING = "{tokens {id,symbol,tokenDayData(orderBy: date, orderDirection: desc, first: 2) {date,volumeUSD}}}";
    private final String QUERY_STRING_PAGINATED = "{tokens(where:{id_gt:\"%s\"}){id,symbol,tokenDayData(orderBy: date, orderDirection: desc, first: 2) {date,volumeUSD}}}";
    private final String TOKENS_FIELD = "tokens";
    private final String ID_FIELD = "id";

    @Override
    public String getGraphQLQuery() {
        return String.format(QUERY_STRING);
    }

    @Override
    protected String getGraphQLQuery(String lastPaginationId) {
        return String.format(QUERY_STRING_PAGINATED, lastPaginationId);
    }

    @Override
    protected boolean isPaginated() {
        return true;
    }

    @Override
    protected int numReturnedElements(JsonNode jsonNode) {
        return jsonNode.get(TOKENS_FIELD).size();
    }

    @Override
    protected String lastElemendId(JsonNode jsonNode) {
        return jsonNode.get(TOKENS_FIELD).get(jsonNode.get(TOKENS_FIELD).size()-1).get(ID_FIELD).asText();
    }

}
