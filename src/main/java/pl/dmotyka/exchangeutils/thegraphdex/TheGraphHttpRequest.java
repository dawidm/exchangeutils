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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

public class TheGraphHttpRequest {

    public static final Logger logger = Logger.getLogger(TheGraphHttpRequest.class.getName());

    private static final int PAGINATION_RESULTS_LIMIT = 100;

    private final TheGraphExchangeSpecs theGraphExchangeSpecs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String ERRORS_FIELD = "errors";
    public static final String DATA_FIELD = "data";

    public TheGraphHttpRequest(TheGraphExchangeSpecs theGraphExchangeSpecs) {
        this.theGraphExchangeSpecs = theGraphExchangeSpecs;
    }

    private JsonNode sendSingle(TheGraphQuery theGraphQuery, String lastPaginationId) throws ExchangeCommunicationException {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody;
        if (theGraphQuery.isPaginated() && lastPaginationId != null) {
            requestBody = theGraphQuery.getJSONQueryString(lastPaginationId);
        } else {
            requestBody = theGraphQuery.getJSONQueryString();
        }
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(theGraphExchangeSpecs.getTheGraphApiURL()))
                                         .header("Content-Type", "application/json")
                                         .header("Accept", "application/json")
                                         .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                         .build();
        try {
            String responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode returnNode = objectMapper.readValue(responseBody, JsonNode.class);
            if (returnNode.has(ERRORS_FIELD)) {
                throw new ExchangeCommunicationException(returnNode.get(ERRORS_FIELD).asText());
            }
            if (returnNode.has(DATA_FIELD)) {
                return returnNode.get(DATA_FIELD);
            } else {
                throw new ExchangeCommunicationException("no data field returned");
            }
        } catch (IOException | InterruptedException e) {
            throw new ExchangeCommunicationException("when sending request to The Graph", e);
        }
    }

    // when there are multiple pages, every page is in separate JsonNode
    public List<JsonNode> send(TheGraphQuery theGraphQuery) throws ExchangeCommunicationException {
        if (!theGraphQuery.isPaginated()) {
            return List.of(sendSingle(theGraphQuery, null));
        } else {
            List<JsonNode> resultsList = new LinkedList<>();
            JsonNode firstPage = sendSingle(theGraphQuery, null);
            if (theGraphQuery.numReturnedElements(firstPage) == 0) {
                return resultsList;
            }
            resultsList.add(firstPage);
            if (theGraphQuery.numReturnedElements(firstPage) < PAGINATION_RESULTS_LIMIT) {
                return resultsList;
            }
            String lastId = theGraphQuery.lastElemendId(firstPage);
            boolean lastRequestEmpty = false;
            while (!lastRequestEmpty) {
                logger.fine("getting next page...");
                JsonNode nextPage = sendSingle(theGraphQuery, lastId);
                if (theGraphQuery.numReturnedElements(nextPage) > 0) {
                    resultsList.add(nextPage);
                    lastId = theGraphQuery.lastElemendId(nextPage);
                } else {
                    lastRequestEmpty = true;
                }
            }
            return resultsList;
        }
    }
}
