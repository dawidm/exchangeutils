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

package pl.dmotyka.exchangeutils.thegraphdex;

import com.fasterxml.jackson.databind.JsonNode;

public class PairsQuery extends TheGraphQuery {

    private final String QUERY_STRING = "{pools(where:{volumeUSD_gt:10000.0}){id,token0{id,symbol}token1{id,symbol}}}";
    private final String QUERY_STRING_PAGINATED = "{pools(where:{id_gt:\"%s\",volumeUSD_gt:10000.0}){id,token0{id,symbol}token1{id,symbol}}}";
    private final String POOLS_FIELD = "pools";
    private final String ID_FIELD = "id";

    @Override
    public String getGraphQLQuery() {
        return QUERY_STRING;
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
        return jsonNode.get(POOLS_FIELD).size();
    }

    @Override
    protected String lastElemendId(JsonNode jsonNode) {
        return jsonNode.get(POOLS_FIELD).get(jsonNode.get(POOLS_FIELD).size()-1).get(ID_FIELD).asText();
    }

}
