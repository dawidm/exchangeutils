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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class TheGraphQuery {

    private final String JSON_QUERY_TEMPLATE = "{\"query\": %s}";
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract String getGraphQLQuery();
    // the graph limits number of returned elements, pagination my be needed to query all the elements
    protected abstract boolean isPaginated();
    // for pagination: check a number of returned elements in returned JSON
    protected abstract int numReturnedElements(JsonNode jsonNode);
    // for pagination: get an id of a last returned element
    protected abstract String lastElemendId(JsonNode jsonNode);
    // for pagination: create query for elements with id greater than lastPaginationId
    protected abstract String getGraphQLQuery(String lastPaginationId);

    public String getJSONQueryString() {
        try {
            return String.format(JSON_QUERY_TEMPLATE, objectMapper.writeValueAsString(getGraphQLQuery()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("when creating json request", e);
        }
    }

    public String getJSONQueryString(String lastPaginationId) {
        try {
            return String.format(JSON_QUERY_TEMPLATE, objectMapper.writeValueAsString(getGraphQLQuery(lastPaginationId)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("when creating json request", e);
        }
    }
}
