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

public class Uniswap3TokenPricesQuery extends TheGraphQuery {

    private final String QUERY_STRING = "{tokens(where: {id_in: [%s]}) {id,derivedETH}}";
    private final String QUERY_STRING_PAGINATED = "{tokens(where:{id_gt:\"%s\",id_in: [%s]}){id,derivedETH}}";
    private final String TOKENS_FIELD = "tokens";
    private final String ID_FIELD = "id";

    private final String[] tokenAddresses;

    public Uniswap3TokenPricesQuery(String[] tokenAddresses) {
        this.tokenAddresses = tokenAddresses;
    }

    private String formatTokensAddresses(String[] tokensAddresses) {
        String[] tokensAddressesCopy = Arrays.copyOf(tokensAddresses, tokensAddresses.length);
        for(int i=0; i<tokensAddressesCopy.length; i++) {
            tokensAddressesCopy[i] = String.format("\"%s\"",tokensAddressesCopy[i]);
        }
        return String.join(",", tokensAddressesCopy);
    }

    @Override
    public String getGraphQLQuery() {
        return String.format(QUERY_STRING, formatTokensAddresses(tokenAddresses));
    }

    @Override
    protected String getGraphQLQuery(String lastPaginationId) {
        return String.format(QUERY_STRING_PAGINATED, lastPaginationId, formatTokensAddresses(tokenAddresses));
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
